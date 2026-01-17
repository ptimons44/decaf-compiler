package decaf.types;

import java.util.HashMap;
import java.util.Map;

import decaf.ParseException;
import lombok.Getter;

public class CFGNode {
    private static final String TYPE_SUFFIX = "_TYPE";
    private static final String VAL_SUFFIX = "_VAL";

    static Map<String, CFGNode> nodeMap = new HashMap<>();

    @Getter private String name;
    @Getter private boolean isTerminal;
    /*
     * Lookahead can be the string value of the token or the token type
     * every lookahead in transitions map should have either _VAL or _TYPE suffix
     */
    private Map<String, String> transitions;

    private CFGNode(String name) {
        assert !nodeMap.containsKey(name) : "CFGNode with name '" + name + "' already exists";
        this.name = name;
        this.nodeMap.put(name, this); // static map of all nodes
        this.isTerminal = true;
    }

    private CFGNode(String name, Map<String, String> transitions) {
        assert !nodeMap.containsKey(name) : "CFGNode with name '" + name + "' already exists";
        this.name = name;
        this.nodeMap.put(name, this); // static map of all nodes
        this.transitions = transitions;
        this.isTerminal = false;
    }

    public CFGNode matchLL1(LexicalToken ll1) throws ParseException {
        String nextNodeName;

        // first match by exact token value
        if (this.transitions.containsKey(ll1.getVal() + VAL_SUFFIX)) {
            nextNodeName = this.transitions.get(ll1.getVal() + VAL_SUFFIX);
            return nodeMap.get(nextNodeName);
        }

        // second match by token type
        if (this.transitions.containsKey(ll1.getVal() + TYPE_SUFFIX)) {
            nextNodeName = this.transitions.get(ll1.getVal() + TYPE_SUFFIX);
            return nodeMap.get(nextNodeName);
        }

        // no match
        throw new ParseException("No matching transition for lookahead: " + ll1);
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
        private Map<String, String> transitions = new HashMap<>();

        private CFGNodeBuilder(String name) {
            this.name = name;
        }

        public CFGNodeBuilder rule(String lookahead, String targetNodeName) {
            transitions.put(lookahead + VAL_SUFFIX, targetNodeName);
            return this;
        }

        public CFGNodeBuilder rule(LexicalToken.TokenType lookahead, String targetNodeName) {
            transitions.put(lookahead + TYPE_SUFFIX, targetNodeName);
            return this;
        }

        public CFGNode build() {
            return new CFGNode(name, transitions);
        }
    }

    // Static method to get existing nodes
    public static CFGNode get(String name) {
        return nodeMap.get(name);
    }
}
