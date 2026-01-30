package lang.grammars.decaf;

import lang.*;
import lang.parser.OperatorPrecedenceHierarchy;
import lang.parser.ParserConfig;
import lang.scanner.ScannerConfig;
import lang.scanner.ScannerStateMachine;
import lang.scanner.TokenTypeMapper;
import lang.types.ASTBase;
import lang.types.CFGGraph;
import lang.types.LexicalToken;

import java.util.List;
import java.util.Set;

/**
 * Decaf language implementation.
 * Provides configuration and factory methods for Decaf-specific compiler components.
 */
public class DecafLanguage implements Language {
    private ScannerConfig<DecafState> cachedScannerConfig;
    private ParserConfig cachedParserConfig;

    @Override
    public ScannerConfig<DecafState> getScannerConfig() {
        if (cachedScannerConfig == null) {
            cachedScannerConfig = buildDecafScannerConfig();
        }
        return cachedScannerConfig;
    }

    @Override
    public ParserConfig getParserConfig() {
        if (cachedParserConfig == null) {
            cachedParserConfig = buildDecafParserConfig();
        }
        return cachedParserConfig;
    }

    @Override
    public String getName() {
        return "Decaf";
    }

    /**
     * Creates a Decaf lexer for the given input.
     *
     * @param input The source code to scan
     * @return A Lexer instance
     */
    public Lexer createLexer(String input) {
        return new Scan(input);
    }

    /**
     * Creates a Decaf parser for the given tokens.
     *
     * @param tokens The lexical tokens to parse
     * @return A Parser instance
     */
    public Parser createParser(List<LexicalToken> tokens) {
        return new Parse(tokens, new DecafCFGGraph());
    }

    /**
     * Builds Decaf-specific scanner configuration.
     */
    private ScannerConfig<DecafState> buildDecafScannerConfig() {
        // Build state machine with error handler
        // Note: For now, error handler is a placeholder since Scan handles errors internally
        ScannerStateMachine<DecafState> stateMachine =
                DecafScannerStateMachine.build(errorMsg -> {
                    // Errors are handled by Scan class internally
                });

        // Build token type mapper
        TokenTypeMapper<DecafState> tokenTypeMapper = DecafTokenTypeMapper.build();

        // Define valid token types for Decaf
        Set<LexicalToken.TokenType> tokenTypes = Set.of(
                LexicalToken.TokenType.CHARLITERAL,
                LexicalToken.TokenType.STRINGLITERAL,
                LexicalToken.TokenType.INTLITERAL,
                LexicalToken.TokenType.LONGLITERAL,
                LexicalToken.TokenType.BOOLEANLITERAL,
                LexicalToken.TokenType.IDENTIFIER,
                LexicalToken.TokenType.PUNCTUATION,
                LexicalToken.TokenType.KEYWORD
        );

        return new ScannerConfig<>(stateMachine, tokenTypeMapper, tokenTypes);
    }

    /**
     * Builds Decaf-specific parser configuration.
     */
    private ParserConfig buildDecafParserConfig() {
        CFGGraph grammar = new DecafCFGGraph();
        OperatorPrecedenceHierarchy precedence = buildDecafPrecedence();
        return new ParserConfig(grammar, precedence);
    }

    /**
     * Builds Decaf operator precedence hierarchy.
     */
    private OperatorPrecedenceHierarchy buildDecafPrecedence() {
        OperatorPrecedenceHierarchy.Builder builder = new OperatorPrecedenceHierarchy.Builder();

        // Infix operators (lowest to highest precedence)
        builder.addInfix("||", 1, 2);
        builder.addInfix("&&", 2, 3);
        builder.addInfix("==", 3, 4);
        builder.addInfix("!=", 3, 4);
        builder.addInfix("<", 4, 5);
        builder.addInfix("<=", 4, 5);
        builder.addInfix(">", 4, 5);
        builder.addInfix(">=", 4, 5);
        builder.addInfix("+", 5, 6);
        builder.addInfix("-", 5, 6);
        builder.addInfix("*", 6, 7);
        builder.addInfix("/", 6, 7);
        builder.addInfix("%", 6, 7);

        // Prefix operators (highest precedence)
        builder.addPrefix("-", 8, 8);
        builder.addPrefix("!", 8, 8);
        builder.addPrefix("++", 8, 8);
        builder.addPrefix("--", 8, 8);

        // Postfix operators
        builder.addPostfix("++", 8, 7);
        builder.addPostfix("--", 8, 7);
        builder.addPostfix("(", 8, 7);  // Function call
        builder.addPostfix("[", 8, 7);  // Array indexing

        return builder.build();
    }
}
