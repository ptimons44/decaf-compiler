package lang.scanner;

import lang.types.LexicalToken;
import java.util.Set;

/**
 * Scanner configuration encapsulating the state machine and token mapping.
 * Provides all language-specific configuration needed for lexical analysis.
 *
 * @param <S> The state type (typically an enum)
 */
public class ScannerConfig<S> {
    /**
     * The state machine defining character transitions
     */
    private final ScannerStateMachine<S> stateMachine;

    /**
     * The token type mapper for converting states to token types
     */
    private final TokenTypeMapper<S> tokenTypeMapper;

    /**
     * The set of valid token types for this language
     */
    private final Set<LexicalToken.TokenType> tokenTypes;

    /**
     * Creates a new scanner configuration.
     *
     * @param stateMachine The state machine for lexical analysis
     * @param tokenTypeMapper The mapper for converting states to token types
     * @param tokenTypes The set of valid token types
     */
    public ScannerConfig(ScannerStateMachine<S> stateMachine,
                        TokenTypeMapper<S> tokenTypeMapper,
                        Set<LexicalToken.TokenType> tokenTypes) {
        this.stateMachine = stateMachine;
        this.tokenTypeMapper = tokenTypeMapper;
        this.tokenTypes = tokenTypes;
    }

    /**
     * Gets the state machine for this scanner configuration.
     *
     * @return The scanner state machine
     */
    public ScannerStateMachine<S> getStateMachine() {
        return stateMachine;
    }

    /**
     * Gets the token type mapper for this scanner configuration.
     *
     * @return The token type mapper
     */
    public TokenTypeMapper<S> getTokenTypeMapper() {
        return tokenTypeMapper;
    }

    /**
     * Gets the set of valid token types for this language.
     *
     * @return The token types
     */
    public Set<LexicalToken.TokenType> getTokenTypes() {
        return tokenTypes;
    }
}
