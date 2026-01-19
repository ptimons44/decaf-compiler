package lang.types;

import java.util.HashMap;
import java.util.Map;

import lang.ParseException;
import lombok.Getter;

public class CFGNode {
    private CFGGraph graph;

    @Getter private String name;
    @Getter private boolean isTerminal;
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
    }

    CFGNode(CFGGraph graph, String name, Map<LookaheadKey, TransitionInner> transitions) {
        this.graph = graph;
        this.name = name;
        this.graph.register(this);
        this.transitions = transitions;
        this.isTerminal = false;
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

        CFGNodeBuilder(CFGGraph graph, String name) {
            this.graph = graph;
            this.name = name;
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

            return new CFGNode(graph, name, transitions);
        }
    }
}
