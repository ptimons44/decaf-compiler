package decaf;

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

public class Scan {
    static class IllegalSyntaxException extends Exception {
        public IllegalSyntaxException(String message) {
            super(message);
        }
    }
    
    // excluding keywords that are literals (false, true)
    public static final Set<String> keywords = new HashSet<>(Arrays.asList(
            "if",
            "bool",
            "break",
            "import",
            "continue",
            "else",
            "for",
            "while",
            "int",
            "long",
            "return",
            "len",
            "void"
    ));

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
                        Pattern.matches("[1-9][0-9]*(?:_+[0-9]+)*[Ll]", token) 
                        || Pattern.matches("0[xX][0-9A-Fa-f]+(?:_+[0-9A-Fa-f]+)*[Ll]", token)
                        || Pattern.matches("0[Ll]", token)
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

    static class DefaultMap extends HashMap<Character, State> {
        private final State defaultValue;

        public DefaultMap(State defaultValue) {
            this.defaultValue = defaultValue;
        }

        @Override
        public State get(Object key) {
            return super.getOrDefault(key, defaultValue);
        }

        @SafeVarargs
        public final void putAll(State value, Character... keys) {
            for (Character key : keys) super.put(key, value);
        }

        public void putRange(State value, Character start, Character end) {
            for (char c = start; c <= end; c++) super.put(c, value);
        }
    }

    private static final Character EOF = null; // used to represent end of file

