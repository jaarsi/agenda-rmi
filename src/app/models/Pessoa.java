package app.models;

import java.io.Serializable;

public class Pessoa implements Serializable {
    private static final long serialVersionUID = 1L;
    public int id;
    public String nome;
    public String endereco;

    public Pessoa(int id, String nome, String endereco) {
        this.id = id;
        this.nome = nome;
        this.endereco = endereco;
    }

    public Pessoa() {
	}

	@Override
    public String toString() {
        return String.format("%-5d %-15s %-50s", this.id, this.nome, this.endereco);
    }
}