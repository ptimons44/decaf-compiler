package decaf.types;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@Data
@AllArgsConstructor
@ToString
public class LexicalToken {
    public enum TokenType {
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
