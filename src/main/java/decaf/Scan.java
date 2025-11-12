package decaf;

import decaf.types.LexicalToken;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;

import lombok.Getter;

public class Scan {
    static class IllegalSyntaxException extends Exception {
        public IllegalSyntaxException(String message) {
            super(message);
        }
    }

    public static enum TokenType {
            CHARLITERAL, STRINGLITERAL, INTLITERAL,
            LONGLITERAL, BOOLEANLITERAL, IDENTIFIER, 
            PUNCTUATION, KEYWORD, IGNORE;
        
            public String toString() {
                if (this == PUNCTUATION || this == KEYWORD) return ""; // punctuation is not annotated
                return this.name() + " ";
            }
    };

    private static enum State {
        START,
        PUNCTUATION,
        SLASH,
        STAR,
        MODULO,
        PLUS,
        MINUS,
        EQUAL,
        LESS_THAN,
        GREATER_THAN,
        BANG,
        DIV_EQ,
        MUL_EQ,
        ADD_EQ,
        SUB_EQ,
        MOD_EQ,
        LEQ,
        GEQ,
        NEQ,
        EQEQ,
        AMPER,
        AMPER_AMPER,
        PIPE,
        PIPE_PIPE,
        INCR,
        DECR,
        ZERO,
        SINGLE_LINE_COMMENT,
        MULTI_LINE_COMMENT,
        MULTI_LINE_COMMENT_SLASH, // used for error check for nested comments
        MULTI_LINE_COMMENT_STAR, // used to check for closing */
        MULTI_LINE_COMMENT_END,
        STRING_LITERAL,
        STRING_LITERAL_IGNORE_NEXT,
        STRING_LITERAL_END,
        CHAR_LITERAL,
        CHAR_LITERAL_IGNORE_NEXT,
        CHAR_LITERAL_END,
        HEX_LITERAL,
        DEC_LITERAL,
        LONG_LITERAL,
        IDENTIFIER,
        WHITESPACE,
        END,
        ERROR;

        public TokenType toTokenType(String token) throws IllegalSyntaxException  {
            switch (this) {
                case CHAR_LITERAL_END:
                    if (Pattern.matches("'(\\\\[btnfr'\"\\\\]|[^\\\\'])'", token)) {
                        return TokenType.CHARLITERAL;
                    } 
                    else {
                        throw new IllegalSyntaxException("Invalid char literal: " + token);
                    }
                case STRING_LITERAL_END:
                    if (Pattern.matches("\"(\\\\[btnfr'\"\\\\]|[^\\\\\"])*\"", token)) {
                        return TokenType.STRINGLITERAL;
                    } 
                    else {
                        throw new IllegalSyntaxException("Invalid string literal: " + token);
                    }
                case LONG_LITERAL:
                    if (
                        Pattern.matches("[0-9]*(?:_+[0-9]+)*[Ll]", token) 
                        || Pattern.matches("0[xX][0-9A-Fa-f]+(?:_+[0-9A-Fa-f]+)*[Ll]", token)
                    ) {
                        return TokenType.LONGLITERAL;
                    } 
                    else {
                        throw new IllegalSyntaxException("Invalid long literal: " + token);
                    }
                case HEX_LITERAL:
                    if (Pattern.matches("0[xX][0-9A-Fa-f]+(?:_+[0-9A-Fa-f]+)*", token)) {
                        return TokenType.INTLITERAL;
                    } 
                    else {
                        throw new IllegalSyntaxException("Invalid hex literal: " + token);
                    }
                case DEC_LITERAL:
                    if (Pattern.matches("[0-9]*(?:_+[0-9]+)*", token) || Pattern.matches("0", token)) {
                        return TokenType.INTLITERAL;
                    } 
                    else {
                        throw new IllegalSyntaxException("Invalid decimal literal: " + token);
                    }
                case ZERO:
                    return TokenType.INTLITERAL;
                case IDENTIFIER:
                    switch (token) {
                        case "true", "false":
                            return TokenType.BOOLEANLITERAL;
                        case "if", "bool", "break", "import", "continue", "else", "for", "while", "int", "long", "return", "len", "void":
                            return TokenType.KEYWORD;
                        default:
                            if (Pattern.matches("[a-zA-Z_][a-zA-Z0-9_]*", token)) {
                                return TokenType.IDENTIFIER;
                            } 
                            else {
                                throw new IllegalSyntaxException("Invalid identifier: " + token);
                            }
                    }
                case SINGLE_LINE_COMMENT, MULTI_LINE_COMMENT_END, WHITESPACE:
                    return TokenType.IGNORE;
                default:
                    return TokenType.PUNCTUATION;
            }
        }
    }

