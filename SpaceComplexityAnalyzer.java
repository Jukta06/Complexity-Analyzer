import java.util.*;

public class SpaceComplexityAnalyzer {
    private Complexity totalSpaceComplexity = new Complexity("O(1)");
    private boolean isRecursive = false;
    private Set<String> visitedFunctions = new HashSet<>();
    private int maxRecursionDepth = 0;

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
            case FUNCTION_DECLARATION:
            case FUNCTION_DEFINITION:
                handleFunctionDeclaration(node);
                break;
            case LOOP:
            case FOR_LOOP:
            case WHILE_LOOP:
            case DO_WHILE_LOOP:
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
            case FUNCTION_CALL:
                handleFunctionCall(node);
                break;
            case RECURSIVE_CALL:
                handleRecursiveCall(node);
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
        String functionName = node.getFunctionName();
        if (functionName == null) {
            functionName = node.getValue();
        }

        if (functionName != null) {
            if (visitedFunctions.contains(functionName)) {
                // Recursive function detected
                isRecursive = true;
            } else {
                visitedFunctions.add(functionName);
                // Analyze function body
                for (ASTNode child : node.getChildren()) {
                    analyzeNode(child);
                }
                visitedFunctions.remove(functionName);
            }
        } else {
            // No function name, just analyze children
            for (ASTNode child : node.getChildren()) {
                analyzeNode(child);
            }
        }
    }

    private void handleFunctionCall(ASTNode node) {
        String functionName = node.getFunctionName();
        if (functionName == null) {
            functionName = node.getValue();
        }

        if (functionName != null && visitedFunctions.contains(functionName)) {
            isRecursive = true;
        }

        for (ASTNode child : node.getChildren()) {
            analyzeNode(child);
        }
    }

    private void handleRecursiveCall(ASTNode node) {
        // Recursive call detected
        isRecursive = true;

        // Analyze children
        for (ASTNode child : node.getChildren()) {
            analyzeNode(child);
        }
    }

    private String getSpaceComplexity() {
        if (isRecursive) {
            if (totalSpaceComplexity.toString().equals("O(1)")) {
                return "O(n)";
            } else {
                Complexity recursionComplexity = new Complexity("O(n)");
                Complexity combined = totalSpaceComplexity.add(recursionComplexity);
                return combined.toString();
            }
        }
        return totalSpaceComplexity.toString();
    }
}
