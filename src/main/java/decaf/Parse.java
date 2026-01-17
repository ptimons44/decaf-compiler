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

    private List<LexicalToken> tokens;
    private int pos = 0;
    private String error = null;
    private List<String> warnings = new ArrayList<>();
    private ASTBase ast = null;

    private enum SyntacticEnv {
        DECL,
        STMT,
        EXPR,
        ERROR
    }

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

    private SyntacticEnv getSyntacticEnv(LexicalToken token) {
        LexicalToken.TokenType tokenType = token.getTokenType();
        String tokenValue = token.getVal();

        // Declarations: start with type keywords (int, long, bool) or void
        if (LexicalToken.TokenType.KEYWORD.equals(tokenType) &&
            ("int".equals(tokenValue) || "long".equals(tokenValue) ||
             "bool".equals(tokenValue) || "void".equals(tokenValue))) {
            return SyntacticEnv.DECL;
        }

        // Statements: start with statement keywords
        if (LexicalToken.TokenType.KEYWORD.equals(tokenType) &&
            ("if".equals(tokenValue) || "while".equals(tokenValue) ||
             "for".equals(tokenValue) || "return".equals(tokenValue) ||
             "break".equals(tokenValue) || "continue".equals(tokenValue))) {
            return SyntacticEnv.STMT;
        }

        // Statements: identifiers can start statements (assignments, method calls)
        if (LexicalToken.TokenType.IDENTIFIER.equals(tokenType)) {
            return SyntacticEnv.STMT;
        }

        // Block statements
        if (LexicalToken.TokenType.PUNCTUATION.equals(tokenType) && "{".equals(tokenValue)) {
            return SyntacticEnv.STMT;
        }

        // Expressions: literals or opening parenthesis
        if (LexicalToken.TokenType.INTLITERAL.equals(tokenType) ||
            LexicalToken.TokenType.STRINGLITERAL.equals(tokenType) ||
            LexicalToken.TokenType.BOOLEANLITERAL.equals(tokenType) ||
            (LexicalToken.TokenType.PUNCTUATION.equals(tokenType) && "(".equals(tokenValue))) {
            return SyntacticEnv.EXPR;
        }

        return SyntacticEnv.ERROR;
    }

    public Parse(List<LexicalToken> tokens) {
        /*
         * Uses LL(1) parsing to parse the token stream for non-Expression grammar rules.
         * Uses Pratt parsing to parse Expression grammar rules.
         */
        this.tokens = new ArrayList<>(tokens); // Create mutable copy
        this.tokens.add(new LexicalToken(LexicalToken.TokenType.PUNCTUATION, "EOF", -1, -1)); // EOF token
        this.ast = new ASTBase();

        // LexicalToken lookahead = null;
        // Integer pos = 0; // spawned methods can modify
        // boolean isValid = true;
        // while (pos < tokens.size()) {
        //     lookahead = tokens.get(pos);
        //     SyntacticEnv syntacticEnv = getSyntacticEnv(lookahead);
        //     if (syntacticEnv == SyntacticEnv.DECL) {
        //         isValid &= parseDecl(pos, this.ast);
        //     } else if (syntacticEnv == SyntacticEnv.STMT) {
        //         isValid &= parseStmt(pos, this.ast);
        //     } else if (syntacticEnv == SyntacticEnv.EXPR) {
        //         continue; // TODO: implement expression parsing
        //         // isValid &= parseExpr(pos, 0);
        //     } else {
        //         this.error = "Unknown syntactic environment for token: " + lookahead.toString();
        //         return;
        //     }
        //     // TODO: remove
        //     break;
        // }
    }

    /*
     * 
     * Decl CFGNodes
     * 
     */
    private static final CFGNode SEMICOLON_T = new CFGNode("SEMICOLON_T");

    private static final CFGNode DECL = new CFGNode("DECL", Map.of(
        new CFGNode.LL1(LexicalToken.TokenType.KEYWORD, "import"), "IMPORT_DECL",
        new CFGNode.LL1(LexicalToken.TokenType.KEYWORD, "int"), "VAR_DECL",
        new CFGNode.LL1(LexicalToken.TokenType.KEYWORD, "long"), "VAR_DECL",
        new CFGNode.LL1(LexicalToken.TokenType.KEYWORD, "bool"), "VAR_DECL"
    ));

    private static final CFGNode IMPORT_DECL = new CFGNode("IMPORT_DECL", Map.of(
        new CFGNode.LL1(LexicalToken.TokenType.KEYWORD, "import"), "IMPORT_SUF"
    ));

    private static final CFGNode IMPORT_SUF = new CFGNode("IMPORT_SUF", Map.of(
        new CFGNode.LL1(LexicalToken.TokenType.IDENTIFIER, null), "IMPORT_SUF2" 
    ));

    private static final CFGNode IMPORT_SUF2 = new CFGNode("IMPORT_SUF", Map.of(
        new CFGNode.LL1(LexicalToken.TokenType.PUNCTUATION, ";"), "SEMICOLON_T"
    ));

    private static final CFGNode VAR_DECL = new CFGNode("VAR_DECL", Map.of(
        new CFGNode.LL1(LexicalToken.TokenType.KEYWORD, "int"), "DECL_OR_ASMT",
        new CFGNode.LL1(LexicalToken.TokenType.KEYWORD, "long"), "DECL_OR_ASMT",
        new CFGNode.LL1(LexicalToken.TokenType.KEYWORD, "bool"), "DECL_OR_ASMT"
    ));

    private static final CFGNode DECL_OR_ASMT = new CFGNode("DECL_OR_ASMT", Map.of(
        new CFGNode.LL1(LexicalToken.TokenType.IDENTIFIER, null), "DECL_OR_ASMT_SUF"
    ));

    private static final CFGNode DECL_OR_ASMT_SUF = new CFGNode("DECL_OR_ASMT_SUF", Map.of(
        new CFGNode.LL1(LexicalToken.TokenType.PUNCTUATION, "="), "ASMT",
        new CFGNode.LL1(LexicalToken.TokenType.PUNCTUATION, ";"), "SEMICOLON_T"
    ));

    private static final CFGNode ASMT = new CFGNode("ASMT", Map.of(
        new CFGNode.LL1(LexicalToken.TokenType.INTLITERAL, null), "ASMT_SUF",
        new CFGNode.LL1(LexicalToken.TokenType.LONGLITERAL, null), "ASMT_SUF",
        new CFGNode.LL1(LexicalToken.TokenType.STRINGLITERAL, null), "ASMT_SUF",
        new CFGNode.LL1(LexicalToken.TokenType.BOOLEANLITERAL, null), "ASMT_SUF"
    ));

    private static final CFGNode ASMT_SUF = new CFGNode("ASMT_SUF", Map.of(
        new CFGNode.LL1(LexicalToken.TokenType.PUNCTUATION, ";"), "SEMICOLON_T"
    ));

    /*
     * 
     * Stmt CFGNodes
     * 
     */

    private boolean parseDeclOrStmt(Integer pos) {
        CFGNode curNode = DECL;
        CFGNode.LL1 ll1;
        while (pos < tokens.size()) {
            ll1 = new CFGNode.LL1(tokens.get(pos).getTokenType(), tokens.get(pos).getVal());
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

    private LexicalToken gobble() throws IndexOutOfBoundsException {
        /*
         * Advances the token position by one.
         * Returns the current token before advancing.
         */
        if (this.pos < this.tokens.size() - 1) {
            LexicalToken currentToken = this.tokens.get(this.pos);
            this.pos++;
            return currentToken;
        } else {
            throw new IndexOutOfBoundsException("No more tokens");
        }
    }
    private LexicalToken lookahead() throws IndexOutOfBoundsException {
        /*
         * Precondition: to be called after gobble
         */
        if (this.pos < this.tokens.size()) {
            return this.tokens.get(this.pos);
        } else {
            throw new IndexOutOfBoundsException("No more tokens");
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

    private boolean parseDecl(Integer pos, ASTBase parent) {
        return parseDeclOrStmt(pos);
    }

    private boolean parseStmt(Integer pos, ASTBase parent) {
        return parseDeclOrStmt(pos);
    }

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
        if (prefixResult == null) return null;
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
                if (right == null) return null;
                pos = result.nextPos;

                root = ASTExpr.binaryInfix(op).left(root).right(right).build();
            }
        }

        // assert post-conditions
        assert true;
        return new ParseResult(root, pos);
    }

    public ParseResult parseExprPrefix(int startPos) throws ParseException {
        LexicalToken token = this.tokens.get(startPos);
        if (token.getTokenType() == LexicalToken.TokenType.PUNCTUATION && token.getVal().equals("(")) {
            // parse parenthesized expression
            ParseResult innerResult = parseExpr(startPos + 1, 0);
            if (innerResult == null) return null;
            int pos = innerResult.nextPos;

            // expect closing parenthesis
            if (pos >= this.tokens.size() || 
                !(this.tokens.get(pos).getTokenType() == LexicalToken.TokenType.PUNCTUATION && 
                  this.tokens.get(pos).getVal().equals(")"))) {
                this.error = "Expected closing parenthesis at position " + pos;
                return null;
            }
            pos++; // consume ')'

            return new ParseResult(innerResult.tree, pos);
        }

        ASTBase root = new ASTBase(this.tokens.get(startPos)); // TODO: initialize with proper prefix unary expr
        int pos = startPos+1;
        return new ParseResult(root, pos);
    }

    public ParseResult parseExprPostfix(ASTBase left, int startPos) throws ParseException {
        if (START_INDEX_TOKEN.equals(this.tokens.get(startPos).getVal())) {
            // parse array indexing
            ParseResult indexResult = parseExpr(startPos + 1, 0);
            if (indexResult == null) return null;
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
        } else if (START_FN_CALL_TOKEN.equals(this.tokens.get(startPos).getVal())) {
            // parse function call
            int pos = startPos + 1;
            List<ASTBase> args = new ArrayList<>();

            while (pos+1 < this.tokens.size() && 
                   !END_FN_CALL_TOKEN.equals(this.tokens.get(pos).getVal())) {
                ParseResult argResult = parseExpr(pos, 0);
                if (argResult == null) return null;
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

    public boolean getIsValidProgram() {
        return this.tokens != null && this.error == null;
    }
}
