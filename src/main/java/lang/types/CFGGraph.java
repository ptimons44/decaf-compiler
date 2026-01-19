package lang.types;

import java.util.HashMap;
import java.util.Map;

public class CFGGraph {
    private final Map<String, CFGNode> nodes = new HashMap<>();

    private CFGNode root;

    public CFGNode getRoot() {
        return root;
    }

    protected void setRoot(CFGNode root) {
        this.root = root;
    }

    public CFGNode get(String name) {
        return nodes.get(name);
    }

    void register(CFGNode node) {
        if (nodes.containsKey(node.getName())) {
            throw new IllegalStateException("Duplicate node " + node.getName());
        }
        nodes.put(node.getName(), node);
    }

    // Factory methods for creating nodes bound to this graph
    public CFGNode t(String name) {
        return new CFGNode(this, name);
    }

    public CFGNode.CFGNodeBuilder nt(String name) {
        return new CFGNode.CFGNodeBuilder(this, name);
    }
}
