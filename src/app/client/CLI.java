package app.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.rmi.ConnectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.UnknownHostException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.sql.SQLException;
import java.util.Date;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

import app.models.Pessoa;
import app.models.Usuario;
import app.controllers.ServerRMIInterface;

public class CLI {
    private Usuario usuario = null;
    private ServerRMIInterface stub = null;
    private String rmireg_host;
    private int rmireg_porta;    
    private String ref_remota;    

    private void identificar_usuario() {
        this.usuario = new Usuario();
        this.usuario.login = this.input_str("Por favor, identifique-se: ");
    }

    private void selecionar_servidor() throws RemoteException, NotBoundException, SQLException {
        this.rmireg_host = this.input_str("Informe o host do 'rmiregistry': ");
        this.rmireg_porta = this.input_int("Informe a porta do 'rmiregistry': ");
        Registry rmireg = LocateRegistry.getRegistry(rmireg_host, rmireg_porta);
        this.ref_remota = this.input_str(String.format(
            "Selecione um servidor entre esses (%s): ", 
            String.join(",", rmireg.list()))
        );
        this.stub = (ServerRMIInterface) rmireg.lookup(ref_remota);
        this.stub.echo();

        this.usuario.servidor = ref_remota;
        this.usuario.dthr_login = new Date();
        this.stub.logar(this.usuario);

        // inicializacao do socket aqui;
        CLI self = this;
        Thread socket_thread = new Thread(new Runnable() {
			@Override
			public void run() {
                try {
                    Socket s = new Socket(self.rmireg_host, self.rmireg_porta+1);
                    BufferedReader in = 
                        new BufferedReader(new InputStreamReader(s.getInputStream()));
                    while (true) 
                        try {
                            String msg = in.readLine();
                            if (msg.indexOf(self.usuario.login) == -1) 
                                continue;
                            msg = "\n\n" + msg + "\n\n";                            
                            self.show_msg(msg);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }                        
                } catch (IOException e) {
                    e.printStackTrace();
                }
			}
        });
        socket_thread.start();
    }

    private int menu() {
        return input_int(
            String.format(
                "Bem-vindo %s, voce esta conectado ao servidor %s !\n", 
                this.usuario.login, this.ref_remota) +
            "1 - Listar todos os contatos\n" + 
            "2 - Filtrar contatos por nome\n" + 
            "3 - Buscar por codigo\n" + 
            "4 - Adicionar um contato\n" + 
            "5 - Alterar um contato\n" + 
            "6 - Excluir um contato\n" + 
            "7 - Cadastro combinado\n" +
            "8 - Selecionar servidor\n" + 
            "0 - Finalizar sistema\n"
            + "Informe o codigo da acao desejada: "
        );
    }

    private void listar() throws RemoteException, SQLException {
        this.show_pessoa(this.stub.listar(this.usuario));
    }

    private void filtrar() throws RemoteException, SQLException {
        String nome = this.input_str("Informe o nome que deve ser filtrado: ");
        this.show_pessoa(this.stub.filtrar(this.usuario, nome));
    }

    private void buscar() throws RemoteException, SQLException {
        int id = this.input_int("Informe o codigo do contato: ");
        Pessoa p = this.stub.buscar(this.usuario, id);
        if (p == null)
            throw new SQLException("Nao existe nenhum contato com o codigo informado ...");
        this.show_pessoa(p);
    }

    private void adicionar() throws RemoteException, SQLException {
        String nome = this.input_str("Informe o nome do contato: ");
        String endereco = this.input_str("Informe o endereco do contato:\n");
        Pessoa p = new Pessoa(0, nome, endereco);
        this.stub.adicionar(this.usuario, p);
        this.show_msg("Contato cadastrado com sucesso!\n");
    }

    private void alterar() throws RemoteException, SQLException {
        int id = this.input_int("Informe o codigo do contato que deseja editar: ");
        Pessoa p = this.stub.buscar(this.usuario, id);
        if (p == null) 
            throw new SQLException("Nao existe nenhum contato com o codigo informado ...");
        String nome = this.input_str(
            String.format("Informe o nome do contato: (%s) ", p.nome));
        if (!nome.trim().isBlank())
            p.nome = nome;
        String endereco = this.input_str(
            String.format("Informe o endereco do contato: (%s)\n", p.endereco));
        if (!endereco.trim().isBlank())
            p.endereco = endereco;
        this.stub.alterar(this.usuario, p);
        this.show_msg("Contato alterado com sucesso!\n");
    }

