package lang.types;

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
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        LexicalToken other = (LexicalToken) obj;
        
        // Compare token type and string value
        return this.tokenType == other.tokenType && 
               java.util.Objects.equals(this.val, other.val);
    }
    
    @Override
    public int hashCode() {
        return java.util.Objects.hash(tokenType, val);
    }

    private static final java.util.Set<String> keywords = java.util.Set.of(
        "if", "bool", "break", "import", "continue", "else", "false", 
        "for", "while", "int", "long", "return", "len", "true", "void"
    );

    public static TokenType inferTokenType(String val) {
        if (val.matches("^[0-9]+$")) {
            return TokenType.INTLITERAL;
        } else if (val.matches("^[0-9]+L$")) {
            return TokenType.LONGLITERAL;
        } else if (val.matches("^\".*\"$")) {
            return TokenType.STRINGLITERAL;
        } else if (val.matches("^(true|false)$")) {
            return TokenType.BOOLEANLITERAL;
        } else if (keywords.contains(val)) {
            return TokenType.KEYWORD;
        } {
            return TokenType.IDENTIFIER;
        }
    }
}
