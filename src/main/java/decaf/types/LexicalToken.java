package decaf.types;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;
import lombok.Getter;

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
    @Getter private TokenType tokenType;
    @Getter private String val;
    private Integer lineNumber;
    private Integer columnNumber;
}
