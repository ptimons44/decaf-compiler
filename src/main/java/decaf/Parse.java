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
        switch (token.getTokenType()) {
            case KEYWORD:
                String val = token.getVal();
                if (val.equals("class") || val.equals("void") || val.equals("int") || val.equals("boolean") || val.equals("char") || val.equals("long") || val.equals("if") || val.equals("while") || val.equals("return")) {
                    return SyntacticEnv.DECL;
                } else {
                    return SyntacticEnv.STMT;
                }
            case IDENTIFIER:
            case INTLITERAL:
            case STRINGLITERAL:
            case CHARLITERAL:
            case BOOLEANLITERAL:
            case LONGLITERAL:
                return SyntacticEnv.EXPR;
            case PUNCTUATION:
                return SyntacticEnv.STMT;
            default:
                return null;
        }
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
