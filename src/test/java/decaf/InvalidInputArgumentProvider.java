package decaf;

import java.util.stream.Stream;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class InvalidInputArgumentProvider implements ArgumentsProvider {
    
    private static final String INPUT_DIR = "src/test/public-tests/phase1-scanner/public/input";
    private static final String OUTPUT_DIR = "src/test/public-tests/phase1-scanner/public/output";
    
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
        Path inputDir = Paths.get(INPUT_DIR);
        Path outputDir = Paths.get(OUTPUT_DIR);
        
        if (!Files.isDirectory(inputDir)) {
            throw new IllegalArgumentException("Input directory does not exist: " + inputDir.toAbsolutePath());
        }
        if (!Files.isDirectory(outputDir)) {
            throw new IllegalArgumentException("Output directory does not exist: " + outputDir.toAbsolutePath());
        }
        
        // Get all files from input directory mapped by base filename (without extension)
        // Filter for files containing "-invalid"
        Map<String, Path> inputFiles = Files.list(inputDir)
                .filter(Files::isRegularFile)
                .filter(path -> path.getFileName().toString().contains("-invalid"))
                .collect(Collectors.toMap(
                    path -> getBaseName(path.getFileName().toString()),
                    Function.identity()
                ));
        
        // Get all files from output directory and pair with input files
        return Files.list(outputDir)
                .filter(Files::isRegularFile)
                .filter(path -> path.getFileName().toString().contains("-invalid"))
                .sorted()
                .map(outputPath -> {
                    String outputFilename = outputPath.getFileName().toString();
                    String baseName = getBaseName(outputFilename);
                    Path inputPath = inputFiles.get(baseName);
                    
                    if (inputPath == null) {
                        throw new RuntimeException("No matching input file for: " + outputFilename);
                    }
                    
                    try {
                        String inputContent = Files.readString(inputPath, StandardCharsets.UTF_8);
                        String outputContent = Files.readString(outputPath, StandardCharsets.UTF_8);
                        // Include filename as third parameter for test naming
                        return Arguments.of(inputContent, outputContent, baseName);
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to read files for: " + outputFilename, e);
                    }
                });
    }
    
    private static String getBaseName(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        return lastDotIndex > 0 ? filename.substring(0, lastDotIndex) : filename;
    }
}