package app.models;

import java.io.Serializable;

public class Pessoa implements Serializable {
    private static final long serialVersionUID = 1L;
    public Long id;
    public String nome;
    public String endereco;

    @Override
    public String toString() {
        return String.format("%d\t%s\t\t%s", this.id, this.nome, this.endereco);
    }
}