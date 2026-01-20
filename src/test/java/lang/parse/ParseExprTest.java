package lang.parse;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import lang.Parse;
import lang.ParseException;
import lang.Parse.ParseResult;
import lang.grammars.decaf.DecafCFGGraph;
import lang.types.ASTBase;
import lang.types.ASTExpr;
import lang.types.LexicalToken;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.stream.Stream;

public class ParseExprTest extends ParseBaseTest {

    // Helper method to handle ParseException in structural tests
    private ParseResult parseExprSafely(Parse parser, int startPos) {
        try {
            return parser.parseExpr(startPos);
        } catch (ParseException e) {
            fail("Unexpected ParseException: " + e.getMessage());
            return null; // unreachable but helps with flow analysis
        }
    }

    /*
     * 1. Structural AST-shape tests (exact tree assertions)
     */
    
    @Test
    public void testSubAdd() {
        List<LexicalToken> tokens = List.of(
            id("a"),
            op("-"),
            id("b"),
            op("+"),
            id("c"),
            punct(";")
        );

        Parse parser = new Parse(tokens, new DecafCFGGraph());
        ParseResult result = parseExprSafely(parser, 0);
        
        // Using the builder pattern for cleaner, more readable test construction
        ASTExpr expectedAST = ASTExpr.add()
            .left(ASTExpr.subtract()
                .left("a")
                .right("b")
                .build())
            .right("c")
            .build();
        ParseResult expectedResult = new ParseResult(expectedAST, tokens.size() - 1);

        assertEquals(
            expectedResult,
            result,
            () -> """
                AST mismatch:
                Expected:
                %s
                Expected nextPos: %d
                Actual:
                %s
                Actual nextPos: %d
                """.formatted(
                    expectedAST.prettyPrint(),
                    expectedResult.nextPos,
                    result.tree.prettyPrint(),
                    result.nextPos
                )
        );

    }

    @Test
    public void testSubSub() {
        List<LexicalToken> tokens = List.of(
            id("a"),
            op("-"),
            id("b"),
            op("-"),
            id("c"),
            punct(";")
        );

        Parse parser = new Parse(tokens, new DecafCFGGraph());
        ParseResult result = parseExprSafely(parser, 0);
        
        ASTExpr expectedAST = ASTExpr.subtract()
            .left(ASTExpr.subtract()
                .left("a")
                .right("b")
                .build())
            .right("c")
            .build();
        ParseResult expectedResult = new ParseResult(expectedAST, tokens.size() - 1);

        assertEquals(
            expectedResult,
            result,
            () -> """
                AST mismatch:
                Expected:
                %s
                Expected nextPos: %d
                Actual:
                %s
                Actual nextPos: %d
                """.formatted(
                    expectedAST.prettyPrint(),
                    expectedResult.nextPos,
                    result.tree.prettyPrint(),
                    result.nextPos
                )
        );

    }

    @Test
    public void testDivMul() {
        List<LexicalToken> tokens = List.of(
            id("a"),
            op("/"),
            id("b"),
            op("*"),
            id("c"),
            punct(";")
        );

        Parse parser = new Parse(tokens, new DecafCFGGraph());
        ParseResult result = parseExprSafely(parser, 0);
        
        ASTExpr expectedAST = ASTExpr.multiply()
            .left(ASTExpr.divide()
                .left("a")
                .right("b")
                .build())
            .right("c")
            .build();
        ParseResult expectedResult = new ParseResult(expectedAST, tokens.size() - 1);
        
        assertEquals(
            expectedResult,
            result,
            () -> """
                AST mismatch:
                Expected:
                %s
                Expected nextPos: %d
                Actual:
                %s
                Actual nextPos: %d
                """.formatted(
                    expectedAST.prettyPrint(),
                    expectedResult.nextPos,
                    result.tree.prettyPrint(),
                    result.nextPos
                )
        );
    }

    @Test
    public void testAddMul() {
        List<LexicalToken> tokens = List.of(
            id("a"),
            op("+"),
            id("b"),
            op("*"),
            id("c"),
            punct(";")
        );

        Parse parser = new Parse(tokens, new DecafCFGGraph());
        ParseResult result = parseExprSafely(parser, 0);

        // Using the builder pattern for cleaner, more readable test construction
        ASTExpr expectedAST = ASTExpr.add()
            .left("a")
            .right(ASTExpr.multiply()
                .left("b")
                .right("c")
                .build())
            .build();
        ParseResult expectedResult = new ParseResult(expectedAST, tokens.size() - 1);

        assertEquals(
            expectedResult,
            result,
            () -> """

                Expected nextPos: %d
                Actual nextPos: %d

                AST mismatch:
                Expected:
                %s
                Actual:
                %s
                
                """.formatted(
                    expectedResult.nextPos,
                    result.nextPos,
                    expectedAST.prettyPrint(),
                    result.tree.prettyPrint()
                )
        );
    }

    @Test
    public void testMulAdd() {
        List<LexicalToken> tokens = List.of(
            id("a"),
            op("*"),
            id("b"),
            op("+"),
            id("c"),
            punct(";")
        );

        Parse parser = new Parse(tokens, new DecafCFGGraph());
        ParseResult result = parseExprSafely(parser, 0);

        // Using the builder pattern for cleaner, more readable test construction
        ASTExpr expectedAST = ASTExpr.add()
            .left(ASTExpr.multiply()
                .left("a")
                .right("b")
                .build())
            .right("c")
            .build();

        ParseResult expectedResult = new ParseResult(expectedAST, tokens.size() - 1);

        assertEquals(
            expectedResult,
            result,
            () -> """
            
                Expected nextPos: %d
                Actual nextPos: %d

                AST mismatch:
                Expected:
                %s
                Actual:
                %s
                
                """.formatted(
                    expectedResult.nextPos,
                    result.nextPos,
                    expectedAST.prettyPrint(),
                    result.tree.prettyPrint()
                )
        );
    }

