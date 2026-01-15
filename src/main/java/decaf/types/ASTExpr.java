package decaf.types;

import decaf.types.LexicalToken;
import java.util.ArrayList;
import java.util.List;

public class ASTExpr extends ASTBase {
    public static enum Fixity {
        LEFT,
        RIGHT
    }
    public static enum Arity {
        UNARY(1),
        BINARY(2),
        TERNARY(3),
        UNBOUNDED(-1);
        
        private final int numOperands;
        
        Arity(int numOperands) {
            this.numOperands = numOperands;
        }
        
        public int getNumOperands() {
            return numOperands;
        }
    }
    
    private Fixity fixity;
    private Arity arity;
    
    public ASTExpr(Fixity fixity, Arity arity) {
        super();
        this.fixity = fixity;
        this.arity = arity;
    }

    @Override
    public void addChild(ASTBase child) {
        if (this.arity != Arity.UNBOUNDED && this.getNumChildren() >= this.arity.getNumOperands()) {
            throw new IllegalStateException("Cannot add more children to ASTExpr than its arity allows");
        }
        super.addChild(child);
    }
    
    // Builder pattern implementation
    public static class Builder {
        private Fixity fixity;
        private Arity arity;
        private LexicalToken operator;
        private List<ASTBase> operands;
        
        public Builder(Fixity fixity, Arity arity, LexicalToken operator) {
            this.fixity = fixity;
            this.arity = arity;
            this.operator = operator;
            this.operands = new ArrayList<>();
        }
        
        // Convenience constructor for string operators (for testing)
        public Builder(Fixity fixity, Arity arity, String operator) {
            this(fixity, arity, new LexicalToken(LexicalToken.TokenType.PUNCTUATION, operator, 0, 0));
        }
        
        public Builder operand(ASTBase operand) {
            if (arity != Arity.UNBOUNDED && operands.size() >= arity.getNumOperands()) {
                throw new IllegalStateException("Too many operands for " + arity + " operation");
            }
            this.operands.add(operand);
            return this;
        }
        
        public Builder operand(LexicalToken token) {
            return operand(new ASTBase(token));
        }
        
        public Builder operand(String identifier) {
            return operand(new LexicalToken(LexicalToken.TokenType.IDENTIFIER, identifier, 0, 0));
        }
        
        // For method calls - function name goes in the operator token
        public Builder function(ASTBase function) {
            if (arity != Arity.UNBOUNDED) {
                throw new IllegalStateException("function() can only be used with method calls");
            }
            if (function.getToken() == null) {
                throw new IllegalStateException("Function must have a token");
            }
            this.operator = function.getToken();
            return this;
        }
        
        public Builder function(LexicalToken token) {
            if (arity != Arity.UNBOUNDED) {
                throw new IllegalStateException("function() can only be used with method calls");
            }
            this.operator = token;
            return this;
        }
        
        public Builder function(String identifier) {
            if (arity != Arity.UNBOUNDED) {
                throw new IllegalStateException("function() can only be used with method calls");
            }
            this.operator = new LexicalToken(LexicalToken.TokenType.IDENTIFIER, identifier, 0, 0);
            return this;
        }
        
        // For method calls - arguments are the operands
        public Builder argument(ASTBase argument) {
            if (arity != Arity.UNBOUNDED) {
                throw new IllegalStateException("argument() can only be used with method calls");
            }
            return operand(argument);
        }
        
        public Builder argument(LexicalToken token) {
            return argument(new ASTBase(token));
        }
        
        public Builder argument(String identifier) {
            return argument(new LexicalToken(LexicalToken.TokenType.IDENTIFIER, identifier, 0, 0));
        }
        
        // For binary operations - more intuitive naming
        public Builder left(ASTBase left) {
            if (arity != Arity.BINARY) {
                throw new IllegalStateException("left() can only be used with BINARY operations");
            }
            return operand(left);
        }
        
        public Builder left(LexicalToken token) {
            return left(new ASTBase(token));
        }
        
        public Builder left(String identifier) {
            return left(new LexicalToken(LexicalToken.TokenType.IDENTIFIER, identifier, 0, 0));
        }
        
