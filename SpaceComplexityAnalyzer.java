import java.util.*;

public class SpaceComplexityAnalyzer {
    private Complexity totalSpaceComplexity = new Complexity("O(1)");
    private boolean isRecursive = false;
    private Set<String> visitedFunctions = new HashSet<>();

    public String analyze(ASTNode node) {
        analyzeNode(node);
        return "Space Complexity: " + getSpaceComplexity();
    }

    private void analyzeNode(ASTNode node) {
        switch (node.getType()) {
            case PROGRAM:
            case TYPE_DECLARATION:
            case CONDITIONAL:
            case EXPRESSION:
                for (ASTNode child : node.getChildren()) {
                    analyzeNode(child);
                }
                break;
            case LOOP:
                handleLoop(node);
                break;
            case ARRAY_DECLARATION:
                handleArrayDeclaration(node);
                break;
            case NEW_INSTANCE:
                handleNewInstance(node);
                break;
            case RETURN_STATEMENT:
                handleReturnStatement(node);
                break;
            case FUNCTION_DECLARATION:
                handleFunctionDeclaration(node);
                break;
            case FUNCTION_CALL:
                handleFunctionCall(node);
                break;
            default:
                break;
        }
    }

    private void handleLoop(ASTNode node) {
        
        for (ASTNode child : node.getChildren()) {
            analyzeNode(child);
        }
    }

    private void handleArrayDeclaration(ASTNode node) {
        int dimensions = node.getDimensions().size();
        
        if (dimensions == 1) {
            totalSpaceComplexity = totalSpaceComplexity.add(new Complexity("n", 2));
        } else {
            totalSpaceComplexity = totalSpaceComplexity.add(new Complexity("n", dimensions));
        }
        
        
        for (ASTNode child : node.getChildren()) {
            analyzeNode(child);
        }
    }

    private void handleNewInstance(ASTNode node) {
       
        totalSpaceComplexity = totalSpaceComplexity.add(new Complexity("O(n)"));
    }

    private void handleReturnStatement(ASTNode node) {
        for (ASTNode child : node.getChildren()) {
            if (child.getType() == ASTNode.Type.EXPRESSION) {
                analyzeNode(child);
            }
        }
    }

    private void handleFunctionDeclaration(ASTNode node) {
        String functionName = node.getValue();
        if (visitedFunctions.contains(functionName)) {
            isRecursive = true;
        } else {
            visitedFunctions.add(functionName);
            for (ASTNode child : node.getChildren()) {
                analyzeNode(child);
            }
            visitedFunctions.remove(functionName);
        }
    }

    private void handleFunctionCall(ASTNode node) {
        String functionName = node.getValue();
        if (visitedFunctions.contains(functionName)) {
            isRecursive = true;
        } else {
            for (ASTNode child : node.getChildren()) {
                analyzeNode(child);
            }
        }
    }

    private String getSpaceComplexity() {
        if (isRecursive) {
            return "O(n)";  
        }
        return totalSpaceComplexity.toString();
    }
}
