package decaf;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.util.List;

public class ParseTest {

    @ParameterizedTest
    @ArgumentsSource(PairedDirectoryArgumentProvider.class)
    public void testOutputMatchesExpectedOutput(String inputContent, String expectedOutput) {
        System.out.println("Input: " + inputContent);
        System.out.println("Expected Output: " + expectedOutput);
        // Add your test logic here
    }
}