    @Test
    public void testAddMulSubDiv() {
        List<LexicalToken> tokens = List.of(
            id("a"),
            op("+"),
            id("b"),
            op("*"),
            id("c"),
            op("-"),
            id("d"),
            op("/"),
            id("e"),
            punct(";")
        );

        Parse parser = new Parse(tokens, new DecafCFGGraph());
        ParseResult result = parseExprSafely(parser, 0);

        // Using the builder pattern for cleaner, more readable test construction
        ASTExpr expectedAST = ASTExpr.subtract()
            .left(ASTExpr.add()
                .left("a")
                .right(ASTExpr.multiply()
                    .left("b")
                    .right("c")
                    .build())
                .build())
            .right(ASTExpr.divide()
                .left("d")
                .right("e")
                .build())
            .build();
        ParseResult expectedResult = new ParseResult(expectedAST, tokens.size() - 1);

        assertEquals(
            expectedResult,
            result,
            () -> """

                Expected nextPos: %d
                Actual nextPos: %d

                AST mismatch:
                Expected:
                %s
                Actual:
                %s
                
                """.formatted(
                    expectedResult.nextPos,
                    result.nextPos,
                    expectedAST.prettyPrint(),
                    result.tree.prettyPrint()
                )
        );
    }

    @Test
    public void testSimpleParentheses() {
        List<LexicalToken> tokens = List.of(
            punct("("),
            id("a"),
            punct(")"),
            punct(";")
        );

        Parse parser = new Parse(tokens, new DecafCFGGraph());
        ParseResult result = parseExprSafely(parser, 0);
        
        ASTBase expectedAST = ASTExpr.leaf("a");
        ParseResult expectedResult = new ParseResult(expectedAST, tokens.size() - 1);

        assertEquals(
            expectedResult,
            result,
            () -> """
                AST mismatch:
                Expected:
                %s
                Expected nextPos: %d
                Actual:
                %s
                Actual nextPos: %d
                """.formatted(
                    expectedAST.prettyPrint(),
                    expectedResult.nextPos,
                    result.tree.prettyPrint(),
                    result.nextPos
                )
        );
    }

    @Test
    public void testParenthesesChangePrecedence() {
        List<LexicalToken> tokens = List.of(
            punct("("),
            id("a"),
            op("+"),
            id("b"),
            punct(")"),
            op("*"),
            id("c"),
            punct(";")
        );

        Parse parser = new Parse(tokens, new DecafCFGGraph());
        ParseResult result = parseExprSafely(parser, 0);
        
        ASTExpr expectedAST = ASTExpr.multiply()
            .left(ASTExpr.add()
                .left("a")
                .right("b")
                .build())
            .right("c")
            .build();
        ParseResult expectedResult = new ParseResult(expectedAST, tokens.size() - 1);

        assertEquals(
            expectedResult,
            result,
            () -> """
                AST mismatch:
                Expected:
                %s
                Expected nextPos: %d
                Actual:
                %s
                Actual nextPos: %d
                """.formatted(
                    expectedAST.prettyPrint(),
                    expectedResult.nextPos,
                    result.tree.prettyPrint(),
                    result.nextPos
                )
        );
    }

    @Test
    public void testNestedParentheses() {
        List<LexicalToken> tokens = List.of(
            punct("("),
            punct("("),
            id("a"),
            op("+"),
            id("b"),
            punct(")"),
            punct(")"),
            punct(";")
        );

        Parse parser = new Parse(tokens, new DecafCFGGraph());
        ParseResult result = parseExprSafely(parser, 0);
        
        ASTExpr expectedAST = ASTExpr.add()
            .left("a")
            .right("b")
            .build();
        ParseResult expectedResult = new ParseResult(expectedAST, tokens.size() - 1);

        assertEquals(
            expectedResult,
            result,
            () -> """
                AST mismatch:
                Expected:
                %s
                Expected nextPos: %d
                Actual:
                %s
                Actual nextPos: %d
                """.formatted(
                    expectedAST.prettyPrint(),
                    expectedResult.nextPos,
                    result.tree.prettyPrint(),
                    result.nextPos
                )
        );
    }

    @Test
    public void testParenthesesWithMultipleOperators() {
        List<LexicalToken> tokens = List.of(
            id("a"),
            op("*"),
            punct("("),
            id("b"),
            op("+"),
            id("c"),
            punct(")"),
            op("/"),
            id("d"),
            punct(";")
        );

        Parse parser = new Parse(tokens, new DecafCFGGraph());
        ParseResult result = parseExprSafely(parser, 0);
        
        ASTExpr expectedAST = ASTExpr.divide()
            .left(ASTExpr.multiply()
                .left("a")
                .right(ASTExpr.add()
                    .left("b")
                    .right("c")
                    .build())
                .build())
            .right("d")
            .build();
        ParseResult expectedResult = new ParseResult(expectedAST, tokens.size() - 1);

        assertEquals(
            expectedResult,
            result,
            () -> """
                AST mismatch:
                Expected:
                %s
                Expected nextPos: %d
                Actual:
                %s
                Actual nextPos: %d
                """.formatted(
                    expectedAST.prettyPrint(),
                    expectedResult.nextPos,
                    result.tree.prettyPrint(),
                    result.nextPos
                )
        );
    }

    @Test
    public void testMethodCallPostfixMultipleArgs() {
        List<LexicalToken> tokens = List.of(
            id("a"),
            punct("("),
            id("b"),
            punct(","),
            id("c"),
            punct(","),
            id("d"),
            punct(")"),
            punct(";")
        );

        Parse parser = new Parse(tokens, new DecafCFGGraph());
        ParseResult result = parseExprSafely(parser, 0);
        
        ASTExpr expectedAST = ASTExpr.methodCall()
            .function("a")
            .argument("b")
            .argument("c")
            .argument("d")
            .build();
        ParseResult expectedResult = new ParseResult(expectedAST, tokens.size() - 1);

        assertEquals(
            expectedResult,
            result,
            () -> """
                AST mismatch:
                Expected:
                %s
                Expected nextPos: %d
                Actual:
                %s
                Actual nextPos: %d
                """.formatted(
                    expectedAST.prettyPrint(),
                    expectedResult.nextPos,
                    result.tree.prettyPrint(),
                    result.nextPos
                )
        );
    }

