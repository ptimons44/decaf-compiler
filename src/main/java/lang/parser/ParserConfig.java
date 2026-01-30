package lang.parser;

import lang.types.CFGGraph;

/**
 * Parser configuration encapsulating the grammar and operator precedence.
 * Provides all language-specific configuration needed for syntax analysis.
 */
public class ParserConfig {
    /**
     * The context-free grammar for LL(1) parsing
     */
    private final CFGGraph grammar;

    /**
     * The operator precedence hierarchy for Pratt parsing of expressions
     */
    private final OperatorPrecedenceHierarchy precedenceHierarchy;

    /**
     * Creates a new parser configuration.
     *
     * @param grammar The context-free grammar
     * @param precedenceHierarchy The operator precedence hierarchy
     */
    public ParserConfig(CFGGraph grammar, OperatorPrecedenceHierarchy precedenceHierarchy) {
        this.grammar = grammar;
        this.precedenceHierarchy = precedenceHierarchy;
    }

    /**
     * Gets the context-free grammar for this parser configuration.
     *
     * @return The CFG graph
     */
    public CFGGraph getGrammar() {
        return grammar;
    }

    /**
     * Gets the operator precedence hierarchy for this parser configuration.
     *
     * @return The operator precedence hierarchy
     */
    public OperatorPrecedenceHierarchy getPrecedenceHierarchy() {
        return precedenceHierarchy;
    }
}
