package decaf.types;

import java.util.List;
import java.util.ArrayList;
import java.util.Objects;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
public class ASTBase {
    public static enum Environment {
        BLOCK,
        DECL,
        STMT,
        EXPR
    }

    private Environment type;
    private List<ASTBase> children = new ArrayList<>(); // Initialize to avoid null pointer
    @Getter @Setter
    private LexicalToken token;

    public ASTBase(LexicalToken token) {
        this.token = token;
        this.children = new ArrayList<>();
    }

    public int getNumChildren() {
        return this.children.size();
    }
    
    public ASTBase getChildAt(int index) {
        return this.children.get(index);
    }
    
    public void addChild(ASTBase child) {
        this.children.add(child);
    }
    
    /**
     * Determines if two AST trees are structurally equal.
     * Two trees are equal if:
     * 1. They have the same token (or both null)
     * 2. They have the same number of children
     * 3. All corresponding children are equal
     * 4. They have the same type (Environment)
     */
    public boolean equals(ASTBase other) {
        if (other == null) return false;
        if (this == other) return true;
        
        // Compare tokens - both null or both equal
        if (!Objects.equals(this.token, other.token)) {
            return false;
        }
        
        // Compare environment types
        if (!Objects.equals(this.type, other.type)) {
            return false;
        }
        
        // Compare number of children
        if (this.getNumChildren() != other.getNumChildren()) {
            return false;
        }
        
        // Recursively compare all children
        for (int i = 0; i < this.getNumChildren(); i++) {
            ASTBase thisChild = this.getChildAt(i);
            ASTBase otherChild = other.getChildAt(i);
            
            // Handle null children
            if (thisChild == null && otherChild == null) {
                continue;
            }
            if (thisChild == null || otherChild == null) {
                return false;
            }
            
            // Recursive equality check
            if (!thisChild.equals(otherChild)) {
                return false;
            }
        }
        
        return true;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ASTBase) {
            return equals((ASTBase) obj);
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        int result = Objects.hash(type, token);
        result = 31 * result + Objects.hashCode(children);
        return result;
    }
    
    public List<ASTBase> getChildren() {
        return new ArrayList<>(children); // Return defensive copy
    }
    
    /**
     * Pretty-prints this AST node and all its children in a tree format
     * @return A formatted string representation of the AST
     */
    public String prettyPrint() {
        return prettyPrint(0, "");
    }

    /**
     * Pretty-prints this AST node with indentation
     * @param depth Current depth in the tree (for indentation)
     * @param prefix Prefix for tree structure visualization
     * @return A formatted string representation of the AST
     */
    private String prettyPrint(int depth, String prefix) {
        StringBuilder sb = new StringBuilder();
        
        // Add the current node
        sb.append(prefix);
        
        // Show node type and token info
        String nodeType = this.getClass().getSimpleName();
        String tokenInfo = "";
        
        if (this.token != null) {
            tokenInfo = String.format(" [%s: '%s']", 
                this.token.getTokenType(), 
                this.token.getVal());
        }
        
        // Special handling for ASTExpr to show operator info
        if (this instanceof ASTExpr) {
            ASTExpr expr = (ASTExpr) this;
            tokenInfo += String.format(" (%s %s)", 
                expr.getArity(), 
                expr.getFixity());
        }
        
        sb.append(nodeType).append(tokenInfo).append("\n");
        
        // Add children with proper tree structure
        int childCount = this.getNumChildren();
        for (int i = 0; i < childCount; i++) {
            ASTBase child = this.getChildAt(i);
            boolean isLast = (i == childCount - 1);
            
            String childPrefix = prefix + (isLast ? "└── " : "├── ");
            String nextPrefix = prefix + (isLast ? "    " : "│   ");
            
            sb.append(child.prettyPrint(depth + 1, childPrefix));
            
            // For multi-line child output, we need to handle the prefix correctly
            if (child.getNumChildren() > 0) {
                // The recursive call already handles the tree structure
            }
        }
        
        return sb.toString();
    }

    /**
     * Prints the AST to System.out for debugging
     */
    public void debugPrint() {
        System.out.println(prettyPrint());
    }

    /**
     * Creates a compact single-line representation for quick debugging
     * @return A compact string representation
     */
    public String toCompactString() {
        StringBuilder sb = new StringBuilder();
        
        if (this.token != null) {
            sb.append(this.token.getVal());
        } else {
            sb.append(this.getClass().getSimpleName());
        }
        
        if (this.getNumChildren() > 0) {
            sb.append("(");
            for (int i = 0; i < this.getNumChildren(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(this.getChildAt(i).toCompactString());
            }
            sb.append(")");
        }
        
        return sb.toString();
    }
}
