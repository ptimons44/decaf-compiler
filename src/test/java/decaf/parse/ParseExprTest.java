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

public class ParseExprTest {

    /*
     * Unit tests
     */
    
    @Test
    public void testAdditionWithSubtraction() {
        List<LexicalToken> tokens = List.of(
            new LexicalToken(LexicalToken.TokenType.IDENTIFIER, "a", 0, 0),
            new LexicalToken(LexicalToken.TokenType.PUNCTUATION, "-", 0, 0),
            new LexicalToken(LexicalToken.TokenType.IDENTIFIER, "b", 0, 0),  
            new LexicalToken(LexicalToken.TokenType.PUNCTUATION, "+", 0, 0),
            new LexicalToken(LexicalToken.TokenType.IDENTIFIER, "c", 0, 0),      
            new LexicalToken(LexicalToken.TokenType.PUNCTUATION, ";", 0, 0)
        );

        Parse parser = new Parse(tokens);
        ParseResult result = parser.parseExpr(0, 0);
        
        // Using the builder pattern for cleaner, more readable test construction
        ASTExpr expectedAST = ASTExpr.add()
            .left(ASTExpr.subtract()
                .left("a")
                .right("b")
                .build())
            .right("c")
            .build();
        ParseResult expectedResult = new ParseResult(expectedAST, 5);
        
        // Your test assertions here...
        assertTrue(expectedResult.equals(result), "Parsed AST does not match expected AST.");
    }

