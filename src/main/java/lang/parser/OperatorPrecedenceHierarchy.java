package lang.parser;

import java.util.HashMap;
import java.util.Map;

/**
 * Operator precedence hierarchy for Pratt parsing of expressions.
 * Defines left and right binding powers for each operator.
 */
public class OperatorPrecedenceHierarchy {
    /**
     * Map from operator symbol to its precedence rule
     */
    private final Map<String, PrecedenceRule> rules;

    /**
     * Private constructor - use Builder to construct
     */
    private OperatorPrecedenceHierarchy(Map<String, PrecedenceRule> rules) {
        this.rules = rules;
    }

    /**
     * Rule defining the precedence and type of an operator.
     */
    public static class PrecedenceRule {
        /**
         * Left binding power - minimum precedence required to accept this operator
         */
        public final int leftBindingPower;

        /**
         * Right binding power - minimum precedence for parsing the right-hand side
         */
        public final int rightBindingPower;

        /**
         * Type of operator
         */
        public final OperatorType type;

        public PrecedenceRule(int lbp, int rbp, OperatorType type) {
            this.leftBindingPower = lbp;
            this.rightBindingPower = rbp;
            this.type = type;
        }
    }

    /**
     * Enum defining operator types
     */
    public enum OperatorType {
        /** Prefix unary operator (e.g., -, !, ++, --) */
        PREFIX,
        /** Infix binary operator (e.g., +, -, *, /, ==, &&, ||) */
        INFIX,
        /** Postfix operator (e.g., ++, --, [], ()) */
        POSTFIX
    }

    /**
     * Gets the precedence rule for an operator.
     *
     * @param operator The operator symbol
     * @return The precedence rule, or null if operator not found
     */
    public PrecedenceRule getRule(String operator) {
        return rules.get(operator);
    }

    /**
     * Checks if an operator is defined in this hierarchy.
     *
     * @param operator The operator symbol
     * @return true if the operator is defined, false otherwise
     */
    public boolean hasOperator(String operator) {
        return rules.containsKey(operator);
    }

    /**
     * Builder for constructing an OperatorPrecedenceHierarchy.
     */
    public static class Builder {
        private final Map<String, PrecedenceRule> rules = new HashMap<>();

        /**
         * Adds an infix operator with left associativity (RBP = LBP + 1).
         *
         * @param operator The operator symbol
         * @param leftBindingPower The left binding power (precedence level)
         * @param rightBindingPower The right binding power (for associativity)
         * @return This builder
         */
        public Builder addInfix(String operator, int leftBindingPower, int rightBindingPower) {
            rules.put(operator, new PrecedenceRule(leftBindingPower, rightBindingPower, OperatorType.INFIX));
            return this;
        }

        /**
         * Adds a prefix unary operator.
         *
         * @param operator The operator symbol
         * @param leftBindingPower The left binding power
         * @param rightBindingPower The right binding power
         * @return This builder
         */
        public Builder addPrefix(String operator, int leftBindingPower, int rightBindingPower) {
            rules.put(operator, new PrecedenceRule(leftBindingPower, rightBindingPower, OperatorType.PREFIX));
            return this;
        }

        /**
         * Adds a postfix operator.
         *
         * @param operator The operator symbol
         * @param leftBindingPower The left binding power
         * @param rightBindingPower The right binding power
         * @return This builder
         */
        public Builder addPostfix(String operator, int leftBindingPower, int rightBindingPower) {
            rules.put(operator, new PrecedenceRule(leftBindingPower, rightBindingPower, OperatorType.POSTFIX));
            return this;
        }

        /**
         * Builds the OperatorPrecedenceHierarchy.
         *
         * @return The constructed hierarchy
         */
        public OperatorPrecedenceHierarchy build() {
            return new OperatorPrecedenceHierarchy(new HashMap<>(rules));
        }
    }
}
