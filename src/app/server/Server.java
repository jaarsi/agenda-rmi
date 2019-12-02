package app.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
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

import app.controllers.ServerController;
import app.controllers.ServerRMIInterface;

public class Server {
    public static void main(final String args[]) throws Exception {
        // parser dos argumentos informados na execução do programa.
        // neste ponto, o algoritmo descobre os valores que são passados 
        // para host e porta do rmiregistry e o nome da referência remota
        // que será exportada. ex.: 

        // java -cp bin:lib/sqlite-jdbc-3.27.2.1.jar:lib/commons-cli-1.3.1.jar 
        //      app.server.Server -rmi_host 192.168.0.10 -rmi_port 2000 -ref server1

        // no comanda acima, o servidor está buscando o rmiregistry no host 192.168.0.10
        // e na porta 2000. Se não encontrar o rmiregistry, este será criado localmente 
        // na porta 2000 e se a porta n for informada, 1099 é assumido.
        // o parametro -ref é o unico que é obrigatório;
        Options options = new Options();
        Option opt = new Option("rmi_host", true, "Host do 'rmiregistry'");
        options.addOption(opt);
        opt = new Option("rmi_porta", true, "Porta do 'rmiregistry'");
        options.addOption(opt);
        opt = new Option("ref", true, "Nome da referencia remota que será exportada");
        opt.setRequired(true);
        options.addOption(opt);
        opt = new Option("rb", "Força a exportação da referência remota");
        options.addOption(opt);

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd;        
        try {
            // parser dos argumentos passados como parâmetro;
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            // se ocorrer algum problema no parser, encerra o programa e mostra 
            // a ajuda;
            cmd = null;
            System.out.println(e.getMessage());
            new HelpFormatter().printHelp("agenda-rmi", options);
            System.exit(0);
        }

        String rmireg_host = cmd.getOptionValue("rmi_host", "localhost");
        int rmireg_porta = Integer.parseInt(cmd.getOptionValue("rmi_porta", "1099"));
        String rmireg_ref_remota = cmd.getOptionValue("ref");
        boolean rebind = cmd.hasOption("rb");

        // carregamento do driver do sqlite;
        Class.forName("org.sqlite.JDBC");
        // criação do objeto que representa a conexão com o banco de dados;
        Connection conexao = DriverManager.getConnection("jdbc:sqlite:agenda-rmi.db");
        // habilita o autocommit. assim, n há necessidade de transacionar os
        // comandos com o banco;
        conexao.setAutoCommit(true);
        // cria, caso n exista, a tabela PESSOA;
        conexao.createStatement().executeUpdate(
            "CREATE TABLE IF NOT EXISTS PESSOA (" + 
            "ID INTEGER PRIMARY KEY NOT NULL," + 
            "NOME TEXT NOT NULL," + 
            "ENDERECO TEXT NOT NULL," +
            "CRIADO_EM INTEGER NOT NULL, " +
            "ALTERADO_EM INTEGER NOT NULL)"
        );
        // cria, caso n exista, a tabela de eventos;
        conexao.createStatement().executeUpdate(
            "CREATE TABLE IF NOT EXISTS EVENTO (" + 
            "ID INTEGER PRIMARY KEY NOT NULL," +
            "PESSOA_ID INTEGER REFERENCES PESSOA(ID)," +
            "USUARIO TEXT NOT NULL," + 
            "SERVIDOR TEXT NOT NULL," + 
            "OPERACAO TEXT NOT NULL," +
            "DTHR_EVENTO INTEGER NOT NULL," +
            "DESPACHADO INTEGER DEFAULT 0)"
        );
        // cria uma tabela temporária de usuarios logados;
        conexao.createStatement().executeUpdate(
            "CREATE TEMPORARY TABLE IF NOT EXISTS USUARIO_LOGADO (" + 
            "ID INTEGER PRIMARY KEY NOT NULL," + 
            "LOGIN TEXT UNIQUE ON CONFLICT REPLACE NOT NULL," + 
            "SERVIDOR TEXT NOT NULL," +
            "DTHR_LOGIN INTEGER NOT NULL)"
        );
        // criação do "controller", que é responsável por validar
        // e repassar os dados recebidos para o modelo processar;
        ServerController controller = new ServerController(conexao);
        // criação e exportação do stub, que é o objeto disponibilizado para 
        // o cliente remoto interagir com as funcionalidades disponibilizadas;
        ServerRMIInterface stub = 
            (ServerRMIInterface) UnicastRemoteObject.exportObject(controller, 0);
        // localiza onde o rmiregistry está sendo executado, a partir das 
        // informações passadas por parâmetro (host e porta) no momento da execução 
        // do programa. caso as uma das ou nenhuma informação sobre o rmiregistry 
        // tenha sido passado, assume-se "localhost" para o host e "1099" para a porta,
        // que são os valores padrões;
        Registry rmireg = LocateRegistry.getRegistry(rmireg_host, rmireg_porta);
        try {
            try {
                // tenta vincular, no rmiregistry, o stub gerado (PessoaController) 
                // à referência informada nos parâmetros;
                rmireg.bind(rmireg_ref_remota, stub);
            } catch (ConnectException e) {
                // caso o rmiregistry n seja encontrado no host e porta informado,
                // o codigo cai nessa exceção. 
                // Aqui, o rmiregistry é criado localmente (localhost) e o stub
                // vinculado à ele;
                System.out.print(
                    "O 'rmiregistry' informado não respondeu, criando localmente ...\n"
                );
                rmireg_host = "localhost";
                rmireg = LocateRegistry.createRegistry(rmireg_porta);
                rmireg.bind(rmireg_ref_remota, stub);
                Thread socket_thread = new Thread(new Runnable() {
					@Override
					public void run() {
                        ServerSocket s;
                        try {
                            s = new ServerSocket(rmireg_porta + 1);
                            while (true)
                                try {
                                    Socket c = s.accept();
                                    PrintWriter out = new PrintWriter(c.getOutputStream(), true);
                                    out.println("Bem-vindo ao servidor " + rmireg_ref_remota);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
					}
                });
                socket_thread.start();
            }
        } catch (AlreadyBoundException e) {
            // se já existir um stub vinculado à referência informada, cai aqui nessa exceção.
            // neste ponto, o algoritmo verifica se a flag do "rebind" foi informada,
            // se sim, o vinculo entre a referência informada e o stub é sobrescrito,
            // se não, uma exceção é lançada; 
            if (rebind)
                rmireg.rebind(rmireg_ref_remota, stub);
            else {
                System.err.print("A referência remota utilizada já está registrada!\n");
                System.exit(0);
            }
        }
        System.out.printf(
            "Servidor pronto | 'rmiregistry' -> %s:%d | ref. remota %s\n",
            rmireg_host, rmireg_porta, rmireg_ref_remota
        );
    }
}