    static class DefaultMap<K extends Comparable<K>, V> extends HashMap<K, V> {
        private final V defaultValue;
        private Map<K, Runnable> actions;

        public DefaultMap(V defaultValue) {
            this.defaultValue = defaultValue;
            this.actions = new HashMap<>();
        }

        @Override
        public V get(Object key) {
            V next = super.getOrDefault(key, defaultValue);
            if (actions.containsKey(next)) actions.get(next).run();
            return next;
        }

        public final void putAction(K transition, Runnable action) {
            /*
             * IMPORTANT: only use to add warning and error messages
             */
            actions.put(transition, action);
        }

        @SafeVarargs
        public final void putAll(V value, K... keys) {
            for (K key : keys) super.put(key, value);
        }

        public void putRange(V value, K start, K end) {
            for (K c = start; c.compareTo(end) <= 0; c = increment(c)) super.put(c, value);
        }

        // Helper method to increment K (only works for Character type in this context)
        private K increment(K c) {
            if (c instanceof Character) {
                return (K) Character.valueOf((char)(((Character) c) + 1));
            }
            throw new UnsupportedOperationException("Increment not supported for type " + c.getClass());
        }
    }

    private void putError(String msg) {
        errors.computeIfAbsent(lineNumber, k -> new ArrayList<>()).add(msg);
        foundError = true;
    };

    private static final Character EOF = null; // used to represent end of file

