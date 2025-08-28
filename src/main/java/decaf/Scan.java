package decaf;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class Scan {
    public static final Set<String> keywords = new HashSet<>(Arrays.asList(
            "if",
            "bool",
            "break",
            "import",
            "continue",
            "else",
            "false",
            "for",
            "while",
            "int",
            "long",
            "return",
            "len",
            "true",
            "void"
    ));

    private static void removeCommentsAndRedundantWhitespace(String in) {
        String whiteSpaceOrComment = "((\\/\\*[\\s\\S]*?\\*\\/)|(\\s)+)";
        String[] tokens = in.split(whiteSpaceOrComment);
        for (String token : tokens) {
            System.out.println("TOKEN: " + token);
        }
    }

    public static void doSomething(InputStream inputStream) throws IOException {
        System.out.println("Hello World");
        String contents = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        Scan.removeCommentsAndRedundantWhitespace(contents);
//        System.out.println(contents);

    }
}
