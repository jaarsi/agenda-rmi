package app.server;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.DriverManager;

import app.controllers.PessoaController;
import app.rmi_interfaces.PessoaRMIInterface;

public class Server {
    public static final int PORTA_RMI_PADRAO = 1099;

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
            int porta_rmi = 
                (args.length > 0) ? Integer.parseInt(args[0]) : PORTA_RMI_PADRAO;
            Registry registro_rmi = LocateRegistry.createRegistry(porta_rmi);
            registro_rmi.rebind("PessoaRMIInterface", stub);
            System.err.println("Servidor pronto. Escutando porta " + porta_rmi);
        } catch (Exception e) {            
            e.printStackTrace();
        }
    }
}