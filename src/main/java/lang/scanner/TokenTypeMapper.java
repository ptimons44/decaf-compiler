package lang.scanner;

import lang.types.LexicalToken;
import java.util.Map;

/**
 * Maps scanner states to token types with validation.
 * Implementations define how to convert a state and token string into a LexicalToken type.
 *
 * @param <S> The state type (typically an enum)
 */
public class TokenTypeMapper<S> {
    /**
     * Functional interface for converting a state and token string to a token type.
     */
    @FunctionalInterface
    public interface StateToTokenConverter<S> {
        /**
         * Converts a state and token string to a LexicalToken type.
         *
         * @param state The scanner state that produced the token
         * @param tokenString The string value of the token
         * @return The LexicalToken type
         * @throws IllegalSyntaxException if the token is invalid
         */
        LexicalToken.TokenType convert(S state, String tokenString) throws IllegalSyntaxException;
    }

    /**
     * Exception thrown when a token fails validation.
     */
    public static class IllegalSyntaxException extends Exception {
        public IllegalSyntaxException(String message) {
            super(message);
        }
    }

    /**
     * Map from states to their token type converters
     */
    private final Map<S, StateToTokenConverter<S>> converters;

    /**
     * Creates a new token type mapper.
     *
     * @param converters Map from states to their conversion functions
     */
    public TokenTypeMapper(Map<S, StateToTokenConverter<S>> converters) {
        this.converters = converters;
    }

    /**
     * Maps a state and token string to a LexicalToken type.
     *
     * @param state The scanner state that produced the token
     * @param tokenString The string value of the token
     * @return The LexicalToken type
     * @throws IllegalSyntaxException if the token is invalid
     * @throws IllegalStateException if no converter is defined for the state
     */
    public LexicalToken.TokenType mapToTokenType(S state, String tokenString)
            throws IllegalSyntaxException {
        StateToTokenConverter<S> converter = converters.get(state);
        if (converter == null) {
            throw new IllegalStateException("No converter for state: " + state);
        }
        return converter.convert(state, tokenString);
    }

    /**
     * Checks if a converter is defined for the given state.
     *
     * @param state The state to check
     * @return true if a converter is defined, false otherwise
     */
    public boolean hasConverter(S state) {
        return converters.containsKey(state);
    }
}
