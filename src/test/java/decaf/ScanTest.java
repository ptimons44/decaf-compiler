package decaf;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.util.List;


public class ScanTest {

    // @Test
    // public void scanTest() throws Exception {
    //     String[] args = {};
    //     Scan.main(args);
    // }

    @ParameterizedTest
    @DirectorySource("src/test/public-tests/phase1-parser/public/illegal") // directory A
    public void testIllegealProgramAssertFail(String in) {
        System.out.println("testing file " + in);
    }
}