package app.client;

import java.util.List;
import java.util.stream.Collector;
import static java.util.stream.Collectors.joining;

import javax.swing.JOptionPane;

import app.models.Pessoa;

public class GUI extends CLI {
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
        JOptionPane.showMessageDialog(null, msg);
    }

    @Override
    protected void show_pessoa(List<Pessoa> pessoas) {
        String s = pessoas.stream()
            .map(p -> p.toString())
            .collect(joining("\n"));
        this.show_msg(s);
    }

    @Override
    protected void show_pessoa(Pessoa pessoa) {
        this.show_msg(pessoa.toString());
    }
}