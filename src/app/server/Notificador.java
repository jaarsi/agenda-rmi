package app.server;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import app.models.Evento;
import app.models.EventoDAO;

public class Notificador {
    private Connection conexao;

    public Notificador(Connection conexao) {
        this.conexao = conexao;
    }

    public void despachar(List<PrintWriter> clientes) throws SQLException {        
        List<Evento> eventos = new EventoDAO().listar(this.conexao);
        for (Evento ev: eventos) {
            for (PrintWriter c: clientes) 
                try {
                    c.println(ev.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            ev.despachado = true;
            new EventoDAO().alterar(this.conexao, ev);
        }
    }
}