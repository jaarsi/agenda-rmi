package app.client;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import app.rmi_interfaces.PessoaRMIInterface;

public class Client {
    public static void main(String[] args) {
        String host_rmireg;
        int porta_rmireg;
        try {
            if (args.length < 1)
                throw new Exception(
                    "Informa host e porta ou somente a porta "+
                    "de onde o 'rmiregistry' está executando");
            try {
                host_rmireg = args[0];
                porta_rmireg = Integer.parseInt(args[1]);
            } catch (ArrayIndexOutOfBoundsException e) {
                host_rmireg = "localhost";
                porta_rmireg = Integer.parseInt(args[0]);
            }
            Registry registro_rmi = LocateRegistry.getRegistry(host_rmireg, porta_rmireg);
            PessoaRMIInterface stub = 
                (PessoaRMIInterface) registro_rmi.lookup("PessoaRMIInterface");
            new GUI(stub).loop();
        } catch (Exception e) {
            e.printStackTrace();
        }    
    }
}