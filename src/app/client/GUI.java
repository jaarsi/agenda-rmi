package app.client;

import java.util.List;

import javax.swing.JOptionPane;

import app.models.Pessoa;

public class GUI extends CLI {
    @Override
    protected String select_str(String msg, String[] escolhas) { 
        return (String) JOptionPane.showInputDialog(
            null, 
            msg,
            "Selecione um dos valores ...", 
            JOptionPane.QUESTION_MESSAGE, 
            null, 
            escolhas, // Array of choices
            escolhas.length > 0 ? escolhas[1] : "");
    }

    @Override
    protected int input_int(String msg) {
        while (true) {
            String n = JOptionPane.showInputDialog(msg).trim();
            try {
                return Integer.parseInt(n);
            } catch (NumberFormatException e) {
                this.show_msg("Informe um numero inteiro ...");    
            }
        }
    }

    @Override
    protected String input_str(String msg) {
        return JOptionPane.showInputDialog(msg).trim();
    }

    @Override
    protected void show_msg(String msg) {
        msg = msg.trim();
        if (!msg.isEmpty())
            JOptionPane.showMessageDialog(null, msg);
    }

    @Override
    protected void show_pessoa(List<Pessoa> pessoas) {
        String s = "";
        for (Pessoa p: pessoas)
            s = s + p.toString() + "\n";
        this.show_msg(s);
    }

    @Override
    protected void show_pessoa(Pessoa pessoa) {
        this.show_msg(pessoa.toString());
    }
}