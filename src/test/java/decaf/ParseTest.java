package decaf;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.util.List;


public class ParseTest {

    // @Test
    // public void scanTest() throws Exception {
    //     String[] args = {};
    //     Scan.main(args);
    // }

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