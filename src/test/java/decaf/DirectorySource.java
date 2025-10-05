package decaf;

import org.junit.jupiter.params.provider.ArgumentsSource;

import java.lang.annotation.*;

@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ArgumentsSource(FilePathArgumentProvider.class)
public @interface DirectorySource {
    /** Directory path (relative to project root or absolute). */
    String value();

    /** Glob pattern for files to include (e.g. "*.txt"). */
    String includes() default "*";

    /** Recurse into subdirectories. */
    boolean recursive() default false;

    /** Charset to read files with. */
    String charset() default "UTF-8";
}
