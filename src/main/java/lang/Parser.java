package lang;

import lang.types.ASTBase;

/**
 * Interface for syntax analysis (parsing) phase of compilation.
 * Implementations parse a token stream into an abstract syntax tree.
 */
public interface Parser {
    /**
     * Parses the token stream into an abstract syntax tree.
     * @return The root node of the AST
     * @throws ParseException if parsing fails
     */
    ASTBase parseProgram() throws ParseException;
}
