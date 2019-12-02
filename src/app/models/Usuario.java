package app.models;

import java.io.Serializable;
import java.util.Date;

public class Usuario implements Serializable {
    private static final long serialVersionUID = 7334013973839963314L;
    public int id;
    public String login;
    public String servidor;
    public Date dthr_login;
}