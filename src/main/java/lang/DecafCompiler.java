package lang;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import lang.grammars.decaf.DecafLanguage;
import lang.utils.CommandLineInterface;
import lang.utils.CommandLineInterface.CompilerAction;

public class DecafCompiler {

    public static void main(String[] args) {
        CommandLineInterface.parse(args, new String[0]);

        // Create Decaf language and generic compiler
        Language decaf = new DecafLanguage();
        Compiler compiler = new Compiler(decaf);

        try (InputStream inputStream = CommandLineInterface.infile == null ?
                System.in : Files.newInputStream(Path.of(CommandLineInterface.infile));
             OutputStream outputStream = CommandLineInterface.outfile == null ?
                System.out : new PrintStream(new FileOutputStream(CommandLineInterface.outfile))) {

            compiler.compile(inputStream, outputStream, CommandLineInterface.target);

        } catch (IOException ioe) {
            System.err.printf("IOException encountered while processing file: %s", CommandLineInterface.infile);
            System.exit(1);
        } catch (ParseException e) {
            System.out.println("Parsing failed: " + e.getMessage());
            System.exit(1);
        }
    }

}