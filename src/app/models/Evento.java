package app.models;

import java.io.Serializable;
import java.util.Date;

public class Evento implements Serializable {
    private static final long serialVersionUID = 1L;
    public int id;
    public int pessoa_id;
    public String usuario;
    public String servidor;
    public String operacao;
    public Date dthr_evento;
    public boolean despachado; 
}