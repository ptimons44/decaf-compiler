package lang;

import lang.types.LexicalToken;

public class ParseException extends Exception {
    public ParseException(String message) {
        super(message);
    }

    public ParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public ParseException(String message, LexicalToken token) {
        super(formatErrorMessage(message, token));
    }

    private static String formatErrorMessage(String message, LexicalToken token) {
        if (token == null) {
            return message;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(message);

        // Add line and column information if available
        if (token.getLineNumber() != null && token.getColumnNumber() != null) {
            sb.append(" at line ").append(token.getLineNumber())
              .append(", column ").append(token.getColumnNumber());
        }

        // Add token details
        sb.append(" (found ");
        if (token.getTokenType() != null) {
            sb.append(token.getTokenType().toString().toLowerCase()).append(" ");
        }
        if (token.getVal() != null && !token.getVal().isEmpty()) {
            sb.append("'").append(token.getVal()).append("'");
        }
        sb.append(")");

        return sb.toString();
    }
}