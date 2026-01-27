package lang.parse;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.ValueSource;

import lang.DirectorySource;
import lang.Parse;
import lang.Scan;
import lang.grammars.decaf.DecafCFGGraph;
import lang.types.LexicalToken;
import lang.utils.CommandLineInterface;

import static org.junit.jupiter.api.Assertions.*;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.List;


public class ParseBaseTest {

    /*
     * DSL methods for constructing LexicalToken lists
     */
    public static LexicalToken id(String name) {
        return new LexicalToken(LexicalToken.TokenType.IDENTIFIER, name, 0, 0);
    }

    public static LexicalToken op(String s) {
        return new LexicalToken(LexicalToken.TokenType.PUNCTUATION, s, 0, 0);
    }

    public static LexicalToken punct(String s) {
        return new LexicalToken(LexicalToken.TokenType.PUNCTUATION, s, 0, 0);
    }

    public static LexicalToken intLit(String v) {
        return new LexicalToken(LexicalToken.TokenType.INTLITERAL, v, 0, 0);
    }

    public static LexicalToken boolLit(String v) {
        return new LexicalToken(LexicalToken.TokenType.BOOLEANLITERAL, v, 0, 0);
    }

    public static LexicalToken keyword(String v) {
        return new LexicalToken(LexicalToken.TokenType.KEYWORD, v, 0, 0);
    }


    @ParameterizedTest(name = "[{index}] {0}")
    @DirectorySource("src/test/public-tests/phase1-parser/public/illegal") // directory A
    public void testBadProgramAssertFail(String filename, String content) {
        String only = System.getenv("ONLY_TEST");
        if (only != null && !filename.contains(only)) {
            Assumptions.assumeTrue(false);
        }
        Scan scan = new Scan(content);
        scan.scan();
        List<LexicalToken> tokens = scan.getTokens();
        assertNotNull(tokens);
        Parse parser = new Parse(tokens, new DecafCFGGraph());
        assertThrows(ParseException.class, () -> parser.parseProgram());
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @DirectorySource("src/test/public-tests/phase1-parser/public/legal") // directory A
    public void testGoodProgramCompilesSucessfully(String filename, String content) {
        String only = System.getenv("ONLY_TEST");
        if (only != null && !filename.contains(only)) {
            Assumptions.assumeTrue(false);
        }

        Scan scan = new Scan(content);
        scan.scan();
        List<LexicalToken> tokens = scan.getTokens();
        assertNotNull(tokens);
        Parse parser = new Parse(tokens, new DecafCFGGraph());
        assertDoesNotThrow(() -> parser.parseProgram());
    }
}