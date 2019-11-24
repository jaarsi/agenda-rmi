package app.client;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import app.models.Pessoa;
import app.rmi_interfaces.PessoaRMIInterface;

public class Client {
    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.getRegistry();
            PessoaRMIInterface stub = (PessoaRMIInterface) registry.lookup("PessoaRMIInterface");
            Pessoa pe = new Pessoa();
            pe.nome = "jairo";
            pe.endereco = "r 244, 145, apto 203, meia-praia - Santa Catarina";
            stub.adicionar(pe);
            for (Pessoa p: stub.todos()) 
                System.out.println(p.id + '\t' + p.nome);            
        } catch (Exception e) {
            System.err.println("Erro: " + e.getMessage());
        }    
    }
}