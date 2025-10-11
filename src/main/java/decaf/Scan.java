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
            PUNCTUATION, KEYWORD;
        
            public String toString() {
                if (this == PUNCTUATION || this == KEYWORD) return " "; // punctuation is not annotated
                return " " + this.name() + " ";
            }
    };

    private static enum State {
        START,
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
        INCR,
        DECR,
        ZERO,
        SINGLE_LINE_COMMENT,
        MULTI_LINE_COMMENT,
        MULTI_LINE_COMMENT_SLASH, // used for error check for nested comments
        MULTI_LINE_COMMENT_STAR, // used to check for closing */
        STRING_LITERAL,
        STRING_LITERAL_IGNORE_NEXT,
        CHAR_LITERAL,
        HEX_LITERAL,
        DEC_LITERAL,
        IDENTIFIER,
        END,
        ERROR
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
            putAll(State.STRING_LITERAL, '"');
            putAll(State.START, ' ', '\t', '\r', '\n');
            putAll(State.START, '(', ')', '[', ']', ';', ',');
            putAll(State.END, EOF);
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
            putAll(State.HEX_LITERAL, 'x', 'X');
            putRange(State.DEC_LITERAL, '0', '9');
            putAll(State.DEC_LITERAL, '_');
            putRange(State.ERROR, 'a', 'z');
            putRange(State.ERROR, 'A', 'Z');
        }});
        transition.put(State.DEC_LITERAL, new DefaultMap(State.START) {{
            putRange(State.DEC_LITERAL, '0', '9');
            putAll(State.DEC_LITERAL, '_');
            putRange(State.ERROR, 'a', 'z');
            putRange(State.ERROR, 'A', 'Z');
        }});
        transition.put(State.HEX_LITERAL, new DefaultMap(State.START) {{
            putRange(State.HEX_LITERAL, '0', '9');
            putRange(State.HEX_LITERAL, 'a', 'f');
            putRange(State.HEX_LITERAL, 'A', 'F');
            putAll(State.HEX_LITERAL, '_');
            putRange(State.ERROR, 'g', 'z');
            putRange(State.ERROR, 'G', 'Z');
        }});
        transition.put(State.IDENTIFIER, new DefaultMap(State.START) {{
            putRange(State.IDENTIFIER, 'a', 'z');
            putRange(State.IDENTIFIER, 'A', 'Z');
            putRange(State.IDENTIFIER, '0', '9');
            putAll(State.IDENTIFIER, '_');
        }});
        transition.put(State.SINGLE_LINE_COMMENT, new DefaultMap(State.SINGLE_LINE_COMMENT) {{
            putAll(State.START, '\n', EOF);
        }});
        transition.put(State.MULTI_LINE_COMMENT, new DefaultMap(State.MULTI_LINE_COMMENT) {{
            putAll(State.MULTI_LINE_COMMENT_STAR, '*');
            putAll(State.MULTI_LINE_COMMENT_SLASH, '/');
        }});
        transition.put(State.MULTI_LINE_COMMENT_SLASH, new DefaultMap(State.MULTI_LINE_COMMENT) {{
            putAll(State.ERROR, '*'); // nested comment error
            putAll(State.MULTI_LINE_COMMENT_SLASH, '/');
        }});
        transition.put(State.MULTI_LINE_COMMENT_STAR, new DefaultMap(State.MULTI_LINE_COMMENT) {{
            putAll(State.START, '/');
            putAll(State.MULTI_LINE_COMMENT_STAR, '*');
        }});
        transition.put(State.STRING_LITERAL, new DefaultMap(State.STRING_LITERAL) {{
            putAll(State.STRING_LITERAL_IGNORE_NEXT, '\\');
            putAll(State.START, '"');
            putAll(State.ERROR, EOF);
        }});
        transition.put(State.STRING_LITERAL_IGNORE_NEXT, new DefaultMap(State.STRING_LITERAL) {{
            putAll(State.ERROR, EOF);
        }});
        transition.put(State.CHAR_LITERAL, new DefaultMap(State.CHAR_LITERAL) {{
            putAll(State.START, '\'');
            putAll(State.ERROR, EOF);
        }});
        transition.put(State.END, new DefaultMap(State.END));
    }

    private class AnnotString {
        String str;
        TokenType annotation;
        Integer lineNumber;

        public AnnotString(String str, TokenType annotation, Integer lineNumber) {
            this.str = str;
            this.annotation = annotation;
            this.lineNumber = lineNumber;
        }

        @Override
        public String toString() {
            return lineNumber + annotation.toString() + str;
        }
    }

    private final String in;
    private Map<Integer, List<AnnotString>> tokens = new TreeMap<>(); // maps line number to token and maintains order for pretty printing
    private Map<Integer, List<String>> errors = new HashMap<>(); // maps line number to error message
    private Map<Integer, List<String>> warnings = new HashMap<>(); // maps line number to warning message

    private int start = 0; // inclusive
    private int end = 0; // non-inclusive
    private Optional<Character> lastChar = Optional.empty();
    private int lineNumber = 1;

    private String getCurrentSubstring() {
        return in.substring(start, end);
    }

    private void finishSequence(TokenType tokenType) {
        if (inIgnoredSequence()) {
            inSingleLineComment = false;
            inMultiLineComment = false;
            inWhitespace = false;
        }
        else {
            assert tokenType != null;
            String token = getCurrentSubstring();
            tokens.computeIfAbsent(lineNumber, k -> new ArrayList<>()).add(new AnnotString(token, tokenType, lineNumber));
        }
        start = end;
    }

    private boolean canGobble() {
        return end <= in.length();
    }

    private void gobble() {
        assert canGobble();
        Character c;
        if (end++ == in.length()) {
            c = EOF;
        } else {
            c = in.charAt(end);
        }

        if (c == '\n') lineNumber++;

        State nextState = transition.get(currentState).get(c);
        if (nextState == State.START) {
        } 
        else if (nextState == State.END) {
        }
        else if (nextState == State.ERROR) {
            String errorMsg = "Unexpected character: '" + c + "'";
            errors.computeIfAbsent(lineNumber, k -> new ArrayList<>()).add(errorMsg);
        } 

        currentState = nextState;
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
            for (AnnotString token : tokens.get(line)) {
                sb.append(token.toString()).append("\n");
            }
        }
        return sb.toString();
    }

    public void write(OutputStream outputStream) throws IOException {
        String output = this.toString();
        outputStream.write(output.getBytes(StandardCharsets.UTF_8));
    }
}
