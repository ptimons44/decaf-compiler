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
}
