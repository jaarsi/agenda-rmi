package app.server;

import java.rmi.AlreadyBoundException;
import java.rmi.ConnectException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.DriverManager;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

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

    private static PessoaRMIInterface gerar_stub() throws Exception {
        Connection conexao = inicializar_conexao();
        PessoaController controller = new PessoaController(conexao);        
        PessoaRMIInterface stub = 
            (PessoaRMIInterface) UnicastRemoteObject.exportObject(controller, 0);
        return stub;
    }

    public static void main(final String args[]) throws Exception {
        Options options = new Options();
        Option opt = new Option("rmi_host", true, "Host do 'rmiregistry'");
        options.addOption(opt);
        opt = new Option("rmi_porta", true, "Porta do 'rmiregistry'");
        options.addOption(opt);
        opt = new Option("ref_remota", true, "Nome da referencia remota que será exportada");
        opt.setRequired(true);
        options.addOption(opt);
        opt = new Option("rb", "Força a exportação da referência remota");
        options.addOption(opt);
        
        HelpFormatter formatter = new HelpFormatter();
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            cmd = null;
            System.out.println(e.getMessage());
            formatter.printHelp("agenda-rmi", options);            
            System.exit(1);
        }

        String rmireg_host = cmd.getOptionValue("rmi_host", "localhost");
        int rmireg_porta = Integer.parseInt(cmd.getOptionValue("rmi_porta", "1099"));
        String rmireg_ref_remota = cmd.getOptionValue("ref_remota");
        boolean rebind = cmd.hasOption("rb");

        PessoaRMIInterface stub = gerar_stub();
        Registry rmireg = LocateRegistry.getRegistry(rmireg_host, rmireg_porta);
        try{
            try {            
                rmireg.bind(rmireg_ref_remota, stub);
            } catch (ConnectException e) {
                System.out.print(
                    "O 'rmiregistry' informado não respondeu, criando localmente ...\n");
                rmireg_host = "localhost";
                rmireg = LocateRegistry.createRegistry(rmireg_porta);
                rmireg.bind(rmireg_ref_remota, stub);
            }
        } catch (AlreadyBoundException e) {
            if (rebind)
                rmireg.rebind(rmireg_ref_remota, stub);
            else {
                System.err.print("A referência remota utilizada já está registrada!\n");
                System.exit(0);
            }
        }
        System.out.printf(
            "Servidor pronto. 'rmiregistry' -> %s:%d/%s\n",
            rmireg_host, rmireg_porta, rmireg_ref_remota);
    }
}