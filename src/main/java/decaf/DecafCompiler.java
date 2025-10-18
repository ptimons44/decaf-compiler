package decaf;

import decaf.utils.CommandLineInterface;
import decaf.utils.CommandLineInterface.CompilerAction;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class DecafCompiler {

    public static void main(String[] args) {
        CommandLineInterface.parse(args, new String[0]);
        try (InputStream inputStream = CommandLineInterface.infile == null ? System.in : Files.newInputStream(Path.of(CommandLineInterface.infile));
             OutputStream outputStream = CommandLineInterface.outfile == null ? System.out : new PrintStream(new FileOutputStream(CommandLineInterface.outfile))) {
            
            String in = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

            Scan scan = new Scan(in);
            scan.scan();
            if (CommandLineInterface.target == CompilerAction.SCAN) {
                scan.write(outputStream);
                return;
            }

            Parse parse = new Parse(scan);
            Boolean isValidProgram = parse.getIsValidProgram();
            if (CommandLineInterface.target == CompilerAction.PARSE) {
                // parse.printErrors();
                // parse.printWarnings();
                System.out.println(isValidProgram ? "OK" : "ERR");
                return;
            }

            if (CommandLineInterface.target == CompilerAction.INTER) {
                return;
            }

            if (CommandLineInterface.target == CompilerAction.ASSEMBLY) {
                return;
            }
            return;
        } catch (IOException ioe) {
            System.err.printf("IOException encountered while processing file: %s", CommandLineInterface.infile);
            System.exit(1);
        }
    }

}