    private final DefaultMap<State, DefaultMap<Character, State>> transition = new DefaultMap<State, DefaultMap<Character, State>>(new DefaultMap<Character, State>(State.START));
    {
        transition.put(State.START, new DefaultMap<Character, State>(State.ERROR) {{
            putAll(State.SLASH, '/');
            putAll(State.STAR, '*');
            putAll(State.PLUS, '+');
            putAll(State.MINUS, '-');
            putAll(State.EQUAL, '=');
            putAll(State.LESS_THAN, '<');
            putAll(State.GREATER_THAN, '>');
            putAll(State.BANG, '!');
            putAll(State.MODULO, '%');
            putAll(State.ZERO, '0');
            putRange(State.DEC_LITERAL, '1', '9');
            putRange(State.IDENTIFIER, 'a', 'z');
            putRange(State.IDENTIFIER, 'A', 'Z');
            putAll(State.IDENTIFIER, '_');
            putAll(State.CHAR_LITERAL, '\'');
            putAll(State.STRING_LITERAL, '\"');
            putAll(State.WHITESPACE, ' ', '\t', '\r', '\n', '\f');
            putAll(State.PUNCTUATION, '(', ')', '[', ']', '{', '}', ';', ',');
            putAll(State.AMPER, '&');
            putAll(State.PIPE, '|');
            putAll(State.END, EOF);
        }});
        transition.put(State.WHITESPACE, new DefaultMap<Character, State>(State.START) {{
            putAll(State.WHITESPACE, ' ', '\t', '\r', '\n', '\f');
        }});
        transition.put(State.SLASH, new DefaultMap<Character, State>(State.START) {{
            putAll(State.SINGLE_LINE_COMMENT, '/');
            putAll(State.MULTI_LINE_COMMENT, '*');
            putAll(State.DIV_EQ, '=');
        }});
        transition.put(State.STAR, new DefaultMap<Character, State>(State.START) {{
            putAll(State.MUL_EQ, '=');
        }});
        transition.put(State.PLUS, new DefaultMap<Character, State>(State.START) {{
            putAll(State.ADD_EQ, '=');
            putAll(State.INCR, '+');
        }});
        transition.put(State.MINUS, new DefaultMap<Character, State>(State.START) {{
            putAll(State.SUB_EQ, '=');
            putAll(State.DECR, '-');
        }});
        transition.put(State.EQUAL, new DefaultMap<Character, State>(State.START) {{
            putAll(State.EQEQ, '=');
        }});
        transition.put(State.LESS_THAN, new DefaultMap<Character, State>(State.START) {{
            putAll(State.LEQ, '=');
        }});
        transition.put(State.GREATER_THAN, new DefaultMap<Character, State>(State.START) {{
            putAll(State.GEQ, '=');
        }});
        transition.put(State.BANG, new DefaultMap<Character, State>(State.START) {{
            putAll(State.NEQ, '=');
        }});
        transition.put(State.MODULO, new DefaultMap<Character, State>(State.START) {{
            putAll(State.MOD_EQ, '=');
        }});
        transition.put(State.ZERO, new DefaultMap<Character, State>(State.START) {{
            putRange(State.DEC_LITERAL, '0', '9');
            putAll(State.DEC_LITERAL, '_');
            putAll(State.HEX_LITERAL, 'x'); 
            putAll(State.START, 'X'); 
            putAll(State.LONG_LITERAL, 'L');
            putAll(State.START, 'l'); 
        }});
        transition.put(State.DEC_LITERAL, new DefaultMap<Character, State>(State.START) {{
            putRange(State.DEC_LITERAL, '0', '9');
            putAll(State.DEC_LITERAL, '_');
            putAll(State.LONG_LITERAL, 'L');
        }});
        transition.put(State.HEX_LITERAL, new DefaultMap<Character, State>(State.START) {{
            putRange(State.HEX_LITERAL, '0', '9');
            putRange(State.HEX_LITERAL, 'a', 'f');
            putRange(State.HEX_LITERAL, 'A', 'F');
            putAll(State.HEX_LITERAL, '_');
            putAll(State.LONG_LITERAL, 'L'); 
        }});
        transition.put(State.IDENTIFIER, new DefaultMap<Character, State>(State.START) {{
            putRange(State.IDENTIFIER, 'a', 'z');
            putRange(State.IDENTIFIER, 'A', 'Z');
            putRange(State.IDENTIFIER, '0', '9');
            putAll(State.IDENTIFIER, '_'); // overwrites previous write
        }});
        transition.put(State.SINGLE_LINE_COMMENT, new DefaultMap<Character, State>(State.SINGLE_LINE_COMMENT) {{
            putAll(State.START, '\n', EOF);
        }});
        transition.put(State.MULTI_LINE_COMMENT, new DefaultMap<Character, State>(State.MULTI_LINE_COMMENT) {{
            putAll(State.MULTI_LINE_COMMENT_STAR, '*');
            putAll(State.MULTI_LINE_COMMENT_SLASH, '/');
            
            // no open comment error
            putAll(State.ERROR, EOF);
            putAction(EOF, () -> putError("Unclosed multi-line comment"));
        }});
        transition.put(State.MULTI_LINE_COMMENT_SLASH, new DefaultMap<Character, State>(State.MULTI_LINE_COMMENT) {{
            putAll(State.MULTI_LINE_COMMENT_SLASH, '/');

            // nested comment error
            putAll(State.ERROR, '*'); 
            putAction('*', () -> putError("Nested multi-line comment are illegal"));
            
            // no open comment error
            putAll(State.ERROR, EOF); 
            putAction(EOF, () -> putError("Unclosed multi-line comment"));
        }});
        transition.put(State.MULTI_LINE_COMMENT_STAR, new DefaultMap<Character, State>(State.MULTI_LINE_COMMENT) {{
            putAll(State.MULTI_LINE_COMMENT_END, '/');
            putAll(State.MULTI_LINE_COMMENT_STAR, '*');

            // no open comment error
            putAll(State.ERROR, EOF); 
            putAction(EOF, () -> putError("Unclosed multi-line comment"));
        }});
        transition.put(State.STRING_LITERAL, new DefaultMap<Character, State>(State.STRING_LITERAL) {{
            putAll(State.STRING_LITERAL_IGNORE_NEXT, '\\');
            putAll(State.STRING_LITERAL_END, '"');
            
            // unclosed string error
            putAll(State.ERROR, EOF);
            putAction(EOF, () -> putError("Unclosed string literal"));

            // line break in string error
            putAll(State.ERROR, '\n');
            putAction('\n', () -> putError("Newline in string literal"));

            // unescaped char error
            putAll(State.ERROR, '\'');
            putAction('\'', () -> putError("Unescaped single quote in string literal"));
        }});
        transition.put(State.STRING_LITERAL_IGNORE_NEXT, new DefaultMap<Character, State>(State.STRING_LITERAL) {{
            // unclosed string error
            putAll(State.ERROR, EOF);
            putAction(EOF, () -> putError("Unclosed string literal"));
        }});
        transition.put(State.CHAR_LITERAL, new DefaultMap<Character, State>(State.CHAR_LITERAL) {{
            putAll(State.START, '\'');
            putAll(State.CHAR_LITERAL_IGNORE_NEXT, '\\');
            putAll(State.CHAR_LITERAL_END, '\'');
            
            // unclosed char error
            putAll(State.ERROR, EOF);
            putAction(EOF, () -> putError("Unclosed char literal"));

            // line break in char error
            putAll(State.ERROR, '\n');
            putAction('\n', () -> putError("Newline or tab in char literal"));

            // tab in char error
            putAll(State.ERROR, '\t');
            putAction('\t', () -> putError("Tab in char literal"));

            // unescaped char error
            putAll(State.ERROR, '\"');
            putAction('\"', () -> putError("Unescaped double quote in char literal"));
        }});
        transition.put(State.CHAR_LITERAL_IGNORE_NEXT, new DefaultMap<Character, State>(State.CHAR_LITERAL) {{
            // unclosed char error
            putAll(State.ERROR, EOF);
            putAction(EOF, () -> putError("Unclosed char literal"));

            // line break in char error
            putAll(State.ERROR, '\n');
            putAction('\n', () -> putError("Newline in char literal"));
        }});
        transition.put(State.AMPER, new DefaultMap<Character, State>(State.ERROR) {{
            putAll(State.AMPER_AMPER, '&');
        }});
        transition.put(State.AMPER_AMPER, new DefaultMap<Character, State>(State.START));
        transition.put(State.PIPE, new DefaultMap<Character, State>(State.ERROR) {{
            putAll(State.PIPE_PIPE, '|');
        }});
        transition.put(State.END, new DefaultMap<Character, State>(State.END));
        transition.put(State.ERROR, new DefaultMap<Character, State>(State.ERROR));
    }

