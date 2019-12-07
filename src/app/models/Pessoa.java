package app.models;

import java.io.Serializable;
import java.util.Date;

public class Pessoa implements Serializable {
    private static final long serialVersionUID = 1L;
    public int id;
    public String nome;
    public String endereco;
    public Date criado_em;
    public Date alterado_em;
    public boolean excluido;

    public Pessoa(int id, String nome, String endereco) {
        this.id = id;
        this.nome = nome;
        this.endereco = endereco;
        this.criado_em = new Date();
        this.alterado_em = new Date();
        this.excluido = false;
    }

    public Pessoa() {
	}

	@Override
    public String toString() {
        return String.format("%-5d %-45s %-50s", this.id, this.nome, this.endereco);
    }
}