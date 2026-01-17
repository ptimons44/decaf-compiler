package decaf.parse;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import decaf.Parse;
import decaf.ParseException;
import decaf.types.LexicalToken;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.stream.Stream;

public class ParseDeclTest extends ParseBaseTest {

    @ParameterizedTest
    @MethodSource("happyPathProvider")
    public void testFunctionalValidDeclaration(List<LexicalToken> tokens) {
        // Test valid variable declaration using token list
        assertNotNull(tokens);
        // Add your parsing logic here
        Parse parser = new Parse(tokens);
        assertDoesNotThrow(() -> parser.parseProgram(), "Parsing valid declaration should not throw an exception");
    }

    @ParameterizedTest
    @MethodSource("sadPathProvider")
    public void testFunctionalInvalidDeclaration(List<LexicalToken> tokens) {
        // Test invalid variable declaration using token list
        assertNotNull(tokens);
        // Add your parsing logic that should fail
        Parse parser = new Parse(tokens);
        assertThrows(ParseException.class, () -> parser.parseProgram(), "Parsing invalid declaration should throw an exception");
    }

    static Stream<List<LexicalToken>> happyPathProvider() {
        return Stream.of(
            List.of(
                keyword("int"),
                id("a"),
                punct(";")
            )
        );
    }

    static Stream<List<LexicalToken>> sadPathProvider() {
        return Stream.of(
            // missing identifier
            List.of(
                keyword("int"),
                punct(";")
            ),
            // just an identifier without type
            List.of(
                id("a")
            )
        );
    }
}