    @Test
    public void testMethodCallExprArgsSimple() {
        List<LexicalToken> tokens = List.of(
            id("fn"),
            punct("("),
            punct("("),
            id("a"),
            punct(")"),
            punct(")"),
            punct(";")
        );

        Parse parser = new Parse(tokens, new DecafCFGGraph());
        ParseResult result = parseExprSafely(parser, 0);
        
        ASTExpr expectedAST = ASTExpr.methodCall()
            .function("fn")
            .argument("a")
            .build();
        ParseResult expectedResult = new ParseResult(expectedAST, tokens.size() - 1);

        assertEquals(
            expectedResult,
            result,
            () -> """
                AST mismatch:
                Expected:
                %s
                Expected nextPos: %d
                Actual:
                %s
                Actual nextPos: %d
                """.formatted(
                    expectedAST.prettyPrint(),
                    expectedResult.nextPos,
                    result.tree.prettyPrint(),
                    result.nextPos
                )
        );
    }

    @Test
    public void testMethodCallPostfix() {
        List<LexicalToken> tokens = List.of(
            id("a"),
            punct("("),
            id("b"),
            punct(")"),
            punct(";")
        );

        Parse parser = new Parse(tokens, new DecafCFGGraph());
        ParseResult result = parseExprSafely(parser, 0);
        
        ASTExpr expectedAST = ASTExpr.methodCall()
            .function("a")
            .argument("b")
            .build();
        ParseResult expectedResult = new ParseResult(expectedAST, tokens.size() - 1);

        assertEquals(
            expectedResult,
            result,
            () -> """
                AST mismatch:
                Expected:
                %s
                Expected nextPos: %d
                Actual:
                %s
                Actual nextPos: %d
                """.formatted(
                    expectedAST.prettyPrint(),
                    expectedResult.nextPos,
                    result.tree.prettyPrint(),
                    result.nextPos
                )
        );
    }

    @Test
    public void testArrayIndexPostfix() {
        List<LexicalToken> tokens = List.of(
            id("a"),
            punct("["),
            id("b"),
            punct("]"),
            punct(";")
        );

        Parse parser = new Parse(tokens, new DecafCFGGraph());
        ParseResult result = parseExprSafely(parser, 0);
        
        ASTExpr expectedAST = ASTExpr.arrayAccess()
            .array("a")
            .index("b")
            .build();
        ParseResult expectedResult = new ParseResult(expectedAST, tokens.size() - 1);

        assertEquals(
            expectedResult,
            result,
            () -> """
                AST mismatch:
                Expected:
                %s
                Expected nextPos: %d
                Actual:
                %s
                Actual nextPos: %d
                """.formatted(
                    expectedAST.prettyPrint(),
                    expectedResult.nextPos,
                    result.tree.prettyPrint(),
                    result.nextPos
                )
        );
    }

    @Test
    public void testArrayIndexPostfixWithExpr() {
        List<LexicalToken> tokens = List.of(
            id("array"),
            punct("["),
            id("a"),
            op("*"),
            punct("("),
            id("b"),
            op("+"),
            id("c"),
            punct(")"),
            op("/"),
            id("d"),
            punct("]"),
            punct(";")
        );

        Parse parser = new Parse(tokens, new DecafCFGGraph());
        ParseResult result = parseExprSafely(parser, 0);
        
        ASTExpr expectedAST = ASTExpr.arrayAccess()
            .array("array")
            .index(
                ASTExpr.divide()
                    .left(ASTExpr.multiply()
                        .left("a")
                        .right(ASTExpr.add()
                            .left("b")
                            .right("c")
                            .build())
                        .build())
                    .right("d")
                    .build()
            )
            .build();
        ParseResult expectedResult = new ParseResult(expectedAST, tokens.size() - 1);

        assertEquals(
            expectedResult,
            result,
            () -> """
                AST mismatch:
                Expected:
                %s
                Expected nextPos: %d
                Actual:
                %s
                Actual nextPos: %d
                """.formatted(
                    expectedAST.prettyPrint(),
                    expectedResult.nextPos,
                    result.tree.prettyPrint(),
                    result.nextPos
                )
        );
    }

    @Test
    public void testArrayIndexPostfixRepeated() {
        List<LexicalToken> tokens = List.of(
            id("a"),
            punct("["),
            id("b"),
            punct("]"),
            punct("["),
            id("c"),
            punct("]"),
            punct("["),
            id("d"),
            punct("]"),
            punct(";")
        );

        Parse parser = new Parse(tokens, new DecafCFGGraph());
        ParseResult result = parseExprSafely(parser, 0);
        
        ASTExpr expectedAST = ASTExpr.arrayAccess()
            .array(
                ASTExpr.arrayAccess()
                    .array(
                        ASTExpr.arrayAccess()
                            .array("a")
                            .index("b")
                            .build()
                    )
                    .index("c")
                    .build()
            )
            .index("d")
            .build();
        ParseResult expectedResult = new ParseResult(expectedAST, tokens.size() - 1);

        assertEquals(
            expectedResult,
            result,
            () -> """
                AST mismatch:
                Expected:
                %s
                Expected nextPos: %d
                Actual:
                %s
                Actual nextPos: %d
                """.formatted(
                    expectedAST.prettyPrint(),
                    expectedResult.nextPos,
                    result.tree.prettyPrint(),
                    result.nextPos
                )
        );
    }

    /*
     * 2. Unary operator tests (exact tree assertions)
     *
     * Tests for prefix operators (!, -, ++, --) and postfix operators (++, --)
     * All tests are disabled because these operators are not yet implemented in the parser.
     */

    // Section 1: Basic Prefix Negation

    @Test
    public void testPrefixNegate() {
        List<LexicalToken> tokens = List.of(
            op("-"),
            intLit("5"),
            punct(";")
        );

        Parse parser = new Parse(tokens, new DecafCFGGraph());
        ParseResult result = parseExprSafely(parser, 0);

        ASTExpr expectedAST = ASTExpr.unaryPrefix("-")
            .operand(intLit("5"))
            .build();
        ParseResult expectedResult = new ParseResult(expectedAST, tokens.size() - 1);

        assertEquals(
            expectedResult,
            result,
            () -> """
                AST mismatch:
                Expected:
                %s
                Expected nextPos: %d
                Actual:
                %s
                Actual nextPos: %d
                """.formatted(
                    expectedAST.prettyPrint(),
                    expectedResult.nextPos,
                    result.tree.prettyPrint(),
                    result.nextPos
                )
        );
    }

