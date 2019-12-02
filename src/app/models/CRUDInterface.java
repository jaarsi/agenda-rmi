package app.models;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface CRUDInterface<T> {
    public List<T> listar(Connection conexao) throws SQLException; 
    public List<T> filtrar(Connection conexao, String s) throws SQLException;
    public T buscar(Connection conexao, int id) throws SQLException;
    public T adicionar(Connection conexao, T item) throws SQLException;
    public T alterar(Connection conexao, T item) throws SQLException;
    public T excluir(Connection conexao, T item) throws SQLException;
}