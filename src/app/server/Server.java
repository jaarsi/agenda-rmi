package app.server;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.DriverManager;

import app.controllers.PessoaController;
import app.rmi_interfaces.PessoaRMIInterface;

public class Server {
    private static Connection inicializar_conexao() throws Exception {        
        Class.forName("org.sqlite.JDBC");
        Connection conn = DriverManager.getConnection("jdbc:sqlite:agenda-rmi.db");
        conn.setAutoCommit(true);
        conn.createStatement().executeUpdate(
            "CREATE TABLE IF NOT EXISTS PESSOA (" +
            "ID         INTEGER PRIMARY KEY NOT NULL," +
            "NOME       TEXT                NOT NULL," +
            "ENDERECO   TEXT                NOT NULL)"
        );
        return conn;
    }

    public static void main(final String args[]) {
        try {            
            Connection conn = inicializar_conexao();
            PessoaController controller = new PessoaController(conn);
            PessoaRMIInterface stub = 
                (PessoaRMIInterface) UnicastRemoteObject.exportObject(controller, 0);
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind("PessoaRMIInterface", stub);
            System.err.println("Servidor pronto!");
        } catch (Exception e) {            
            System.err.println("Não foi possível iniciar o servidor!");
            e.printStackTrace();
        }
    }
}