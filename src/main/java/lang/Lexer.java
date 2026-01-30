package lang;

import lang.types.LexicalToken;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * Interface for lexical analysis (scanning) phase of compilation.
 * Implementations tokenize source code into a stream of lexical tokens.
 */
public interface Lexer {
    /**
     * Performs lexical analysis on the input source code.
     * After calling this method, tokens should be available via getTokens().
     */
    void scan();

    /**
     * Returns the list of tokens produced by scanning.
     * @return List of lexical tokens
     */
    List<LexicalToken> getTokens();

    /**
     * Writes the token stream to the given output stream.
     * Used for SCAN stage output.
     * @param outputStream Stream to write tokens to
     * @throws IOException if writing fails
     */
    void write(OutputStream outputStream) throws IOException;

    /**
     * Gets any errors encountered during scanning.
     * @return Error messages as a string
     */
    String getErrors();

    /**
     * Gets any warnings encountered during scanning.
     * @return Warning messages as a string
     */
    String getWarnings();
}
