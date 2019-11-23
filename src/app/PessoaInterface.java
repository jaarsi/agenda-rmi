package app;

import java.rmi.Remote;
import java.util.List;

public interface PessoaInterface extends Remote {
    public List<Pessoa> todos() throws Exception; 
    public List<Pessoa> filtrar(String nome) throws Exception;
    public Pessoa adicionar(Pessoa pessoa) throws Exception;
    public Pessoa alterar(Pessoa pessoa) throws Exception;
    public Pessoa excluir(Pessoa pessoa) throws Exception;
}