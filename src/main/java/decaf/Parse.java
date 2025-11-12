package decaf;

import decaf.types.LexicalToken;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.ArrayList;

public class Parse {
    private List<LexicalToken> tokens;
    private String error = null;
    private List<String> warnings = new ArrayList<>();

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
        int pos = 0;
        while (pos < tokens.size()) {
            lookahead = tokens.get(pos);
            SyntacticEnv syntacticEnv = getSyntacticEnv(lookahead);
            if (syntacticEnv == SyntacticEnv.DECL) {
                parseDecl(pos);
            } else if (syntacticEnv == SyntacticEnv.STMT) {
                parseStmt(pos);
            } else if (syntacticEnv == SyntacticEnv.EXPR) {
                parseExpr(pos, 0);
            } else {
                this.error = "Unknown syntactic environment for token: " + lookahead.toString();
                return;
            }

        }
    }

    private void parseDecl(int pos) {
    }

    private void parseStmt(int pos) {
    }

    private void parseExpr(int pos, int precedence) {
    }

    public boolean getIsValidProgram() {
        return this.tokens != null && this.error == null;
    }
}
