package app.client;

import javax.swing.JOptionPane;

import app.rmi_interfaces.PessoaRMIInterface;

public class GUI {
    private PessoaRMIInterface stub = null;

    private void menu() {
        //JOptionPane.showInputDialog(parentComponent, message)        
    }

    public GUI(PessoaRMIInterface stub) {
        this.stub = stub;
    }

    public void loop() {

    }
}