package lang.grammars.decaf;

import lang.types.CFGNode;
import lang.types.CFGGraph;
import lang.types.LexicalToken;
import lang.types.LexicalToken.TokenType;

public class DecafCFGGraph extends CFGGraph {
    /*
     *
     * Decl CFGNodes - Organized top-down
     *
     */
    {
        setRoot(nt("PROGRAM")
            .rule("import", "IMPORT_DECL_1")

            .rule("int", "MEMBER_DECL_1")
            .rule("long", "MEMBER_DECL_1")
            .rule("bool", "MEMBER_DECL_1")

            .rule("void", "METHOD_DECL_1")

            .rule("EOF", "EOF")
            .build());

        // Import declaration hierarchy
        nt("IMPORT_DECL_0")
            .rule("import", "IMPORT_DECL_1")
            .rule("EOF", "EOF")
            .build();

        nt("IMPORT_DECL_1")
            .rule(LexicalToken.TokenType.IDENTIFIER, "IMPORT_DECL_2")
            .build();

        nt("IMPORT_DECL_2")
            .rule(";", "IMPORT_DECL_0") // unlimited import declarations permissible
            .build();
        
        // Member (field, array_field, or method) declaration hierarchy
        nt("MEMBER_DECL_0")
            .rule("int", "MEMBER_DECL_1")
            .rule("long", "MEMBER_DECL_1")
            .rule("bool", "MEMBER_DECL_1")
            .rule("void", "METHOD_DECL_1")
            .rule("EOF", "EOF")
            .build();

        nt("MEMBER_DECL_1")
            .rule(TokenType.IDENTIFIER, "MEMBER_DECL_2")
            .build();
        
        nt("MEMBER_DECL_2")
            .rule("(", "METHOD_DECL_1")
            .rule("[", "ARRAY_FIELD_DECL_1")
            .rule(",", "MEMBER_DECL_3") // allow repeated field and array decl's
            .rule(";", "MEMBER_DECL_0")
            .build();

        nt("MEMBER_DECL_3")
            .rule(TokenType.IDENTIFIER, "MEMBER_DECL_5")
            .build();
        
        nt("MEMBER_DECL_4")
            .rule(",", "MEMBER_DECL_3")
            .rule("[", "ARRAY_FIELD_DECL_1")
            .rule(";", "MEMBER_DECL_0")
            .build();

        nt("ARRAY_FIELD_DECL_1")
            .rule(TokenType.INTLITERAL, "ARRAY_FIELD_DECL_2")
            .build();
        
        nt("ARRAY_FIELD_DECL_2")
            .rule("]", "ARRAY_FIELD_DECL_3")
            .build();
        
        nt("ARRAY_FIELD_DECL_3")
            .rule(",", "MEMBER_DECL_3")
            .rule(";", "MEMBER_DECL_0")
            .build();

        // Method declaration hierarchy
        nt("METHOD_DECL_0")
            .rule("int", "METHOD_DECL_1")
            .rule("long", "METHOD_DECL_1")
            .rule("bool", "METHOD_DECL_1")
            .rule("void", "METHOD_DECL_1")
            .rule("EOF", "EOF")
            .build();

        nt("METHOD_DECL_1")
            .rule(LexicalToken.TokenType.IDENTIFIER, "METHOD_DECL_2")
            .build();

        nt("METHOD_DECL_2")
            .rule("(", "METHOD_ARG_1")
            .build();

        nt("METHOD_ARG_1")  
            .rule(")", "BLOCK_1")
            .build();

        nt("METHOD_ARG_2")
            .rule("int", "METHOD_ARG_3")
            .rule("long", "METHOD_ARG_3")
            .rule("bool", "METHOD_ARG_3")
            .build();
        
        nt("METHOD_ARG_3")
            .rule(TokenType.IDENTIFIER, "METHOD_ARG_4")
            .build();
        
        nt("METHOD_ARG_4")
            .rule(",", "METHOD_ARG_1")
            .rule(")", "BLOCK_1")
            .build();

        nt("METHOD_DECL_3")
            .rule(")", "BLOCK_1")
            .build();

        nt("BLOCK_1")
            .rule("{", "BLOCK_2")
            .build();
        
        nt("BLOCK_2")
            // TODO
            .build();
        
        nt("BLOCK_FIELD_DECL_1")
            // TODO
            .build();
        
        nt("BLOCK_METHOD_DECL_1")
            // TODO
            .build();

        nt("BLOCK_N")
            .rule("}", "METHOD_DECL_0")
            .build();

        t("EOF");
    }

}
