package app.client;

import java.rmi.ConnectException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Scanner;

import app.models.Pessoa;
import app.rmi_interfaces.PessoaRMIInterface;

public class CLI {
    private PessoaRMIInterface stub;
    private Registry rmireg;
    private String ref_remota;

    private void selecionar_servidor() throws Exception {
        String rmireg_host = this.input_str("Informe o host do 'rmiregistry': ");
        int rmireg_porta = this.input_int("Informe a porta do 'rmiregistry': ");
        rmireg = LocateRegistry.getRegistry(rmireg_host, rmireg_porta);
        ref_remota = this.input_str(
            String.format(
                "Selecione um servidor entre esses (%s): ", 
                 String.join(",", rmireg.list())));
        stub = (PessoaRMIInterface) rmireg.lookup(ref_remota);
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
            "0 - Finalizar sistema\n" +
            "Informe o codigo da ação desejada: "
        );
    }

    private void listar() throws Exception {
        this.show_pessoa(this.stub.listar());
    }

    private void filtrar() throws Exception {
        String nome = this.input_str("Informe o nome que deve ser filtrado: ");
        this.show_pessoa(this.stub.filtrar(nome));
    }

    private void buscar() throws Exception {
        int id = this.input_int("Informe o codigo do contato: ");
        Pessoa p = this.stub.buscar(id);
        if (p == null)
            throw new Exception("Não existe nenhum contato com o codigo informado ...");
        this.show_pessoa(p);
    }

    private void adicionar() throws Exception {
        String nome = this.input_str("Informe o nome do contato: ");
        String endereco = this.input_str("Informe o endereco do contato:\n");
        Pessoa p = new Pessoa(0, nome, endereco);
        this.stub.adicionar(p);
        this.show_msg("Contato cadastrado com sucesso!\n");
    }

    private void alterar() throws Exception {
        int id = this.input_int("Informe o codigo do contato que deseja editar: ");
        Pessoa p = this.stub.buscar(id);
        if (p == null) 
            throw new Exception("Não existe nenhum contato com o codigo informado ...");
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

    private void excluir() throws Exception {
        int id = this.input_int("Informe o codigo do contato que deseja excluir: ");
        Pessoa p = this.stub.buscar(id);
        if (p == null) 
            throw new Exception("Não existe nenhum contato com o codigo informado ...");
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
        System.out.print(msg);
        Scanner leitor = new Scanner(System.in);
        return leitor.nextInt();
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

    public void loop() throws Exception {
        this.selecionar_servidor();
        this.show_msg("\n\n");
        int opcao = this.menu();
        while (true)
            try {                
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
            } catch (ConnectException e) {
                try{
                    this.rmireg.unbind(this.ref_remota);
                } catch (Exception x) {
                    this.show_msg(
                        "A conexão com o 'rmiregistry' foi perdida. "+
                        "Entre em contato com o administrador...\n");
                    System.exit(0);
                }
                this.show_msg("\n\n");                
                this.show_msg("A conexão com o servidor caiu, selecione outro ...\n");
                this.selecionar_servidor();
            } catch (Exception e) {
                this.show_msg("\n\n");
                this.show_msg(e.getMessage() + "\n");
            } finally {
                this.show_msg("\n\n");
                this.show_msg("Conectado à " + this.ref_remota + "\n\n");
                opcao = this.menu();
            }
        }
}