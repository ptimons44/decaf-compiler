package lang.grammars.decaf;

import lang.scanner.ScannerStateMachine;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Factory for creating the Decaf language scanner state machine.
 * Defines all state transitions for Decaf lexical analysis.
 */
public class DecafScannerStateMachine {
    /**
     * Special character representing end of file
     */
    public static final Character EOF = null;

    /**
     * Helper class for building state transitions with default values
     */
    private static class StateTransitionBuilder {
        private final Map<Character, DecafState> transitions = new HashMap<>();
        private final DecafState defaultState;
        private final Map<Character, Runnable> actions = new HashMap<>();

        public StateTransitionBuilder(DecafState defaultState) {
            this.defaultState = defaultState;
        }

        public StateTransitionBuilder put(DecafState state, Character... chars) {
            for (Character c : chars) {
                transitions.put(c, state);
            }
            return this;
        }

        public StateTransitionBuilder putRange(DecafState state, char start, char end) {
            for (char c = start; c <= end; c++) {
                transitions.put(c, state);
            }
            return this;
        }

        public StateTransitionBuilder putAction(Character trigger, Runnable action) {
            actions.put(trigger, action);
            return this;
        }

        public Map<Character, DecafState> build() {
            return new HashMap<>(transitions) {
                @Override
                public DecafState get(Object key) {
                    return super.getOrDefault(key, defaultState);
                }
            };
        }

        public Map<Character, Runnable> getActions() {
            return actions;
        }
    }

