package lang.types;

import java.util.HashMap;
import java.util.Map;

import lang.ParseException;
import lombok.Getter;

public class CFGNode {
    public enum CFGNodeKind {
        NORMAL,
        FRAGMENT_ENTRY,
        EXPR_ENTRY
    }

    private CFGGraph graph;

    @Getter private String name;
    @Getter private boolean isTerminal;
    @Getter private CFGNodeKind kind;
    private String successorName; // Name of the successor node after completing this fragment/expression
    public CFGNode getSuccessor() {
        return this.graph.get(this.successorName);
    }
    /*
     * Lookahead can be the string value of the token or the token type
     */
    sealed interface LookaheadKey permits TokenValue, TokenTypeKey, Epsilon {}
    record TokenValue(String value) implements LookaheadKey {}
    record TokenTypeKey(LexicalToken.TokenType type) implements LookaheadKey {}
    record Epsilon() implements LookaheadKey {}
    private record TransitionInner(
        String targetNodeName,
        boolean consumesToken
    ) {}
    public record Transition(
        CFGNode targetNode,
        boolean consumesToken
    ) {}

    private Map<LookaheadKey, TransitionInner> transitions;

    CFGNode(CFGGraph graph, String name) {
        this.graph = graph;
        this.name = name;
        this.graph.register(this);
        this.isTerminal = true;
        this.kind = CFGNodeKind.NORMAL;
        this.successorName = null;
    }

    CFGNode(CFGGraph graph, String name, Map<LookaheadKey, TransitionInner> transitions, CFGNodeKind kind, String successorName) {
        this.graph = graph;
        this.name = name;
        this.graph.register(this);
        this.transitions = transitions;
        this.isTerminal = false;
        this.kind = kind;
        this.successorName = successorName;
    }

    public Transition matchLL1(LexicalToken ll1) throws ParseException {
        TransitionInner next =
        transitions.getOrDefault(new TokenValue(ll1.getVal()),
        transitions.getOrDefault(new TokenTypeKey(ll1.getTokenType()),
        transitions.get(new Epsilon())));

        if (next == null) {
            throw new ParseException("No transition from " + name + " on " + ll1.toString());
        }
        return new Transition(graph.get(next.targetNodeName), next.consumesToken);
     }
    
    public boolean isExpr() {
        return this.name.equals("EXPR");
    }

    public static class CFGNodeBuilder {
        private CFGGraph graph;
        private String name;
        private Map<LookaheadKey, TransitionInner> transitions = new HashMap<>();
        private CFGNodeKind kind = CFGNodeKind.NORMAL;
        private String successorName = null;

        CFGNodeBuilder(CFGGraph graph, String name) {
            this.graph = graph;
            this.name = name;
        }

        public CFGNodeBuilder kind(CFGNodeKind kind) {
            this.kind = kind;
            return this;
        }

        public CFGNodeBuilder successor(String successorNodeName) {
            this.successorName = successorNodeName;
            return this;
        }

        public CFGNodeBuilder rule(String lookahead, String targetNodeName) {
            transitions.put(new TokenValue(lookahead), new TransitionInner(targetNodeName, true));
            return this;
        }

        public CFGNodeBuilder rule(LexicalToken.TokenType lookahead, String targetNodeName) {
            transitions.put(new TokenTypeKey(lookahead), new TransitionInner(targetNodeName, true));
            return this;
        }

        public CFGNodeBuilder epsilon(String targetNodeName) {
            transitions.put(new Epsilon(), new TransitionInner(targetNodeName, false));
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

            return new CFGNode(graph, name, transitions, kind, successorName);
        }
    }
}
