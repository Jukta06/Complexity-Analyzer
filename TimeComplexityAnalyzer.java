public class TimeComplexityAnalyzer {

    private ASTNode root;

    public TimeComplexityAnalyzer() {
        this.root = null;
    }

    public TimeComplexityAnalyzer(ASTNode root) {
        this.root = root;
    }

    public void setRoot(ASTNode root) {
        this.root = root;
    }

    public TimeComplexity analyze(ASTNode rootNode) {
        if (rootNode == null) {
            throw new IllegalArgumentException("Provided AST node is null.");
        }
        return analyzeNode(rootNode);
    }

    private TimeComplexity analyzeNode(ASTNode node) {
        switch (node.getType()) {
            case PROGRAM:
            case FUNCTION:
            case STATEMENT:
            case BLOCK:
                return analyzeBlock(node);
            case LOOP:
            case FOR_LOOP:
            case WHILE_LOOP:
            case DO_WHILE_LOOP:
                return analyzeLoop(node);
            case CONDITIONAL:
                return analyzeConditional(node);
            case FUNCTION_CALL:
                return analyzeFunctionCall(node);
            case RECURSIVE_CALL:
                return analyzeRecursiveCall(node);
            default:
                return new TimeComplexity("Unknown", TimeComplexity.ComplexityType.CONSTANT, 1);
        }
    }

    private TimeComplexity analyzeBlock(ASTNode block) {
        TimeComplexity.ComplexityType maxType = TimeComplexity.ComplexityType.CONSTANT;
        int maxDegree = 1;

        for (ASTNode child : block.getChildren()) {
            TimeComplexity childComplexity = analyzeNode(child);
            if (childComplexity.getComplexity().ordinal() > maxType.ordinal()) {
                maxType = childComplexity.getComplexity();
                maxDegree = childComplexity.getDegree();
            }
        }

        return new TimeComplexity("Block", maxType, maxDegree);
    }

    private TimeComplexity analyzeLoop(ASTNode loopNode) {
        TimeComplexity innerComplexity = analyzeBlock(loopNode);
        TimeComplexity.ComplexityType complexityType = TimeComplexity.ComplexityType.LINEAR;
        int degree = 1;

        if (innerComplexity.getComplexity() == TimeComplexity.ComplexityType.LINEAR) {
            complexityType = TimeComplexity.ComplexityType.QUADRATIC; // O(n^2)
        } else if (innerComplexity.getComplexity() == TimeComplexity.ComplexityType.QUADRATIC) {
            complexityType = TimeComplexity.ComplexityType.CUBIC; // O(n^3)
        } else if (innerComplexity.getComplexity().ordinal() > TimeComplexity.ComplexityType.LINEAR.ordinal()) {
            complexityType = innerComplexity.getComplexity();
        }

        return new TimeComplexity("Loop", complexityType, degree);
    }

    private TimeComplexity analyzeRecursiveCall(ASTNode recursiveNode) {
        if (simpleLinearRecursion(recursiveNode)) {
            return new TimeComplexity("Recursive Call", TimeComplexity.ComplexityType.LINEAR, 1); 
        }

        if (multipleRecursiveCalls(recursiveNode)) {
            return new TimeComplexity("Recursive Call", TimeComplexity.ComplexityType.EXPONENTIAL, 1); 
        }

        return new TimeComplexity("Recursive Call", TimeComplexity.ComplexityType.UNKNOWN, 1);
    }

    private boolean simpleLinearRecursion(ASTNode node) {
        if (node.getType() != ASTNode.Type.FUNCTION_DECLARATION
                && node.getType() != ASTNode.Type.FUNCTION_DEFINITION) {
            return false;
        }

        String functionName = node.getFunctionName();
        if (functionName == null || functionName.isEmpty()) {
            return false;
        }

        ASTNode body = null;
        try {
            body = node.getBody();
        } catch (IllegalStateException e) {
            return false;
        }

        for (ASTNode child : body.getChildren()) {
            if (child.getType() == ASTNode.Type.RECURSIVE_CALL) {
                String recursiveCallCode = child.getCode();

                if (recursiveCallCode != null && recursiveCallCode.contains(functionName + "(")) {
                    if (recursiveCallCode.matches(".*\\(.*n\\s*-\\s*1.*\\).*")) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private boolean multipleRecursiveCalls(ASTNode recursiveNode) {
        int recursiveCallCount = 0;
        for (ASTNode child : recursiveNode.getChildren()) {
            if (child.getType() == ASTNode.Type.RECURSIVE_CALL) {
                recursiveCallCount++;
            }
        }
        return recursiveCallCount > 1;
    }

    private TimeComplexity analyzeConditional(ASTNode conditionalNode) {
        TimeComplexity.ComplexityType maxType = TimeComplexity.ComplexityType.CONSTANT;
        int maxDegree = 1;

        for (ASTNode child : conditionalNode.getChildren()) {
            TimeComplexity childComplexity = analyzeNode(child);
            if (childComplexity.getComplexity().ordinal() > maxType.ordinal()) {
                maxType = childComplexity.getComplexity();
                maxDegree = childComplexity.getDegree();
            }
        }

        return new TimeComplexity("Conditional", maxType, maxDegree);
    }

    private TimeComplexity analyzeFunctionCall(ASTNode functionCallNode) {
        ASTNode functionDefinition = findFunctionDefinition(functionCallNode.getCode());
        if (functionDefinition != null) {
            TimeComplexity bodyComplexity = analyzeBlock(functionDefinition);
            return new TimeComplexity("Function Call", bodyComplexity.getComplexity(), bodyComplexity.getDegree());
        }
        return new TimeComplexity("Unknown Function Call", TimeComplexity.ComplexityType.UNKNOWN, 1);
    }

    private ASTNode findFunctionDefinition(String functionName) {
        return null; 
    }
}   