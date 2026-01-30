package lang;

import lang.grammars.decaf.DecafLanguage;
import lang.types.ASTBase;
import lang.utils.CommandLineInterface.CompilerAction;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Generic compiler that uses a Language abstraction for language-specific components.
 * Orchestrates the compilation pipeline from source code to target output.
 */
public class Compiler {
    private final Language language;

    /**
     * Creates a new compiler for the given language.
     *
     * @param language The language implementation to use
     */
    public Compiler(Language language) {
        this.language = language;
    }

    /**
     * Compiles the input source code to the specified target stage.
     *
     * @param input The input stream containing source code
     * @param output The output stream for compilation results
     * @param target The target compilation stage (SCAN, PARSE, INTER, ASSEMBLY)
     * @throws IOException if I/O errors occur
     * @throws ParseException if parsing fails
     */
    public void compile(InputStream input, OutputStream output, CompilerAction target)
            throws IOException, ParseException {
        // Read source code
        String sourceCode = new String(input.readAllBytes(), StandardCharsets.UTF_8);

        // Scan stage - lexical analysis
        Lexer lexer;
        if (language instanceof DecafLanguage) {
            // Use DecafLanguage factory method for now
            lexer = ((DecafLanguage) language).createLexer(sourceCode);
        } else {
            throw new UnsupportedOperationException("Language not yet supported: " + language.getName());
        }

        lexer.scan();

        if (target == CompilerAction.SCAN) {
            lexer.write(output);
            return;
        }

        // Parse stage - syntax analysis
        Parser parser;
        if (language instanceof DecafLanguage) {
            // Use DecafLanguage factory method for now
            parser = ((DecafLanguage) language).createParser(lexer.getTokens());
        } else {
            throw new UnsupportedOperationException("Language not yet supported: " + language.getName());
        }

        ASTBase ast = parser.parseProgram();

        if (target == CompilerAction.PARSE) {
            return;
        }

        // Future stages: Intermediate representation and assembly generation
        if (target == CompilerAction.INTER) {
            // Analyzer analyzer = language.createAnalyzer(ast);
            // analyzer.analyze(ast);
            return;
        }

        if (target == CompilerAction.ASSEMBLY) {
            // Code generation would go here
            return;
        }
    }
}