    @Test
    public void testPrefixNegateIdentifier() {
        List<LexicalToken> tokens = List.of(
            op("-"),
            id("x"),
            punct(";")
        );

        Parse parser = new Parse(tokens, new DecafCFGGraph());
        ParseResult result = parseExprSafely(parser, 0);

        ASTExpr expectedAST = ASTExpr.unaryPrefix("-")
            .operand("x")
            .build();
        ParseResult expectedResult = new ParseResult(expectedAST, tokens.size() - 1);

        assertEquals(
            expectedResult,
            result,
            () -> """
                AST mismatch:
                Expected:
                %s
                Expected nextPos: %d
                Actual:
                %s
                Actual nextPos: %d
                """.formatted(
                    expectedAST.prettyPrint(),
                    expectedResult.nextPos,
                    result.tree.prettyPrint(),
                    result.nextPos
                )
        );
    }

    @Test
    public void testDoublePrefixNegate() {
        List<LexicalToken> tokens = List.of(
            op("-"),
            op("-"),
            id("x"),
            punct(";")
        );

        Parse parser = new Parse(tokens, new DecafCFGGraph());
        ParseResult result = parseExprSafely(parser, 0);

        ASTExpr expectedAST = ASTExpr.unaryPrefix("-")
            .operand(ASTExpr.unaryPrefix("-")
                .operand("x")
                .build())
            .build();
        ParseResult expectedResult = new ParseResult(expectedAST, tokens.size() - 1);

        assertEquals(
            expectedResult,
            result,
            () -> """
                AST mismatch:
                Expected:
                %s
                Expected nextPos: %d
                Actual:
                %s
                Actual nextPos: %d
                """.formatted(
                    expectedAST.prettyPrint(),
                    expectedResult.nextPos,
                    result.tree.prettyPrint(),
                    result.nextPos
                )
        );
    }

    @Test
    public void testTriplePrefixNegate() {
        List<LexicalToken> tokens = List.of(
            op("-"),
            op("-"),
            op("-"),
            intLit("5"),
            punct(";")
        );

        Parse parser = new Parse(tokens, new DecafCFGGraph());
        ParseResult result = parseExprSafely(parser, 0);

        ASTExpr expectedAST = ASTExpr.unaryPrefix("-")
            .operand(ASTExpr.unaryPrefix("-")
                .operand(ASTExpr.unaryPrefix("-")
                    .operand(intLit("5"))
                    .build())
                .build())
            .build();
        ParseResult expectedResult = new ParseResult(expectedAST, tokens.size() - 1);

        assertEquals(
            expectedResult,
            result,
            () -> """
                AST mismatch:
                Expected:
                %s
                Expected nextPos: %d
                Actual:
                %s
                Actual nextPos: %d
                """.formatted(
                    expectedAST.prettyPrint(),
                    expectedResult.nextPos,
                    result.tree.prettyPrint(),
                    result.nextPos
                )
        );
    }

    // Section 2: Prefix Logical NOT

    @Test
    public void testPrefixNot() {
        List<LexicalToken> tokens = List.of(
            op("!"),
            boolLit("true"),
            punct(";")
        );

        Parse parser = new Parse(tokens, new DecafCFGGraph());
        ParseResult result = parseExprSafely(parser, 0);

        ASTExpr expectedAST = ASTExpr.unaryPrefix("!")
            .operand(boolLit("true"))
            .build();
        ParseResult expectedResult = new ParseResult(expectedAST, tokens.size() - 1);

        assertEquals(
            expectedResult,
            result,
            () -> """
                AST mismatch:
                Expected:
                %s
                Expected nextPos: %d
                Actual:
                %s
                Actual nextPos: %d
                """.formatted(
                    expectedAST.prettyPrint(),
                    expectedResult.nextPos,
                    result.tree.prettyPrint(),
                    result.nextPos
                )
        );
    }

    @Test
    public void testPrefixNotIdentifier() {
        List<LexicalToken> tokens = List.of(
            op("!"),
            id("x"),
            punct(";")
        );

        Parse parser = new Parse(tokens, new DecafCFGGraph());
        ParseResult result = parseExprSafely(parser, 0);

        ASTExpr expectedAST = ASTExpr.unaryPrefix("!")
            .operand("x")
            .build();
        ParseResult expectedResult = new ParseResult(expectedAST, tokens.size() - 1);

        assertEquals(
            expectedResult,
            result,
            () -> """
                AST mismatch:
                Expected:
                %s
                Expected nextPos: %d
                Actual:
                %s
                Actual nextPos: %d
                """.formatted(
                    expectedAST.prettyPrint(),
                    expectedResult.nextPos,
                    result.tree.prettyPrint(),
                    result.nextPos
                )
        );
    }

    @Test
    public void testDoublePrefixNot() {
        List<LexicalToken> tokens = List.of(
            op("!"),
            op("!"),
            boolLit("true"),
            punct(";")
        );

        Parse parser = new Parse(tokens, new DecafCFGGraph());
        ParseResult result = parseExprSafely(parser, 0);

        ASTExpr expectedAST = ASTExpr.unaryPrefix("!")
            .operand(ASTExpr.unaryPrefix("!")
                .operand(boolLit("true"))
                .build())
            .build();
        ParseResult expectedResult = new ParseResult(expectedAST, tokens.size() - 1);

        assertEquals(
            expectedResult,
            result,
            () -> """
                AST mismatch:
                Expected:
                %s
                Expected nextPos: %d
                Actual:
                %s
                Actual nextPos: %d
                """.formatted(
                    expectedAST.prettyPrint(),
                    expectedResult.nextPos,
                    result.tree.prettyPrint(),
                    result.nextPos
                )
        );
    }

    @Test
    public void testPrefixNotWithParentheses() {
        List<LexicalToken> tokens = List.of(
            op("!"),
            punct("("),
            id("x"),
            punct(")"),
            punct(";")
        );

        Parse parser = new Parse(tokens, new DecafCFGGraph());
        ParseResult result = parseExprSafely(parser, 0);

        ASTExpr expectedAST = ASTExpr.unaryPrefix("!")
            .operand("x")
            .build();
        ParseResult expectedResult = new ParseResult(expectedAST, tokens.size() - 1);

        assertEquals(
            expectedResult,
            result,
            () -> """
                AST mismatch:
                Expected:
                %s
                Expected nextPos: %d
                Actual:
                %s
                Actual nextPos: %d
                """.formatted(
                    expectedAST.prettyPrint(),
                    expectedResult.nextPos,
                    result.tree.prettyPrint(),
                    result.nextPos
                )
        );
    }

