package app.client;

import java.util.List;
import java.util.Scanner;

import app.models.Pessoa;
import app.rmi_interfaces.PessoaRMIInterface;

public class CLI {
    private PessoaRMIInterface stub = null;

    private int menu() {
        System.out.print(
            "1 - Listar todos os contatos\n" + 
            "2 - Filtrar contatos por nome\n" + "3 - Buscar por código\n" +
            "4 - Adicionar um contato\n" +
            "5 - Alterar um contato\n" +
            "6 - Excluir um contato\n" +
            "Informe o codigo da ação desejada: ");        
        return input_int();
    }

    private void listar() throws Exception {
        this.print_pessoa(this.stub.listar());
    }

    private void filtrar() throws Exception {
        System.out.print("Informe o nome que deve ser filtrado: ");
        String nome = this.input_str();
        this.print_pessoa(this.stub.filtrar(nome));
    }

    private void buscar() throws Exception {
        System.out.print("Informe o codigo do contato: ");
        int id = this.input_int();
        Pessoa p = this.stub.buscar(id);
        if (p == null)
            throw new Exception("Não existe nenhum contato com o codigo informado ...");
        this.print_pessoa(p);
    }

    private void adicionar() throws Exception {
        System.out.print("Informe o nome do contato: ");
        String nome = this.input_str();
        System.out.print("Informe o endereco do contato: ");
        String endereco = this.input_str();
        Pessoa p = new Pessoa(0, nome, endereco);
        this.stub.adicionar(p);
        System.out.print("Contato cadastrado com sucesso!");
    }

    private void alterar() throws Exception {
        System.out.print("Informe o codigo do contato que deseja editar: ");
        int id = this.input_int();
        Pessoa p = this.stub.buscar(id);

        if (p == null)
            throw new Exception("Não existe nenhum contato com o codigo informado ...");
        
        System.out.printf("Informe o nome do contato: (%s)", p.nome);
        p.nome = this.input_str();
        System.out.printf("Informe o endereco do contato: (%s)", p.endereco);
        p.endereco = this.input_str();
        this.stub.alterar(p);
        System.out.print("Contato alterado com sucesso!");
    }

    private void excluir() throws Exception {
        System.out.print("Informe o codigo do contato que deseja excluir: ");
        int id = this.input_int();
        Pessoa p = this.stub.buscar(id);
        if (p == null)
            throw new Exception("Não existe nenhum contato com o codigo informado ...");            
        System.out.print("Este procedimento excluirá o contato abaixo:\n");
        this.print_pessoa(p);
        System.out.print("Confirmar exclusao? ('s' para SIM ou 'n' para NÃO): ");
        String confirmacao = this.input_str();
        if (confirmacao.equals("s")) {
            this.stub.excluir(p);
            System.out.print("Contato excluído com sucesso!");
        }
    }

    private String input_str() {
        Scanner leitor = new Scanner(System.in);
        return leitor.nextLine().trim();
    }

    private int input_int() {
        Scanner leitor = new Scanner(System.in);
        return leitor.nextInt();
    }

    private void print_pessoa(Pessoa pessoa) {
        System.out.printf("%-4s %-15s %-50s\n", "ID", "Nome", "Endereco");
        System.out.printf("%s\n", pessoa.toString());
    }

    private void print_pessoa(List<Pessoa> pessoas) {
        System.out.printf("%-4s %-15s %-50s\n", "ID", "Nome", "Endereco");
        for (Pessoa p: pessoas)            
            System.out.printf("%s\n", p.toString());
    }

    public CLI(PessoaRMIInterface stub) {
        this.stub = stub;
    }

    public void loop() throws Exception {
        while (true)
            try {
                int opcao = this.menu();
                switch (opcao) {
                    case 1: { this.listar(); break; }
                    case 2: { this.filtrar(); break; }
                    case 3: { this.buscar(); break; }
                    case 4: { this.adicionar(); break; }
                    case 5: { this.alterar(); break; }
                    case 6: { this.excluir(); break; }                
                    default: System.out.print("Opção incorreta ...\n");
                }
            } catch (Exception e) {
                System.err.printf("%s\n", e.getMessage());
            }
    }
}