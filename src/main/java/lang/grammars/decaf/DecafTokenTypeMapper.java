package lang.grammars.decaf;

import lang.scanner.TokenTypeMapper;
import lang.types.LexicalToken;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Factory for creating the Decaf token type mapper.
 * Defines how to convert Decaf scanner states to token types.
 */
public class DecafTokenTypeMapper {
    /**
     * Set of Decaf keywords
     */
    private static final Set<String> KEYWORDS = Set.of(
            "if", "bool", "break", "import", "continue", "else", "for", "while",
            "int", "long", "return", "len", "void"
    );

    /**
     * Builds the Decaf token type mapper.
     *
     * @return The configured token type mapper
     */
    public static TokenTypeMapper<DecafState> build() {
        Map<DecafState, TokenTypeMapper.StateToTokenConverter<DecafState>> converters = new HashMap<>();

        // CHAR_LITERAL_END state
        converters.put(DecafState.CHAR_LITERAL_END, (state, token) -> {
            if (Pattern.matches("'(\\\\[btnfr'\"\\\\]|[^\\\\'])'", token)) {
                return LexicalToken.TokenType.CHARLITERAL;
            } else {
                throw new TokenTypeMapper.IllegalSyntaxException("Invalid char literal: " + token);
            }
        });

        // STRING_LITERAL_END state
        converters.put(DecafState.STRING_LITERAL_END, (state, token) -> {
            if (Pattern.matches("\"(\\\\[btnfr'\"\\\\]|[^\\\\\"])*\"", token)) {
                return LexicalToken.TokenType.STRINGLITERAL;
            } else {
                throw new TokenTypeMapper.IllegalSyntaxException("Invalid string literal: " + token);
            }
        });

        // LONG_LITERAL state
        converters.put(DecafState.LONG_LITERAL, (state, token) -> {
            if (Pattern.matches("[0-9]*(?:_+[0-9]+)*[Ll]", token)
                    || Pattern.matches("0[xX][0-9A-Fa-f]+(?:_+[0-9A-Fa-f]+)*[Ll]", token)) {
                return LexicalToken.TokenType.LONGLITERAL;
            } else {
                throw new TokenTypeMapper.IllegalSyntaxException("Invalid long literal: " + token);
            }
        });

        // HEX_LITERAL state
        converters.put(DecafState.HEX_LITERAL, (state, token) -> {
            if (Pattern.matches("0[xX][0-9A-Fa-f]+(?:_+[0-9A-Fa-f]+)*", token)) {
                return LexicalToken.TokenType.INTLITERAL;
            } else {
                throw new TokenTypeMapper.IllegalSyntaxException("Invalid hex literal: " + token);
            }
        });

        // DEC_LITERAL state
        converters.put(DecafState.DEC_LITERAL, (state, token) -> {
            if (Pattern.matches("[0-9]*(?:_+[0-9]+)*", token) || Pattern.matches("0", token)) {
                return LexicalToken.TokenType.INTLITERAL;
            } else {
                throw new TokenTypeMapper.IllegalSyntaxException("Invalid decimal literal: " + token);
            }
        });

        // ZERO state
        converters.put(DecafState.ZERO, (state, token) -> LexicalToken.TokenType.INTLITERAL);

        // IDENTIFIER state
        converters.put(DecafState.IDENTIFIER, (state, token) -> {
            switch (token) {
                case "true":
                case "false":
                    return LexicalToken.TokenType.BOOLEANLITERAL;
                default:
                    if (KEYWORDS.contains(token)) {
                        return LexicalToken.TokenType.KEYWORD;
                    } else if (Pattern.matches("[a-zA-Z_][a-zA-Z0-9_]*", token)) {
                        return LexicalToken.TokenType.IDENTIFIER;
                    } else {
                        throw new TokenTypeMapper.IllegalSyntaxException("Invalid identifier: " + token);
                    }
            }
        });

        // SINGLE_LINE_COMMENT, MULTI_LINE_COMMENT_END, WHITESPACE - these states don't produce tokens
        // The scanner should not call the mapper for these states
        // We'll add a converter that throws an exception to catch bugs
        TokenTypeMapper.StateToTokenConverter<DecafState> ignoreConverter =
            (state, token) -> {
                throw new IllegalStateException("Mapper should not be called for ignored state: " + state);
            };
        converters.put(DecafState.SINGLE_LINE_COMMENT, ignoreConverter);
        converters.put(DecafState.MULTI_LINE_COMMENT_END, ignoreConverter);
        converters.put(DecafState.WHITESPACE, ignoreConverter);

        // All other states default to PUNCTUATION
        TokenTypeMapper.StateToTokenConverter<DecafState> punctuationConverter =
            (state, token) -> LexicalToken.TokenType.PUNCTUATION;
        for (DecafState state : DecafState.values()) {
            if (!converters.containsKey(state)) {
                converters.put(state, punctuationConverter);
            }
        }

        return new TokenTypeMapper<>(converters);
    }
}
