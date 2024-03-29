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
import java.util.ArrayList;
import java.util.List;

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
        // parser dos argumentos informados na execucao do programa.
        // neste ponto, o algoritmo descobre os valores que sao passados 
        // para host e porta do rmiregistry e o nome da referencia remota
        // que sera exportada. ex.: 

        // java -cp bin:lib/sqlite-jdbc-3.27.2.1.jar:lib/commons-cli-1.3.1.jar 
        //      app.server.Server -rmi_host 192.168.0.10 -rmi_port 2000 -ref server1

        // no comanda acima, o servidor esta buscando o rmiregistry no host 192.168.0.10
        // e na porta 2000. Se nao encontrar o rmiregistry, este sera criado localmente 
        // na porta 2000 e se a porta n for informada, 1099 é assumido.
        // o parametro -ref é o unico que é obrigatório;
        Options options = new Options();
        Option opt = new Option("rmi_host", true, "Host do 'rmiregistry'");
        options.addOption(opt);
        opt = new Option("ref", true, "Nome da referencia remota que sera exportada");
        opt.setRequired(true);
        options.addOption(opt);
        opt = new Option("rb", "Forca a exportacao da referencia remota");
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
            new HelpFormatter().printHelp("agenda-rmi-server", options);
            System.exit(0);
        }

        String rmireg_host = cmd.getOptionValue("rmi_host", "localhost");        
        String rmireg_ref_remota = cmd.getOptionValue("ref");
        boolean rebind = cmd.hasOption("rb");

        // carregamento do driver do sqlite;
        Class.forName("org.sqlite.JDBC");
        // criacao do objeto que representa a conexao com o banco de dados;
        Connection conexao = DriverManager.getConnection("jdbc:sqlite:agenda-rmi.db");
        // habilita o autocommit. assim, n ha necessidade de transacionar os
        // comandos com o banco;
        conexao.setAutoCommit(true);
        // cria, caso n exista, a tabela PESSOA;
        conexao.createStatement().executeUpdate(
            "CREATE TABLE IF NOT EXISTS PESSOA (" + 
            "ID INTEGER PRIMARY KEY NOT NULL," + 
            "NOME TEXT NOT NULL," + 
            "ENDERECO TEXT NOT NULL," +
            "CRIADO_EM INTEGER NOT NULL, " +
            "ALTERADO_EM INTEGER NOT NULL," +
            "EXCLUIDO INTEGER DEFAULT 0)"
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
        // cria uma tabela temporaria de usuarios logados;
        conexao.createStatement().executeUpdate(
            "CREATE TEMPORARY TABLE IF NOT EXISTS USUARIO_LOGADO (" + 
            "ID INTEGER PRIMARY KEY NOT NULL," + 
            "LOGIN TEXT UNIQUE ON CONFLICT REPLACE NOT NULL," + 
            "SERVIDOR TEXT NOT NULL," +
            "DTHR_LOGIN INTEGER NOT NULL)"
        );
        // criacao do "controller", que é responsavel por validar
        // e repassar os dados recebidos para o modelo processar;
        ServerController controller = new ServerController(conexao);
        // criacao e exportacao do stub, que é o objeto disponibilizado para 
        // o cliente remoto interagir com as funcionalidades disponibilizadas;
        ServerRMIInterface stub = 
            (ServerRMIInterface) UnicastRemoteObject.exportObject(controller, 0);
        // localiza onde o rmiregistry esta sendo executado, a partir das 
        // informacões passadas por parâmetro (host e porta) no momento da execucao 
        // do programa. caso as uma das ou nenhuma informacao sobre o rmiregistry 
        // tenha sido passado, assume-se "localhost" para o host e "1099" para a porta,
        // que sao os valores padrões;
        Registry rmireg = LocateRegistry.getRegistry(rmireg_host, 1099);
        try {
            try {
                // tenta vincular, no rmiregistry, o stub gerado (PessoaController) 
                // à referencia informada nos parâmetros;
                rmireg.bind(rmireg_ref_remota, stub);
            } catch (ConnectException e) {
                // caso o rmiregistry n seja encontrado no host e porta informado,
                // o codigo cai nessa excecao. 
                // Aqui, o rmiregistry é criado localmente (localhost) e o stub
                // vinculado à ele;
                System.out.print(
                    "O 'rmiregistry' informado nao respondeu, criando localmente ...\n"
                );
                rmireg_host = "localhost";
                rmireg = LocateRegistry.createRegistry(1099);
                rmireg.bind(rmireg_ref_remota, stub);

                // criando a thread responsavel por receber as requisicoes dos 
                // sockets dos clientes. ele tem a funcao de armazenar em um array
                // o "escritor" dos clientes que se conectam ao servidor, para 
                // de ser usado na classe 'Notificador'.
                // ele só é criado qdo rmiregistry é local;                
                List<PrintWriter> clientes = new ArrayList<PrintWriter>();
                Thread socket_thread = new Thread(new Runnable() {
					@Override
					public void run() {
                        ServerSocket s;
                        try {
                            s = new ServerSocket(2000);
                            while (true)
                                try {
                                    Socket c = s.accept();
                                    PrintWriter out = new PrintWriter(c.getOutputStream(), true);
                                    clientes.add(out);                                    
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
					}
                });
                socket_thread.start();

                // criando Thread de notificacao de mensagens;
                // a classe 'Notificador',  tem a funcao de carregar os eventos 
                // nao despachados, registrados no banco de dados e disparar esses
                // eventos para cada cliente conectado ao socket do servidor.
                // ele despacha os eventos a cada 10 segundos;
                Thread notificacao_thread = new Thread(new Runnable(){
                    @Override
                    public void run() {
                        Notificador notificador = new Notificador(conexao);
                        while (true) 
                            try {
                                notificador.despachar(clientes);
                                // aguarda por 10 segundos antes de despachar novos
                                // eventos;
                                Thread.sleep(10000);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                });
                notificacao_thread.start();
            }
        } catch (AlreadyBoundException e) {
            // se ja existir um stub vinculado à referencia informada, cai aqui nessa excecao.
            // neste ponto, o algoritmo verifica se a flag do "rebind" foi informada,
            // se sim, o vinculo entre a referencia informada e o stub é sobrescrito,
            // se nao, uma excecao é lancada; 
            if (rebind)
                rmireg.rebind(rmireg_ref_remota, stub);
            else {
                System.err.print("A referencia remota utilizada ja esta registrada!\n");
                System.exit(0);
            }
        }        
        System.out.printf(
            "Servidor pronto | 'rmiregistry' -> %s:1099 | ref. remota %s\n",  
            rmireg_host, rmireg_ref_remota
        );
    }
}