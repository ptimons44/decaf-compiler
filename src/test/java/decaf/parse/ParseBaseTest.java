package decaf.parse;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.ValueSource;

import decaf.DirectorySource;
import decaf.types.LexicalToken;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
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


    @ParameterizedTest
    @DirectorySource("src/test/public-tests/phase1-parser/public/illegal") // directory A
    public void testBadProgramAssertFail(String in) {
        System.out.println("testing file " + in);
    }

    @ParameterizedTest
    @DirectorySource("src/test/public-tests/phase1-parser/public/legal") // directory A
    public void testGoodProgramCompilesSucessfully(String in) {
        System.out.println("testing file " + in);
    }
}