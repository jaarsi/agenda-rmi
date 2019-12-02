package app.controllers;

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
        if (pessoa.nome.trim().isBlank())
            throw new SQLException("Preencha o nome da pessoa.");
        if (pessoa.endereco.trim().isBlank())
            throw new SQLException("Preencha o endereco da pessoa.");

        pessoa.criado_em = new Date();
        pessoa.alterado_em = new Date();
        Pessoa p = new PessoaDAO().adicionar(this.conexao, pessoa);
        
        Evento ev = new Evento();
        ev.pessoa_id = pessoa.id;
        ev.usuario = usuario.login;
        ev.servidor = usuario.servidor;
        ev.operacao = "adicionar";
        ev.dthr_evento = new Date();
        ev.despachado = false;
        new EventoDAO().adicionar(conexao, ev);

        return p;
    }

    @Override
    public Pessoa alterar(Usuario usuario, Pessoa pessoa) throws RemoteException, SQLException {
        if (pessoa.nome.trim().isBlank())
            throw new SQLException("Preencha o nome da pessoa.");
        if (pessoa.endereco.trim().isBlank())
            throw new SQLException("Preencha o endereco da pessoa.");        
        
        pessoa.alterado_em = new Date();
        Pessoa p = new PessoaDAO().alterar(this.conexao, pessoa);

        Evento ev = new Evento();
        ev.pessoa_id = pessoa.id;
        ev.usuario = usuario.login;
        ev.servidor = usuario.servidor;
        ev.operacao = "alterar";
        ev.dthr_evento = new Date();
        ev.despachado = false;
        new EventoDAO().adicionar(conexao, ev);

        return p;    
    }

    @Override
    public Pessoa excluir(Usuario usuario, Pessoa pessoa) throws RemoteException, SQLException {
        Pessoa p = new PessoaDAO().excluir(this.conexao, pessoa);

        Evento ev = new Evento();
        ev.pessoa_id = pessoa.id;
        ev.usuario = usuario.login;
        ev.servidor = usuario.servidor;
        ev.operacao = "excluir";
        ev.dthr_evento = new Date();
        ev.despachado = false;
        new EventoDAO().adicionar(conexao, ev);

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
        new UsuarioDAO().adicionar(conexao, usuario);

        Evento ev = new Evento();
        ev.usuario = usuario.login;
        ev.servidor = usuario.servidor;
        ev.operacao = "logar";
        ev.dthr_evento = new Date();
        ev.despachado = false;
        new EventoDAO().adicionar(conexao, ev);

    }
}