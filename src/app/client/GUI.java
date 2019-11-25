package app.client;

import java.util.Scanner;

import app.models.Pessoa;
import app.rmi_interfaces.PessoaRMIInterface;

public class GUI {
    private PessoaRMIInterface stub = null;

    private void print_menu() {
        System.out.print("1 - Listar todos os contatos\n"+
                         "2 - Filtrar contatos por nome\n"+
                         "3 - Buscar por código\n"+
                         "4 - Adicionar um contato\n"+                         
                         "5 - Alterar um contato\n"+
                         "6 - Excluir um contato\n");
    }

    private String input_usuario() {
        Scanner scanner = new Scanner(System.in);
        return scanner.nextLine();
    }

    public GUI(PessoaRMIInterface stub) {
        this.stub = stub;
    }

    public void loop() throws Exception {
        while (true)
            try {
                this.print_menu();
                int opcao = Integer.parseInt(this.input_usuario());
                if (opcao == 1)
                    for (Pessoa p : this.stub.todos())
                        System.out.printf("%s\n", p.toString());
                else if (opcao == 2) {
                    System.out.print("Informe o nome que deve ser filtrado: ");
                    String nome = this.input_usuario();
                    for (Pessoa p : this.stub.filtrar(nome))
                        System.out.printf("%s\n", p.toString());                
                } else if (opcao == 3) {
                    System.out.print("Informe o codigo do contato: ");
                    Long id = Long.parseLong(this.input_usuario());
                    System.out.printf("%s\n", this.stub.buscar(id).toString());
                } else if (opcao == 4) {
                    Pessoa p = new Pessoa();
                    System.out.print("Informe o nome: ");
                    p.nome = this.input_usuario();
                    System.out.print("Informe o endereco: ");
                    p.endereco = this.input_usuario();
                    this.stub.adicionar(p);
                } else if (opcao == 5) {                    
                    System.out.print("Informe o codigo: ");
                    Long id = Long.parseLong(this.input_usuario());
                    Pessoa p = this.stub.buscar(id);
                    System.out.print("Informe o nome: ");
                    p.nome = this.input_usuario();
                    System.out.print("Informe o endereco: ");
                    p.endereco = this.input_usuario();
                    this.stub.alterar(p);
                } else if (opcao == 6) {
                    System.out.print("Informe o codigo para a exclusao: ");
                    Long id = Long.parseLong(this.input_usuario());
                    Pessoa p = this.stub.buscar(id);
                    this.stub.excluir(p);
                }
            } catch (Exception e) {
                System.err.print(e.getMessage());
            } 
    }
}