    /*
     * Functional Tests
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
                new LexicalToken(LexicalToken.TokenType.IDENTIFIER, "x", 0, 0)
            ),
            // Integer literal
            List.of(
                new LexicalToken(LexicalToken.TokenType.INTLITERAL, "42", 0, 0)
            ),
            // Boolean literal
            List.of(
                new LexicalToken(LexicalToken.TokenType.KEYWORD, "true", 0, 0)
            ),
            // Binary arithmetic: x + 5
            List.of(
                new LexicalToken(LexicalToken.TokenType.IDENTIFIER, "x", 0, 0),
                new LexicalToken(LexicalToken.TokenType.PUNCTUATION, "+", 0, 0),
                new LexicalToken(LexicalToken.TokenType.INTLITERAL, "5", 0, 0)
            ),
            // Binary comparison: a < b
            List.of(
                new LexicalToken(LexicalToken.TokenType.IDENTIFIER, "a", 0, 0),
                new LexicalToken(LexicalToken.TokenType.PUNCTUATION, "<", 0, 0),
                new LexicalToken(LexicalToken.TokenType.IDENTIFIER, "b", 0, 0)
            ),
            // Operator precedence: a + b * c (should parse as a + (b * c))
            List.of(
                new LexicalToken(LexicalToken.TokenType.IDENTIFIER, "a", 0, 0),
                new LexicalToken(LexicalToken.TokenType.PUNCTUATION, "+", 0, 0),
                new LexicalToken(LexicalToken.TokenType.IDENTIFIER, "b", 0, 0),
                new LexicalToken(LexicalToken.TokenType.PUNCTUATION, "*", 0, 0),
                new LexicalToken(LexicalToken.TokenType.IDENTIFIER, "c", 0, 0)
            ),
            // Operator precedence: a * b + c (should parse as (a * b) + c)
            List.of(
                new LexicalToken(LexicalToken.TokenType.IDENTIFIER, "a", 0, 0),
                new LexicalToken(LexicalToken.TokenType.PUNCTUATION, "*", 0, 0),
                new LexicalToken(LexicalToken.TokenType.IDENTIFIER, "b", 0, 0),
                new LexicalToken(LexicalToken.TokenType.PUNCTUATION, "+", 0, 0),
                new LexicalToken(LexicalToken.TokenType.IDENTIFIER, "c", 0, 0)
            ),
            // Division precedence: a / b + c (should parse as (a / b) + c)
            List.of(
                new LexicalToken(LexicalToken.TokenType.IDENTIFIER, "a", 0, 0),
                new LexicalToken(LexicalToken.TokenType.PUNCTUATION, "/", 0, 0),
                new LexicalToken(LexicalToken.TokenType.IDENTIFIER, "b", 0, 0),
                new LexicalToken(LexicalToken.TokenType.PUNCTUATION, "+", 0, 0),
                new LexicalToken(LexicalToken.TokenType.IDENTIFIER, "c", 0, 0)
            ),
            // Subtraction precedence: a - b * c (should parse as a - (b * c))
            List.of(
                new LexicalToken(LexicalToken.TokenType.IDENTIFIER, "a", 0, 0),
                new LexicalToken(LexicalToken.TokenType.PUNCTUATION, "-", 0, 0),
                new LexicalToken(LexicalToken.TokenType.IDENTIFIER, "b", 0, 0),
                new LexicalToken(LexicalToken.TokenType.PUNCTUATION, "*", 0, 0),
                new LexicalToken(LexicalToken.TokenType.IDENTIFIER, "c", 0, 0)
            ),
            // Mixed operators: a + b - c * d / e
            List.of(
                new LexicalToken(LexicalToken.TokenType.IDENTIFIER, "a", 0, 0),
                new LexicalToken(LexicalToken.TokenType.PUNCTUATION, "+", 0, 0),
                new LexicalToken(LexicalToken.TokenType.IDENTIFIER, "b", 0, 0),
                new LexicalToken(LexicalToken.TokenType.PUNCTUATION, "-", 0, 0),
                new LexicalToken(LexicalToken.TokenType.IDENTIFIER, "c", 0, 0),
                new LexicalToken(LexicalToken.TokenType.PUNCTUATION, "*", 0, 0),
                new LexicalToken(LexicalToken.TokenType.IDENTIFIER, "d", 0, 0),
                new LexicalToken(LexicalToken.TokenType.PUNCTUATION, "/", 0, 0),
                new LexicalToken(LexicalToken.TokenType.IDENTIFIER, "e", 0, 0)
            ),
            // Left associativity: a - b - c (should parse as (a - b) - c)
            List.of(
                new LexicalToken(LexicalToken.TokenType.IDENTIFIER, "a", 0, 0),
                new LexicalToken(LexicalToken.TokenType.PUNCTUATION, "-", 0, 0),
                new LexicalToken(LexicalToken.TokenType.IDENTIFIER, "b", 0, 0),
                new LexicalToken(LexicalToken.TokenType.PUNCTUATION, "-", 0, 0),
                new LexicalToken(LexicalToken.TokenType.IDENTIFIER, "c", 0, 0)
            ),
            // Left associativity: a / b / c (should parse as (a / b) / c)
            List.of(
                new LexicalToken(LexicalToken.TokenType.IDENTIFIER, "a", 0, 0),
                new LexicalToken(LexicalToken.TokenType.PUNCTUATION, "/", 0, 0),
                new LexicalToken(LexicalToken.TokenType.IDENTIFIER, "b", 0, 0),
                new LexicalToken(LexicalToken.TokenType.PUNCTUATION, "/", 0, 0),
                new LexicalToken(LexicalToken.TokenType.IDENTIFIER, "c", 0, 0)
            ),
            // Same precedence: a * b / c (should parse as (a * b) / c)
            List.of(
                new LexicalToken(LexicalToken.TokenType.IDENTIFIER, "a", 0, 0),
                new LexicalToken(LexicalToken.TokenType.PUNCTUATION, "*", 0, 0),
                new LexicalToken(LexicalToken.TokenType.IDENTIFIER, "b", 0, 0),
                new LexicalToken(LexicalToken.TokenType.PUNCTUATION, "/", 0, 0),
                new LexicalToken(LexicalToken.TokenType.IDENTIFIER, "c", 0, 0)
            ),
            // Parenthesized expression: (x + y)
            List.of(
                new LexicalToken(LexicalToken.TokenType.PUNCTUATION, "(", 0, 0),
                new LexicalToken(LexicalToken.TokenType.IDENTIFIER, "x", 0, 0),
                new LexicalToken(LexicalToken.TokenType.PUNCTUATION, "+", 0, 0),
                new LexicalToken(LexicalToken.TokenType.IDENTIFIER, "y", 0, 0),
                new LexicalToken(LexicalToken.TokenType.PUNCTUATION, ")", 0, 0)
            ),
            // Nested parentheses: ((x + y))
            List.of(
                new LexicalToken(LexicalToken.TokenType.PUNCTUATION, "(", 0, 0),
                new LexicalToken(LexicalToken.TokenType.PUNCTUATION, "(", 0, 0),
                new LexicalToken(LexicalToken.TokenType.IDENTIFIER, "x", 0, 0),
                new LexicalToken(LexicalToken.TokenType.PUNCTUATION, "+", 0, 0),
                new LexicalToken(LexicalToken.TokenType.IDENTIFIER, "y", 0, 0),
                new LexicalToken(LexicalToken.TokenType.PUNCTUATION, ")", 0, 0),
                new LexicalToken(LexicalToken.TokenType.PUNCTUATION, ")", 0, 0)
            ),
            // Complex nested: (x + (y * z))
            List.of(
                new LexicalToken(LexicalToken.TokenType.PUNCTUATION, "(", 0, 0),
                new LexicalToken(LexicalToken.TokenType.IDENTIFIER, "x", 0, 0),
                new LexicalToken(LexicalToken.TokenType.PUNCTUATION, "+", 0, 0),
                new LexicalToken(LexicalToken.TokenType.PUNCTUATION, "(", 0, 0),
                new LexicalToken(LexicalToken.TokenType.IDENTIFIER, "y", 0, 0),
                new LexicalToken(LexicalToken.TokenType.PUNCTUATION, "*", 0, 0),
                new LexicalToken(LexicalToken.TokenType.IDENTIFIER, "z", 0, 0),
                new LexicalToken(LexicalToken.TokenType.PUNCTUATION, ")", 0, 0),
                new LexicalToken(LexicalToken.TokenType.PUNCTUATION, ")", 0, 0)
            ),
            // Multiple levels: ((a + b) * (c - d))
            List.of(
                new LexicalToken(LexicalToken.TokenType.PUNCTUATION, "(", 0, 0),
                new LexicalToken(LexicalToken.TokenType.PUNCTUATION, "(", 0, 0),
                new LexicalToken(LexicalToken.TokenType.IDENTIFIER, "a", 0, 0),
                new LexicalToken(LexicalToken.TokenType.PUNCTUATION, "+", 0, 0),
                new LexicalToken(LexicalToken.TokenType.IDENTIFIER, "b", 0, 0),
                new LexicalToken(LexicalToken.TokenType.PUNCTUATION, ")", 0, 0),
                new LexicalToken(LexicalToken.TokenType.PUNCTUATION, "*", 0, 0),
                new LexicalToken(LexicalToken.TokenType.PUNCTUATION, "(", 0, 0),
                new LexicalToken(LexicalToken.TokenType.IDENTIFIER, "c", 0, 0),
                new LexicalToken(LexicalToken.TokenType.PUNCTUATION, "-", 0, 0),
                new LexicalToken(LexicalToken.TokenType.IDENTIFIER, "d", 0, 0),
                new LexicalToken(LexicalToken.TokenType.PUNCTUATION, ")", 0, 0),
                new LexicalToken(LexicalToken.TokenType.PUNCTUATION, ")", 0, 0)
            ),
            // Deep nesting: (((x)))
            List.of(
                new LexicalToken(LexicalToken.TokenType.PUNCTUATION, "(", 0, 0),
                new LexicalToken(LexicalToken.TokenType.PUNCTUATION, "(", 0, 0),
                new LexicalToken(LexicalToken.TokenType.PUNCTUATION, "(", 0, 0),
                new LexicalToken(LexicalToken.TokenType.IDENTIFIER, "x", 0, 0),
                new LexicalToken(LexicalToken.TokenType.PUNCTUATION, ")", 0, 0),
                new LexicalToken(LexicalToken.TokenType.PUNCTUATION, ")", 0, 0),
                new LexicalToken(LexicalToken.TokenType.PUNCTUATION, ")", 0, 0)
            ),
            // Parentheses with method call: (foo() + bar())
            List.of(
                new LexicalToken(LexicalToken.TokenType.PUNCTUATION, "(", 0, 0),
                new LexicalToken(LexicalToken.TokenType.IDENTIFIER, "foo", 0, 0),
                new LexicalToken(LexicalToken.TokenType.PUNCTUATION, "(", 0, 0),
                new LexicalToken(LexicalToken.TokenType.PUNCTUATION, ")", 0, 0),
                new LexicalToken(LexicalToken.TokenType.PUNCTUATION, "+", 0, 0),
                new LexicalToken(LexicalToken.TokenType.IDENTIFIER, "bar", 0, 0),
                new LexicalToken(LexicalToken.TokenType.PUNCTUATION, "(", 0, 0),
                new LexicalToken(LexicalToken.TokenType.PUNCTUATION, ")", 0, 0),
                new LexicalToken(LexicalToken.TokenType.PUNCTUATION, ")", 0, 0)
            ),
            // Array access: arr[i]
            List.of(
                new LexicalToken(LexicalToken.TokenType.IDENTIFIER, "arr", 0, 0),
                new LexicalToken(LexicalToken.TokenType.PUNCTUATION, "[", 0, 0),
                new LexicalToken(LexicalToken.TokenType.IDENTIFIER, "i", 0, 0),
                new LexicalToken(LexicalToken.TokenType.PUNCTUATION, "]", 0, 0)
            ),
            // Method call: foo()
            List.of(
                new LexicalToken(LexicalToken.TokenType.IDENTIFIER, "foo", 0, 0),
                new LexicalToken(LexicalToken.TokenType.PUNCTUATION, "(", 0, 0),
                new LexicalToken(LexicalToken.TokenType.PUNCTUATION, ")", 0, 0)
            ),
            // Method call with argument: bar(x)
            List.of(
                new LexicalToken(LexicalToken.TokenType.IDENTIFIER, "bar", 0, 0),
                new LexicalToken(LexicalToken.TokenType.PUNCTUATION, "(", 0, 0),
                new LexicalToken(LexicalToken.TokenType.IDENTIFIER, "x", 0, 0),
                new LexicalToken(LexicalToken.TokenType.PUNCTUATION, ")", 0, 0)
            )
        );
    }

    static Stream<List<LexicalToken>> sadPathProvider() {
        return Stream.of(
            // Missing closing parenthesis
            List.of(
                new LexicalToken(LexicalToken.TokenType.PUNCTUATION, "(", 0, 0),
                new LexicalToken(LexicalToken.TokenType.IDENTIFIER, "x", 0, 0)
            ),
            // Missing operand after PUNCTUATION
            List.of(
                new LexicalToken(LexicalToken.TokenType.IDENTIFIER, "x", 0, 0),
                new LexicalToken(LexicalToken.TokenType.PUNCTUATION, "+", 0, 0)
            ),
            // Missing array index
            List.of(
                new LexicalToken(LexicalToken.TokenType.IDENTIFIER, "arr", 0, 0),
                new LexicalToken(LexicalToken.TokenType.PUNCTUATION, "[", 0, 0),
                new LexicalToken(LexicalToken.TokenType.PUNCTUATION, "]", 0, 0)
            ),
            // Invalid PUNCTUATION sequence
            List.of(
                new LexicalToken(LexicalToken.TokenType.IDENTIFIER, "x", 0, 0),
                new LexicalToken(LexicalToken.TokenType.PUNCTUATION, "+", 0, 0),
                new LexicalToken(LexicalToken.TokenType.PUNCTUATION, "*", 0, 0),
                new LexicalToken(LexicalToken.TokenType.INTLITERAL, "5", 0, 0)
            ),
            // Missing closing bracket for array access
            List.of(
                new LexicalToken(LexicalToken.TokenType.IDENTIFIER, "arr", 0, 0),
                new LexicalToken(LexicalToken.TokenType.PUNCTUATION, "[", 0, 0),
                new LexicalToken(LexicalToken.TokenType.IDENTIFIER, "i", 0, 0)
            )
        );
    }
}
