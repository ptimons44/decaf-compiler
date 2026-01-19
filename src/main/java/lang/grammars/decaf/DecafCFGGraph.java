package lang.grammars.decaf;

import lang.types.CFGNode;

import java.util.List;

import lang.types.CFGGraph;
import lang.types.LexicalToken;
import lang.types.LexicalToken.TokenType;

public class DecafCFGGraph extends CFGGraph {
    /*
     *
     * Helper methods for common patterns
     *
     */
    CFGNode.CFGNodeBuilder typeDispatchBeforeTypeTransition(String prefix, String cont) {
        return nt(prefix)
            .rule("int",  cont + "_TYPE_INT")
            .rule("long", cont + "_TYPE_LONG")
            .rule("bool", cont + "_TYPE_BOOL");     
    }
    CFGNode.CFGNodeBuilder returnTypeDispatchBeforeTypeTransition(String prefix, String cont) {
        return typeDispatchBeforeTypeTransition(prefix, cont)
            .rule("void", cont + "_TYPE_VOID");
    }
    CFGNode.CFGNodeBuilder typeDispatchBeforeType(String prefix) {
        return typeDispatchBeforeTypeTransition(prefix, prefix);
    }
    CFGNode.CFGNodeBuilder returnTypeDispatchBeforeType(String prefix) {
        return returnTypeDispatchBeforeTypeTransition(prefix, prefix);
    }
    List<CFGNode.CFGNodeBuilder> typeDispatchAfterType(String prefix, String cont) {
        return List.of(
            nt(prefix + "_TYPE_INT").epsilon(cont),
            nt(prefix + "_TYPE_LONG").epsilon(cont),
            nt(prefix + "_TYPE_BOOL").epsilon(cont)
        );
    }
    List<CFGNode.CFGNodeBuilder> returnTypeDispatchAfterType(String prefix, String cont) {
        return List.of(
            nt(prefix + "_TYPE_INT").epsilon(cont),
            nt(prefix + "_TYPE_LONG").epsilon(cont),
            nt(prefix + "_TYPE_BOOL").epsilon(cont),
            nt(prefix + "_TYPE_VOID").epsilon(cont)
        );
    }
    List<CFGNode.CFGNodeBuilder> returnTypeDispatchAfterType(String prefix) {
        return returnTypeDispatchAfterType(prefix, prefix);
    }

    /*
     *
     * Decl CFGNodes - Organized top-down
     *
     */
    {
        setRoot(
            returnTypeDispatchBeforeTypeTransition("PROGRAM", "MEMBER_DECL")
            .rule("import", "IMPORT_DECL_AFTER_IMPORT")
            .rule("EOF", "EOF")
            .build()
        );

        // Import declaration hierarchy
        returnTypeDispatchBeforeTypeTransition("IMPORT_LIST", "MEMBER_DECL")
            .rule("import", "IMPORT_DECL_AFTER_IMPORT")
            .rule("EOF", "EOF")
            .build();

        nt("IMPORT_DECL_AFTER_IMPORT")
            .rule(LexicalToken.TokenType.IDENTIFIER, "IMPORT_DECL_AFTER_ID")
            .build();

        nt("IMPORT_DECL_AFTER_ID")
            .rule(";", "IMPORT_LIST") // unlimited import declarations permissible
            .build();
        
        // New Member declaration hierarchy
        returnTypeDispatchAfterType("MEMBER_LIST", "MEMBER_DECL").forEach(builder -> builder.build());
        returnTypeDispatchBeforeType("MEMBER_DECL").build();
        returnTypeDispatchAfterType("MEMBER_DECL", "MEMBER_DECL_AFTER_TYPE").forEach(builder -> builder.build());
        nt("MEMBER_DECL_AFTER_TYPE")
            .rule(TokenType.IDENTIFIER, "MEMBER_DECL_AFTER_ID")
            .build();
        nt("MEMBER_DECL_AFTER_ID")
            .rule("(", "METHOD_DECL_REST")
            .rule("[", "ARRAY_DECL_REST")
            .rule(",", "FIELD_LIST")
            .rule(";", "MEMBER_LIST")
            .build();

        typeDispatchBeforeType("PARAM_LIST")
            .rule(")", "AFTER_PARAMS")
            .build();
        typeDispatchAfterType("PARAM_LIST", "PARAM_LIST_AFTER_TYPE")
            .forEach(builder -> builder.build());
        nt("PARAM_LIST_AFTER_TYPE")
            .rule(TokenType.IDENTIFIER, "PARAM_LIST_AFTER_ID")
            .build();
        nt("PARAM_LIST_AFTER_ID")
            .rule(",", "PARAM_LIST")
            .rule(")", "AFTER_PARAMS")
            .build();

        t("EOF");
    }

}
