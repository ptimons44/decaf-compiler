package lang;

import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.support.AnnotationConsumer;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.stream.Stream;

public class FilePathArgumentProvider
        implements ArgumentsProvider, AnnotationConsumer<DirectorySource> {

    private Path dir;
    private String includes;
    private boolean recursive;
    private Charset charset;

    @Override
    public void accept(DirectorySource src) {
        this.dir = Paths.get(src.value());
        this.includes = src.includes();
        this.recursive = src.recursive();
        this.charset = Charset.forName(src.charset());
    }

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
        if (!Files.isDirectory(dir)) {
            throw new IllegalArgumentException("Not a directory: " + dir.toAbsolutePath());
        }

        final PathMatcher matcher = dir.getFileSystem()
                .getPathMatcher("glob:" + includes);

        Stream<Path> paths = recursive
                ? Files.walk(dir).filter(Files::isRegularFile)
                : Files.list(dir).filter(Files::isRegularFile);

        // Each invocation will receive (String filename, String contents)
        return paths
                .filter(p -> matcher.matches(p.getFileName()))
                .sorted()
                .map(p -> {
                    try {
                        String content = Files.readString(p, charset);
                        return Arguments.of(p.getFileName().toString(), content);
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to read " + p, e);
                    }
                });
    }
}