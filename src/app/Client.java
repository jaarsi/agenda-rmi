package app;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Client {
    public static void main(String[] args) {
        String host = (args.length < 1) ? null : args[0];
        try {
            Registry registry = LocateRegistry.getRegistry(host);
            PessoaInterface stub = (PessoaInterface) registry.lookup("PessoaInterface");
            Pessoa pe = new Pessoa();
            pe.nome = "jairo";
            pe.endereco = "r 244, 145, apto 203, meia-praia - Santa Catarina";
            stub.adicionar(pe);
            for (Pessoa p: stub.todos()) 
                System.out.println(p.id + '\t' + p.nome);            
        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }    
    }
}