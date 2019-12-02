package app.models;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO implements CRUDInterface<Usuario> {

    @Override
    public List<Usuario> listar(Connection conexao) throws SQLException {
        List<Usuario> list = new ArrayList<Usuario>();
        Statement st = conexao.createStatement();
        ResultSet rs = st.executeQuery("SELECT * FROM USUARIO_LOGADO");
        while (rs.next()) {
            Usuario u = new Usuario();
            u.id = rs.getInt("id");
            u.login = rs.getString("login");
            u.servidor = rs.getString("servidor");
            u.dthr_login = rs.getDate("dthr_login");
            list.add(u);
        }
        st.close();
        return list;        
    }

    @Override
    public List<Usuario> filtrar(Connection conexao, String s) throws SQLException {
        throw new SQLException("Funcionalidade nao implementada.");
    }

    @Override
    public Usuario buscar(Connection conexao, int id) throws SQLException {
        throw new SQLException("Funcionalidade nao implementada.");
    }

    @Override
    public Usuario adicionar(Connection conexao, Usuario item) throws SQLException {
        PreparedStatement ps = conexao.prepareStatement(
            "INSERT INTO USUARIO_LOGADO (LOGIN, SERVIDOR, DTHR_LOGIN) " + 
            "VALUES (?,?,?)",
            Statement.RETURN_GENERATED_KEYS
        );
        ps.setString(1, item.login);
        ps.setString(2, item.servidor);
        ps.setDate(3, new Date(item.dthr_login.getTime()));
        ps.executeUpdate();
        ResultSet generatedKeys = ps.getGeneratedKeys();
        if (generatedKeys.next())
            item.id = generatedKeys.getInt(1);                
        return item;     
    }

    @Override
    public Usuario alterar(Connection conexao, Usuario item) throws SQLException {
        PreparedStatement ps = conexao.prepareStatement(
            "UPDATE USUARIO_LOGADO SET LOGIN=?, SERVIDOR=?, DTHR_LOGIN=? WHERE ID=?"
        );
        ps.setString(1, item.login);
        ps.setString(2, item.servidor);
        ps.setDate(3, new Date(item.dthr_login.getTime()));
        ps.setLong(4, item.id);
        ps.executeUpdate();
        return item;
    }

    @Override
    public Usuario excluir(Connection conexao, Usuario item) throws SQLException {
        throw new SQLException("Funcionalidade nao implementada.");
    }

}