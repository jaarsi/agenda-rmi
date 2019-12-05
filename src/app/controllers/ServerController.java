package app.controllers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import app.models.Evento;
import app.models.EventoDAO;
import app.models.Pessoa;
import app.models.PessoaDAO;
import app.models.Usuario;
import app.models.UsuarioDAO;

public class ServerController implements ServerRMIInterface {
    private Connection conexao = null;
    private boolean executando_cad_comb = false;

    public ServerController(Connection conexao) {
        this.conexao = conexao;
    }

    @Override
    public List<Pessoa> listar(Usuario usuario) throws RemoteException, SQLException {
        return new PessoaDAO().listar(this.conexao);
    }

    @Override
    public List<Pessoa> filtrar(Usuario usuario, String nome) throws RemoteException, SQLException {
        return new PessoaDAO().filtrar(this.conexao, nome);
    }

    @Override
    public Pessoa adicionar(Usuario usuario, Pessoa pessoa) throws RemoteException, SQLException {
        if (pessoa.nome.trim().isEmpty())
            throw new SQLException("Preencha o nome da pessoa.");
        if (pessoa.endereco.trim().isEmpty())
            throw new SQLException("Preencha o endereco da pessoa.");

        pessoa.criado_em = new Date();
        pessoa.alterado_em = new Date();
        Pessoa p = new PessoaDAO().adicionar(this.conexao, pessoa);

        if (usuario != null) {
            Evento ev = new Evento();
            ev.pessoa = pessoa;
            ev.usuario = usuario.login;
            ev.servidor = usuario.servidor;
            ev.operacao = "adicionar";
            ev.dthr_evento = new Date();
            ev.despachado = false;
            new EventoDAO().adicionar(this.conexao, ev);
        }

        return p;
    }

    @Override
    public Pessoa alterar(Usuario usuario, Pessoa pessoa) throws RemoteException, SQLException {
        if (pessoa.nome.trim().isEmpty())
            throw new SQLException("Preencha o nome da pessoa.");
        if (pessoa.endereco.trim().isEmpty())
            throw new SQLException("Preencha o endereco da pessoa.");

        pessoa.alterado_em = new Date();
        Pessoa p = new PessoaDAO().alterar(this.conexao, pessoa);

        Evento ev = new Evento();
        ev.pessoa = pessoa;
        ev.usuario = usuario.login;
        ev.servidor = usuario.servidor;
        ev.operacao = "alterar";
        ev.dthr_evento = new Date();
        ev.despachado = false;
        new EventoDAO().adicionar(this.conexao, ev);

        return p;
    }

    @Override
    public Pessoa excluir(Usuario usuario, Pessoa pessoa) throws RemoteException, SQLException {
        Pessoa p = new PessoaDAO().excluir(this.conexao, pessoa);

        Evento ev = new Evento();
        ev.pessoa = pessoa;
        ev.usuario = usuario.login;
        ev.servidor = usuario.servidor;
        ev.operacao = "excluir";
        ev.dthr_evento = new Date();
        ev.despachado = false;
        new EventoDAO().adicionar(this.conexao, ev);

        return p;
    }

    @Override
    public Pessoa buscar(Usuario usuario, int id) throws RemoteException, SQLException {
        return new PessoaDAO().buscar(this.conexao, id);
    }

    @Override
    public String echo() throws RemoteException {
        return "online";
    }

    @Override
    public void logar(Usuario usuario) throws RemoteException, SQLException {
        new UsuarioDAO().adicionar(this.conexao, usuario);

        Evento ev = new Evento();
        ev.usuario = usuario.login;
        ev.servidor = usuario.servidor;
        ev.operacao = "logar";
        ev.dthr_evento = new Date();
        ev.despachado = false;
        new EventoDAO().adicionar(this.conexao, ev);

    }

    @Override
    public void cadastro_combinado() throws RemoteException, SQLException {
        if (this.executando_cad_comb)
            return;

        try {
            ServerSocket s = new ServerSocket(5555);
            
            ServerController self = this;            
            new Thread(new Runnable(){
                @Override
                public void run() {
                    try {
                        self.executando_cad_comb = true;
                        Pessoa p = new Pessoa();
                        Socket u1 = s.accept();                    
                        BufferedReader u1_in = new BufferedReader(new InputStreamReader(u1.getInputStream()));
                        PrintWriter u1_out = new PrintWriter(u1.getOutputStream(), true);
                        u1_out.println("Aguardando o outro cliente ...");
                        Socket u2 = s.accept();
                        BufferedReader u2_in = new BufferedReader(new InputStreamReader(u2.getInputStream()));
                        PrintWriter u2_out = new PrintWriter(u2.getOutputStream(), true);
                        u2_out.println("Aguarde enquanto o cliente 1 preenche os dados ...");
                        u1_out.println("Informe o primeiro nome do contato: ");
                        p.nome = u1_in.readLine();
                        u1_out.println("Aguarde enquanto o cliente 2 preenche os dados ...");
                        u2_out.println("Informe o sobrenome do contato: ");
                        p.nome = p.nome + " " + u2_in.readLine();
                        u2_out.println("Aguarde enquanto o cliente 1 preenche os dados ...");
                        u1_out.println("Informe o logradouro do contato: ");
                        p.endereco = u1_in.readLine();
                        u1_out.println("Aguarde enquanto o cliente 2 preenche os dados ...");
                        u2_out.println("Informe bairro e cidade do contato: ");
                        p.endereco = p.endereco + " " + u2_in.readLine();
                        self.adicionar(null, p);                        
                        u1_out.println(String.format(
                            "Usuário [%d] %s cadastrado com sucesso!\n", p.id, p.nome
                        ));                        
                        u2_out.println(String.format(
                            "Usuário [%d] %s cadastrado com sucesso!\n", p.id, p.nome
                        ));                        
                        u1_out.println("fim");
                        u2_out.println("fim");
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        self.executando_cad_comb = false;
                    }                            
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}