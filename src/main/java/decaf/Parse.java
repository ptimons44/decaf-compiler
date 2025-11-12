package decaf;

import decaf.types.LexicalToken;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.ArrayList;

public class Parse {
    private List<LexicalToken> tokens;
    private List<String> errors = new ArrayList<>();
    private List<String> warnings = new ArrayList<>();
    
    public Parse(List<LexicalToken> tokens) {
        this.tokens = tokens;
    }

    public boolean getIsValidProgram() {
        return this.tokens != null && this.errors.isEmpty();
    }
}
