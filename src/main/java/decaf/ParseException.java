package decaf;

public class ParseException extends Exception {
    private final int line;
    private final int column;
    
    public ParseException(String message) {
        super(message);
        this.line = -1;
        this.column = -1;
    }
    
    public ParseException(String message, int line, int column) {
        super(message);
        this.line = line;
        this.column = column;
    }
    
    public ParseException(String message, Throwable cause) {
        super(message, cause);
        this.line = -1;
        this.column = -1;
    }
    
    public ParseException(String message, Throwable cause, int line, int column) {
        super(message, cause);
        this.line = line;
        this.column = column;
    }
    
    public int getLine() {
        return line;
    }
    
    public int getColumn() {
        return column;
    }
    
    @Override
    public String toString() {
        if (line >= 0 && column >= 0) {
            return String.format("ParseException at line %d, column %d: %s", line, column, getMessage());
        }
        return String.format("ParseException: %s", getMessage());
    }
}