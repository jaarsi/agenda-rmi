package app.rmi_interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.List;

import app.models.Pessoa;

public interface PessoaRMIInterface extends Remote {
    public List<Pessoa> todos() throws Exception; 
    public List<Pessoa> filtrar(String nome) throws RemoteException, SQLException;
    public Pessoa buscar(Long id) throws RemoteException, SQLException;    
    public Pessoa adicionar(Pessoa pessoa) throws RemoteException, SQLException;
    public Pessoa alterar(Pessoa pessoa) throws RemoteException, SQLException;
    public Pessoa excluir(Pessoa pessoa) throws RemoteException, SQLException;
}