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
    private boolean inSingleLineComment = false;
    private boolean inMultiLineComment = false;
    private boolean inWhitespace = false;
    private boolean inStringLiteral = false;
    private boolean inCharLiteral = false;
    private boolean inHexLiteral = false;
    private boolean inDecimalLiteral = false;
    private boolean inOperator = false;

    private boolean inIgnoredSequence() {
        return inSingleLineComment || inMultiLineComment || inWhitespace;
    }

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
        return end < in.length();
    }

    private void gobble() {
        assert canGobble();
        // lastChar = in.charAt(end++);
        // if (end >= in.length()) return;
        char c = in.charAt(end++);
        if (c == '\n') lineNumber++;

        /*
         * Check for ignored sequences of whitespace, single line comments, and multi line comments
         */

        if (inWhitespace && Character.isWhitespace(c)) {
            lastChar = Optional.of(c);
            return; // continue gobbling Whitespace
        }
    
        
        /*
         * Check for closing of syntatic groups COMMENTS, STRING LITERALS, CHAR LITERALS
         */
        if (inSingleLineComment) {
            if (c == '\n') {
                finishSequence(null);
                return;
            }
        }

        if (inMultiLineComment) {
            if (lastChar.isPresent() && lastChar.get() == '*' && c == '/') {  
                finishSequence(null);
                return;
            }

        }
            
        // check if in string literal
        if (inStringLiteral) {
            // check if ended string literal
            if (c == '"' && lastChar.isPresent() && lastChar.get() != '\\') {
                finishSequence(TokenType.STRINGLITERAL);
                return;
            }
        }
            
        // check if in char literal
        if (inCharLiteral) {
            // check if ended char literal
            if (c == '\'' && lastChar.isPresent() && lastChar.get() != '\\') {
                finishSequence(TokenType.CHARLITERAL);
                return;
            }
        }

        /*
         * Check for opening of syntatic groups COMMENTS, STRING LITERALS, CHAR LITERALS
         * Also check for token boundaries (Whitespace, punctuation, hex literals)
         * Also check for multi-char operators (!=, <=, >=, ==, +=, -=, /=, *=, %=)
         */
        switch (c) {
            case '/' -> {
                // check for single-line comment
                if (lastChar.isPresent() && lastChar.get() == '/') {
                    inSingleLineComment = true;
                }
                // encountered /= | / operator
                else {
                    inOperator = true;
                }
                break;
            }
            case '*' -> {
                // check for multi-line comment
                if (lastChar.isPresent() && lastChar.get() == '/') {
                    inMultiLineComment = true;
                }
                // encountered *= | * operator
                else {
                    inOperator = true;
                }
                break;
            }
            case '=' -> {
                // tokenizes +=, -=, *=, /=, %=, <=, !=, >= operator
                if (inOperator) {
                    finishSequence(TokenType.PUNCTUATION);
                    inOperator = false;
                }
                // encountered == | = operators
                else {
                    inOperator = true;
                }
                break;
            }
            case 'L' -> {
                if (inHexLiteral || inDecimalLiteral) {
                    finishSequence(TokenType.LONGLITERAL);
                    inHexLiteral = false;
                    inDecimalLiteral = false;
                }
                else {
                    inOperator = false;
                }
            }
            case '!', '<', '>', '%' -> {
                // encountered != | <= | >= | %= | ! | < | > | % operator
                inOperator = false;
            }
            case '+', '-' -> {
                // check for multi-char operators
                if (inOperator) {
                    finishSequence(TokenType.PUNCTUATION);
                    inOperator = false;
                } else {
                    inOperator = true;
                }
            }
            case ' ', '\t', '\r', '\n', '(', ')', '[', ']', ';' -> {
                /*
                 * whitespace check at beginning of function guarantees that this whitespace is
                 * the exact end of a token
                 */
                if (inHexLiteral || inDecimalLiteral) {
                    finishSequence(TokenType.INTLITERAL);
                    inHexLiteral = false;
                    inDecimalLiteral = false;
                }
                else if (getCurrentSubstring().equals("true") || getCurrentSubstring().equals("false")) {
                    finishSequence(TokenType.BOOLEANLITERAL);
                }
                else if (keywords.contains(getCurrentSubstring())) {
                    finishSequence(TokenType.KEYWORD); // keywords are treated as punctuation
                }
                // classify identifier
                else if (Pattern.matches("[a-zA-Z_][a-zA-Z0-9_]*", getCurrentSubstring())) {
                    finishSequence(TokenType.IDENTIFIER);
                }
                // classify punctuation
                else {
                    finishSequence(TokenType.PUNCTUATION);
                }

                inWhitespace = Character.isWhitespace(c);
                inOperator = false;
            }
            case 'x', 'X' -> {
                if (lastChar.isPresent() && lastChar.get() == '0') {
                    inHexLiteral = true;
                }
                break;
            }
            default -> {
                // raise warning if non a-f or A-F char encountered in hex literal
                if (inHexLiteral && !Pattern.matches("[0-9a-fA-F]", String.valueOf(c))) {
                    // end hex literal
                    finishSequence(TokenType.INTLITERAL);
                    warnings.computeIfAbsent(lineNumber, k -> new ArrayList<>()).add("No Whitespace after hex literal");
                }
                // remaining characters
                inOperator = false;
            }
        }

        lastChar = Optional.of(c);
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
