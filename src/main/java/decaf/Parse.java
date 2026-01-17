package decaf;

import decaf.types.LexicalToken;
import lombok.AllArgsConstructor;
import decaf.types.ASTBase;
import decaf.types.ASTExpr;
import decaf.types.CFGNode;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.ArrayList;

public class Parse {
    private static final String START_INDEX_TOKEN = "[";
    private static final String END_INDEX_TOKEN = "]";
    private static final String START_FN_CALL_TOKEN = "(";
    private static final String ARG_DELIMITER_TOKEN = ",";
    private static final String END_FN_CALL_TOKEN = ")";
    private static final String POSTFIX_INCREMENT_TOKEN = "++";
    private static final String POSTFIX_DECREMENT_TOKEN = "--";

    /*
     * 
     * Decl CFGNodes
     * 
     */
    private static final CFGNode SEMICOLON_T = CFGNode.t("SEMICOLON");

    private static final CFGNode DECL = CFGNode.nt("DECL")
        .rule("import", "IMPORT_DECL")
        .rule("int", "VAR_DECL")
        .rule("long", "VAR_DECL")
        .rule("bool", "VAR_DECL")
        .build();

    private static final CFGNode IMPORT_DECL = CFGNode.nt("IMPORT_DECL")
        .rule("import", "IMPORT_SUF")
        .build();

    private static final CFGNode IMPORT_SUF = CFGNode.nt("IMPORT_SUF")
        .rule(LexicalToken.TokenType.IDENTIFIER, "IMPORT_SUF2")
        .build();

    private static final CFGNode IMPORT_SUF2 = CFGNode.nt("IMPORT_SUF2")
        .rule(";", "SEMICOLON")
        .build();

    private static final CFGNode VAR_DECL = CFGNode.nt("VAR_DECL")
        .rule("int", "DECL_OR_ASMT")
        .rule("long", "DECL_OR_ASMT")
        .rule("bool", "DECL_OR_ASMT")
        .build();

    private static final CFGNode DECL_OR_ASMT = CFGNode.nt("DECL_OR_ASMT")
        .rule(LexicalToken.TokenType.IDENTIFIER, "DECL_OR_ASMT_SUF")
        .build();

    private static final CFGNode DECL_OR_ASMT_SUF = CFGNode.nt("DECL_OR_ASMT_SUF")
        .rule("=", "ASMT")
        .rule(";", "SEMICOLON")
        .build();

    private static final CFGNode ASMT = CFGNode.nt("ASMT")
        .rule(LexicalToken.TokenType.INTLITERAL, "ASMT_SUF")
        .rule(LexicalToken.TokenType.LONGLITERAL, "ASMT_SUF")
        .rule(LexicalToken.TokenType.STRINGLITERAL, "ASMT_SUF")
        .rule(LexicalToken.TokenType.BOOLEANLITERAL, "ASMT_SUF")
        .build();

    private static final CFGNode ASMT_SUF = CFGNode.nt("ASMT_SUF")
        .rule(";", "SEMICOLON")
        .build();

    /*
     * 
     * Stmt CFGNodes
     * 
     */
    // TODO: write out statement CFGNodes

    private List<LexicalToken> tokens;
    private int pos = 0;
    private String error = null;
    private List<String> warnings = new ArrayList<>();
    private ASTBase ast = null;

    @AllArgsConstructor
    public static class ParseResult {
        public ASTBase tree;
        public int nextPos;

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;

            ParseResult other = (ParseResult) obj;

