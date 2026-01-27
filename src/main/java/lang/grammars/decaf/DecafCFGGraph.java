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

        nt("METHOD_LIST")
            .rule("void", "METHOD_DECL_AFTER_RETURN_TYPE")
            .rule("int",  "METHOD_DECL_AFTER_RETURN_TYPE")
            .rule("long", "METHOD_DECL_AFTER_RETURN_TYPE")
            .rule("bool", "METHOD_DECL_AFTER_RETURN_TYPE")
            .rule("EOF",  "EOF")
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
            .rule(")", "METHOD_DECL_AFTER_PARAMS")
            .build();

        nt("PARAM_LIST_REQUIRED")
            .rule("int", "PARAM_LIST_AFTER_TYPE")
            .rule("long", "PARAM_LIST_AFTER_TYPE")
            .rule("bool", "PARAM_LIST_AFTER_TYPE")
            .build();

        nt("METHOD_DECL_AFTER_PARAMS")
            .successor("METHOD_LIST")
            .epsilon("BLOCK")
            .build();

        /*
         *
         * Block and Statement CFGNodes
         *
         */

        // BLOCK is marked as FRAGMENT_ENTRY for recursive parsing
        nt("BLOCK")
            .kind(CFGNodeKind.FRAGMENT_ENTRY)
            .rule("{", "FIELD_DECL_LIST")
            .build();

        // Field declaration list - variable declarations must come before statements
        nt("FIELD_DECL_LIST")
            .rule("int", "VAR_DECL_AFTER_TYPE")
            .rule("long", "VAR_DECL_AFTER_TYPE")
            .rule("bool", "VAR_DECL_AFTER_TYPE")
            .epsilon("STATEMENT_LIST")  // Transition to statements after all declarations
            .build();

        // Statement list - can be empty or contain statements (no variable declarations)
        nt("STATEMENT_LIST")
            .rule("}", "BLOCK_END")                // Check for end of block (epsilon - don't consume yet)
            .rule("if", "IF_STATEMENT")
            .rule("while", "WHILE_STATEMENT")
            .rule("for", "FOR_STATEMENT")
            .rule("return", "RETURN_STATEMENT")
            .rule("break", "BREAK_STATEMENT")
            .rule("continue", "CONTINUE_STATEMENT")
            .rule(TokenType.IDENTIFIER, "IDENTIFIER_STATEMENT")
            .epsilon("BLOCK") // Nested Block
            .build();

        t("BLOCK_END");


        /*
         * Variable declarations inside blocks
         */
        nt("VAR_DECL_AFTER_TYPE")
            .rule(TokenType.IDENTIFIER, "VAR_DECL_AFTER_ID")
            .build();

        nt("VAR_DECL_AFTER_ID")
            .rule(";", "FIELD_DECL_LIST")          // int x;
            .rule("," ,"VAR_DECL_AFTER_TYPE")      // int x, y;
            .rule("[", "ARR_DECL")                // int x[10];
            .build();

        nt("ARR_DECL")
            .rule(TokenType.INTLITERAL, "ARR_DECL_AFTER_SIZE")
            .build();

        nt("ARR_DECL_AFTER_SIZE")
            .rule("]", "VAR_DECL_AFTER_ID")
            .build();

        /*
         * If statement
         */
        nt("IF_STATEMENT")
            .rule("(", "IF_CONDITION")
            .build();

        nt("IF_CONDITION")
            .successor("AFTER_IF_CONDITION_EXPR")
            .epsilon("EXPR")  // Parse condition expression
            .build();

        nt("AFTER_IF_CONDITION_EXPR")
            .successor("AFTER_IF_BLOCK")
            .rule(")", "BLOCK")  // Parse if body block
            .build();

        // After if block, check for else
        nt("AFTER_IF_BLOCK")
            .rule("else", "ELSE_CLAUSE")
            .epsilon("STATEMENT_LIST")  // No else, continue to next statement
            .build();

        nt("ELSE_CLAUSE")
            .successor("AFTER_ELSE_BLOCK")
            .rule("if", "IF_STATEMENT")  // else if (...)
            .epsilon("BLOCK")
            .build();

        nt("AFTER_ELSE_BLOCK")
            .epsilon("STATEMENT_LIST")
            .build();

        /*
         * While loop
         */
        nt("WHILE_STATEMENT")
            .rule("(", "WHILE_CONDITION")
            .build();

        nt("WHILE_CONDITION")
            .successor("AFTER_WHILE_CONDITION_EXPR")
            .epsilon("EXPR")  // Parse condition expression
            .build();

        nt("AFTER_WHILE_CONDITION_EXPR")
            .successor("AFTER_WHILE_BLOCK")
            .rule(")", "BLOCK")  // Parse while body block
            .build();

        nt("AFTER_WHILE_BLOCK")
            .epsilon("STATEMENT_LIST")
            .build();

        /*
         * For loop
         */
        nt("FOR_STATEMENT")
            .rule("(", "FOR_INIT")
            .build();

        nt("FOR_INIT")
            .rule(TokenType.IDENTIFIER, "FOR_INIT_ASSIGN")
            .rule(";", "FOR_CONDITION")  // Empty init
            .build();

        nt("FOR_INIT_ASSIGN")
            .rule("=", "FOR_INIT_EXPR")
            .build();

        nt("FOR_INIT_EXPR")
            .successor("AFTER_FOR_INIT_EXPR")
            .epsilon("EXPR")  // Parse init expression
            .build();

        nt("AFTER_FOR_INIT_EXPR")
            .rule(";", "FOR_CONDITION")
            .build();

        nt("FOR_CONDITION")
            .successor("AFTER_FOR_CONDITION_EXPR")
            .epsilon("EXPR")  // Parse condition expression (can be empty)
            .rule(";", "FOR_UPDATE")  // Empty condition
            .build();

        nt("AFTER_FOR_CONDITION_EXPR")
            .rule(";", "FOR_UPDATE")
            .build();

        nt("FOR_UPDATE")
            .successor("AFTER_FOR_BLOCK")
            .rule(TokenType.IDENTIFIER, "FOR_UPDATE_ASSIGN")
            .build();

        nt("FOR_UPDATE_ASSIGN")
            .rule("=", "FOR_UPDATE_EXPR")
            .rule("++", "AFTER_FOR_UPDATE_EXPR")
            .rule("--", "AFTER_FOR_UPDATE_EXPR")
            .rule("+=", "FOR_UPDATE_EXPR")
            .rule("-=", "FOR_UPDATE_EXPR")
            .rule("*=", "FOR_UPDATE_EXPR")
            .rule("/=", "FOR_UPDATE_EXPR")
            .rule("%=", "FOR_UPDATE_EXPR")
            .build();

        nt("FOR_UPDATE_EXPR")
            .successor("AFTER_FOR_UPDATE_EXPR")
            .epsilon("EXPR")  // Parse update expression
            .build();

        nt("AFTER_FOR_UPDATE_EXPR")
            .successor("AFTER_FOR_BLOCK")
            .rule(")", "BLOCK")  // Parse for body block
            .build();

        nt("AFTER_FOR_BLOCK")
            .epsilon("STATEMENT_LIST")
            .build();

        /*
         * Return statement
         */
        nt("RETURN_STATEMENT")
            .successor("AFTER_RETURN_EXPR")
            .rule(";", "STATEMENT_LIST")  // return;
            .epsilon("EXPR")              // return expr;
            .build();

        nt("AFTER_RETURN_EXPR")
            .rule(";", "STATEMENT_LIST")
            .build();

        /*
         * Break/Continue statements
         */
        nt("BREAK_STATEMENT")
            .rule(";", "STATEMENT_LIST")
            .build();

        nt("CONTINUE_STATEMENT")
            .rule(";", "STATEMENT_LIST")
            .build();

        /*
         * Identifier statement (assignment or method call)
         */
        nt("IDENTIFIER_STATEMENT")
            .rule("=", "ASSIGNMENT_EXPR")
            .rule("+=", "ASSIGNMENT_EXPR")
            .rule("-=", "ASSIGNMENT_EXPR")
            .rule("*=", "ASSIGNMENT_EXPR")
            .rule("/=", "ASSIGNMENT_EXPR")
            .rule("%=", "ASSIGNMENT_EXPR")
            .rule("[", "ARRAY_ASSIGN")
            .rule("(", "METHOD_CALL")
            .build();

        nt("ASSIGNMENT_EXPR")
            .successor("AFTER_ASSIGNMENT_EXPR")
            .epsilon("EXPR")  // Parse right-hand side expression
            .build();

        nt("AFTER_ASSIGNMENT_EXPR")
            .rule(";", "STATEMENT_LIST")
            .build();

        nt("ARRAY_ASSIGN")
            .successor("AFTER_ARRAY_INDEX_EXPR")
            .epsilon("EXPR")  // Parse array index
            .build();

        nt("AFTER_ARRAY_INDEX_EXPR")
            .rule("]", "ARRAY_ASSIGN_AFTER_INDEX")
            .build();

        nt("ARRAY_ASSIGN_AFTER_INDEX")
            .rule("=", "ARRAY_ASSIGN_VALUE")
            .build();

        nt("ARRAY_ASSIGN_VALUE")
            .successor("AFTER_ARRAY_ASSIGN_EXPR")
            .epsilon("EXPR")  // Parse value expression
            .build();

        nt("AFTER_ARRAY_ASSIGN_EXPR")
            .rule(";", "STATEMENT_LIST")
            .build();

        nt("METHOD_CALL")
            .rule(")", "METHOD_CALL_END")
            .epsilon("CALL_ARG_LIST")
            .build();

        nt("CALL_ARG_LIST")
            .rule(TokenType.STRINGLITERAL, "AFTER_CALL_ARG_EXPR")
            .successor("AFTER_CALL_ARG_EXPR")
            .epsilon("EXPR")  // Parse first argument
            .build();

        nt("AFTER_CALL_ARG_EXPR")
            .rule(",", "CALL_ARG_LIST")  // More arguments
            .rule(")", "METHOD_CALL_END")
            .build();

        nt("METHOD_CALL_END")
            .rule(";", "STATEMENT_LIST")
            .build();

        /*
         * Expression entry point - marked for recursive expression parsing
         */
        nt("EXPR")
            .kind(CFGNodeKind.EXPR_ENTRY)
            .epsilon("EXPR_END")
            .build();

        t("EXPR_END");

        t("EOF");
    }

}
