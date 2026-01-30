package lang;

import lang.scanner.ScannerConfig;
import lang.parser.ParserConfig;
import lang.types.ASTBase;

/**
 * Language abstraction that provides language-specific configuration for compilation.
 * Implementations define the scanner state machine, parser grammar, and other
 * language-specific components.
 */
public interface Language {
    /**
     * Returns the scanner configuration (state machine + token set + token type mapper)
     * @return Scanner configuration for this language
     */
    ScannerConfig getScannerConfig();

    /**
     * Returns the parser configuration (grammar + precedence hierarchy)
     * @return Parser configuration for this language
     */
    ParserConfig getParserConfig();

    /**
     * Creates a static analyzer for semantic analysis (future use)
     * @param ast The abstract syntax tree from parsing
     * @return An Analyzer instance
     */
    default Analyzer createAnalyzer(ASTBase ast) {
        throw new UnsupportedOperationException("Static analysis not yet implemented");
    }

    /**
     * Gets the language name for display purposes
     * @return The language name (e.g., "Decaf")
     */
    String getName();
}
