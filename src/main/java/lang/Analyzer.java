package lang;

import lang.types.ASTBase;

/**
 * Interface for semantic analysis phase of compilation.
 * Implementations perform type checking, scope resolution, and other semantic validation.
 */
public interface Analyzer {
    /**
     * Performs semantic analysis on the abstract syntax tree.
     * @param ast The AST to analyze
     * @throws SemanticException if semantic errors are found
     */
    void analyze(ASTBase ast) throws SemanticException;
}
