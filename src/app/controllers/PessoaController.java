package app.controllers;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import app.models.Pessoa;
import app.models.PessoaDAO;
import app.rmi_interfaces.PessoaRMIInterface;

public class PessoaController implements PessoaRMIInterface {
    private Connection conexao = null;

    public PessoaController(Connection conexao) {
        this.conexao = conexao;
    }

    @Override
    public List<Pessoa> listar() throws RemoteException, SQLException {
        return new PessoaDAO().listar(this.conexao);
    }

    @Override
    public List<Pessoa> filtrar(String nome) throws RemoteException, SQLException {
        return new PessoaDAO().filtrar(this.conexao, nome);
    }

    @Override
    public Pessoa adicionar(Pessoa pessoa) throws RemoteException, SQLException {
        if (pessoa.nome.trim().isBlank())
            throw new SQLException("Preencha o nome da pessoa.");
        if (pessoa.endereco.trim().isBlank())
            throw new SQLException("Preencha o endereco da pessoa.");
        return new PessoaDAO().adicionar(this.conexao, pessoa);
    }

    @Override
    public Pessoa alterar(Pessoa pessoa) throws RemoteException, SQLException {
        if (pessoa.nome.trim().isBlank())
            throw new SQLException("Preencha o nome da pessoa.");
        if (pessoa.endereco.trim().isBlank())
            throw new SQLException("Preencha o endereco da pessoa.");
        return new PessoaDAO().alterar(this.conexao, pessoa);
    }

    @Override
    public Pessoa excluir(Pessoa pessoa) throws RemoteException, SQLException {
        return new PessoaDAO().excluir(this.conexao, pessoa);
    }

    @Override
    public Pessoa buscar(int id) throws RemoteException, SQLException {        
        return new PessoaDAO().buscar(this.conexao, id);
    }

    @Override
    public String echo() throws RemoteException {
        return "online";
    }
}