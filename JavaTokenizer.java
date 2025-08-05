import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JavaTokenizer {

    private static final String[] KEYWORDS = {
        "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class", "const", "continue",
        "default", "do", "double", "else", "enum", "extends", "final", "finally", "float", "for", "goto", "if",
        "implements", "import", "instanceof", "int", "interface", "long", "native", "new", "package", "private",
        "protected", "public", "return", "short", "static", "strictfp", "super", "switch", "synchronized", "this",
        "throw", "throws", "transient", "try", "void", "volatile", "while"
    };

    private static final String[] OPERATORS = {
        "=", "==", "!=", ">", "<", ">=", "<=", "+", "-", "*", "/", "++", "--", "&&", "||", "!", "&", "|", "^", "~", "<<", ">>", ">>>"
    };

    private static final String[] SEPARATORS = {
        "\\(", "\\)", "\\{", "\\}", "\\[", "\\]", ";", ",", "\\.", ":", "->", "::"
    };

    public List<Token> tokenize(String code) {
        List<Token> tokens = new ArrayList<>();

        
        code = code.replaceAll("//.*|/\\*((.|\\n)(?!=*/))+\\*/", "");

        String tokenPatterns = String.join("|", createTokenPatterns());
        Pattern tokenPattern = Pattern.compile(tokenPatterns);
        Matcher matcher = tokenPattern.matcher(code);

        while (matcher.find()) {
            String tokenValue = matcher.group();
            Token token = identifyToken(tokenValue);
            tokens.add(token);
        }

        return tokens;
    }

    private List<String> createTokenPatterns() {
        List<String> patterns = new ArrayList<>();

        patterns.add("\\b(" + String.join("|", KEYWORDS) + ")\\b");
        patterns.addAll(createStringPatterns(OPERATORS));
        patterns.addAll(createStringPatterns(SEPARATORS));
        patterns.add("[a-zA-Z_][a-zA-Z0-9_]*"); 
        patterns.add("\\d+"); 
        patterns.add("\"(\\\\.|[^\"])*\""); 
        patterns.add("'.'"); 
        return patterns;
    }

    private List<String> createStringPatterns(String[] strings) {
        List<String> patterns = new ArrayList<>();
        for (String s : strings) {
            patterns.add(Pattern.quote(s));
        }
        return patterns;
    }

    private Token identifyToken(String tokenValue) {
        if (isKeyword(tokenValue)) {
            return new Token(Token.Type.KEYWORD, tokenValue);
        } else if (isOperator(tokenValue)) {
            return new Token(Token.Type.OPERATOR, tokenValue);
        } else if (isSeparator(tokenValue)) {
            return new Token(Token.Type.SEPARATOR, tokenValue);
        } else if (tokenValue.matches("[a-zA-Z_][a-zA-Z0-9_]*")) { 
            return new Token(Token.Type.IDENTIFIER, tokenValue);
        } else if (tokenValue.matches("\\d+")) { 
            return new Token(Token.Type.LITERAL, tokenValue);
        } else if (tokenValue.matches("\"(\\\\.|[^\"])*\"") || tokenValue.matches("'.'")) { 
            return new Token(Token.Type.LITERAL, tokenValue);
        } else {
            return new Token(Token.Type.UNKNOWN, tokenValue);
        }
    }

    private boolean isKeyword(String tokenValue) {
        for (String keyword : KEYWORDS) {
            if (keyword.equals(tokenValue)) {
                return true;
            }
        }
        return false;
    }

    private boolean isOperator(String tokenValue) {
        for (String operator : OPERATORS) {
            if (operator.equals(tokenValue)) {
                return true;
            }
        }
        return false;
    }

    private boolean isSeparator(String tokenValue) {
        for (String separator : SEPARATORS) {
            if (separator.equals(tokenValue)) {
                return true;
            }
        }
        return false;
    }
}
