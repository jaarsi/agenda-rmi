package app.models;

import java.io.Serializable;
import java.util.Date;

public class Evento implements Serializable {
    private static final long serialVersionUID = 1L;
    public int id;
    public Pessoa pessoa;
    public String usuario;
    public String servidor;
    public String operacao;
    public Date dthr_evento;
    public boolean despachado;
    
    @Override
    public String toString() {
        if (this.pessoa != null) 
            return String.format(
                "O usuario %s acabou de %s o registro [%d] %s no servidor %s em %tc", 
                this.usuario, this.operacao, this.pessoa.id, this.pessoa.nome, 
                this.servidor, this.dthr_evento
            );
        else
            return String.format(
                "O usuario %s acabou de %s no servidor %s em %tc", 
                this.usuario, this.operacao, this.servidor, this.dthr_evento
            );
    }
}