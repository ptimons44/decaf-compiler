package decaf;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.List;

public class ScanTest {

    @ParameterizedTest(name = "{2}")
    @ArgumentsSource(ValidInputArgumentProvider.class)
    public void testValidInputsMatchExpectedOutput(String inputContent, String expectedOutput, String filename) {
        // Create a ByteArrayOutputStream to capture the output
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(outputStream);
        
        // Add your test logic here
        Scan scan = new Scan(inputContent);
        scan.scan();
        String actualOutput = scan.toString();
        String expectedOutputTrimmed = expectedOutput.trim();
        
        assertEquals(expectedOutputTrimmed, actualOutput);
    }

    @ParameterizedTest(name = "{2}")
    @ArgumentsSource(InvalidInputArgumentProvider.class)
    public void testInvalidInputsThrowCompileError(String inputContent, String expectedOutput, String filename) {
        // Test that invalid inputs throw a compile error or produce error output
        Scan scan = new Scan(inputContent);
        
        // Assuming scan() throws an exception for invalid input, or check for error in output
        assertThrows(Exception.class, () -> {
            scan.scan();
        }, "Expected compile error for invalid input");
    }
}
