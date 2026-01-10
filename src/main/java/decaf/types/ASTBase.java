package decaf.types;

import java.util.List;
import java.util.ArrayList;
import java.util.Objects;

import lombok.NoArgsConstructor;

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
}
