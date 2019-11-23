package app;

import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class Server implements PessoaInterface {
    private static Connection conn = null;

    private static void inicializar_banco() throws Exception {
        conn.createStatement().executeUpdate(
            "CREATE TABLE PESSOA (" +
            "ID         INTEGER PRIMARY KEY NOT NULL," +
            "NOME       TEXT                NOT NULL," +
            "ENDERECO   TEXT                NOT NULL)"
        );
    }

    @Override
    public List<Pessoa> todos() throws Exception {
        List<Pessoa> list = new ArrayList<Pessoa>();
        ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM PESSOA");
        while(rs.next()) {             
            Pessoa p = new Pessoa();
            p.id = rs.getLong("id");            
            p.nome = rs.getString("nome"); 
            p.endereco = rs.getString("endereco"); 
            list.add(p); 
        } 
        rs.close(); 
        return list;
    }

    @Override
    public List<Pessoa> filtrar(final String nome) throws Exception {
        List<Pessoa> list = new ArrayList<Pessoa>();
        PreparedStatement ps = conn.prepareStatement(
            "SELECT * FROM PESSOA WHERE NOME LIKE ?"
        );
        ps.setString(1, "%"+nome+"%");
        ResultSet rs = ps.executeQuery();
        while(rs.next()) {             
            Pessoa p = new Pessoa();
            p.id = rs.getLong("id");            
            p.nome = rs.getString("nome"); 
            p.endereco = rs.getString("endereco"); 
            list.add(p); 
        } 
        rs.close(); 
        return list;        
    }

    @Override
    public Pessoa adicionar(final Pessoa pessoa) throws Exception {
        PreparedStatement ps = conn.prepareStatement(
            "INSERT INTO PESSOA (NOME, ENDERECO) VALUES (?,?)",
            Statement.RETURN_GENERATED_KEYS
        );
        ps.setString(1, pessoa.nome);
        ps.setString(2, pessoa.endereco);
        ps.executeUpdate();

        ResultSet generatedKeys = ps.getGeneratedKeys();
        if (generatedKeys.next())
            pessoa.id = generatedKeys.getLong(1);

        return pessoa;    
    }

    @Override
    public Pessoa alterar(final Pessoa pessoa) throws Exception {
        PreparedStatement ps = conn.prepareStatement(
            "UPDATE PESSOA SET NOME=?, ENDERECO=? WHERE ID=?"            
        );
        ps.setString(1, pessoa.nome);
        ps.setString(2, pessoa.endereco);
        ps.setLong(3, pessoa.id);
        ps.executeUpdate();
        return pessoa;
    }

    @Override
    public Pessoa excluir(final Pessoa pessoa) throws Exception {
        PreparedStatement ps = conn.prepareStatement(
            "DELETE FROM PESSOA WHERE ID=?"            
        );
        ps.setLong(1, pessoa.id);
        ps.executeUpdate();
        return pessoa;
    }

    public static void main(final String args[]) {
        try {            
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite::memory:");
            conn.setAutoCommit(true);
            inicializar_banco();
            System.out.println("Banco de dados criando com sucesso ...");
            PessoaInterface stub = (PessoaInterface) UnicastRemoteObject
                .exportObject(new Server(), 0);
            LocateRegistry.getRegistry().bind("PessoaInterface", stub);
            System.err.println("Servidor pronto para receber requisições!");
        } catch (final Exception e) {            
            System.err.println("Não foi possível iniciar o servidor!");
            System.err.println("mais detalhes abaixo ...");
            e.printStackTrace();
        }
    }
}