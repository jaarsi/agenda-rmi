package app.models;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class EventoDAO implements CRUDInterface<Evento> {

    @Override
    public List<Evento> listar(Connection conexao) throws SQLException {
        List<Evento> list = new ArrayList<Evento>();
        Statement st = conexao.createStatement();
        ResultSet rs = st.executeQuery("SELECT * FROM EVENTO");
        while (rs.next()) {
            Evento e = new Evento();
            e.id = rs.getInt("id");
            e.pessoa_id = rs.getInt("pessoa_id");
            e.usuario = rs.getString("login");
            e.servidor = rs.getString("servidor");
            e.operacao = rs.getString("dthr_login");
            e.dthr_evento = rs.getDate("dthr_evento");
            e.despachado = rs.getBoolean("despachado");
            list.add(e);
        }
        st.close();
        return list;
    }

    @Override
    public List<Evento> filtrar(Connection conexao, String s) throws SQLException {
        throw new SQLException("Funcionalidade nao implementada.");
    }

    @Override
    public Evento buscar(Connection conexao, int id) throws SQLException {
        throw new SQLException("Funcionalidade nao implementada.");
    }

    @Override
    public Evento adicionar(Connection conexao, Evento item) throws SQLException {
        PreparedStatement ps = conexao.prepareStatement(
            "INSERT INTO EVENTO (" + 
                "PESSOA_ID, USUARIO, SERVIDOR, OPERACAO, DTHR_EVENTO, DESPACHADO) " + 
            "VALUES (?,?,?,?,?,?)",
            Statement.RETURN_GENERATED_KEYS
        );        
        if (item.pessoa_id > 0)
            ps.setInt(1, item.pessoa_id);
        else
            ps.setNull(1, java.sql.Types.INTEGER);
        ps.setString(2, item.usuario);
        ps.setString(3, item.servidor);
        ps.setString(4, item.operacao);
        ps.setDate(5, new Date(item.dthr_evento.getTime()));
        ps.setBoolean(6, item.despachado); 
        ps.executeUpdate();
        ResultSet generatedKeys = ps.getGeneratedKeys();
        if (generatedKeys.next())
            item.id = generatedKeys.getInt(1);
        return item;
    }

    @Override
    public Evento alterar(Connection conexao, Evento item) throws SQLException {
        PreparedStatement ps = conexao.prepareStatement(
            "UPDATE EVENTO SET " + 
                "PESSOA_ID=?, USUARIO=?, SERVIDOR=?, OPERACAO=?, DTHR_EVENTO=?, DESPACHADO=? " + 
            "WHERE ID=?",
            Statement.RETURN_GENERATED_KEYS
        );
        ps.setInt(1, item.pessoa_id);
        ps.setString(2, item.usuario);
        ps.setString(3, item.servidor);
        ps.setString(4, item.operacao);
        ps.setDate(5, new Date(item.dthr_evento.getTime()));
        ps.setBoolean(6, item.despachado); 
        ps.setInt(7, item.id);
        ps.executeUpdate();
        return item;
    }

    @Override
    public Evento excluir(Connection conexao, Evento item) throws SQLException {
        throw new SQLException("Funcionalidade nao implementada.");
    }

}