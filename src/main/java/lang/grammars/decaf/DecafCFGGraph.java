package lang.grammars.decaf;

import lang.types.CFGNode;

import java.util.List;

import lang.types.CFGGraph;
import lang.types.LexicalToken;
import lang.types.CFGNode.CFGNodeKind;
import lang.types.LexicalToken.TokenType;

public class DecafCFGGraph extends CFGGraph {
    /*
     *
     * Decl CFGNodes - Organized top-down
     *
     */
    {
        setRoot(
            nt("PROGRAM")
                .epsilon("IMPORT_LIST")
                .build()
        );

        // Import declaration hierarchy
        nt("IMPORT_LIST")
            .rule("import", "IMPORT_DECL_AFTER_IMPORT")
            .epsilon("MEMBER_LIST")
            .build();


        nt("IMPORT_DECL_AFTER_IMPORT")
            .rule(LexicalToken.TokenType.IDENTIFIER, "IMPORT_DECL_AFTER_ID")
            .build();

        nt("IMPORT_DECL_AFTER_ID")
            .rule(";", "IMPORT_LIST") // unlimited import declarations permissible
            .build();
        
        // New Member declaration hierarchy
        nt("MEMBER_LIST")
            .rule("void", "METHOD_DECL_AFTER_RETURN_TYPE")
            .rule("int",  "MEMBER_DECL_AFTER_TYPE")
            .rule("long", "MEMBER_DECL_AFTER_TYPE")
            .rule("bool", "MEMBER_DECL_AFTER_TYPE")
            .rule("EOF",  "EOF")
            .build();
        
        nt("MEMBER_DECL_AFTER_TYPE")
            .rule(TokenType.IDENTIFIER, "MEMBER_DECL_AFTER_ID")
            .build();

        nt("MEMBER_DECL_AFTER_ID")
            .rule("(", "PARAM_LIST")
            .rule("[", "ARRAY_DECL_REST")
            .rule(",", "MEMBER_DECL_AFTER_TYPE")
            .rule(";", "MEMBER_LIST")
            .build();
        
        nt("ARRAY_DECL_REST")
            .rule(TokenType.INTLITERAL, "ARRAY_DECL_AFTER_SIZE")
            .build();
        
        nt("ARRAY_DECL_AFTER_SIZE")
            .rule("]", "MEMBER_DECL_AFTER_ID")
            .build();

        nt("METHOD_DECL_AFTER_RETURN_TYPE")
            .rule(TokenType.IDENTIFIER, "METHOD_DECL_AFTER_ID")
            .build();
        
        nt("METHOD_DECL_AFTER_ID")
            .rule("(", "PARAM_LIST")
            .build();

        nt("PARAM_LIST")
            .rule(")", "METHOD_DECL_AFTER_PARAMS")
            .rule("int", "PARAM_LIST_AFTER_TYPE")
            .rule("long", "PARAM_LIST_AFTER_TYPE")
            .rule("bool", "PARAM_LIST_AFTER_TYPE")
            .build();

        nt("PARAM_LIST_AFTER_TYPE")
            .rule(TokenType.IDENTIFIER, "PARAM_LIST_AFTER_ID")
            .build();

        nt("PARAM_LIST_AFTER_ID")
            .rule(",", "PARAM_LIST_REQUIRED")
            .rule(")", "BLOCK")
            .build();
        
        nt("PARAM_LIST_REQUIRED")                                                                                                                            
            .rule("int", "PARAM_LIST_AFTER_TYPE")                                                                                                            
            .rule("long", "PARAM_LIST_AFTER_TYPE")                                                                                                           
            .rule("bool", "PARAM_LIST_AFTER_TYPE")                                                                                                           
            .build();

        // TODO
        nt("BLOCK")
            .kind(CFGNodeKind.FRAGMENT_ENTRY)
            .build();

        t("EOF");
    }

}