    // Section 3: Prefix Increment/Decrement

    @Test
    public void testPrefixIncrement() {
        List<LexicalToken> tokens = List.of(
            op("++"),
            id("x"),
            punct(";")
        );

        Parse parser = new Parse(tokens, new DecafCFGGraph());
        ParseResult result = parseExprSafely(parser, 0);

        ASTExpr expectedAST = ASTExpr.unaryPrefix("++")
            .operand("x")
            .build();
        ParseResult expectedResult = new ParseResult(expectedAST, tokens.size() - 1);

        assertEquals(
            expectedResult,
            result,
            () -> """
                AST mismatch:
                Expected:
                %s
                Expected nextPos: %d
                Actual:
                %s
                Actual nextPos: %d
                """.formatted(
                    expectedAST.prettyPrint(),
                    expectedResult.nextPos,
                    result.tree.prettyPrint(),
                    result.nextPos
                )
        );
    }

    @Test
    public void testPrefixDecrement() {
        List<LexicalToken> tokens = List.of(
            op("--"),
            id("x"),
            punct(";")
        );

        Parse parser = new Parse(tokens, new DecafCFGGraph());
        ParseResult result = parseExprSafely(parser, 0);

        ASTExpr expectedAST = ASTExpr.unaryPrefix("--")
            .operand("x")
            .build();
        ParseResult expectedResult = new ParseResult(expectedAST, tokens.size() - 1);

        assertEquals(
            expectedResult,
            result,
            () -> """
                AST mismatch:
                Expected:
                %s
                Expected nextPos: %d
                Actual:
                %s
                Actual nextPos: %d
                """.formatted(
                    expectedAST.prettyPrint(),
                    expectedResult.nextPos,
                    result.tree.prettyPrint(),
                    result.nextPos
                )
        );
    }

    // Section 4: Postfix Increment/Decrement

    @Test
    public void testPostfixIncrement() {
        List<LexicalToken> tokens = List.of(
            id("x"),
            op("++"),
            punct(";")
        );

        Parse parser = new Parse(tokens, new DecafCFGGraph());
        ParseResult result = parseExprSafely(parser, 0);

        ASTExpr expectedAST = ASTExpr.unaryPostfix("++")
            .operand("x")
            .build();
        ParseResult expectedResult = new ParseResult(expectedAST, tokens.size() - 1);

        assertEquals(
            expectedResult,
            result,
            () -> """
                AST mismatch:
                Expected:
                %s
                Expected nextPos: %d
                Actual:
                %s
                Actual nextPos: %d
                """.formatted(
                    expectedAST.prettyPrint(),
                    expectedResult.nextPos,
                    result.tree.prettyPrint(),
                    result.nextPos
                )
        );
    }

    @Test
    public void testPostfixDecrement() {
        List<LexicalToken> tokens = List.of(
            id("x"),
            op("--"),
            punct(";")
        );

        Parse parser = new Parse(tokens, new DecafCFGGraph());
        ParseResult result = parseExprSafely(parser, 0);

        ASTExpr expectedAST = ASTExpr.unaryPostfix("--")
            .operand("x")
            .build();
        ParseResult expectedResult = new ParseResult(expectedAST, tokens.size() - 1);

        assertEquals(
            expectedResult,
            result,
            () -> """
                AST mismatch:
                Expected:
                %s
                Expected nextPos: %d
                Actual:
                %s
                Actual nextPos: %d
                """.formatted(
                    expectedAST.prettyPrint(),
                    expectedResult.nextPos,
                    result.tree.prettyPrint(),
                    result.nextPos
                )
        );
    }

    @Test
    public void testPostfixIncrementAfterLiteral() {
        List<LexicalToken> tokens = List.of(
            intLit("5"),
            op("++"),
            punct(";")
        );

        Parse parser = new Parse(tokens, new DecafCFGGraph());
        ParseResult result = parseExprSafely(parser, 0);

        ASTExpr expectedAST = ASTExpr.unaryPostfix("++")
            .operand(intLit("5"))
            .build();
        ParseResult expectedResult = new ParseResult(expectedAST, tokens.size() - 1);

        assertEquals(
            expectedResult,
            result,
            () -> """
                AST mismatch:
                Expected:
                %s
                Expected nextPos: %d
                Actual:
                %s
                Actual nextPos: %d
                """.formatted(
                    expectedAST.prettyPrint(),
                    expectedResult.nextPos,
                    result.tree.prettyPrint(),
                    result.nextPos
                )
        );
    }

    // Section 5: Mixed Unary Operators

    @Test
    public void testPrefixNotNegate() {
        List<LexicalToken> tokens = List.of(
            op("!"),
            op("-"),
            id("x"),
            punct(";")
        );

        Parse parser = new Parse(tokens, new DecafCFGGraph());
        ParseResult result = parseExprSafely(parser, 0);

        ASTExpr expectedAST = ASTExpr.unaryPrefix("!")
            .operand(ASTExpr.unaryPrefix("-")
                .operand("x")
                .build())
            .build();
        ParseResult expectedResult = new ParseResult(expectedAST, tokens.size() - 1);

        assertEquals(
            expectedResult,
            result,
            () -> """
                AST mismatch:
                Expected:
                %s
                Expected nextPos: %d
                Actual:
                %s
                Actual nextPos: %d
                """.formatted(
                    expectedAST.prettyPrint(),
                    expectedResult.nextPos,
                    result.tree.prettyPrint(),
                    result.nextPos
                )
        );
    }

    @Test
    public void testPrefixNegateNot() {
        List<LexicalToken> tokens = List.of(
            op("-"),
            op("!"),
            id("x"),
            punct(";")
        );

        Parse parser = new Parse(tokens, new DecafCFGGraph());
        ParseResult result = parseExprSafely(parser, 0);

        ASTExpr expectedAST = ASTExpr.unaryPrefix("-")
            .operand(ASTExpr.unaryPrefix("!")
                .operand("x")
                .build())
            .build();
        ParseResult expectedResult = new ParseResult(expectedAST, tokens.size() - 1);

        assertEquals(
            expectedResult,
            result,
            () -> """
                AST mismatch:
                Expected:
                %s
                Expected nextPos: %d
                Actual:
                %s
                Actual nextPos: %d
                """.formatted(
                    expectedAST.prettyPrint(),
                    expectedResult.nextPos,
                    result.tree.prettyPrint(),
                    result.nextPos
                )
        );
    }