    private final String in;
    @Getter
    private List<LexicalToken> tokens = new ArrayList<>(); // maps line number to token and maintains order for pretty printing
    private Map<Integer, List<String>> errors = new HashMap<>(); // maps line number to error message
    private Map<Integer, List<String>> warnings = new HashMap<>(); // maps line number to warning message

    private int start = 0; // inclusive
    private int end = 0; // non-inclusive
    private State currentState = State.START;
    private Integer lineNumber = 1;
    private Integer columnNumber = 1;

    private boolean foundError = false;
    private boolean canGobble() {
        return !foundError && end < in.length();
    }

    private Character peek() {
        if (end == in.length()) {
            return EOF;
        } else {
            return in.charAt(end);
        }
    }

    private void gobble() {
        assert canGobble();
        
        
        Character c = peek();
        if (currentState == State.START) {
            currentState = transition.get(currentState).get(c);
            start = end;
            return; // Always return to START state between terminal state transitions
        }
        else {
            end++;
            columnNumber += 1;
            c = peek();
        }
        State nextState = transition.get(currentState).get(c);
        if (nextState == State.START) {
            String token = in.substring(start, end);
            TokenType tokenType;
            try {
                tokenType = currentState.toTokenType(token);
                System.out.println("Type: " + tokenType + " Token: " + token);
                // Only create LexicalToken if tokenType is NOT IGNORE
                if (tokenType != TokenType.IGNORE) {
                    // Convert Scan.TokenType to LexicalToken.TokenType
                    LexicalToken.TokenType lexicalTokenType = convertToLexicalTokenType(tokenType);
                    tokens.add(new LexicalToken(lexicalTokenType, token, lineNumber, columnNumber));
                }
            } catch (IllegalSyntaxException e) {
                String errorMsg = e.getMessage();
                System.out.println("Error: " + errorMsg);
                putError(errorMsg);
            }
        } 
        else if (nextState == State.ERROR) {
            String token = in.substring(start, end);
            String errorMsg = "Unexpected character '" + c + "' after '" + token + "'";
            System.out.println("Error: " + errorMsg);
            putError(errorMsg);
        }
        
        currentState = nextState;
        if (c != null && c == '\n') {
            lineNumber++;
            columnNumber = 1;
        }
    }

