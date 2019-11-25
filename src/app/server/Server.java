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
        int porta_rmireg;
        try {            
            try{
                porta_rmireg = Integer.parseInt(args[0]);
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new Exception(
                    "Informe a porta onde o 'rmiregistry' será alocado ..."
                );
            }
            Connection conn = inicializar_conexao();
            PessoaController controller = new PessoaController(conn);
            PessoaRMIInterface stub = 
                (PessoaRMIInterface) UnicastRemoteObject.exportObject(controller, 0);            
            Registry registro_rmi = LocateRegistry.createRegistry(porta_rmireg);
            registro_rmi.rebind("PessoaRMIInterface", stub);
            System.err.printf("Servidor pronto. Escutando porta %d ...\n", porta_rmireg);
        } catch (Exception e) {            
            e.printStackTrace();
        }
    }
}