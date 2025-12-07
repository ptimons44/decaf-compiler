package decaf;

import decaf.types.LexicalToken;
import decaf.types.AST;
import decaf.types.CFGNode;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.ArrayList;

public class Parse {
    private List<LexicalToken> tokens;
    private String error = null;
    private List<String> warnings = new ArrayList<>();
    private AST ast = null;

    private enum SyntacticEnv {
        DECL,
        STMT,
        EXPR,
        ERROR
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
        this.tokens = tokens;

        LexicalToken lookahead = null;
        Integer pos = 0; // spawned methods can modify
        boolean isValid = true;
        while (pos < tokens.size()) {
            lookahead = tokens.get(pos);
            SyntacticEnv syntacticEnv = getSyntacticEnv(lookahead);
            if (syntacticEnv == SyntacticEnv.DECL) {
                isValid &= parseDecl(pos);
            } else if (syntacticEnv == SyntacticEnv.STMT) {
                isValid &= parseStmt(pos);
            } else if (syntacticEnv == SyntacticEnv.EXPR) {
                isValid &= parseExpr(pos, 0);
            } else {
                this.error = "Unknown syntactic environment for token: " + lookahead.toString();
                return;
            }
            // TODO: remove
            break;
        }
    }

    // Terminal CFGNode instances
    private static final CFGNode SEMICOLON_T = new CFGNode("SEMICOLON_T");

    // Non-Terminal CFGNode instances with LL1 transition maps
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

    private boolean parseDecl(Integer pos) {
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

    private boolean parseStmt(int pos) {
        return true;
    }

    private boolean parseExpr(int pos) {
        return true;
    }

    private boolean parseExpr(int pos, int precedence) {
        return true;
    }

    public boolean getIsValidProgram() {
        return this.tokens != null && this.error == null;
    }
}
