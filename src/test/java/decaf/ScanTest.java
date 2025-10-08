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


    @ParameterizedTest
    @ArgumentsSource(PairedDirectoryArgumentProvider.class)
    public void testOutputMatchesExpectedOutput(String inputContent, String expectedOutput) {
        // System.out.println("Input: " + inputContent);
        // System.out.println("Expected Output: " + expectedOutput);
        
        // Create a ByteArrayOutputStream to capture the output
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(outputStream);
        
        // Add your test logic here
        Scan scan = new Scan(inputContent);
        scan.scan();

        try {
            // Write to our capture stream
            scan.write(printStream);
        } catch (java.io.IOException e) {
            fail("IOException thrown: " + e.getMessage());
        }
        
        // Convert captured output to string and compare
        String actualOutput = outputStream.toString().trim();
        String expectedOutputTrimmed = expectedOutput.trim();
        
        assertEquals(expectedOutputTrimmed, actualOutput);
    }
}
