package app.client;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class Client {
    public static void main(String[] args) throws Exception {
        Options options = new Options();
        Option opt = new Option("gui", "Força a exportação da referência remota");
        options.addOption(opt);

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd;        
        try {
            // parser dos argumentos passados como parâmetro;
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            // se ocorrer algum problema no parser, encerra o programa e mostra 
            // a ajuda;
            cmd = null;
            System.out.println(e.getMessage());
            new HelpFormatter().printHelp("agenda-rmi-client", options);
            System.exit(0);
        }

        boolean gui = cmd.hasOption("gui");

        if (gui)
            new GUI().loop();        
        else
            new CLI().loop();
    }
}