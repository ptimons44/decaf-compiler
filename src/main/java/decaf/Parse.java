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

    private boolean parseDecl(Integer pos, ASTBase parent) {
        return parseDeclOrStmt(pos);
    }

    private boolean parseStmt(Integer pos, ASTBase parent) {
        return parseDeclOrStmt(pos);
    }

    private class PrecedenceInfo {
        public int precedence;
        public ASTExpr.Fixity fixity;

        public PrecedenceInfo(int precedence, ASTExpr.Fixity fixity) {
            this.precedence = precedence;
            this.fixity = fixity;
        }

        public PrecedenceInfo(LexicalToken token) {
            assert token != null : "Token cannot be null";
            assert token.getTokenType() == LexicalToken.TokenType.PUNCTUATION : 
                "Token must be an OPERATOR, got: " + token.getTokenType();
            
            String op = token.getVal();
            assert op != null : "Operator value cannot be null";
            
            // Set precedence and fixity based on operator
            switch (op) {
                case "||":
                    this.precedence = 1;
                    this.fixity = ASTExpr.Fixity.LEFT;
                    break;
                case "&&":
                    this.precedence = 2;
                    this.fixity = ASTExpr.Fixity.LEFT;
                    break;
                case "==":
                case "!=":
                    this.precedence = 3;
                    this.fixity = ASTExpr.Fixity.LEFT;
                    break;
                case "<":
                case "<=":
                case ">":
                case ">=":
                    this.precedence = 4;
                    this.fixity = ASTExpr.Fixity.LEFT;
                    break;
                case "+":
                case "-":
                    this.precedence = 5;
                    this.fixity = ASTExpr.Fixity.LEFT;
                    break;
                case "*":
                case "/":
                case "%":
                    this.precedence = 6;
                    this.fixity = ASTExpr.Fixity.LEFT;
                    break;
                case "!":
                case "++":
                case "--":
                    this.precedence = 7;
                    this.fixity = ASTExpr.Fixity.RIGHT;
                    break;
                default:
                    throw new IllegalArgumentException("Unknown operator: " + op);
            }
        }
    }


    public ParseResult parseExpr(int startPos, int precedence) throws IndexOutOfBoundsException {
        /*
         * Pre-conditions
         *   - startPos is within [0, tokens.size())
         *   - startPos is a valid start to an expression
         *   - precedence >= 0
         * 
         * Uses Pratt-style parsing to parse all subexpressions >= precedence.
         * 
         * Post-conditions
         *   - [startPos, nextPos) forms a valid stand-alone expression
         * 
         */

        // assert pre-conditions
        assert startPos >= 0 && startPos < this.tokens.size();
        // assert getSyntacticEnv(this.tokens.get(startPos)) == SyntacticEnv.EXPR;
        assert precedence >= 0;

        // parsing logic here
        int pos = startPos;
        ASTBase root = new ASTBase(this.tokens.get(pos)); // TODO: initialize with proper prefix unary expr

        while (hasNextOperator(pos) && getNextOperatorPrecedence(pos) >= precedence) {
            // parse subexpression
            pos++;
            LexicalToken op = this.tokens.get(pos);
            PrecedenceInfo precInfo = new PrecedenceInfo(op);
            if (precInfo.precedence > precedence) {
                // creates deeper tree in right subtree
                ParseResult rightResult = parseExpr(pos+1, precInfo.precedence);
                if (rightResult == null) return null;
                root = ASTExpr.binaryInfix(op).left(root).right(rightResult.tree).build();
                pos = rightResult.nextPos;
            } else {
                // tree is left-deep
                ASTBase rightNode = new ASTBase(this.tokens.get(++pos)); // TODO: initialize with proper prefix unary expr
                root = ASTExpr.binaryInfix(op).left(root).right(rightNode).build();
            }
        }

        
        // assert post-conditions
        assert true;
        return new ParseResult(root, ++pos);
    }

    public boolean hasNextOperator(int pos) {
        /*
         * Returns true if there is another infix or postfix operator token after pos.
         */
        if (pos+1 < this.tokens.size()) {
            return isOperator(this.tokens.get(pos+1));
        }
        return false;
    }

    public int getNextOperatorPrecedence(int pos) {
        /*
         * Returns the precedence of the next operator token after pos.
         * Returns -1 if there is no next operator.
         */
        if (pos+1 < this.tokens.size()) {
            LexicalToken nextToken = this.tokens.get(pos+1);
            if (isOperator(nextToken)) {
                PrecedenceInfo precInfo = new PrecedenceInfo(nextToken);
                return precInfo.precedence;
            }
        }
        return -1;
    }

    public boolean isOperator(LexicalToken token) {
        /*
         * Returns true if the token is an operator token.
         */
        if (token.getTokenType() == LexicalToken.TokenType.PUNCTUATION) {
            String op = token.getVal();
            Set<String> operators = Set.of(
                "||", "&&", "==", "!=", "<", "<=", ">", ">=",
                "+", "-", "*", "/", "%", "!", "++", "--"
            );
            return operators.contains(op);
        }
        return false;
    }

    public boolean getIsValidProgram() {
        return this.tokens != null && this.error == null;
    }
}
