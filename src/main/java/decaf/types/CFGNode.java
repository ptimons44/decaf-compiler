package decaf.types;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;

public class CFGNode {
    static Map<String, CFGNode> nodeMap = new HashMap<>();
    public record LL1(LexicalToken.TokenType type, String value) {}

    @Getter private String name;
    @Getter private boolean isTerminal;
    private Map<LL1, String> transitions;

    public CFGNode(String name) {
        assert name.endsWith("_T");
        this.name = name;
        this.nodeMap.put(name, this);
        this.isTerminal = true;
    }

    public CFGNode(String name, Map<LL1, String> transitions) {
        assert !(name.endsWith("_T"));
        this.name = name;
        this.nodeMap.put(name, this);
        this.transitions = transitions;
        this.isTerminal = false;
    }

    public CFGNode matchLL1(LL1 ll1) {
        /*
         * Returns null if unable to match lookahead
         */
        String nextNodeName = this.transitions.get(ll1);
        if (nextNodeName == null) {
            return null;
        }
        return nodeMap.get(nextNodeName);
    }
}
