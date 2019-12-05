package app.controllers;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.List;

import app.models.Pessoa;
import app.models.Usuario;

public interface ServerRMIInterface extends Remote {
    public String echo() throws RemoteException;
    public void logar(Usuario usuario) throws RemoteException, SQLException;
    public List<Pessoa> listar(Usuario usuario) throws RemoteException, SQLException; 
    public List<Pessoa> filtrar(Usuario usuario, String nome) throws RemoteException, SQLException;
    public Pessoa buscar(Usuario usuario, int id) throws RemoteException, SQLException;    
    public Pessoa adicionar(Usuario usuario, Pessoa pessoa) throws RemoteException, SQLException;
    public Pessoa alterar(Usuario usuario, Pessoa pessoa) throws RemoteException, SQLException;
    public Pessoa excluir(Usuario usuario, Pessoa pessoa) throws RemoteException, SQLException;
    public void cadastro_combinado() throws RemoteException, SQLException;
}