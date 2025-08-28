package decaf;

import decaf.utils.CommandLineInterface;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class DecafCompiler {

    public static void main(String[] args) {
        CommandLineInterface.parse(args, new String[0]);
        try (InputStream inputStream = CommandLineInterface.infile == null ? System.in : Files.newInputStream(Path.of(CommandLineInterface.infile));
             OutputStream outputStream = CommandLineInterface.outfile == null ? System.out : new PrintStream(new FileOutputStream(CommandLineInterface.outfile))) {
            switch (CommandLineInterface.target) {
                case SCAN -> {
                }
                case PARSE -> {
                }
                case INTER -> {
                }
                case ASSEMBLY -> {
                }
            }
        } catch (IOException ioe) {
            System.err.printf("IOException encountered while processing file: %s", CommandLineInterface.infile);
            System.exit(1);
        }
    }

}