    // Section 6: Precedence with Binary Operators

    @Test
    public void testNegateWithAddition() {
        List<LexicalToken> tokens = List.of(
            op("-"),
            id("a"),
            op("+"),
            id("b"),
            punct(";")
        );

        Parse parser = new Parse(tokens, new DecafCFGGraph());
        ParseResult result = parseExprSafely(parser, 0);

        ASTExpr expectedAST = ASTExpr.add()
            .left(ASTExpr.unaryPrefix("-")
                .operand("a")
                .build())
            .right("b")
            .build();
        ParseResult expectedResult = new ParseResult(expectedAST, tokens.size() - 1);

        assertEquals(
            expectedResult,
            result,
            () -> """
                AST mismatch:
                Expected:
                %s
                Expected nextPos: %d
                Actual:
                %s
                Actual nextPos: %d
                """.formatted(
                    expectedAST.prettyPrint(),
                    expectedResult.nextPos,
                    result.tree.prettyPrint(),
                    result.nextPos
                )
        );
    }

    @Test
    public void testAdditionThenNegate() {
        List<LexicalToken> tokens = List.of(
            id("a"),
            op("+"),
            op("-"),
            id("b"),
            punct(";")
        );

        Parse parser = new Parse(tokens, new DecafCFGGraph());
        ParseResult result = parseExprSafely(parser, 0);

        ASTExpr expectedAST = ASTExpr.add()
            .left("a")
            .right(ASTExpr.unaryPrefix("-")
                .operand("b")
                .build())
            .build();
        ParseResult expectedResult = new ParseResult(expectedAST, tokens.size() - 1);

        assertEquals(
            expectedResult,
            result,
            () -> """
                AST mismatch:
                Expected:
                %s
                Expected nextPos: %d
                Actual:
                %s
                Actual nextPos: %d
                """.formatted(
                    expectedAST.prettyPrint(),
                    expectedResult.nextPos,
                    result.tree.prettyPrint(),
                    result.nextPos
                )
        );
    }

    @Test
    public void testNegateWithMultiplication() {
        List<LexicalToken> tokens = List.of(
            op("-"),
            id("a"),
            op("*"),
            id("b"),
            punct(";")
        );

        Parse parser = new Parse(tokens, new DecafCFGGraph());
        ParseResult result = parseExprSafely(parser, 0);

        ASTExpr expectedAST = ASTExpr.multiply()
            .left(ASTExpr.unaryPrefix("-")
                .operand("a")
                .build())
            .right("b")
            .build();
        ParseResult expectedResult = new ParseResult(expectedAST, tokens.size() - 1);

        assertEquals(
            expectedResult,
            result,
            () -> """
                AST mismatch:
                Expected:
                %s
                Expected nextPos: %d
                Actual:
                %s
                Actual nextPos: %d
                """.formatted(
                    expectedAST.prettyPrint(),
                    expectedResult.nextPos,
                    result.tree.prettyPrint(),
                    result.nextPos
                )
        );
    }

    @Test
    public void testMultiplicationThenNegate() {
        List<LexicalToken> tokens = List.of(
            id("a"),
            op("*"),
            op("-"),
            id("b"),
            punct(";")
        );

        Parse parser = new Parse(tokens, new DecafCFGGraph());
        ParseResult result = parseExprSafely(parser, 0);

        ASTExpr expectedAST = ASTExpr.multiply()
            .left("a")
            .right(ASTExpr.unaryPrefix("-")
                .operand("b")
                .build())
            .build();
        ParseResult expectedResult = new ParseResult(expectedAST, tokens.size() - 1);

        assertEquals(
            expectedResult,
            result,
            () -> """
                AST mismatch:
                Expected:
                %s
                Expected nextPos: %d
                Actual:
                %s
                Actual nextPos: %d
                """.formatted(
                    expectedAST.prettyPrint(),
                    expectedResult.nextPos,
                    result.tree.prettyPrint(),
                    result.nextPos
                )
        );
    }

    @Test
    public void testNotWithAnd() {
        List<LexicalToken> tokens = List.of(
            op("!"),
            id("a"),
            op("&&"),
            id("b"),
            punct(";")
        );

        Parse parser = new Parse(tokens, new DecafCFGGraph());
        ParseResult result = parseExprSafely(parser, 0);

        ASTExpr expectedAST = ASTExpr.and()
            .left(ASTExpr.unaryPrefix("!")
                .operand("a")
                .build())
            .right("b")
            .build();
        ParseResult expectedResult = new ParseResult(expectedAST, tokens.size() - 1);

        assertEquals(
            expectedResult,
            result,
            () -> """
                AST mismatch:
                Expected:
                %s
                Expected nextPos: %d
                Actual:
                %s
                Actual nextPos: %d
                """.formatted(
                    expectedAST.prettyPrint(),
                    expectedResult.nextPos,
                    result.tree.prettyPrint(),
                    result.nextPos
                )
        );
    }

    @Test
    public void testNotWithOr() {
        List<LexicalToken> tokens = List.of(
            op("!"),
            id("a"),
            op("||"),
            id("b"),
            punct(";")
        );

        Parse parser = new Parse(tokens, new DecafCFGGraph());
        ParseResult result = parseExprSafely(parser, 0);

        ASTExpr expectedAST = ASTExpr.or()
            .left(ASTExpr.unaryPrefix("!")
                .operand("a")
                .build())
            .right("b")
            .build();
        ParseResult expectedResult = new ParseResult(expectedAST, tokens.size() - 1);

        assertEquals(
            expectedResult,
            result,
            () -> """
                AST mismatch:
                Expected:
                %s
                Expected nextPos: %d
                Actual:
                %s
                Actual nextPos: %d
                """.formatted(
                    expectedAST.prettyPrint(),
                    expectedResult.nextPos,
                    result.tree.prettyPrint(),
                    result.nextPos
                )
        );
    }

