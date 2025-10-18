package decaf;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LexicalToken {
    enum TokenType {
        CHARLITERAL, 
        STRINGLITERAL, 
        INTLITERAL,
        LONGLITERAL, 
        BOOLEANLITERAL, 
        IDENTIFIER, 
        PUNCTUATION, 
        KEYWORD
    };
    private TokenType tokenType;
    private String val;
    private Integer lineNumber;
    private Integer columnNumber;
}
