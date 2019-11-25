package app.models;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface PessoaInterface {
    public List<Pessoa> todos(Connection conexao) throws SQLException; 
    public List<Pessoa> filtrar(Connection conexao, String nome) throws SQLException;
    public Pessoa buscar(Connection conexao, Long id) throws SQLException;
    public Pessoa adicionar(Connection conexao, Pessoa pessoa) throws SQLException;
    public Pessoa alterar(Connection conexao, Pessoa pessoa) throws SQLException;
    public Pessoa excluir(Connection conexao, Pessoa pessoa) throws SQLException;
}