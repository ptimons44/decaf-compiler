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
import java.util.Set;
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
            PUNCTUATION
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
            return lineNumber + (annotation == TokenType.PUNCTUATION ? "" : " " + annotation.toString()) + " " + str;
        }
    }

    private final String in;
    private Map<Integer, List<AnnotString>> tokens = new HashMap<>(); // maps line number to token
    private Map<Integer, List<String>> errors = new HashMap<>(); // maps line number to error message
    private Map<Integer, List<String>> warnings = new HashMap<>(); // maps line number to warning message

    private int start = 0; // inclusive
    private int end = 0; // non-inclusive
    private Character lastChar = null;
    private int lineNumber = 1;
    private boolean inSingleLineComment = false;
    private boolean inMultiLineComment = false;
    private boolean inStringLiteral = false;
    private boolean inCharLiteral = false;
    private boolean inHexLiteral = false;
    private boolean inDecimalLiteral = false;
    private boolean inOperator = false;

    private void endSequence() {
        start = ++end;
        end++;
    }

    private void endIgnoredSequence() {
        inSingleLineComment = false;
        inMultiLineComment = false;
        // inStringLiteral = false;
        // inCharLiteral = false;
        // ignore this sequence and advance pointers
        endSequence();
    }

    private String getCurrentSubstring() {
        return in.substring(start, end);
    }

    private void endTokenSequence(TokenType tokenType) {
        String token = getCurrentSubstring();
        tokens.computeIfAbsent(lineNumber, k -> new ArrayList<>()).add(new AnnotString(token, tokenType, lineNumber));
        endSequence();
    }

    private boolean canGobble() {
        return end <= in.length();
    }

    private void gobble() {
        assert canGobble();
        // lastChar = in.charAt(end++);
        // if (end >= in.length()) return;
        char c = in.charAt(end);
        if (c == '\n') lineNumber++;
        
        /*
         * Check for closing of syntatic groups COMMENTS, STRING LITERALS, CHAR LITERALS
         */

        // check if in single line comment
        if (inSingleLineComment) {
            // check if ended single line comment
            if (c == '\n') {
                endIgnoredSequence();
                return;
            }
        }

        // check if in multi line comment
        if (inMultiLineComment) {
            // check if ended multi line comment
            if (lastChar == '*' && c == '/') {  
                endIgnoredSequence();
                return;
            }

        }
            
        // check if in string literal
        if (inStringLiteral) {
            // check if ended string literal
            if (c == '"' && lastChar != '\\') {
                endTokenSequence(TokenType.STRINGLITERAL);
                return;
            }
        }
            
        // check if in char literal
        if (inCharLiteral) {
            // check if ended char literal
            if (c == '\'' && lastChar != '\\') {
                endTokenSequence(TokenType.CHARLITERAL);
                return;
            }
        }

        /*
         * Check for opening of syntatic groups COMMENTS, STRING LITERALS, CHAR LITERALS
         * Also check for token boundaries (whitespace, punctuation, hex literals)
         * Also check for multi-char operators (!=, <=, >=, ==, +=, -=, /=, *=, %=)
         */
        switch (c) {
            case '/' -> {
                // check for single-line comment
                if (lastChar == '/') {
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
                if (lastChar == '/') {
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
                    endTokenSequence(TokenType.PUNCTUATION);
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
                    endTokenSequence(TokenType.LONGLITERAL);
                    inHexLiteral = false;
                    inDecimalLiteral = false;
                }
            }
            case '!', '<', '>', '%' -> {
                // encountered != | <= | >= | %= | ! | < | > | % operator
                inOperator = false;
            }
            case '+', '-' -> {
                // check for multi-char operators
                if (inOperator) {
                    endTokenSequence(TokenType.PUNCTUATION);
                    inOperator = false;
                } else {
                    inOperator = true;
                }
            }
            case ' ', '\t', '\r', '\n', '(', ')', '[', ']', ';' -> {
                // process initial token identically
                // classify current token as INTLITERAL, BOOLEANLITERAL, IDENTIFIER
                if (inHexLiteral || inDecimalLiteral) {
                    endTokenSequence(TokenType.INTLITERAL);
                    inHexLiteral = false;
                    inDecimalLiteral = false;
                }
                // classify boolean literal
                else if (getCurrentSubstring().equals("true") || getCurrentSubstring().equals("false")) {
                    endTokenSequence(TokenType.BOOLEANLITERAL);
                }
                // classify keyword (use punctuation type for pretty-printing)
                else if (keywords.contains(getCurrentSubstring())) {
                    endTokenSequence(TokenType.PUNCTUATION); // keywords are treated as punctuation
                }
                // classify identifier
                else if (Pattern.matches("[a-zA-Z_][a-zA-Z0-9_]*", getCurrentSubstring())) {
                    endTokenSequence(TokenType.IDENTIFIER);
                }

                // additionally process punctuation token
                if (!Character.isWhitespace(c)) {
                    endTokenSequence(TokenType.PUNCTUATION);
                }
            }
            case 'x', 'X' -> {
                if (lastChar == '0') {
                    inHexLiteral = true;
                }
                break;
            }
            default -> {
                // report error if non a-f or A-F char encountered in hex literal
                if (!Pattern.matches("[0-9a-fA-F]", String.valueOf(c)) && inHexLiteral) {
                    // end hex literal
                    endTokenSequence(TokenType.INTLITERAL);
                    warnings.computeIfAbsent(lineNumber, k -> new ArrayList<>()).add("No whitespace after hex literal");
                }
            }
        }

        // update last char and advance end pointer to maintain end is non-inclusive invariant
        lastChar = in.charAt(end++);
    }

    public Scan(String in) {
        this.in = in;
    }

    public void scan() {
        while (this.canGobble()) {
            this.gobble();
        }
    }

    public void write(OutputStream outputStream) throws IOException {
        for (int line : tokens.keySet()) {
            for (AnnotString token : tokens.get(line)) {
                outputStream.write((token.toString() + "\n").getBytes(StandardCharsets.UTF_8));
            }
        }
    }
}
