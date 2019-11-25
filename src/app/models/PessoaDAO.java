package app.models;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PessoaDAO implements PessoaInterface {
    @Override
    public List<Pessoa> todos(Connection conexao) throws SQLException {        
        List<Pessoa> list = new ArrayList<Pessoa>();
        Statement st = conexao.createStatement();
        ResultSet rs = st.executeQuery(
            "SELECT * FROM PESSOA ORDER BY NOME");
        while (rs.next()) {
            Pessoa p = new Pessoa();
            p.id = rs.getLong("id");
            p.nome = rs.getString("nome");
            p.endereco = rs.getString("endereco");
            list.add(p);
        };
        st.close();
        return list;
    }

    @Override
    public List<Pessoa> filtrar(Connection conexao, String nome) throws SQLException {
        List<Pessoa> list = new ArrayList<Pessoa>();
        PreparedStatement ps = conexao.prepareStatement(
            "SELECT * FROM PESSOA WHERE NOME LIKE ? ORDER BY NOME");
        ps.setString(1, "%" + nome + "%");
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            Pessoa p = new Pessoa();
            p.id = rs.getLong("id");
            p.nome = rs.getString("nome");
            p.endereco = rs.getString("endereco");
            list.add(p);
        }
        rs.close();
        return list;
    }

    @Override
    public Pessoa adicionar(Connection conexao, Pessoa pessoa) throws SQLException {
        PreparedStatement ps = conexao.prepareStatement(
                "INSERT INTO PESSOA (NOME, ENDERECO) VALUES (?,?)",
                Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, pessoa.nome);
        ps.setString(2, pessoa.endereco);
        ps.executeUpdate();
        ResultSet generatedKeys = ps.getGeneratedKeys();
        if (generatedKeys.next())
            pessoa.id = generatedKeys.getLong(1);
                
        return pessoa;         
    }

    @Override
    public Pessoa alterar(Connection conexao, Pessoa pessoa) throws SQLException {
        PreparedStatement ps = conexao.prepareStatement(
            "UPDATE PESSOA SET NOME=?, ENDERECO=? WHERE ID=?");
        ps.setString(1, pessoa.nome);
        ps.setString(2, pessoa.endereco);
        ps.setLong(3, pessoa.id);
        ps.executeUpdate();
        return pessoa;
    }

    @Override
    public Pessoa excluir(Connection conexao, Pessoa pessoa) throws SQLException {
        PreparedStatement ps = conexao.prepareStatement(
            "DELETE FROM PESSOA WHERE ID=?");
        ps.setLong(1, pessoa.id);
        ps.executeUpdate();
        return pessoa;
    }

    @Override
    public Pessoa buscar(Connection conexao, Long id) throws SQLException {
        PreparedStatement ps = conexao.prepareStatement(
            "SELECT * FROM PESSOA WHERE ID = ?");
        ps.setLong(1, id);
        ResultSet rs = ps.executeQuery();        
        try {
            while (rs.next()) {            
                Pessoa p = new Pessoa();
                p.id = rs.getLong("id");
                p.nome = rs.getString("nome");
                p.endereco = rs.getString("endereco");
                return p;
            }
            return null;
        } finally {
            rs.close();
        }
    }
}