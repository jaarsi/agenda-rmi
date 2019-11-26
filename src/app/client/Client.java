package app.client;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import app.rmi_interfaces.PessoaRMIInterface;

public class Client {
    public static void main(String[] args) {
        try {
            String host_rmireg;
            int porta_rmireg;
            try {
                host_rmireg = args[0];
                porta_rmireg = Integer.parseInt(args[1]);
            } catch (ArrayIndexOutOfBoundsException e) {
                host_rmireg = "localhost";
                try {
                    porta_rmireg = Integer.parseInt(args[0]);
                } catch (ArrayIndexOutOfBoundsException x) {
                    porta_rmireg = 1099;
                }                
            }
    
            Registry registro_rmi = LocateRegistry.getRegistry(host_rmireg, porta_rmireg);
            PessoaRMIInterface stub = 
                (PessoaRMIInterface) registro_rmi.lookup("server1");
            new CLI(stub).loop();
        } catch (Exception e) {
            e.printStackTrace();
        }    
    }
}