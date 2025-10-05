package decaf;

import decaf.utils.CommandLineInterface;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.decaf.utils.CommandLineInterface.CompilerAction;

public class DecafCompiler {

    public static void main(String[] args) {
        CommandLineInterface.parse(args, new String[0]);
        try (InputStream inputStream = CommandLineInterface.infile == null ? System.in : Files.newInputStream(Path.of(CommandLineInterface.infile));
             OutputStream outputStream = CommandLineInterface.outfile == null ? System.out : new PrintStream(new FileOutputStream(CommandLineInterface.outfile))) {
            
            String in = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

            Scan scan = new Scan(in);
            scan.scan();
            if (CommandLineInterface.target == SCAN) {
                scan.write(outputStream);
                return 0;
            }

            Parse parse = new Parse(scan);
            parse.parse();
            if (CommandLineInterface.target == PARSE) {
                return parse.getErrors();
            }

            if (CommandLineInterface.target == INTER) {
                return 0;
            }

            if (CommandLineInterface.target == ASSEMBLY) {
                return 0;
            }
            return 0;
        } catch (IOException ioe) {
            System.err.printf("IOException encountered while processing file: %s", CommandLineInterface.infile);
            System.exit(1);
        }
    }

}