    @Test
    public void testComplexLogical() {
        List<LexicalToken> tokens = List.of(
            op("!"),
            id("a"),
            op("&&"),
            op("!"),
            id("b"),
            op("||"),
            id("c"),
            punct(";")
        );

        Parse parser = new Parse(tokens, new DecafCFGGraph());
        ParseResult result = parseExprSafely(parser, 0);

        ASTExpr expectedAST = ASTExpr.or()
            .left(ASTExpr.and()
                .left(ASTExpr.unaryPrefix("!")
                    .operand("a")
                    .build())
                .right(ASTExpr.unaryPrefix("!")
                    .operand("b")
                    .build())
                .build())
            .right("c")
            .build();
        ParseResult expectedResult = new ParseResult(expectedAST, tokens.size() - 1);

        assertEquals(
            expectedResult,
            result,
            () -> """
                AST mismatch:
                Expected:
                %s
                Expected nextPos: %d
                Actual:
                %s
                Actual nextPos: %d
                """.formatted(
                    expectedAST.prettyPrint(),
                    expectedResult.nextPos,
                    result.tree.prettyPrint(),
                    result.nextPos
                )
        );
    }

    // Section 7: Postfix with Binary Operators

    @Test
    public void testPostfixIncrementWithAddition() {
        List<LexicalToken> tokens = List.of(
            id("x"),
            op("++"),
            op("+"),
            id("y"),
            punct(";")
        );

        Parse parser = new Parse(tokens, new DecafCFGGraph());
        ParseResult result = parseExprSafely(parser, 0);

        ASTExpr expectedAST = ASTExpr.add()
            .left(ASTExpr.unaryPostfix("++")
                .operand("x")
                .build())
            .right("y")
            .build();
        ParseResult expectedResult = new ParseResult(expectedAST, tokens.size() - 1);

        assertEquals(
            expectedResult,
            result,
            () -> """
                AST mismatch:
                Expected:
                %s
                Expected nextPos: %d
                Actual:
                %s
                Actual nextPos: %d
                """.formatted(
                    expectedAST.prettyPrint(),
                    expectedResult.nextPos,
                    result.tree.prettyPrint(),
                    result.nextPos
                )
        );
    }

    @Test
    public void testAdditionThenPostfixIncrement() {
        List<LexicalToken> tokens = List.of(
            punct("("),
            id("a"),
            op("+"),
            id("b"),
            punct(")"),
            op("++"),
            punct(";")
        );

        Parse parser = new Parse(tokens, new DecafCFGGraph());
        ParseResult result = parseExprSafely(parser, 0);

        ASTExpr expectedAST = ASTExpr.unaryPostfix("++")
            .operand(ASTExpr.add()
                .left("a")
                .right("b")
                .build())
            .build();
        ParseResult expectedResult = new ParseResult(expectedAST, tokens.size() - 1);

        assertEquals(
            expectedResult,
            result,
            () -> """
                AST mismatch:
                Expected:
                %s
                Expected nextPos: %d
                Actual:
                %s
                Actual nextPos: %d
                """.formatted(
                    expectedAST.prettyPrint(),
                    expectedResult.nextPos,
                    result.tree.prettyPrint(),
                    result.nextPos
                )
        );
    }

    // Section 8: Postfix with Other Postfix Operators

    @Test
    public void testArrayAccessPostfixIncrement() {
        List<LexicalToken> tokens = List.of(
            id("arr"),
            punct("["),
            id("i"),
            punct("]"),
            op("++"),
            punct(";")
        );

        Parse parser = new Parse(tokens, new DecafCFGGraph());
        ParseResult result = parseExprSafely(parser, 0);

        ASTExpr expectedAST = ASTExpr.unaryPostfix("++")
            .operand(ASTExpr.arrayAccess()
                .array("arr")
                .index("i")
                .build())
            .build();
        ParseResult expectedResult = new ParseResult(expectedAST, tokens.size() - 1);

        assertEquals(
            expectedResult,
            result,
            () -> """
                AST mismatch:
                Expected:
                %s
                Expected nextPos: %d
                Actual:
                %s
                Actual nextPos: %d
                """.formatted(
                    expectedAST.prettyPrint(),
                    expectedResult.nextPos,
                    result.tree.prettyPrint(),
                    result.nextPos
                )
        );
    }

    @Test
    public void testMethodCallPostfixIncrement() {
        List<LexicalToken> tokens = List.of(
            id("foo"),
            punct("("),
            punct(")"),
            op("++"),
            punct(";")
        );

        Parse parser = new Parse(tokens, new DecafCFGGraph());
        ParseResult result = parseExprSafely(parser, 0);

        ASTExpr expectedAST = ASTExpr.unaryPostfix("++")
            .operand(ASTExpr.methodCall()
                .function("foo")
                .build())
            .build();
        ParseResult expectedResult = new ParseResult(expectedAST, tokens.size() - 1);

        assertEquals(
            expectedResult,
            result,
            () -> """
                AST mismatch:
                Expected:
                %s
                Expected nextPos: %d
                Actual:
                %s
                Actual nextPos: %d
                """.formatted(
                    expectedAST.prettyPrint(),
                    expectedResult.nextPos,
                    result.tree.prettyPrint(),
                    result.nextPos
                )
        );
    }

    /*
     * 3. Validity / acceptance tests (should parse)
     */

    @ParameterizedTest
    @MethodSource("happyPathProvider")
    public void testValidExpression(List<LexicalToken> tokens) {
        assertNotNull(tokens);
        Parse parser = new Parse(tokens, new DecafCFGGraph());
        
        // For valid expressions, we should not get a ParseException
        assertDoesNotThrow(() -> parser.parseExpr(0), 
            "Valid expression should not throw ParseException");
    }

    /*
     * 3. Invalidity / rejection tests (should fail)
     */
    @ParameterizedTest
    @MethodSource("sadPathProvider")
    public void testInvalidExpression(List<LexicalToken> tokens) {
        assertNotNull(tokens);
        Parse parser = new Parse(tokens, new DecafCFGGraph());

        // For invalid expressions, we expect either a ParseException or incomplete token consumption
        boolean testPassed = false;
        ParseResult result;
        try {
            result = parser.parseExpr(0);
            // If we get here, no exception was thrown
            // Test passes if parser didn't consume all tokens
            testPassed = (result.nextPos < tokens.size());
        } catch (ParseException e) {
            // Exception thrown - test passes
            testPassed = true;
        }

        assertTrue(testPassed,
            "Invalid expression should either throw ParseException or not consume all tokens");
    }