    private void excluir() throws RemoteException, SQLException {
        int id = this.input_int("Informe o codigo do contato que deseja excluir: ");
        Pessoa p = this.stub.buscar(this.usuario, id);
        if (p == null) 
            throw new SQLException("Nao existe nenhum contato com o codigo informado ...");
        this.show_msg("Este procedimento excluira o contato abaixo:\n");
        this.show_pessoa(p);
        String confirmacao = this.input_str(
            "Confirmar exclusao? ('s' para SIM ou 'n' para NAO): "
        );
        if (confirmacao.equals("s")) {
            this.stub.excluir(this.usuario, p);
            this.show_msg("Contato excluido com sucesso!\n");
        }
    }

    private void cadastro_combinado() throws SQLException, IOException {
        Socket s = null;
        try {
            this.stub.cadastro_combinado();
            Thread.sleep(3000);
            s = new Socket(this.rmireg_host, 5555);
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            PrintWriter out = new PrintWriter(s.getOutputStream(), true);
            while (true) {
                String msg = in.readLine();
                if (msg.equals("fim")) 
                    break;
                else if (msg.lastIndexOf(":") > 0)                     
                    out.printf("%s\n", this.input_str(msg));
                else
                    this.show_msg(msg+"\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (s != null)
                s.close();
        }
    }

    protected String input_str(String msg) {
        System.out.print(msg);
        Scanner leitor = new Scanner(System.in);
        return leitor.nextLine().trim();
    }

    protected int input_int(String msg) {
        int n = 0;
        Scanner leitor = new Scanner(System.in);
        while (true) 
            try{
                System.out.print(msg);
                n = leitor.nextInt();
                break;
            } catch (InputMismatchException e) {
                this.show_msg("\nInforme um numero inteiro...\n\n"); 
                leitor.nextLine();               
            }
        return n;
    }

    protected void show_msg(String msg) {
        System.out.print(msg);
    }

    protected void show_pessoa(Pessoa pessoa) {
        System.out.printf("%-5s %-15s %-50s\n", "ID", "Nome", "Endereco");
        System.out.print("-".repeat(70)+'\n');
        System.out.printf("%s\n", pessoa.toString());
    }

    protected void show_pessoa(List<Pessoa> pessoas) {
        System.out.printf("%-5s %-15s %-50s\n", "ID", "Nome", "Endereco");
        System.out.print("-".repeat(70)+'\n');
        for (Pessoa p: pessoas)            
            System.out.printf("%s\n", p.toString());
    }

    protected void clearScreen() {  
        System.out.print("\033[H\033[2J");  
        System.out.flush();  
    }

    public void loop() {        
        while (true)
            try {
                if (this.usuario == null) 
                    this.identificar_usuario();
                if (this.stub == null)
                    this.selecionar_servidor();
                int opcao = this.menu();                
                this.clearScreen();
                switch (opcao) {
                    case 1: { this.listar(); break; }
                    case 2: { this.filtrar(); break; }
                    case 3: { this.buscar(); break; }
                    case 4: { this.adicionar(); break; }
                    case 5: { this.alterar(); break; }
                    case 6: { this.excluir(); break; }
                    case 7: { this.cadastro_combinado(); break; }
                    case 8: { this.selecionar_servidor(); break; }
                    case 0: { System.exit(0); break; }
                    default: this.show_msg("Opcao incorreta ...\n");
                }
            } catch (UnknownHostException e) {
                this.show_msg("\n\n");
                this.show_msg("O host fornecido e desconhecido ...");
            } catch (NotBoundException e) {
                this.show_msg("\n\n");
                this.show_msg(
                    "A referencia remota nao existe ou servidor que "+
                    "a hospeda esta inoperante ou e inacancavel ..."
                );
                this.stub = null;                    
            } catch (ConnectException e) {
                this.show_msg("\n\n");
                this.show_msg(
                    "O servidor que hospeda a referencia remota nao responde ..."
                );
                this.stub = null;                    
            } catch (RemoteException e) {
                this.show_msg("\n\n");
                e.printStackTrace();
            } catch (SQLException e) {
                this.show_msg("\n\n");
                this.show_msg(e.getMessage());
            } catch (Exception e) {
                this.show_msg("\n\n");
                this.show_msg(e.getClass().getName() + e.getMessage());
            } finally {
                this.show_msg("\n\n");
            }
    }
}