    /**
     * Builds the Decaf scanner state machine.
     *
     * @param errorHandler Consumer for error messages (line number will be tracked separately)
     * @return The configured scanner state machine
     */
    public static ScannerStateMachine<DecafState> build(Consumer<String> errorHandler) {
        Map<DecafState, Map<Character, DecafState>> transitions = new HashMap<>();

        // START state transitions
        {
            StateTransitionBuilder builder = new StateTransitionBuilder(DecafState.ERROR);
            builder.put(DecafState.SLASH, '/');
            builder.put(DecafState.STAR, '*');
            builder.put(DecafState.PLUS, '+');
            builder.put(DecafState.MINUS, '-');
            builder.put(DecafState.EQUAL, '=');
            builder.put(DecafState.LESS_THAN, '<');
            builder.put(DecafState.GREATER_THAN, '>');
            builder.put(DecafState.BANG, '!');
            builder.put(DecafState.MODULO, '%');
            builder.put(DecafState.ZERO, '0');
            builder.putRange(DecafState.DEC_LITERAL, '1', '9');
            builder.putRange(DecafState.IDENTIFIER, 'a', 'z');
            builder.putRange(DecafState.IDENTIFIER, 'A', 'Z');
            builder.put(DecafState.IDENTIFIER, '_');
            builder.put(DecafState.CHAR_LITERAL, '\'');
            builder.put(DecafState.STRING_LITERAL, '\"');
            builder.put(DecafState.WHITESPACE, ' ', '\t', '\r', '\n', '\f');
            builder.put(DecafState.PUNCTUATION, '(', ')', '[', ']', '{', '}', ';', ',');
            builder.put(DecafState.AMPER, '&');
            builder.put(DecafState.PIPE, '|');
            builder.put(DecafState.END, EOF);
            transitions.put(DecafState.START, builder.build());
        }

        // WHITESPACE state
        {
            StateTransitionBuilder builder = new StateTransitionBuilder(DecafState.START);
            builder.put(DecafState.WHITESPACE, ' ', '\t', '\r', '\n', '\f');
            transitions.put(DecafState.WHITESPACE, builder.build());
        }

        // SLASH state
        {
            StateTransitionBuilder builder = new StateTransitionBuilder(DecafState.START);
            builder.put(DecafState.SINGLE_LINE_COMMENT, '/');
            builder.put(DecafState.MULTI_LINE_COMMENT, '*');
            builder.put(DecafState.DIV_EQ, '=');
            transitions.put(DecafState.SLASH, builder.build());
        }

        // STAR state
        {
            StateTransitionBuilder builder = new StateTransitionBuilder(DecafState.START);
            builder.put(DecafState.MUL_EQ, '=');
            transitions.put(DecafState.STAR, builder.build());
        }

        // PLUS state
        {
            StateTransitionBuilder builder = new StateTransitionBuilder(DecafState.START);
            builder.put(DecafState.ADD_EQ, '=');
            builder.put(DecafState.INCR, '+');
            transitions.put(DecafState.PLUS, builder.build());
        }

        // MINUS state
        {
            StateTransitionBuilder builder = new StateTransitionBuilder(DecafState.START);
            builder.put(DecafState.SUB_EQ, '=');
            builder.put(DecafState.DECR, '-');
            transitions.put(DecafState.MINUS, builder.build());
        }

        // EQUAL state
        {
            StateTransitionBuilder builder = new StateTransitionBuilder(DecafState.START);
            builder.put(DecafState.EQEQ, '=');
            transitions.put(DecafState.EQUAL, builder.build());
        }

        // LESS_THAN state
        {
            StateTransitionBuilder builder = new StateTransitionBuilder(DecafState.START);
            builder.put(DecafState.LEQ, '=');
            transitions.put(DecafState.LESS_THAN, builder.build());
        }

        // GREATER_THAN state
        {
            StateTransitionBuilder builder = new StateTransitionBuilder(DecafState.START);
            builder.put(DecafState.GEQ, '=');
            transitions.put(DecafState.GREATER_THAN, builder.build());
        }

        // BANG state
        {
            StateTransitionBuilder builder = new StateTransitionBuilder(DecafState.START);
            builder.put(DecafState.NEQ, '=');
            transitions.put(DecafState.BANG, builder.build());
        }

        // MODULO state
        {
            StateTransitionBuilder builder = new StateTransitionBuilder(DecafState.START);
            builder.put(DecafState.MOD_EQ, '=');
            transitions.put(DecafState.MODULO, builder.build());
        }

        // ZERO state
        {
            StateTransitionBuilder builder = new StateTransitionBuilder(DecafState.START);
            builder.putRange(DecafState.DEC_LITERAL, '0', '9');
            builder.put(DecafState.DEC_LITERAL, '_');
            builder.put(DecafState.HEX_LITERAL, 'x');
            builder.put(DecafState.START, 'X');
            builder.put(DecafState.LONG_LITERAL, 'L');
            builder.put(DecafState.START, 'l');
            transitions.put(DecafState.ZERO, builder.build());
        }

        // DEC_LITERAL state
        {
            StateTransitionBuilder builder = new StateTransitionBuilder(DecafState.START);
            builder.putRange(DecafState.DEC_LITERAL, '0', '9');
            builder.put(DecafState.DEC_LITERAL, '_');
            builder.put(DecafState.LONG_LITERAL, 'L');
            transitions.put(DecafState.DEC_LITERAL, builder.build());
        }

        // HEX_LITERAL state
        {
            StateTransitionBuilder builder = new StateTransitionBuilder(DecafState.START);
            builder.putRange(DecafState.HEX_LITERAL, '0', '9');
            builder.putRange(DecafState.HEX_LITERAL, 'a', 'f');
            builder.putRange(DecafState.HEX_LITERAL, 'A', 'F');
            builder.put(DecafState.HEX_LITERAL, '_');
            builder.put(DecafState.LONG_LITERAL, 'L');
            transitions.put(DecafState.HEX_LITERAL, builder.build());
        }

        // IDENTIFIER state
        {
            StateTransitionBuilder builder = new StateTransitionBuilder(DecafState.START);
            builder.putRange(DecafState.IDENTIFIER, 'a', 'z');
            builder.putRange(DecafState.IDENTIFIER, 'A', 'Z');
            builder.putRange(DecafState.IDENTIFIER, '0', '9');
            builder.put(DecafState.IDENTIFIER, '_');
            transitions.put(DecafState.IDENTIFIER, builder.build());
        }

        // SINGLE_LINE_COMMENT state
        {
            StateTransitionBuilder builder = new StateTransitionBuilder(DecafState.SINGLE_LINE_COMMENT);
            builder.put(DecafState.START, '\n', EOF);
            transitions.put(DecafState.SINGLE_LINE_COMMENT, builder.build());
        }

        // MULTI_LINE_COMMENT state
        {
            StateTransitionBuilder builder = new StateTransitionBuilder(DecafState.MULTI_LINE_COMMENT);
            builder.put(DecafState.MULTI_LINE_COMMENT_STAR, '*');
            builder.put(DecafState.MULTI_LINE_COMMENT_SLASH, '/');
            builder.put(DecafState.ERROR, EOF);
            builder.putAction(EOF, () -> errorHandler.accept("Unclosed multi-line comment"));
            transitions.put(DecafState.MULTI_LINE_COMMENT, builder.build());
        }

        // MULTI_LINE_COMMENT_SLASH state
        {
            StateTransitionBuilder builder = new StateTransitionBuilder(DecafState.MULTI_LINE_COMMENT);
            builder.put(DecafState.MULTI_LINE_COMMENT_SLASH, '/');
            builder.put(DecafState.ERROR, '*');
            builder.putAction('*', () -> errorHandler.accept("Nested multi-line comment are illegal"));
            builder.put(DecafState.ERROR, EOF);
            builder.putAction(EOF, () -> errorHandler.accept("Unclosed multi-line comment"));
            transitions.put(DecafState.MULTI_LINE_COMMENT_SLASH, builder.build());
        }

        // MULTI_LINE_COMMENT_STAR state
        {
            StateTransitionBuilder builder = new StateTransitionBuilder(DecafState.MULTI_LINE_COMMENT);
            builder.put(DecafState.MULTI_LINE_COMMENT_END, '/');
            builder.put(DecafState.MULTI_LINE_COMMENT_STAR, '*');
            builder.put(DecafState.ERROR, EOF);
            builder.putAction(EOF, () -> errorHandler.accept("Unclosed multi-line comment"));
            transitions.put(DecafState.MULTI_LINE_COMMENT_STAR, builder.build());
        }

        // STRING_LITERAL state
        {
            StateTransitionBuilder builder = new StateTransitionBuilder(DecafState.STRING_LITERAL);
            builder.put(DecafState.STRING_LITERAL_IGNORE_NEXT, '\\');
            builder.put(DecafState.STRING_LITERAL_END, '"');
            builder.put(DecafState.ERROR, EOF);
            builder.putAction(EOF, () -> errorHandler.accept("Unclosed string literal"));
            builder.put(DecafState.ERROR, '\n');
            builder.putAction('\n', () -> errorHandler.accept("Newline in string literal"));
            builder.put(DecafState.ERROR, '\'');
            builder.putAction('\'', () -> errorHandler.accept("Unescaped single quote in string literal"));
            transitions.put(DecafState.STRING_LITERAL, builder.build());
        }

        // STRING_LITERAL_IGNORE_NEXT state
        {
            StateTransitionBuilder builder = new StateTransitionBuilder(DecafState.STRING_LITERAL);
            builder.put(DecafState.ERROR, EOF);
            builder.putAction(EOF, () -> errorHandler.accept("Unclosed string literal"));
            transitions.put(DecafState.STRING_LITERAL_IGNORE_NEXT, builder.build());
        }

        // CHAR_LITERAL state
        {
            StateTransitionBuilder builder = new StateTransitionBuilder(DecafState.CHAR_LITERAL);
            builder.put(DecafState.START, '\'');
            builder.put(DecafState.CHAR_LITERAL_IGNORE_NEXT, '\\');
            builder.put(DecafState.CHAR_LITERAL_END, '\'');
            builder.put(DecafState.ERROR, EOF);
            builder.putAction(EOF, () -> errorHandler.accept("Unclosed char literal"));
            builder.put(DecafState.ERROR, '\n');
            builder.putAction('\n', () -> errorHandler.accept("Newline or tab in char literal"));
            builder.put(DecafState.ERROR, '\t');
            builder.putAction('\t', () -> errorHandler.accept("Tab in char literal"));
            builder.put(DecafState.ERROR, '\"');
            builder.putAction('\"', () -> errorHandler.accept("Unescaped double quote in char literal"));
            transitions.put(DecafState.CHAR_LITERAL, builder.build());
        }

        // CHAR_LITERAL_IGNORE_NEXT state
        {
            StateTransitionBuilder builder = new StateTransitionBuilder(DecafState.CHAR_LITERAL);
            builder.put(DecafState.ERROR, EOF);
            builder.putAction(EOF, () -> errorHandler.accept("Unclosed char literal"));
            builder.put(DecafState.ERROR, '\n');
            builder.putAction('\n', () -> errorHandler.accept("Newline in char literal"));
            transitions.put(DecafState.CHAR_LITERAL_IGNORE_NEXT, builder.build());
        }

        // AMPER state
        {
            StateTransitionBuilder builder = new StateTransitionBuilder(DecafState.ERROR);
            builder.put(DecafState.AMPER_AMPER, '&');
            transitions.put(DecafState.AMPER, builder.build());
        }

        // AMPER_AMPER state
        {
            StateTransitionBuilder builder = new StateTransitionBuilder(DecafState.START);
            transitions.put(DecafState.AMPER_AMPER, builder.build());
        }

        // PIPE state
        {
            StateTransitionBuilder builder = new StateTransitionBuilder(DecafState.ERROR);
            builder.put(DecafState.PIPE_PIPE, '|');
            transitions.put(DecafState.PIPE, builder.build());
        }

        // END state
        {
            StateTransitionBuilder builder = new StateTransitionBuilder(DecafState.END);
            transitions.put(DecafState.END, builder.build());
        }

        // ERROR state
        {
            StateTransitionBuilder builder = new StateTransitionBuilder(DecafState.ERROR);
            transitions.put(DecafState.ERROR, builder.build());
        }

        return new ScannerStateMachine<>(transitions, DecafState.START);
    }
}
