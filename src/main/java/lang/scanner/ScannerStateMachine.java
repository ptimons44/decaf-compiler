package lang.scanner;

import java.util.HashMap;
import java.util.Map;

/**
 * State machine abstraction for lexical analysis.
 * Defines state transitions based on input characters.
 *
 * @param <S> The state type (typically an enum)
 */
public class ScannerStateMachine<S> {
    /**
     * Transition table: State → Character → NextState
     */
    private final Map<S, Map<Character, S>> transitions;

    /**
     * Actions to execute on specific character inputs
     */
    private final Map<Character, Runnable> actions;

    /**
     * The initial state of the state machine
     */
    private final S startState;

    /**
     * Creates a new scanner state machine.
     *
     * @param transitions The state transition table
     * @param startState The initial state
     */
    public ScannerStateMachine(Map<S, Map<Character, S>> transitions, S startState) {
        this.transitions = transitions;
        this.startState = startState;
        this.actions = new HashMap<>();
    }

    /**
     * Gets the start state of this state machine.
     *
     * @return The initial state
     */
    public S getStartState() {
        return startState;
    }

    /**
     * Performs a state transition based on the current state and input character.
     *
     * @param current The current state
     * @param c The input character
     * @return The next state, or null if no transition is defined
     */
    public S transition(S current, char c) {
        Map<Character, S> charTransitions = transitions.get(current);
        if (charTransitions == null) {
            return null;
        }
        return charTransitions.get(c);
    }

    /**
     * Gets all character transitions from a given state.
     *
     * @param state The state to get transitions for
     * @return Map of character to next state, or null if state not found
     */
    public Map<Character, S> getTransitions(S state) {
        return transitions.get(state);
    }

    /**
     * Adds an action to be executed when a specific character is encountered.
     *
     * @param c The character that triggers the action
     * @param action The action to execute
     */
    public void addAction(char c, Runnable action) {
        actions.put(c, action);
    }

    /**
     * Executes any action associated with the given character.
     *
     * @param c The character to check for actions
     */
    public void executeAction(char c) {
        Runnable action = actions.get(c);
        if (action != null) {
            action.run();
        }
    }

    /**
     * Checks if an action is defined for the given character.
     *
     * @param c The character to check
     * @return true if an action is defined, false otherwise
     */
    public boolean hasAction(char c) {
        return actions.containsKey(c);
    }
}
