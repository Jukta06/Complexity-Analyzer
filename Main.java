import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("Welcome to the Complexity Analyzer!");

        String code = UserInterface.getInputCode();

        System.out.println(code);

        String language = UserInterface.determineLanguage(code);

        if (language.equals("Unsupported")) {
            System.out.println("Unsupported Language. Exiting.");
            return;
        }

        List<Token> tokens;
        Parser parser;

        if (language.equals("Java")) {
            JavaTokenizer tokenizer = new JavaTokenizer();
            tokens = tokenizer.tokenize(code);
            parser = new JavaParser();
        } else if (language.equals("C")) {
            CTokenizer tokenizer = new CTokenizer();
            tokens = tokenizer.tokenize(code);
            parser = new CParser();
        } else {
            System.out.println("Unsupported Language");
            return;
        }

        ASTNode ast = parser.parse(tokens);

        if (ast == null) {
            System.out.println("Failed to generate AST. Exiting.");
            return;
        }

        SpaceComplexityAnalyzer spaceAnalyzer = new SpaceComplexityAnalyzer();
        String spaceComplexity = spaceAnalyzer.analyze(ast);
        
        TimeComplexityAnalyzer analyzer = new TimeComplexityAnalyzer();
        TimeComplexity timeComplexity = analyzer.analyze(ast); 

       
        System.out.println("Time Complexity: " + timeComplexity.toString()); 
        System.out.println("Space Complexity: " + spaceComplexity);
    }
}  