    private LexicalToken.TokenType convertToLexicalTokenType(TokenType scanTokenType) {
        switch (scanTokenType) {
            case CHARLITERAL:
                return LexicalToken.TokenType.CHARLITERAL;
            case STRINGLITERAL:
                return LexicalToken.TokenType.STRINGLITERAL;
            case INTLITERAL:
                return LexicalToken.TokenType.INTLITERAL;
            case LONGLITERAL:
                return LexicalToken.TokenType.LONGLITERAL;
            case BOOLEANLITERAL:
                return LexicalToken.TokenType.BOOLEANLITERAL;
            case IDENTIFIER:
                return LexicalToken.TokenType.IDENTIFIER;
            case PUNCTUATION:
                return LexicalToken.TokenType.PUNCTUATION;
            case KEYWORD:
                return LexicalToken.TokenType.KEYWORD;
            default:
                throw new IllegalArgumentException("Cannot convert IGNORE token type to LexicalToken.TokenType");
        }
    }

    public Scan(String in) {
        this.in = in;
    }

    public void scan() {
        while (this.canGobble()) {
            this.gobble();
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (LexicalToken token : tokens) {
            sb.append(Integer.valueOf(token.getLineNumber()).toString()).append(" ");
            if (token.getTokenType() != LexicalToken.TokenType.KEYWORD && token.getTokenType() != LexicalToken.TokenType.PUNCTUATION) {
                sb.append(token.getTokenType().toString()).append(" ");
            }
            sb.append(token.getVal()).append("\n");
        }
        return sb.toString().strip();
    }

    public String getErrors() {
        StringBuilder sb = new StringBuilder();
        for (int line : errors.keySet()) {
            for (String error : errors.get(line)) {
                sb.append("Error on line ").append(Integer.valueOf(line).toString()).append(": ").append(error).append("\n");
            }
        }
        return sb.toString().strip();
    }

    public String getWarnings() {
        StringBuilder sb = new StringBuilder();
        for (int line : warnings.keySet()) {
            for (String warning : warnings.get(line)) {
                sb.append("Warning on line ").append(Integer.valueOf(line).toString()).append(": ").append(warning).append("\n");
            }
        }
        return sb.toString().strip();
    }

    public void write(OutputStream outputStream) throws IOException {
        String output = this.toString();
        outputStream.write(output.getBytes(StandardCharsets.UTF_8));
    }
}