    private static final Map<State, DefaultMap> transition = new HashMap<>();
    static {
        transition.put(State.START, new DefaultMap(State.ERROR) {{
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
        transition.put(State.PUNCTUATION, new DefaultMap(State.START));
        transition.put(State.WHITESPACE, new DefaultMap(State.START) {{
            putAll(State.WHITESPACE, ' ', '\t', '\r', '\n', '\f');
        }});
        transition.put(State.SLASH, new DefaultMap(State.START) {{
            putAll(State.SINGLE_LINE_COMMENT, '/');
            putAll(State.MULTI_LINE_COMMENT, '*');
            putAll(State.DIV_EQ, '=');
        }});
        transition.put(State.STAR, new DefaultMap(State.START) {{
            putAll(State.MUL_EQ, '=');
        }});
        transition.put(State.PLUS, new DefaultMap(State.START) {{
            putAll(State.ADD_EQ, '=');
            putAll(State.INCR, '+');
        }});
        transition.put(State.MINUS, new DefaultMap(State.START) {{
            putAll(State.SUB_EQ, '=');
            putAll(State.DECR, '-');
        }});
        transition.put(State.EQUAL, new DefaultMap(State.START) {{
            putAll(State.EQEQ, '=');
        }});
        transition.put(State.LESS_THAN, new DefaultMap(State.START) {{
            putAll(State.LEQ, '=');
        }});
        transition.put(State.GREATER_THAN, new DefaultMap(State.START) {{
            putAll(State.GEQ, '=');
        }});
        transition.put(State.BANG, new DefaultMap(State.START) {{
            putAll(State.NEQ, '=');
        }});
        transition.put(State.MODULO, new DefaultMap(State.START) {{
            putAll(State.MOD_EQ, '=');
        }});
        transition.put(State.ZERO, new DefaultMap(State.START) {{
            putRange(State.DEC_LITERAL, '0', '9');
            putAll(State.DEC_LITERAL, '_');
            putAll(State.HEX_LITERAL, 'x'); 
            putAll(State.START, 'X'); 
            putAll(State.LONG_LITERAL, 'L');
            putAll(State.START, 'l'); 
        }});
        transition.put(State.DEC_LITERAL, new DefaultMap(State.START) {{
            putRange(State.DEC_LITERAL, '0', '9');
            putAll(State.DEC_LITERAL, '_');
            putAll(State.LONG_LITERAL, 'L');
        }});
        transition.put(State.HEX_LITERAL, new DefaultMap(State.START) {{
            putRange(State.HEX_LITERAL, '0', '9');
            putRange(State.HEX_LITERAL, 'a', 'f');
            putRange(State.HEX_LITERAL, 'A', 'F');
            putAll(State.HEX_LITERAL, '_');
            putAll(State.LONG_LITERAL, 'L'); 
        }});
        transition.put(State.LONG_LITERAL, new DefaultMap(State.START));
        transition.put(State.IDENTIFIER, new DefaultMap(State.START) {{
            putRange(State.IDENTIFIER, 'a', 'z');
            putRange(State.IDENTIFIER, 'A', 'Z');
            putRange(State.IDENTIFIER, '0', '9');
            putAll(State.IDENTIFIER, '_'); // overwrites previous write
        }});
        transition.put(State.SINGLE_LINE_COMMENT, new DefaultMap(State.SINGLE_LINE_COMMENT) {{
            putAll(State.START, '\n', EOF);
        }});
        transition.put(State.MULTI_LINE_COMMENT, new DefaultMap(State.MULTI_LINE_COMMENT) {{
            putAll(State.MULTI_LINE_COMMENT_STAR, '*');
            putAll(State.MULTI_LINE_COMMENT_SLASH, '/');
            putAll(State.ERROR, EOF); // no open comment error
        }});
        transition.put(State.MULTI_LINE_COMMENT_SLASH, new DefaultMap(State.MULTI_LINE_COMMENT) {{
            putAll(State.ERROR, '*'); // nested comment error
            putAll(State.MULTI_LINE_COMMENT_SLASH, '/');
            putAll(State.ERROR, EOF); // no open comment error
        }});
        transition.put(State.MULTI_LINE_COMMENT_STAR, new DefaultMap(State.MULTI_LINE_COMMENT) {{
            putAll(State.MULTI_LINE_COMMENT_END, '/');
            putAll(State.MULTI_LINE_COMMENT_STAR, '*');
            putAll(State.ERROR, EOF); // no open comment error
        }});
        transition.put(State.MULTI_LINE_COMMENT_END, new DefaultMap(State.START));
        transition.put(State.STRING_LITERAL, new DefaultMap(State.STRING_LITERAL) {{
            putAll(State.STRING_LITERAL_IGNORE_NEXT, '\\');
            putAll(State.STRING_LITERAL_END, '"');
            putAll(State.ERROR, EOF);
        }});
        transition.put(State.STRING_LITERAL_IGNORE_NEXT, new DefaultMap(State.STRING_LITERAL) {{
            putAll(State.ERROR, EOF); // no open string error
        }});
        transition.put(State.STRING_LITERAL_END, new DefaultMap(State.START));
        transition.put(State.CHAR_LITERAL, new DefaultMap(State.CHAR_LITERAL) {{
            putAll(State.START, '\'');
            putAll(State.CHAR_LITERAL_IGNORE_NEXT, '\\');
            putAll(State.CHAR_LITERAL_END, '\'');
            putAll(State.ERROR, EOF);
        }});
        transition.put(State.CHAR_LITERAL_IGNORE_NEXT, new DefaultMap(State.CHAR_LITERAL) {{
            putAll(State.ERROR, EOF); // no close char error
        }});
        transition.put(State.AMPER, new DefaultMap(State.ERROR) {{
            putAll(State.AMPER_AMPER, '&');
        }});
        transition.put(State.AMPER_AMPER, new DefaultMap(State.START));
        transition.put(State.PIPE, new DefaultMap(State.ERROR) {{
            putAll(State.PIPE_PIPE, '|');
        }});
        transition.put(State.PIPE_PIPE, new DefaultMap(State.START));
        transition.put(State.CHAR_LITERAL_END, new DefaultMap(State.START));
        transition.put(State.DIV_EQ, new DefaultMap(State.START));
        transition.put(State.MUL_EQ, new DefaultMap(State.START));
        transition.put(State.ADD_EQ, new DefaultMap(State.START));
        transition.put(State.SUB_EQ, new DefaultMap(State.START));
        transition.put(State.MOD_EQ, new DefaultMap(State.START));
        transition.put(State.LEQ, new DefaultMap(State.START));
        transition.put(State.GEQ, new DefaultMap(State.START));
        transition.put(State.NEQ, new DefaultMap(State.START));
        transition.put(State.EQEQ, new DefaultMap(State.START));
        transition.put(State.INCR, new DefaultMap(State.START));
        transition.put(State.DECR, new DefaultMap(State.START));
        transition.put(State.END, new DefaultMap(State.END));
        transition.put(State.ERROR, new DefaultMap(State.ERROR));
    }


    private class StateString {
        String str;
        TokenType tokenType;

        public StateString(String str, TokenType tokenType) {
            this.str = str;
            this.tokenType = tokenType;
        }

        @Override
        public String toString() {
            return tokenType.toString() + str;
        }
    }

    private final String in;
    private Map<Integer, List<StateString>> tokens = new TreeMap<>(); // maps line number to token and maintains order for pretty printing
    private Map<Integer, List<String>> errors = new HashMap<>(); // maps line number to error message
    private Map<Integer, List<String>> warnings = new HashMap<>(); // maps line number to warning message

    private int start = 0; // inclusive
    private int end = 0; // non-inclusive
    private State currentState = State.START;
    private int lineNumber = 1;

    static final int MAX_ITERS = 1000;
    private int curIters = 0;
    private boolean canGobble() {
        return end < in.length() && curIters < MAX_ITERS;
    }

    private Character peek() {
        if (end == in.length()) {
            return EOF;
        } else {
            return in.charAt(end);
        }
    }

    private void gobble() {
        curIters++;
        assert canGobble();
        
        
        Character c = peek();
        System.out.println(currentState + " " + c);
        if (currentState == State.START) {
            currentState = transition.get(currentState).get(c);
            start = end;
            return; // Always return to START state between terminal state transitions
        }
        else {
            end++;
            c = peek();
        }
        System.out.println("State: " + currentState);
        System.out.println("Next char: " + c);
        State nextState = transition.get(currentState).get(c);
        System.out.println("Next state: " + nextState);
        if (nextState == State.START) {
            String token = in.substring(start, end);
            System.out.println("Token: " + token);
            TokenType tokenType;
            try {
                tokenType = currentState.toTokenType(token);
                System.out.println("Type: " + tokenType);
                tokens.computeIfAbsent(lineNumber, k -> new ArrayList<>()).add(new StateString(token, tokenType));
            } catch (IllegalSyntaxException e) {
                String errorMsg = e.getMessage();
                System.out.println("Error: " + errorMsg);
                errors.computeIfAbsent(lineNumber, k -> new ArrayList<>()).add(errorMsg);
            }
        } 
        else if (nextState == State.ERROR) {
            String token = in.substring(start, end);
            String errorMsg = "Unexpected character '" + c + "' after '" + token + "'";
            System.out.println("Error: " + errorMsg);
            errors.computeIfAbsent(lineNumber, k -> new ArrayList<>()).add(errorMsg);
        }
        
        currentState = nextState;
        if (c != null && c == '\n') lineNumber++;
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
        for (int line : tokens.keySet()) {
            for (StateString token : tokens.get(line)) {
                if (token.tokenType != TokenType.IGNORE) {
                    sb.append(Integer.valueOf(line).toString()).append(" ").append(token.toString()).append("\n");
                }
            }
        }
        return sb.toString().strip();
    }

    public void write(OutputStream outputStream) throws IOException {
        String output = this.toString();
        outputStream.write(output.getBytes(StandardCharsets.UTF_8));
    }
}