            return this.nextPos == other.nextPos &&
                   ((this.tree == null && other.tree == null) ||
                    (this.tree != null && this.tree.equals(other.tree)));
        }
    }

    public Parse(List<LexicalToken> tokens) {
        /*
         * Uses LL(1) parsing to parse the token stream for non-Expression grammar rules.
         * Uses Pratt parsing to parse Expression grammar rules.
         */
        this.tokens = new ArrayList<>(tokens); // Create mutable copy
        this.tokens.add(new LexicalToken(LexicalToken.TokenType.PUNCTUATION, "EOF", -1, -1)); // EOF token
        this.ast = new ASTBase();
    }

    private LexicalToken gobble() throws ParseException {
        /*
         * Advances the token position by one.
         * Returns the current token before advancing.
         */
        if (this.pos < this.tokens.size() - 1) {
            LexicalToken currentToken = this.tokens.get(this.pos);
            this.pos++;
            return currentToken;
        } else {
            throw new ParseException("Index out of bounds error: No more tokens");
        }
    }
    private LexicalToken lookahead() throws ParseException {
        /*
         * Precondition: to be called after gobble
         */
        if (this.pos < this.tokens.size()) {
            return this.tokens.get(this.pos);
        } else {
            throw new ParseException("Index out of bounds error: No more tokens");
        }
    }

    /**
     * Simple assertion helper used by the parser to record errors and abort parsing.
     * Throws IndexOutOfBoundsException to match the existing parse method exception signatures.
     */
    private void expect(boolean condition, String message) throws ParseException {
        if (!condition) {
            this.error = message;
            throw new IndexOutOfBoundsException(message);
        }
    }

    public ASTBase parseProgram() throws ParseException {
        throw new ParseException("Not Implemented");
    }

    private boolean parseDeclOrStmt(Integer pos) throws ParseException {
        CFGNode curNode = DECL;
        LexicalToken ll1;
        while (pos < tokens.size()) {
            ll1 = tokens.get(pos);
            CFGNode nextNode = curNode.matchLL1(ll1);
            curNode = nextNode;
            pos++;

            if (nextNode == null) {
                this.error = "Unable to match lookahead token " + ll1.toString() +
                             " in declaration parsing at token position " + pos;
                return false;
            }
            else if (nextNode.isTerminal()) {
                return true;
            }
        }
        return false;
    }

    private boolean parseDecl(Integer pos, ASTBase parent) throws ParseException {
        return parseDeclOrStmt(pos);
    }

    private boolean parseStmt(Integer pos, ASTBase parent) throws ParseException {
        return parseDeclOrStmt(pos);
    }

    /*
     * Methods for Expression parsing (Pratt)
     */

    private class PrecedenceInfo {
        public int leftBindingPower;
        public int rightBindingPower;
        public boolean isPostfix = false;

        public PrecedenceInfo(int leftBindingPower, int rightBindingPower) {
            this.leftBindingPower = leftBindingPower;
            this.rightBindingPower = rightBindingPower;
        }

        public PrecedenceInfo(LexicalToken token) {
            assert token != null : "Token cannot be null";
            assert token.getTokenType() == LexicalToken.TokenType.PUNCTUATION : 
                "Token must be an OPERATOR, got: " + token.getTokenType();
            
            String op = token.getVal();
            assert op != null : "Operator value cannot be null";
            
            // Set binding powers based on operator
            switch (op) {
                case "||":
                    this.leftBindingPower = 1;
                    this.rightBindingPower = 2; // LEFT: rbp = lbp + 1
                    break;
                case "&&":
                    this.leftBindingPower = 2;
                    this.rightBindingPower = 3; // LEFT: rbp = lbp + 1
                    break;
                case "==":
                case "!=":
                    this.leftBindingPower = 3;
                    this.rightBindingPower = 4; // LEFT: rbp = lbp + 1
                    break;
                case "<":
                case "<=":
                case ">":
                case ">=":
                    this.leftBindingPower = 4;
                    this.rightBindingPower = 5; // LEFT: rbp = lbp + 1
                    break;
                case "+":
                case "-":
                    this.leftBindingPower = 5;
                    this.rightBindingPower = 6; // LEFT: rbp = lbp + 1
                    break;
                case "*":
                case "/":
                case "%":
                    this.leftBindingPower = 6;
                    this.rightBindingPower = 7; // LEFT: rbp = lbp + 1
                    break;
                case "!":
                case "++":
                case "--":
                    this.leftBindingPower = 7;
                    this.rightBindingPower = 7; // RIGHT: rbp = lbp
                    this.isPostfix = true;
                    break;
                case "(":
                case "[":
                    this.isPostfix = true;
                    break;
                default:
                    throw new IllegalArgumentException("Unknown operator: " + op);
            }
        }
    }

    public ParseResult parseExpr(int startPos) throws ParseException {
        return parseExpr(startPos, 0);
    }

    public ParseResult parseExpr(int startPos, int precedence) throws ParseException {
        /*
         * Pre-conditions
         *   - startPos is within [0, tokens.size())
         *   - startPos is a valid start to an expression
         *   - precedence >= 0
         * 
         * Uses Pratt-style parsing to parse all subexpressions >= precedence.
         *   - The RHS of an operator must be parsed starting at the token after the operator, 
         *     with a strictly higher minimum binding power than the operator’s left binding power.
         *   - An operator is postfix if it requires a fully-formed LHS before it can be recognized.
         * 
         * Post-conditions
         *   - [startPos, nextPos) forms a valid stand-alone expression
         *   - returns a fully-formed AST subtree
         *     with no missing children and no post-hoc mutation required
         * 
         */

        // assert pre-conditions
        assert startPos >= 0 && startPos < this.tokens.size();
        // assert getSyntacticEnv(this.tokens.get(startPos)) == SyntacticEnv.EXPR;
        assert precedence >= 0;

        // parse prefix operators
        ParseResult prefixResult = parseExprPrefix(startPos);
        int pos = prefixResult.nextPos;
        ASTBase root = prefixResult.tree; 

        // parse infix operators
        while (hasInfixOrPostfixOperator(pos)) {
            /*
             * pos is always an operator token here
             */
            
            LexicalToken op = this.tokens.get(pos);
            PrecedenceInfo precInfo = new PrecedenceInfo(op);
            if (precInfo.leftBindingPower < precedence) {
                break;
            }

            if (precInfo.isPostfix) {
                ParseResult result = parseExprPostfix(root, pos);
                root = result.tree;
                pos = result.nextPos;
                continue;
            }

            pos++; // consume operator
            
            if (!precInfo.isPostfix) {
                ParseResult result = parseExpr(pos, precInfo.rightBindingPower);
                ASTBase right = result.tree;
                pos = result.nextPos;

                root = ASTExpr.binaryInfix(op).left(root).right(right).build();
            }
        }

        // assert post-conditions
        assert true;
        return new ParseResult(root, pos);
    }

    public ParseResult parseExprPrefix(int startPos) throws ParseException {
        if (startPos >= this.tokens.size()) {
            throw new ParseException("Unexpected end of input while parsing expression at position " + startPos);
        }
        
        LexicalToken token = this.tokens.get(startPos);
        if (token.getTokenType() == LexicalToken.TokenType.PUNCTUATION && token.getVal().equals("(")) {
            // parse parenthesized expression
            ParseResult innerResult = parseExpr(startPos + 1, 0);
            int pos = innerResult.nextPos;

            // expect closing parenthesis
            if (pos >= this.tokens.size()) {
                throw new ParseException("Unexpected end of input, expected closing parenthesis ')' at position " + pos);
            }
            
            LexicalToken closingToken = this.tokens.get(pos);
            if (!(closingToken.getTokenType() == LexicalToken.TokenType.PUNCTUATION && 
                  closingToken.getVal().equals(")"))) {
                throw new ParseException("Expected closing parenthesis ')' at position " + pos + 
                                       ", but found: " + closingToken.getVal());
            }
            pos++; // consume ')'

            return new ParseResult(innerResult.tree, pos);
        }

        ASTBase root = new ASTBase(this.tokens.get(startPos)); // TODO: initialize with proper prefix unary expr
        int pos = startPos+1;
        return new ParseResult(root, pos);
    }

    public ParseResult parseExprPostfix(ASTBase left, int startPos) throws ParseException {
        expect(
            startPos < this.tokens.size(),
            "Unexpected end of input while parsing postfix expression at position " + startPos
        );
        
        LexicalToken opToken = this.tokens.get(startPos);
        String op = opToken.getVal();
        
        if (START_INDEX_TOKEN.equals(op)) {
            // parse array indexing
            expect(
                startPos + 1 < this.tokens.size(),
                "Unexpected end of input after '[' at position " + (startPos + 1)
            );

            ParseResult indexResult = parseExpr(startPos + 1, 0);
            int pos = indexResult.nextPos;

            // expect closing bracket
            expect(
                END_INDEX_TOKEN.equals(this.tokens.get(pos).getVal()),
                "Expected closing bracket at position " + pos
            );

            pos++; // consume ']'

            ASTExpr tree = ASTExpr.arrayAccess()
                .array(left)
                .index(indexResult.tree)
                .build();

            return new ParseResult(tree, pos);
            
        } else if (START_FN_CALL_TOKEN.equals(op)) {
            // parse function call
            int pos = startPos + 1;
            List<ASTBase> args = new ArrayList<>();

            while (pos < this.tokens.size() && 
                   !END_FN_CALL_TOKEN.equals(this.tokens.get(pos).getVal())) {
                ParseResult argResult = parseExpr(pos, 0);
                args.add(argResult.tree);
                pos = argResult.nextPos;

                // check for argument delimiter
                if (!ARG_DELIMITER_TOKEN.equals(this.tokens.get(pos).getVal())) {
                    break;
                }
                pos++; // consume ','
            }

            expect(
                END_FN_CALL_TOKEN.equals(this.tokens.get(pos).getVal()),
                "Expected closing parenthesis at position " + pos
            );

            pos++; // consume ')'

            ASTExpr tree = ASTExpr.methodCall()
                .function(left)
                .arguments(args)
                .build();

            return new ParseResult(tree, pos);
        
        } else if (POSTFIX_INCREMENT_TOKEN.equals(this.tokens.get(startPos).getVal()) ||
                   POSTFIX_DECREMENT_TOKEN.equals(this.tokens.get(startPos).getVal())) {
            throw new ParseException("Not Implemented");
        }
        else {
            throw new ParseException("Unknown postfix operator: " + this.tokens.get(startPos).getVal());
        }
    }

    public boolean hasInfixOrPostfixOperator(int pos) {
        /*
         * Returns true if there is a infix operator token at pos.
         */
        if (pos < this.tokens.size()) {
            LexicalToken token = this.tokens.get(pos);
            if (token.getTokenType() == LexicalToken.TokenType.PUNCTUATION) {
                String op = token.getVal();
                Set<String> operators = Set.of(
                    "||", "&&", "==", "!=", "<", "<=", ">", ">=",
                    "+", "-", "*", "/", "%", "!", "++", "--", "(", "["
                );
                return operators.contains(op);
            }
            return false;
        }
        return false;
    }
}
