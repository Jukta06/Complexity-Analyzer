import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        boolean continueAnalyzing = true;
        
        System.out.println("Welcome to the Complexity Analyzer!");
        
        while (continueAnalyzing) {
            String code = UserInterface.getInputCode();

            System.out.println(code);

            String language = UserInterface.determineLanguage(code);

            if (language.equals("Unsupported")) {
                System.out.println("Unsupported Language.");
            } else {
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
                    tokens = null;
                    parser = null;
                }

                if (parser != null && tokens != null) {
                    ASTNode ast = parser.parse(tokens);

                    if (ast == null) {
                        System.out.println("Failed to generate AST.");
                    } else {
                        SpaceComplexityAnalyzer spaceAnalyzer = new SpaceComplexityAnalyzer();
                        String spaceComplexity = spaceAnalyzer.analyze(ast);
                        
                        TimeComplexityAnalyzer analyzer = new TimeComplexityAnalyzer();
                        TimeComplexity timeComplexity = analyzer.analyze(ast); 

                        System.out.println("Time Complexity: " + timeComplexity.toString()); 
                        System.out.println("Space Complexity: " + spaceComplexity);
                    }
                }
            }
            
            System.out.println("\nDo you want to analyze another code? (yes/no):");
            String response = scanner.nextLine().trim().toLowerCase();
            
            if (!response.equals("yes") && !response.equals("y")) {
                continueAnalyzing = false;
                System.out.println("Thank you for using the Complexity Analyzer. Goodbye!");
            } else {
                System.out.println("\n--- Starting New Analysis ---\n");
            }
        }
        
        scanner.close();
    }
}  