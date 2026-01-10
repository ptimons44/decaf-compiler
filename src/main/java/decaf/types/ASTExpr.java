package decaf.types;

import decaf.types.LexicalToken;

public class ASTExpr extends ASTBase {
    public static enum Fixity {
        PREFIX,
        INFIX,
        POSTFIX
    }
    public static enum Arity {
        UNARY(1),
        BINARY(2),
        TERNARY(3);
        
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
        if (this.getNumChildren() >= this.arity.getNumOperands()) {
            throw new IllegalStateException("Cannot add more children to ASTExpr than its arity allows");
        }
        super.addChild(child);
    }
    
    // Builder pattern implementation
    public static class Builder {
        private Fixity fixity;
        private Arity arity;
        private LexicalToken operator;
        private ASTBase[] operands;
        private int operandCount = 0;
        
        public Builder(Fixity fixity, Arity arity, LexicalToken operator) {
            this.fixity = fixity;
            this.arity = arity;
            this.operator = operator;
            this.operands = new ASTBase[arity.getNumOperands()];
        }
        
        // Convenience constructor for string operators (for testing)
        public Builder(Fixity fixity, Arity arity, String operator) {
            this(fixity, arity, new LexicalToken(LexicalToken.TokenType.PUNCTUATION, operator, 0, 0));
        }
        
        public Builder operand(ASTBase operand) {
            if (operandCount >= arity.getNumOperands()) {
                throw new IllegalStateException("Too many operands for " + arity + " operation");
            }
            this.operands[operandCount++] = operand;
            return this;
        }
        
        public Builder operand(LexicalToken token) {
            return operand(new ASTBase(token));
        }
        
        public Builder operand(String identifier) {
            return operand(new LexicalToken(LexicalToken.TokenType.IDENTIFIER, identifier, 0, 0));
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
            if (operandCount != 1) {
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
            if (operandCount != arity.getNumOperands()) {
                throw new IllegalStateException("Expected " + arity.getNumOperands() + 
                    " operands but got " + operandCount);
            }
            
            ASTExpr expr = new ASTExpr(fixity, arity);
            // Add operator as first child for identification
            expr.addChild(new ASTBase(operator));
            // Add operands
            for (ASTBase operand : operands) {
                expr.addChild(operand);
            }
            return expr;
        }
    }
    
    // Static factory methods for binary infix operations
    public static Builder binaryInfix(LexicalToken operator) {
        return new Builder(Fixity.INFIX, Arity.BINARY, operator);
    }
    
    public static Builder binaryInfix(String operator) {
        return new Builder(Fixity.INFIX, Arity.BINARY, operator);
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
        return new Builder(Fixity.PREFIX, Arity.UNARY, operator);
    }
    
    public static Builder unaryPrefix(String operator) {
        return new Builder(Fixity.PREFIX, Arity.UNARY, operator);
    }
    
    public static Builder unaryPostfix(LexicalToken operator) {
        return new Builder(Fixity.POSTFIX, Arity.UNARY, operator);
    }
    
    public static Builder unaryPostfix(String operator) {
        return new Builder(Fixity.POSTFIX, Arity.UNARY, operator);
    }
    
    public static Builder negate() {
        return unaryPrefix("-");
    }
    
    public static Builder not() {
        return unaryPrefix("!");
    }
    
    // Static factory method for ternary operations
    public static Builder ternary(LexicalToken operator) {
        return new Builder(Fixity.INFIX, Arity.TERNARY, operator);
    }
    
    public static Builder ternary(String operator) {
        return new Builder(Fixity.INFIX, Arity.TERNARY, operator);
    }
    
    public static Builder conditional() {
        return ternary("?:");
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
