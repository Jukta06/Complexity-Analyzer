import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class CTokenizer {

    private static final String[] KEYWORDS = {
        "auto", "break", "case", "char", "const", "continue", "default", "do", "double", "else", "enum", "extern",
        "float", "for", "goto", "if", "inline", "int", "long", "register", "restrict", "return", "short", "signed",
        "sizeof", "static", "struct", "switch", "typedef", "union", "unsigned", "void", "volatile", "while"
    };

    private static final String[] OPERATORS = {
        "=", "==", "!=", ">", "<", ">=", "<=", "+", "-", "*", "/", "++", "--", "&&", "||", "!", "&", "|", "^", "~", "<<", ">>"
    };

    private static final String[] SEPARATORS = {
        "\\(", "\\)", "\\{", "\\}", "\\[", "\\]", ";", ",", "\\.", "->"
    };

    public List<Token> tokenize(String code) {

        
        List<Token> tokens = new ArrayList<>();

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
