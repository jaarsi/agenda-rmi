package app.client;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import app.rmi_interfaces.PessoaRMIInterface;

public class Client {
    public static final int PORTA_RMI_PADRAO = 1099;

    public static void main(String[] args) {
        try {
            int porta_rmi = 
                (args.length > 0) ? Integer.parseInt(args[0]) : PORTA_RMI_PADRAO;
            Registry registro_rmi = LocateRegistry.getRegistry(porta_rmi);
            PessoaRMIInterface stub = 
                (PessoaRMIInterface) registro_rmi.lookup("PessoaRMIInterface");
        } catch (Exception e) {
            System.err.println("Erro: " + e.getMessage());
        }    
    }
}