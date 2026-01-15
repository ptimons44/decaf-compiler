package decaf.parse;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import decaf.Parse;
import decaf.Parse.ParseResult;
import decaf.types.ASTBase;
import decaf.types.ASTExpr;
import decaf.types.LexicalToken;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.stream.Stream;

public class ParseExprTest extends ParseBaseTest {

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

        Parse parser = new Parse(tokens);
        ParseResult result = parser.parseExpr(0);
        
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

        Parse parser = new Parse(tokens);
        ParseResult result = parser.parseExpr(0);
        
        // Using the builder pattern for cleaner, more readable test construction
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

        Parse parser = new Parse(tokens);
        ParseResult result = parser.parseExpr(0);
        
        // Using the builder pattern for cleaner, more readable test construction
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

        Parse parser = new Parse(tokens);
        ParseResult result = parser.parseExpr(0);

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

        Parse parser = new Parse(tokens);
        ParseResult result = parser.parseExpr(0);

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

        Parse parser = new Parse(tokens);
        ParseResult result = parser.parseExpr(0);

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

        Parse parser = new Parse(tokens);
        ParseResult result = parser.parseExpr(0);
        
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

        Parse parser = new Parse(tokens);
        ParseResult result = parser.parseExpr(0);
        
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

        Parse parser = new Parse(tokens);
        ParseResult result = parser.parseExpr(0);
        
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

        Parse parser = new Parse(tokens);
        ParseResult result = parser.parseExpr(0);
        
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
    public void testMethodCallPostfix() {
        List<LexicalToken> tokens = List.of(
            id("a"),
            punct("("),
            id("b"),
            punct(")"),
            punct(";")
        );

        Parse parser = new Parse(tokens);
        ParseResult result = parser.parseExpr(0);
        
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

        Parse parser = new Parse(tokens);
        ParseResult result = parser.parseExpr(0);
        
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

    /*
     * 2. Validity / acceptance tests (should parse)
     */

    @ParameterizedTest
    @MethodSource("happyPathProvider")
    public void testValidExpression(List<LexicalToken> tokens) {
        // Test valid expression using token list
        assertNotNull(tokens);
        Parse parser = new Parse(tokens);
        Boolean isValidProgram = parser.getIsValidProgram();
        assertTrue(isValidProgram, "Expected valid expression to be parsed successfully.");
    }

    /*
     * 3. Invalidity / rejection tests (should fail)
     */
    @ParameterizedTest
    @MethodSource("sadPathProvider")
    public void testInvalidExpression(List<LexicalToken> tokens) {
        // Test invalid expression using token list
        assertNotNull(tokens);
        Parse parser = new Parse(tokens);
        Boolean isValidProgram = parser.getIsValidProgram();
        assertFalse(isValidProgram, "Expected invalid expression to fail parsing.");
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
            )
        );
    }
}