        public Builder right(ASTBase right) {
            if (arity != Arity.BINARY) {
                throw new IllegalStateException("right() can only be used with BINARY operations");
            }
            if (operands.size() != 1) {
                throw new IllegalStateException("Must set left operand before right operand");
            }
            return operand(right);
        }
        
        public Builder right(LexicalToken token) {
            return right(new ASTBase(token));
        }
        
        public Builder right(String identifier) {
            return right(new LexicalToken(LexicalToken.TokenType.IDENTIFIER, identifier, 0, 0));
        }
        
        public ASTExpr build() {
            if (arity != Arity.UNBOUNDED && operands.size() != arity.getNumOperands()) {
                throw new IllegalStateException("Expected " + arity.getNumOperands() + 
                    " operands but got " + operands.size());
            }
            // For method calls, we don't require a minimum number of operands (0-arg functions are valid)
            
            ASTExpr expr = new ASTExpr(fixity, arity);
            expr.setToken(operator);
            // Add operands
            for (ASTBase operand : operands) {
                expr.addChild(operand);
            }
            return expr;
        }
    }
    
    // Static factory methods for binary infix operations
    public static Builder binaryInfix(LexicalToken operator) {
        return new Builder(Fixity.LEFT, Arity.BINARY, operator);
    }
    
    public static Builder binaryInfix(String operator) {
        return new Builder(Fixity.LEFT, Arity.BINARY, operator);
    }
    
    public static Builder add() {
        return binaryInfix("+");
    }
    
    public static Builder subtract() {
        return binaryInfix("-");
    }
    
    public static Builder multiply() {
        return binaryInfix("*");
    }
    
    public static Builder divide() {
        return binaryInfix("/");
    }
    
    public static Builder lessThan() {
        return binaryInfix("<");
    }
    
    public static Builder greaterThan() {
        return binaryInfix(">");
    }
    
    public static Builder equals() {
        return binaryInfix("==");
    }
    
    public static Builder notEquals() {
        return binaryInfix("!=");
    }
    
    public static Builder and() {
        return binaryInfix("&&");
    }
    
    public static Builder or() {
        return binaryInfix("||");
    }
    
    // Static factory methods for unary operations
    public static Builder unaryPrefix(LexicalToken operator) {
        return new Builder(Fixity.RIGHT, Arity.UNARY, operator);
    }
    
    public static Builder unaryPrefix(String operator) {
        return new Builder(Fixity.RIGHT, Arity.UNARY, operator);
    }
    
    public static Builder unaryPostfix(LexicalToken operator) {
        return new Builder(Fixity.LEFT, Arity.UNARY, operator);
    }
    
    public static Builder unaryPostfix(String operator) {
        return new Builder(Fixity.LEFT, Arity.UNARY, operator);
    }
    
    public static Builder negate() {
        return unaryPrefix("-");
    }
    
    public static Builder not() {
        return unaryPrefix("!");
    }
    
    // Static factory method for ternary operations
    public static Builder ternary(LexicalToken operator) {
        return new Builder(Fixity.LEFT, Arity.TERNARY, operator);
    }
    
    public static Builder ternary(String operator) {
        return new Builder(Fixity.LEFT, Arity.TERNARY, operator);
    }
    
    public static Builder conditional() {
        return ternary("?:");
    }

    public static Builder methodCall() {
        // Start with empty function name - will be set via function() method
        return new Builder(Fixity.LEFT, Arity.UNBOUNDED, new LexicalToken(LexicalToken.TokenType.IDENTIFIER, "", 0, 0));
    }
    
    // Helper method to create leaf nodes from tokens
    public static ASTBase leaf(LexicalToken token) {
        return new ASTBase(token);
    }
    
    // Helper method to create leaf nodes from strings (for testing convenience)
    public static ASTBase leaf(String value) {
        return new ASTBase(new LexicalToken(LexicalToken.TokenType.IDENTIFIER, value, 0, 0));
    }
    
    // Getters
    public Fixity getFixity() { return fixity; }
    public Arity getArity() { return arity; }
}
