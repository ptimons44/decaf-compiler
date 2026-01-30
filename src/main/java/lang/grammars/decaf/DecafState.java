package lang.grammars.decaf;

/**
 * Scanner states for the Decaf language.
 * Defines all states used in the Decaf lexical analysis state machine.
 */
public enum DecafState {
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
    MULTI_LINE_COMMENT_SLASH,  // used for error check for nested comments
    MULTI_LINE_COMMENT_STAR,   // used to check for closing */
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
    ERROR
}
