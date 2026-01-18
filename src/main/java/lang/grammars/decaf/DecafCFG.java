package lang.grammars.decaf;

import lang.types.CFGNode;
import lang.types.LexicalToken;
import lang.types.LexicalToken.TokenType;

public class DecafCFG {
    /*
     * 
     * Decl CFGNodes - Organized top-down
     * 
     */
    static final CFGNode PROGRAM;
    static {
        // keep pointer to PROGRAM to avoid GC
        PROGRAM = CFGNode.nt("PROGRAM")
            .rule("import", "IMPORT_DECL_1")
            
            .rule("int", "MEMBER_DECL_1")
            .rule("long", "MEMBER_DECL_1")
            .rule("bool", "MEMBER_DECL_1")

            .rule("void", "METHOD_DECL_1")

            .rule("EOF", "EOF")
            .build();
            
        // Import declaration hierarchy
        CFGNode.nt("IMPORT_DECL_0")
            .rule("import", "IMPORT_DECL_1")
            .rule("EOF", "EOF")
            .build();

        CFGNode.nt("IMPORT_DECL_1")
            .rule(LexicalToken.TokenType.IDENTIFIER, "IMPORT_DECL_2")
            .build();

        CFGNode.nt("IMPORT_DECL_2")
            .rule(";", "IMPORT_DECL_0") // unlimited import declarations permissible
            .build();
        
        // Member (field, array_field, or method) declaration hierarchy
        CFGNode.nt("MEMBER_DECL_0")
            .rule("int", "MEMBER_DECL_1")
            .rule("long", "MEMBER_DECL_1")
            .rule("bool", "MEMBER_DECL_1")
            .rule("void", "METHOD_DECL_1")
            .rule("EOF", "EOF")
            .build();

        CFGNode.nt("MEMBER_DECL_1")
            .rule(TokenType.IDENTIFIER, "MEMBER_DECL_2")
            .build();
        
        CFGNode.nt("MEMBER_DECL_2")
            .rule("(", "METHOD_DECL_1")
            .rule("[", "ARRAY_FIELD_DECL_1")
            .rule(",", "MEMBER_DECL_3") // allow repeated field and array decl's
            .rule(";", "MEMBER_DECL_0")
            .build();

        CFGNode.nt("MEMBER_DECL_3")
            .rule(TokenType.IDENTIFIER, "MEMBER_DECL_5")
            .build();
        
        CFGNode.nt("MEMBER_DECL_4")
            .rule(",", "MEMBER_DECL_3")
            .rule("[", "ARRAY_FIELD_DECL_1")
            .rule(";", "MEMBER_DECL_0")
            .build();

        CFGNode.nt("ARRAY_FIELD_DECL_1")
            .rule(TokenType.INTLITERAL, "ARRAY_FIELD_DECL_2")
            .build();
        
        CFGNode.nt("ARRAY_FIELD_DECL_2")
            .rule("]", "ARRAY_FIELD_DECL_3")
            .build();
        
        CFGNode.nt("ARRAY_FIELD_DECL_3")
            .rule(",", "MEMBER_DECL_3")
            .rule(";", "MEMBER_DECL_0")
            .build();

        // Method declaration hierarchy
        CFGNode.nt("METHOD_DECL_0")
            .rule("int", "METHOD_DECL_1")
            .rule("long", "METHOD_DECL_1")
            .rule("bool", "METHOD_DECL_1")
            .rule("void", "METHOD_DECL_1")
            .rule("EOF", "EOF")
            .build();

        CFGNode.nt("METHOD_DECL_1")
            .rule(LexicalToken.TokenType.IDENTIFIER, "METHOD_DECL_2")
            .build();

        CFGNode.nt("METHOD_DECL_2")
            .rule("(", "METHOD_ARG_1")
            .build();

        CFGNode.nt("METHOD_ARG_1")  
            .rule(")", "BLOCK_1")
            .build();

        CFGNode.nt("METHOD_ARG_2")
            .rule("int", "METHOD_ARG_3")
            .rule("long", "METHOD_ARG_3")
            .rule("bool", "METHOD_ARG_3")
            .build();
        
        CFGNode.nt("METHOD_ARG_3")
            .rule(TokenType.IDENTIFIER, "METHOD_ARG_4")
            .build();
        
        CFGNode.nt("METHOD_ARG_4")
            .rule(",", "METHOD_ARG_1")
            .rule(")", "BLOCK_1")
            .build();

        CFGNode.nt("METHOD_DECL_3")
            .rule(")", "BLOCK_1")
            .build();

        CFGNode.nt("BLOCK_1")
            .rule("{", "BLOCK_2")
            .build();
        
        CFGNode.nt("BLOCK_2")
            // TODO
            .build();
        
        CFGNode.nt("BLOCK_FIELD_DECL_1")
            // TODO
            .build();
        
        CFGNode.nt("BLOCK_METHOD_DECL_1")
            // TODO
            .build();

        CFGNode.nt("BLOCK_N")
            .rule("}", "METHOD_DECL_0")
            .build();

        CFGNode.t("EOF");
}
}
