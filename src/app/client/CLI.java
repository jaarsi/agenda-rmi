package app.client;

import java.rmi.ConnectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.UnknownHostException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.sql.SQLException;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

import app.models.Pessoa;
import app.rmi_interfaces.PessoaRMIInterface;

public class CLI {
    private PessoaRMIInterface stub = null;
    private Registry rmireg = null;
    private String ref_remota;

    private void selecionar_servidor() throws RemoteException, NotBoundException {
        String rmireg_host = this.input_str("Informe o host do 'rmiregistry': ");
        int rmireg_porta = this.input_int("Informe a porta do 'rmiregistry': ");
        this.rmireg = LocateRegistry.getRegistry(rmireg_host, rmireg_porta);
        this.ref_remota = this
                .input_str(String.format(
                    "Selecione um servidor entre esses (%s): ", 
                    String.join(",", rmireg.list())));
        this.stub = (PessoaRMIInterface) rmireg.lookup(ref_remota);
        this.stub.echo();
    }

    private int menu() {
        return input_int(
                "1 - Listar todos os contatos\n" + 
                "2 - Filtrar contatos por nome\n" + 
                "3 - Buscar por código\n" + 
                "4 - Adicionar um contato\n" + 
                "5 - Alterar um contato\n" + 
                "6 - Excluir um contato\n" + 
                "7 - Selecionar servidor\n" + 
                "0 - Finalizar sistema\n"
                + "Informe o codigo da ação desejada: ");
    }

    private void listar() throws RemoteException, SQLException {
        this.show_pessoa(this.stub.listar());
    }

    private void filtrar() throws RemoteException, SQLException {
        String nome = this.input_str("Informe o nome que deve ser filtrado: ");
        this.show_pessoa(this.stub.filtrar(nome));
    }

    private void buscar() throws RemoteException, SQLException {
        int id = this.input_int("Informe o codigo do contato: ");
        Pessoa p = this.stub.buscar(id);
        if (p == null)
            throw new SQLException("Não existe nenhum contato com o codigo informado ...");
        this.show_pessoa(p);
    }

    private void adicionar() throws RemoteException, SQLException {
        String nome = this.input_str("Informe o nome do contato: ");
        String endereco = this.input_str("Informe o endereco do contato:\n");
        Pessoa p = new Pessoa(0, nome, endereco);
        this.stub.adicionar(p);
        this.show_msg("Contato cadastrado com sucesso!\n");
    }

    private void alterar() throws RemoteException, SQLException {
        int id = this.input_int("Informe o codigo do contato que deseja editar: ");
        Pessoa p = this.stub.buscar(id);
        if (p == null) 
            throw new SQLException("Não existe nenhum contato com o codigo informado ...");
        String nome = this.input_str(
            String.format("Informe o nome do contato: (%s) ", p.nome));
        if (!nome.trim().isBlank())
            p.nome = nome;
        String endereco = this.input_str(
            String.format("Informe o endereco do contato: (%s)\n", p.endereco));
        if (!endereco.trim().isBlank())
            p.endereco = endereco;
        this.stub.alterar(p);
        this.show_msg("Contato alterado com sucesso!\n");
    }

    private void excluir() throws RemoteException, SQLException {
        int id = this.input_int("Informe o codigo do contato que deseja excluir: ");
        Pessoa p = this.stub.buscar(id);
        if (p == null) 
            throw new SQLException("Não existe nenhum contato com o codigo informado ...");
        this.show_msg("Este procedimento excluirá o contato abaixo:\n");
        this.show_pessoa(p);
        String confirmacao = this.input_str(
            "Confirmar exclusao? ('s' para SIM ou 'n' para NÃO): ");
        if (confirmacao.equals("s")) {
            this.stub.excluir(p);
            this.show_msg("Contato excluído com sucesso!\n");
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
                this.show_msg("\nInforme um numero inteiro ...\n\n"); 
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

    private void clearScreen() {  
        System.out.print("\033[H\033[2J");  
        System.out.flush();  
    }

    public void loop() {        
        while (true)
            try {
                if (this.stub == null)
                    this.selecionar_servidor();
                this.show_msg("Conectado à " + this.ref_remota + "\n\n");
                int opcao = this.menu();                
                this.clearScreen();
                switch (opcao) {
                    case 1: { this.listar(); break; }
                    case 2: { this.filtrar(); break; }
                    case 3: { this.buscar(); break; }
                    case 4: { this.adicionar(); break; }
                    case 5: { this.alterar(); break; }
                    case 6: { this.excluir(); break; }
                    case 7: { this.selecionar_servidor(); break; }
                    case 0: { System.exit(0); break; }
                    default: this.show_msg("Opção incorreta ...\n");
                }
            } catch (UnknownHostException e) {
                this.show_msg("\n\n");
                this.show_msg("O host fornecido é desconhecido ...");
            } catch (NotBoundException e) {
                this.show_msg("\n\n");
                this.show_msg(
                    "A referência remota não existe ou servidor que "+
                    "a hospeda está inoperante ou é inacançável ...");
                this.stub = null;                    
            } catch (ConnectException e) {
                this.show_msg("\n\n");
                this.show_msg(
                    "O servidor que hospeda a referência remota não responde ...");
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