    static Stream<List<LexicalToken>> happyPathProvider() {
        return Stream.of(
            // Simple identifier
            List.of(
                id("x")
            ),
            // Integer literal
            List.of(
                intLit("42")
            ),
            // Boolean literal
            List.of(
                boolLit("true")
            ),
            // Binary arithmetic: x + 5
            List.of(
                id("x"),
                op("+"),
                intLit("5")
            ),
            // Binary comparison: a < b
            List.of(
                id("a"),
                op("<"),
                id("b")
            ),
            // Operator precedence: a + b * c (should parse as a + (b * c))
            List.of(
                id("a"),
                op("+"),
                id("b"),
                op("*"),
                id("c")
            ),
            // Operator precedence: a * b + c (should parse as (a * b) + c)
            List.of(
                id("a"),
                op("*"),
                id("b"),
                op("+"),
                id("c")
            ),
            // Division precedence: a / b + c (should parse as (a / b) + c)
            List.of(
                id("a"),
                op("/"),
                id("b"),
                op("+"),
                id("c")
            ),
            // Subtraction precedence: a - b * c (should parse as a - (b * c))
            List.of(
                id("a"),
                op("-"),
                id("b"),
                op("*"),
                id("c")
            ),
            // Mixed operators: a + b - c * d / e
            List.of(
                id("a"),
                op("+"),
                id("b"),
                op("-"),
                id("c"),
                op("*"),
                id("d"),
                op("/"),
                id("e")
            ),
            // Left associativity: a - b - c (should parse as (a - b) - c)
            List.of(
                id("a"),
                op("-"),
                id("b"),
                op("-"),
                id("c")
            ),
            // Left associativity: a / b / c (should parse as (a / b) / c)
            List.of(
                id("a"),
                op("/"),
                id("b"),
                op("/"),
                id("c")
            ),
            // Same precedence: a * b / c (should parse as (a * b) / c)
            List.of(
                id("a"),
                op("*"),
                id("b"),
                op("/"),
                id("c")
            ),
            // Parenthesized expression: (x + y)
            List.of(
                punct("("),
                id("x"),
                op("+"),
                id("y"),
                punct(")")
            ),
            // Nested parentheses: ((x + y))
            List.of(
                punct("("),
                punct("("),
                id("x"),
                op("+"),
                id("y"),
                punct(")"),
                punct(")")
            ),
            // Complex nested: (x + (y * z))
            List.of(
                punct("("),
                id("x"),
                op("+"),
                punct("("),
                id("y"),
                op("*"),
                id("z"),
                punct(")"),
                punct(")")
            ),
            // Multiple levels: ((a + b) * (c - d))
            List.of(
                punct("("),
                punct("("),
                id("a"),
                op("+"),
                id("b"),
                punct(")"),
                op("*"),
                punct("("),
                id("c"),
                op("-"),
                id("d"),
                punct(")"),
                punct(")")
            ),
            // Deep nesting: (((x)))
            List.of(
                punct("("),
                punct("("),
                punct("("),
                id("x"),
                punct(")"),
                punct(")"),
                punct(")")
            ),
            // Parentheses with method call: (foo() + bar())
            List.of(
                punct("("),
                id("foo"),
                punct("("),
                punct(")"),
                op("+"),
                id("bar"),
                punct("("),
                punct(")"),
                punct(")")
            ),
            // Array access: arr[i]
            List.of(
                id("arr"),
                punct("["),
                id("i"),
                punct("]")
            ),
            // Method call: foo()
            List.of(
                id("foo"),
                punct("("),
                punct(")")
            ),
            // Method call with argument: bar(x)
            List.of(
                id("bar"),
                punct("("),
                id("x"),
                punct(")")
            ),
            // Prefix negation with literal: -5
            List.of(
                op("-"),
                intLit("5")
            ),
            // Prefix negation with identifier: -x
            List.of(
                op("-"),
                id("x")
            ),
            // Prefix NOT with boolean: !true
            List.of(
                op("!"),
                boolLit("true")
            ),
            // Prefix NOT with identifier: !x
            List.of(
                op("!"),
                id("x")
            ),
            // Prefix increment: ++x
            List.of(
                op("++"),
                id("x")
            ),
            // Prefix decrement: --x
            List.of(
                op("--"),
                id("x")
            ),
            // Postfix increment: x++
            List.of(
                id("x"),
                op("++")
            ),
            // Postfix decrement: x--
            List.of(
                id("x"),
                op("--")
            ),
            // Prefix negation with addition: -a + b
            List.of(
                op("-"),
                id("a"),
                op("+"),
                id("b")
            ),
            // Prefix NOT with AND: !a && b
            List.of(
                op("!"),
                id("a"),
                op("&&"),
                id("b")
            ),
            // Postfix increment with addition: x++ + y
            List.of(
                id("x"),
                op("++"),
                op("+"),
                id("y")
            ),
            // Array access with postfix increment: arr[i]++
            List.of(
                id("arr"),
                punct("["),
                id("i"),
                punct("]"),
                op("++")
            )
        );
    }

    static Stream<List<LexicalToken>> sadPathProvider() {
        return Stream.of(
            // Missing closing parenthesis
            List.of(
                punct("("),
                id("x")
            ),
            // Missing operand after PUNCTUATION
            List.of(
                id("x"),
                op("+")
            ),
            // Missing array index
            List.of(
                id("arr"),
                punct("["),
                punct("]")
            ),
            // Invalid PUNCTUATION sequence
            List.of(
                id("x"),
                op("+"),
                op("*"),
                intLit("5")
            ),
            // Missing closing bracket for array access
            List.of(
                id("arr"),
                punct("["),
                id("i")
            ),
            // Missing operand after NOT
            List.of(
                op("!")
            ),
            // Missing operand after prefix increment
            List.of(
                op("++")
            ),
            // Missing operand after prefix decrement
            List.of(
                op("--")
            ),
            // Invalid operator sequence: negation followed by binary operator
            List.of(
                op("-"),
                op("+")
            ),
            // Ambiguous sequence: x - - (could be x-- or x - -)
            List.of(
                id("x"),
                op("-"),
                op("-")
            ),
            // Invalid operator sequence: double prefix increment
            List.of(
                op("++"),
                op("++")
            )
        );
    }
}
