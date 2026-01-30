package lang;

/**
 * Exception thrown during semantic analysis when semantic errors are detected.
 * This includes type checking errors, scope resolution errors, and other
 * semantic violations.
 */
public class SemanticException extends Exception {
    /**
     * Creates a new semantic exception with the given message.
     *
     * @param message The error message
     */
    public SemanticException(String message) {
        super(message);
    }

    /**
     * Creates a new semantic exception with the given message and cause.
     *
     * @param message The error message
     * @param cause The underlying cause
     */
    public SemanticException(String message, Throwable cause) {
        super(message, cause);
    }
}
