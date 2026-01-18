package decaf.types;

import java.util.HashMap;
import java.util.Map;

import decaf.ParseException;
import lombok.Getter;

public class CFGNode {
    static Map<String, CFGNode> nodeMap = new HashMap<>();

    @Getter private String name;
    @Getter private boolean isTerminal;
    /*
     * Lookahead can be the string value of the token or the token type
     */
    sealed interface LookaheadKey permits TokenValue, TokenTypeKey {}
    record TokenValue(String value) implements LookaheadKey {}
    record TokenTypeKey(LexicalToken.TokenType type) implements LookaheadKey {}
    private Map<LookaheadKey, String> transitions;

    private CFGNode(String name) {
        assert !nodeMap.containsKey(name) : "CFGNode with name '" + name + "' already exists";
        this.name = name;
        this.nodeMap.put(name, this); // static map of all nodes
        this.isTerminal = true;
    }

    private CFGNode(String name, Map<LookaheadKey, String> transitions) {
        assert !nodeMap.containsKey(name) : "CFGNode with name '" + name + "' already exists";
        this.name = name;
        this.nodeMap.put(name, this); // static map of all nodes
        this.transitions = transitions;
        this.isTerminal = false;
    }

    public CFGNode matchLL1(LexicalToken ll1) throws ParseException {
        String next =
        transitions.getOrDefault(new TokenValue(ll1.getVal()),
        transitions.get(new TokenTypeKey(ll1.getTokenType())));

        if (next == null) {
            throw new ParseException("No transition from " + name + " on " + ll1.toString());
        }
        return nodeMap.get(next);
     }
    
    public boolean isExpr() {
        return this.name.equals("EXPR");
    }

    // Builder methods
    public static CFGNode t(String name) {
        return new CFGNode(name);
    }

    public static CFGNodeBuilder nt(String name) {
        return new CFGNodeBuilder(name);
    }

    public static class CFGNodeBuilder {
        private String name;
        private Map<LookaheadKey, String> transitions = new HashMap<>();

        private CFGNodeBuilder(String name) {
            this.name = name;
        }

        public CFGNodeBuilder rule(String lookahead, String targetNodeName) {
            transitions.put(new TokenValue(lookahead), targetNodeName);
            return this;
        }

        public CFGNodeBuilder rule(LexicalToken.TokenType lookahead, String targetNodeName) {
            transitions.put(new TokenTypeKey(lookahead), targetNodeName);
            return this;
        }

        public CFGNode build() {
            for (LookaheadKey k : transitions.keySet()) {
                if (k instanceof TokenValue v) {
                    LexicalToken.TokenType t = LexicalToken.inferTokenType(v.value());
                    if (transitions.containsKey(new TokenTypeKey(t))) {
                        throw new IllegalStateException(
                            "LL(1) conflict in " + name +
                            ": value '" + v.value() +
                            "' conflicts with type " + t
                        );
                    }
                }
            }

            return new CFGNode(name, transitions);
        }
    }

    // Static method to get existing nodes
    public static CFGNode get(String name) {
        return nodeMap.get(name);
    }
}
