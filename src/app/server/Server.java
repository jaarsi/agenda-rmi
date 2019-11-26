package app.server;

import java.rmi.AlreadyBoundException;
import java.rmi.ConnectException;
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
            "ENDERECO   TEXT                NOT NULL)");
        return conn;
    }

    public static void main(final String args[]) throws Exception {
        if (args.length < 3) 
            throw new Exception(
                "Informe o host e porta onde o 'rmiregistry' está executando\n"+
                "e o nome da referência remota que será exportada.\n"+
                "uso: [comando] localhost 2000 referencia ");

        String rmireg_host = args[0];
        int rmireg_porta = Integer.parseInt(args[1]);
        String rmireg_ref_remota = args[2];

        Connection conexao = inicializar_conexao();
        PessoaController controller = new PessoaController(conexao);        
        PessoaRMIInterface stub = 
            (PessoaRMIInterface) UnicastRemoteObject.exportObject(controller, 0);
        try {
            Registry rmireg = LocateRegistry.getRegistry(rmireg_host, rmireg_porta);
            rmireg.bind(rmireg_ref_remota, stub);    
        } catch (ConnectException e) {
            rmireg_host = "localhost";
            Registry rmireg = LocateRegistry.createRegistry(rmireg_porta);
            rmireg.bind(rmireg_ref_remota, stub);    
        } catch (AlreadyBoundException e) {
            throw new Exception("A referência remota utilizada já está registrada!");
        }
        System.err.printf(
            "Servidor pronto. Objeto remoto em %s:%d/%s ...\n", 
            rmireg_host, rmireg_porta, rmireg_ref_remota);
    }
}