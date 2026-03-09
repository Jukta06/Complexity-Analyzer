import java.util.List;

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
            case FUNCTION_DECLARATION:
            case FUNCTION_DEFINITION:
                return analyzeFunctionDeclaration(node);
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
            case QUICK_SORT:
                // QuickSort worst case is O(n²)
                return new TimeComplexity("Quick Sort", TimeComplexity.ComplexityType.QUADRATIC, 1);
            case MERGE_SORT:
                // MergeSort is O(n log n)
                return new TimeComplexity("Merge Sort", TimeComplexity.ComplexityType.LINEARITHMIC, 1);
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
        // Check if loop has constant bound
        boolean isConstantBound = isLoopBoundConstant(loopNode);

        if (isConstantBound) {
            // Loop runs constant times, so O(1)
            return new TimeComplexity("Loop", TimeComplexity.ComplexityType.CONSTANT, 1);
        }

        // Check if loop has logarithmic growth (multiplicative/divisive update)
        boolean isLogarithmic = isLogarithmicLoop(loopNode);

        // Check if this loop contains amortized nested loops
        boolean hasAmortizedNestedLoop = hasAmortizedNestedLoop(loopNode);

        TimeComplexity innerComplexity = analyzeBlock(loopNode);
        TimeComplexity.ComplexityType complexityType;
        int degree = 1;

        if (isLogarithmic) {
            // Loop itself is O(log n)
            if (innerComplexity.getComplexity() == TimeComplexity.ComplexityType.CONSTANT) {
                complexityType = TimeComplexity.ComplexityType.LOGARITHMIC; // O(log n)
            } else if (innerComplexity.getComplexity() == TimeComplexity.ComplexityType.LINEAR) {
                complexityType = TimeComplexity.ComplexityType.LINEARITHMIC; // O(n log n)
            } else if (innerComplexity.getComplexity() == TimeComplexity.ComplexityType.LOGARITHMIC) {
                complexityType = TimeComplexity.ComplexityType.LOGARITHMIC; // O(log n * log n) ≈ O(log n)
            } else {
                complexityType = innerComplexity.getComplexity();
            }
        } else {
            // Loop itself is O(n)
            if (innerComplexity.getComplexity() == TimeComplexity.ComplexityType.LINEAR) {
                // Check if this is amortized case
                if (hasAmortizedNestedLoop) {
                    // Amortized: outer O(n) + inner O(n) = O(n), not O(n^2)
                    complexityType = TimeComplexity.ComplexityType.LINEAR; // O(n)
                } else {
                    complexityType = TimeComplexity.ComplexityType.QUADRATIC; // O(n^2)
                }
            } else if (innerComplexity.getComplexity() == TimeComplexity.ComplexityType.QUADRATIC) {
                complexityType = TimeComplexity.ComplexityType.CUBIC; // O(n^3)
            } else if (innerComplexity.getComplexity() == TimeComplexity.ComplexityType.LOGARITHMIC) {
                complexityType = TimeComplexity.ComplexityType.LINEARITHMIC; // O(n log n)
            } else if (innerComplexity.getComplexity().ordinal() > TimeComplexity.ComplexityType.LINEAR.ordinal()) {
                complexityType = innerComplexity.getComplexity();
            } else {
                complexityType = TimeComplexity.ComplexityType.LINEAR; // O(n)
            }
        }

        return new TimeComplexity("Loop", complexityType, degree);
    }

    private boolean isLogarithmicLoop(ASTNode loopNode) {
        String update = loopNode.getUpdate();

        // For FOR loops, check the update expression
        if (update != null && !update.isEmpty()) {
            // Remove all whitespace
            String cleanUpdate = update.replaceAll("\\s+", "");

            // Check for multiplicative or divisive operations
            // Pattern: i *= 2, i /= 2, i = i * 2, i = i / 2, etc.

            // Check for *= or /=
            if (cleanUpdate.contains("*=") || cleanUpdate.contains("/=")) {
                return true;
            }

            // Check for patterns like: i = i * 2, i = i / 2, j = j * 3, etc.
            if (cleanUpdate.matches(".*=.*[*/].*")) {
                // Has assignment with multiplication or division
                String[] parts = cleanUpdate.split("=");
                if (parts.length == 2) {
                    String variable = parts[0].trim();
                    String expression = parts[1].trim();

                    // Check if the variable appears in the expression (i = i * 2)
                    if (expression.contains(variable) && (expression.contains("*") || expression.contains("/"))) {
                        return true;
                    }
                }
            }
        }

        // For WHILE/DO-WHILE loops, check the body for binary search or
        // divide-and-conquer patterns
        if (loopNode.getType() == ASTNode.Type.WHILE_LOOP ||
                loopNode.getType() == ASTNode.Type.DO_WHILE_LOOP) {

            // Check if body contains division patterns (binary search)
            if (hasBinarySearchPattern(loopNode)) {
                return true;
            }
        }

        return false;
    }

    private boolean hasBinarySearchPattern(ASTNode loopNode) {
        // Look for binary search patterns in the loop body:
        // TRUE binary search requires BOTH:
        // 1. mid = (low + high) / 2 (or similar)
        // 2. high = mid - 1 OR low = mid + 1 (search space halving using mid)

        StringBuilder allCode = new StringBuilder();
        for (ASTNode child : loopNode.getChildren()) {
            if (child.getValue() != null) {
                allCode.append(child.getValue()).append(" ");
            }
        }

        String fullCode = allCode.toString().replaceAll("\\s+", "");

        boolean hasMidCalculation = false;
        boolean hasSearchSpaceHalving = false;

        // Check for mid calculation pattern: mid=(low+high)/2
        if (fullCode.matches(".*mid=.*\\+.*\\/2.*") ||
                fullCode.matches(".*mid=.*\\(.*\\+.*\\)\\/2.*")) {
            hasMidCalculation = true;
        }

        // Check for search space halving using mid (not just decrement/increment by 1)
        // Pattern: high = mid - 1, low = mid + 1, etc.
        if (fullCode.contains("high=mid-") || fullCode.contains("low=mid+") ||
                fullCode.contains("high=mid+") || fullCode.contains("low=mid-") ||
                fullCode.contains("right=mid-") || fullCode.contains("left=mid+") ||
                fullCode.contains("end=mid-") || fullCode.contains("start=mid+")) {
            hasSearchSpaceHalving = true;
        }

        // BOTH conditions must be true for binary search
        if (hasMidCalculation && hasSearchSpaceHalving) {
            return true;
        }

        // Check for simple division by 2 patterns (like n = n / 2)
        // This should match variable names on both sides
        if (fullCode.matches(".*([a-z]+)=\\1\\/2.*") || fullCode.contains("/=2")) {
            return true;
        }

        for (ASTNode child : loopNode.getChildren()) {
            if (child.getChildren().size() > 0) {
                if (hasBinarySearchPattern(child)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean hasAmortizedNestedLoop(ASTNode outerLoop) {

        if (outerLoop.getType() == ASTNode.Type.FOR_LOOP) {
            String outerInit = outerLoop.getInitialization();
            String outerCond = outerLoop.getCondition();
            String outerUpdate = outerLoop.getUpdate();

            for (ASTNode child : outerLoop.getChildren()) {
                if (child.getType() == ASTNode.Type.WHILE_LOOP ||
                        child.getType() == ASTNode.Type.DO_WHILE_LOOP) {

                    String innerVar = extractLoopVariable(child);

                    if (innerVar != null && !innerVar.isEmpty()) {
                        // Check if inner variable appears in outer loop header
                        boolean inOuterHeader = false;

                        if (outerInit != null && outerInit.contains(innerVar)) {
                            inOuterHeader = true;
                        }
                        if (outerCond != null && outerCond.contains(innerVar)) {
                            inOuterHeader = true;
                        }
                        if (outerUpdate != null && outerUpdate.contains(innerVar)) {
                            inOuterHeader = true;
                        }

                        if (!inOuterHeader) {
                            // Inner variable not in outer header
                            // Now check if it's reset in outer body
                            boolean isReset = checkNodeForAssignment(outerLoop, innerVar, child);

                            if (!isReset) {
                                // Not reset - this is amortized!
                                return true;
                            }
                        }
                    }
                }

                // Check nested children recursively
                if (child.getChildren().size() > 0 && !isLoop(child)) {
                    if (hasAmortizedInSubtree(child, outerLoop)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private boolean hasAmortizedInSubtree(ASTNode node, ASTNode outerLoop) {
        for (ASTNode child : node.getChildren()) {
            // Only check WHILE and DO_WHILE loops for amortized analysis, NOT FOR loops
            if (child.getType() == ASTNode.Type.WHILE_LOOP ||
                    child.getType() == ASTNode.Type.DO_WHILE_LOOP) {

                String innerVar = extractLoopVariable(child);

                if (innerVar != null && !innerVar.isEmpty()) {
                    // Check if this variable is managed by outer loop
                    String outerInit = outerLoop.getInitialization();
                    String outerCond = outerLoop.getCondition();
                    String outerUpdate = outerLoop.getUpdate();

                    boolean inOuterHeader = false;
                    if (outerInit != null && outerInit.contains(innerVar))
                        inOuterHeader = true;
                    if (outerCond != null && outerCond.contains(innerVar))
                        inOuterHeader = true;
                    if (outerUpdate != null && outerUpdate.contains(innerVar))
                        inOuterHeader = true;

                    if (!inOuterHeader) {
                        boolean isReset = checkNodeForAssignment(outerLoop, innerVar, child);
                        if (!isReset) {
                            return true;
                        }
                    }
                }
            }

            if (child.getChildren().size() > 0 && !isLoop(child)) {
                if (hasAmortizedInSubtree(child, outerLoop)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean hasAmortizedNestedLoopInChildren(ASTNode node) {
        return false;
    }

    private boolean isLoop(ASTNode node) {
        ASTNode.Type type = node.getType();
        return type == ASTNode.Type.FOR_LOOP ||
                type == ASTNode.Type.WHILE_LOOP ||
                type == ASTNode.Type.DO_WHILE_LOOP ||
                type == ASTNode.Type.LOOP;
    }

    private String extractLoopVariable(ASTNode loopNode) {
        String initialization = loopNode.getInitialization();
        String condition = loopNode.getCondition();

        // Try initialization first (for FOR loops)
        if (initialization != null && !initialization.isEmpty()) {
            // Pattern: int j = 0, j = 0, etc.
            String cleanInit = initialization.replaceAll("\\s+", "");
            String[] parts = cleanInit.split("=");
            if (parts.length >= 1) {
                String var = parts[0].replaceAll("(int|char|long|short|float|double)", "").trim();
                if (!var.isEmpty()) {
                    return var;
                }
            }
        }

        // Try condition (for WHILE/DO_WHILE loops or if initialization not available)
        if (condition != null && !condition.isEmpty()) {
            // Pattern: j < n, j <= n, etc.
            String cleanCond = condition.replaceAll("\\s+", "");

            // Extract variable before comparison operator
            String[] comparisonOps = { "<=", ">=", "!=", "==", "<", ">" };
            for (String op : comparisonOps) {
                if (cleanCond.contains(op)) {
                    String[] parts = cleanCond.split(op, 2);
                    if (parts.length >= 1) {
                        String var = parts[0].trim();
                        // Make sure it's a valid variable name (not a number)
                        if (var.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
                            return var;
                        }
                    }
                    break;
                }
            }
        }

        return null;
    }

    private boolean isVariableResetInLoop(ASTNode loopNode, String variable, ASTNode excludeChild) {
        return checkNodeForAssignment(loopNode, variable, excludeChild);
    }

    private boolean checkNodeForAssignment(ASTNode node, String variable, ASTNode excludeNode) {
        if (node == excludeNode) {
            return false;
        }

        if (containsAssignment(node, variable)) {
            return true;
        }

        // Check all children
        for (ASTNode child : node.getChildren()) {
            if (checkNodeForAssignment(child, variable, excludeNode)) {
                return true;
            }
        }

        return false;
    }

    private boolean containsAssignment(ASTNode node, String variable) {
        // Check if this node contains an assignment to the variable
        String value = node.getValue();

        if (value != null && !value.isEmpty()) {
            String cleanValue = value.replaceAll("\\s+", "");
            // Pattern: j=, j++, j--, j+=, j-=, etc.
            // After removing whitespace, check for these patterns
            if (cleanValue.contains(variable + "=") ||
                    cleanValue.contains(variable + "++") ||
                    cleanValue.contains(variable + "--") ||
                    cleanValue.contains(variable + "+=") ||
                    cleanValue.contains(variable + "-=") ||
                    cleanValue.contains(variable + "*=") ||
                    cleanValue.contains(variable + "/=") ||
                    cleanValue.equals(variable)) {
                if (cleanValue.contains("=") && cleanValue.contains(variable)) {
                    return true;
                }
                if (cleanValue.contains("++") || cleanValue.contains("--")) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean isVariableDeclaredInNode(ASTNode node, String variable) {
        for (ASTNode child : node.getChildren()) {
            String value = child.getValue();
            if (value != null && !value.isEmpty()) {
                String cleanValue = value.replaceAll("\\s+", "");
                if (cleanValue.matches(".*(int|char|long|short|float|double)\\s*" + variable + ".*")) {
                    return true;
                }
            }

            if (child.getChildren().size() > 0) {
                if (isVariableDeclaredInNode(child, variable)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean isLoopBoundConstant(ASTNode loopNode) {
        String condition = loopNode.getCondition();

        if (condition == null || condition.isEmpty()) {
            return false;
        }

        // First check if this loop has constant iterations (e.g., j=i; j<i+1)
        if (hasConstantIterations(loopNode)) {
            return true;
        }

        // Remove all whitespace for easier parsing
        String cleanCondition = condition.replaceAll("\\s+", "");

        // Look for patterns like: i<10, i<=5, i>0, i>=1, i!=100, i==20, etc.
        String[] comparisonOps = { "<=", ">=", "!=", "==", "<", ">" };
        for (String op : comparisonOps) {
            if (cleanCondition.contains(op)) {
                // Split by the operator
                String[] parts = cleanCondition.split(op, 2);
                if (parts.length == 2) {
                    // Check both sides of the comparison
                    String leftSide = parts[0].trim();
                    String rightSide = parts[1].trim();

                    // If either side is a pure numeric constant, loop has constant bound
                    if (isNumericConstant(leftSide) || isNumericConstant(rightSide)) {
                        return true;
                    }

                    // Also check for preprocessor constants (all uppercase letters/digits)
                    // like MAX_SIZE, ARRAY_LEN, etc. Treat these as constants
                    if (isPreprocessorConstant(leftSide) || isPreprocessorConstant(rightSide)) {
                        return true;
                    }
                }
                break; // Found a comparison operator, no need to check others
            }
        }

        return false; // No constant bound found
    }

    private boolean hasConstantIterations(ASTNode loopNode) {
        String initialization = loopNode.getInitialization();
        String condition = loopNode.getCondition();

        if (initialization == null || initialization.isEmpty() ||
                condition == null || condition.isEmpty()) {
            return false;
        }

        // Remove whitespace
        String cleanInit = initialization.replaceAll("\\s+", "");
        String cleanCond = condition.replaceAll("\\s+", "");

        // Pattern: j = i (loop var initialized to another variable)
        // Extract loop variable and its initial value
        // Pattern: variable = value (or type variable = value)
        String initPattern = ".*=.*";
        if (!cleanInit.matches(initPattern)) {
            return false;
        }

        // Extract the variable and its initialization value
        // Handle patterns like: j=i, int j=i, etc.
        String[] initParts = cleanInit.split("=");
        if (initParts.length != 2) {
            return false;
        }

        // Get the loop variable (remove type keywords like int, char, etc.)
        String loopVar = initParts[0].replaceAll("(int|char|long|short|float|double)", "").trim();
        String initValue = initParts[1].trim();

        // Skip if initialization is with a constant (already handled elsewhere)
        if (isNumericConstant(initValue)) {
            return false;
        }

        // Check if condition uses the same loop variable
        if (!cleanCond.contains(loopVar)) {
            return false;
        }

        // Pattern: j < i + constant, j <= i, j < i + 1, etc.

        String[] comparisonOps = { "<=", ">=", "<", ">", "!=", "==" };
        for (String op : comparisonOps) {
            if (cleanCond.contains(op)) {
                String[] condParts = cleanCond.split(op, 2);
                if (condParts.length == 2) {
                    String leftSide = condParts[0].trim();
                    String rightSide = condParts[1].trim();

                    if (leftSide.equals(loopVar) && rightSide.contains(initValue)) {
                        String remainder = rightSide.replace(initValue, "").trim();

                        // If remainder is empty, it's just comparing with same value (j <= i)
                        if (remainder.isEmpty()) {
                            return true; // Constant 1 iteration
                        }

                        // Check if remainder is +constant or -constant
                        if (remainder.matches("[+\\-]\\d+")) {
                            return true; // Constant iterations
                        }
                    } else if (rightSide.equals(loopVar) && leftSide.contains(initValue)) {
                        // Reverse case (i+1 > j)
                        String remainder = leftSide.replace(initValue, "").trim();
                        if (remainder.isEmpty() || remainder.matches("[+\\-]\\d+")) {
                            return true;
                        }
                    }
                }
                break;
            }
        }

        return false;
    }

    private boolean isPreprocessorConstant(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }

        str = str.trim();

        // Check if string is all uppercase with optional underscores and digits
        // Examples: MAX_SIZE, ARRAY_LEN, SIZE100, N
        // Exclude single lowercase variables like 'i', 'j', 'n'
        if (str.length() > 1 && str.matches("[A-Z][A-Z0-9_]*")) {
            return true;
        }

        return false;
    }

    private boolean isNumericConstant(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }

        str = str.trim();

        // Check if string represents a number (integer or decimal)
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private TimeComplexity analyzeRecursiveCall(ASTNode recursiveNode) {
        // Detect recursion pattern based on the function containing it
        ASTNode functionNode = findParentFunction(recursiveNode);

        if (functionNode != null) {
            // Count recursive calls in the function
            int recursiveCallCount = countRecursiveCalls(functionNode);

            if (recursiveCallCount == 1) {
                // Single recursive call - typically O(n) for linear recursion
                // Check if it's divide and conquer (like binary search) - O(log n)
                if (isDivideAndConquerRecursion(functionNode)) {
                    return new TimeComplexity("Recursive Call", TimeComplexity.ComplexityType.LOGARITHMIC, 1);
                }
                // Linear recursion (like factorial, fibonacci with memoization)
                return new TimeComplexity("Recursive Call", TimeComplexity.ComplexityType.LINEAR, 1);
            } else if (recursiveCallCount == 2) {
                // Two recursive calls - typically O(2^n) for tree recursion (like fibonacci)
                return new TimeComplexity("Recursive Call", TimeComplexity.ComplexityType.EXPONENTIAL, 1);
            } else if (recursiveCallCount > 2) {
                // Multiple recursive calls - exponential or worse
                return new TimeComplexity("Recursive Call", TimeComplexity.ComplexityType.EXPONENTIAL, 1);
            }
        }

        return new TimeComplexity("Recursive Call", TimeComplexity.ComplexityType.LINEAR, 1);
    }

    private ASTNode findParentFunction(ASTNode node) {
        return null;
    }

    private int countRecursiveCalls(ASTNode functionNode) {
        int count = 0;
        for (ASTNode child : functionNode.getChildren()) {
            if (child.getType() == ASTNode.Type.RECURSIVE_CALL) {
                count++;
            }
            count += countRecursiveCalls(child);
        }
        return count;
    }

    private boolean isDivideAndConquerRecursion(ASTNode functionNode) {
        // Problem size halved each time: func(n/2), mid = (low+high)/2
        // Look for "mid" variable calculations

        String fullCode = extractAllCode(functionNode);
        fullCode = fullCode.toLowerCase(); // Case insensitive

        // Pattern 1: Check for mid calculation (binary search pattern)
        // mid = (low + high) / 2 or similar
        if (fullCode.contains("mid")) {
            // If function has "mid" variable and recursion, likely divide-and-conquer
            int recursiveCallCount = countRecursiveCalls(functionNode);
            if (recursiveCallCount > 0) {
                // Check if mid is calculated by division
                if (fullCode.matches(".*mid.*=.*/.*2.*") ||
                        fullCode.contains("/2") ||
                        fullCode.contains("/ 2")) {
                    return true;
                }
            }
        }

        // Pattern 2: Direct division in recursion (like func(n/2))
        if (fullCode.contains("/2") || fullCode.contains("/ 2") || fullCode.contains(">>1")) {
            return true;
        }

        return false;
    }

    private String extractAllCode(ASTNode node) {
        StringBuilder sb = new StringBuilder();
        extractCodeHelper(node, sb);
        return sb.toString();
    }

    private void extractCodeHelper(ASTNode node, StringBuilder sb) {
        if (node.getValue() != null) {
            sb.append(node.getValue()).append(" ");
        }
        for (ASTNode child : node.getChildren()) {
            extractCodeHelper(child, sb);
        }
    }

    private boolean hasConditionalRecursion(ASTNode functionNode) {
        int totalRecursiveCalls = countRecursiveCalls(functionNode);
        if (totalRecursiveCalls < 2) {
            return false;
        }

        if (hasSequentialRecursiveCalls(functionNode)) {
            return false;
        }

        if (hasRecursionInsideConditional(functionNode)) {
            return true; // Binary Search pattern
        }

        return false;
    }

    private boolean hasSequentialRecursiveCalls(ASTNode node) {
        List<ASTNode> children = node.getChildren();
        int directRecursiveCalls = 0;

        // Count recursive calls at this level
        for (ASTNode child : children) {
            if (child.getType() == ASTNode.Type.RECURSIVE_CALL) {
                directRecursiveCalls++;
            }
        }

        if (directRecursiveCalls >= 2) {
            int recursiveCallsAfterReturn = 0;
            for (int i = 1; i < children.size(); i++) {
                if (children.get(i).getType() == ASTNode.Type.RECURSIVE_CALL) {
                    if (children.get(i - 1).getType() == ASTNode.Type.RETURN_STATEMENT) {
                        recursiveCallsAfterReturn++;
                    }
                }
            }

            if (recursiveCallsAfterReturn == directRecursiveCalls && directRecursiveCalls >= 2) {
                return false;
            }
            return true;
        }

        for (ASTNode child : children) {
            if (child.getType() != ASTNode.Type.RETURN_STATEMENT) {
                if (hasSequentialRecursiveCalls(child)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean hasRecursionInsideConditional(ASTNode node) {
        if (node.getType() == ASTNode.Type.CONDITIONAL ||
                node.getType() == ASTNode.Type.IF_STATEMENT) {
            // Check if this conditional contains recursive calls
            if (containsRecursiveCall(node)) {
                return true;
            }
        }

        // Check children
        for (ASTNode child : node.getChildren()) {
            if (hasRecursionInsideConditional(child)) {
                return true;
            }
        }

        return false;
    }

    private boolean hasRecursionInMutuallyExclusiveBranches(ASTNode node) {
        if (node.getType() == ASTNode.Type.CONDITIONAL ||
                node.getType() == ASTNode.Type.IF_STATEMENT) {

            int returnStatementsWithRecursion = 0;

            for (ASTNode child : node.getChildren()) {
                if (hasReturnWithRecursion(child)) {
                    returnStatementsWithRecursion++;
                }
            }

            if (returnStatementsWithRecursion >= 2) {
                return true;
            }
        }

        // Check children recursively
        for (ASTNode child : node.getChildren()) {
            if (hasRecursionInMutuallyExclusiveBranches(child)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasReturnWithRecursion(ASTNode node) {
        // Check if this branch has a return statement that contains a recursive call
        if (node.getType() == ASTNode.Type.RETURN_STATEMENT) {
            if (containsRecursiveCall(node)) {
                return true;
            }
        }

        // For direct children only (not deep recursion)
        for (ASTNode child : node.getChildren()) {
            if (child.getType() == ASTNode.Type.RETURN_STATEMENT && containsRecursiveCall(child)) {
                return true;
            }
        }

        return false;
    }

    private boolean hasRecursionInConditionalBranches(ASTNode node) {
        if (node.getType() == ASTNode.Type.CONDITIONAL ||
                node.getType() == ASTNode.Type.IF_STATEMENT) {
            // Check if different children have recursive calls
            int branchesWithRecursion = 0;
            for (ASTNode child : node.getChildren()) {
                if (containsRecursiveCall(child)) {
                    branchesWithRecursion++;
                }
            }
            if (branchesWithRecursion >= 2) {
                return true;
            }
        }

        // Check children recursively
        for (ASTNode child : node.getChildren()) {
            if (hasRecursionInConditionalBranches(child)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsRecursiveCall(ASTNode node) {
        if (node.getType() == ASTNode.Type.RECURSIVE_CALL) {
            return true;
        }
        for (ASTNode child : node.getChildren()) {
            if (containsRecursiveCall(child)) {
                return true;
            }
        }
        return false;
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

        int recursiveCallCount = countRecursiveCalls(node);
        return recursiveCallCount == 1;
    }

    private boolean multipleRecursiveCalls(ASTNode recursiveNode) {
        int recursiveCallCount = countRecursiveCalls(recursiveNode);
        return recursiveCallCount > 1;
    }

    private boolean hasMultipleRecursiveCallsInSameReturn(ASTNode functionNode) {
        return checkForAdjacentRecursiveCalls(functionNode);
    }

    private boolean checkForAdjacentRecursiveCalls(ASTNode node) {
        List<ASTNode> children = node.getChildren();

        for (int i = 0; i < children.size(); i++) {
            ASTNode child = children.get(i);

            if (child.getType() == ASTNode.Type.RETURN_STATEMENT) {
                int recursiveCallCount = 0;
                for (int j = i + 1; j < children.size() && j < i + 10; j++) {
                    if (children.get(j).getType() == ASTNode.Type.RETURN_STATEMENT) {
                        break;
                    }
                    if (children.get(j).getType() == ASTNode.Type.RECURSIVE_CALL) {
                        recursiveCallCount++;
                    }
                }
                if (recursiveCallCount >= 2) {
                    return true;
                }
            }

            if (child.getType() == ASTNode.Type.RETURN_STATEMENT) {
                int callsInReturn = countRecursiveCallsInNode(child);
                if (callsInReturn >= 2) {
                    return true;
                }
            }

            // Recursively check children
            if (checkForAdjacentRecursiveCalls(child)) {
                return true;
            }
        }

        return false;
    }

    private boolean checkReturnStatementsForMultipleRecursion(ASTNode node) {
        if (node.getType() == ASTNode.Type.RETURN_STATEMENT) {
            // Count recursive calls within this return statement
            int callsInReturn = countRecursiveCallsInNode(node);
            if (callsInReturn >= 2) {
                return true;
            }
        }

        // Check children
        for (ASTNode child : node.getChildren()) {
            if (checkReturnStatementsForMultipleRecursion(child)) {
                return true;
            }
        }
        return false;
    }

    private int countRecursiveCallsInNode(ASTNode node) {
        int count = 0;
        if (node.getType() == ASTNode.Type.RECURSIVE_CALL) {
            count++;
        }
        for (ASTNode child : node.getChildren()) {
            count += countRecursiveCallsInNode(child);
        }
        return count;
    }

    private boolean containsLoop(ASTNode node) {
        // Check if this function contains any loop (for, while, do-while)
        if (node.getType() == ASTNode.Type.FOR_LOOP ||
                node.getType() == ASTNode.Type.WHILE_LOOP ||
                node.getType() == ASTNode.Type.DO_WHILE_LOOP ||
                node.getType() == ASTNode.Type.LOOP) {
            return true;
        }

        // Check children
        for (ASTNode child : node.getChildren()) {
            if (containsLoop(child)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsFunctionCalls(ASTNode node) {
        // Check if this function makes calls to other functions (not recursive)
        if (node.getType() == ASTNode.Type.FUNCTION_CALL) {
            return true;
        }

        // Check children
        for (ASTNode child : node.getChildren()) {
            if (containsFunctionCalls(child)) {
                return true;
            }
        }
        return false;
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

    private TimeComplexity analyzeFunctionDeclaration(ASTNode functionNode) {
        // Analyze the body of the function declaration
        // First check if this function has recursion
        int recursiveCallCount = countRecursiveCalls(functionNode);

        String funcName = functionNode.getFunctionName();
        String nodeName = functionNode.getValue();

        if (recursiveCallCount > 0) {
            // This is a recursive function

            if (recursiveCallCount == 1) {
                // Single recursive call
                // Check if it's divide and conquer (like binary search with single path)
                boolean isDivideConquer = isDivideAndConquerRecursion(functionNode);
                if (isDivideConquer) {
                    return new TimeComplexity("Function Declaration", TimeComplexity.ComplexityType.LOGARITHMIC, 1);
                }
                // Linear recursion O(n) like factorial
                return new TimeComplexity("Function Declaration", TimeComplexity.ComplexityType.LINEAR, 1);
            } else if (recursiveCallCount >= 2) {
                // Multiple recursive calls - need to distinguish between:
                // Quick Sort: sequential calls + partition → O(n²) [CHECK FIRST!]
                // Binary Search: mutually exclusive branches (only 1 executes) → O(log n)
                // Fibonacci: both calls in same return expression → O(2^n)
                // Merge Sort: sequential calls + O(n) work with balanced division → O(n log
                // n)
                // Check for QuickSort pattern FIRST
                // This must come before other checks because quickSort has 2 recursive calls
                // but is NOT binary search or fibonacci pattern
                String functionCode = extractAllCode(functionNode).toLowerCase();

                // Check multiple sources for QuickSort identification
                boolean isQuickSort = false;

                // Check 1: functionName field contains "quick" or "partition"
                if (funcName != null) {
                    String lowerFuncName = funcName.toLowerCase();
                    if (lowerFuncName.contains("quick") || lowerFuncName.contains("partition")) {
                        isQuickSort = true;
                    }
                }

                // Check 2: nodeName (getValue) contains "quick" or "partition"
                if (!isQuickSort && nodeName != null) {
                    String lowerNodeName = nodeName.toLowerCase();
                    if (lowerNodeName.contains("quick") || lowerNodeName.contains("partition")) {
                        isQuickSort = true;
                    }
                }

                // Check 3: Function code contains "partition" or "pivot"
                boolean hasPartitionPattern = functionCode.contains("partition") ||
                        functionCode.contains("pivot");

                if (isQuickSort || hasPartitionPattern) {
                    // Quick Sort pattern: worst case O(n²) due to unbalanced partitions
                    return new TimeComplexity("Function Declaration", TimeComplexity.ComplexityType.QUADRATIC, 1);
                }

                // Check for binary search pattern
                boolean hasConditional = hasConditionalRecursion(functionNode);
                if (hasConditional) {
                    return new TimeComplexity("Function Declaration", TimeComplexity.ComplexityType.LOGARITHMIC, 1);
                }

                // Check for exponential pattern (fibonacci)
                boolean hasMultipleInReturn = hasMultipleRecursiveCallsInSameReturn(functionNode);
                if (hasMultipleInReturn) {
                    return new TimeComplexity("Function Declaration", TimeComplexity.ComplexityType.EXPONENTIAL, 1);
                }

                // Sequential divide-and-conquer (Merge Sort)

                // Check if there's work being done (loop, function calls, etc.)
                boolean hasLinearWork = containsLoop(functionNode) || containsFunctionCalls(functionNode);

                if (hasLinearWork) {
                    // Has O(n) work per level
                    // Check if it has balanced mid calculation (Merge Sort pattern)
                    boolean isDivideConquer = isDivideAndConquerRecursion(functionNode);

                    // Also check for merge-related patterns
                    boolean hasMergePattern = functionCode.contains("merge");

                    if (isDivideConquer && hasMergePattern) {
                        // Merge Sort pattern: T(n) = 2T(n/2) + O(n) = O(n log n)
                        return new TimeComplexity("Function Declaration", TimeComplexity.ComplexityType.LINEARITHMIC,
                                1);
                    } else {
                        // Default to O(n²) for sequential recursive calls with linear work
                        // unless clearly identified as balanced divide-and-conquer
                        return new TimeComplexity("Function Declaration", TimeComplexity.ComplexityType.QUADRATIC, 1);
                    }
                } else {
                    // Divide and conquer without obvious extra work
                    // Check if it's merge sort (has merge pattern and divide-conquer)
                    boolean isDivideConquer = isDivideAndConquerRecursion(functionNode);
                    boolean hasMergePattern = functionCode.contains("merge");

                    if (isDivideConquer && hasMergePattern) {
                        // Merge sort without inline loop (merge in separate function)
                        return new TimeComplexity("Function Declaration", TimeComplexity.ComplexityType.LINEARITHMIC,
                                1);
                    } else {
                        // assume O(n²) for unidentified patterns
                        // Better to overestimate than underestimate complexity
                        return new TimeComplexity("Function Declaration", TimeComplexity.ComplexityType.QUADRATIC, 1);
                    }
                }
            }
        }

        // Not recursive, analyze the body normally
        return analyzeBlock(functionNode);
    }

    private TimeComplexity analyzeFunctionCall(ASTNode functionCallNode) {
        String functionName = functionCallNode.getFunctionName();

        // Check for known library functions with specific complexities
        if (functionName != null) {
            // Examples of known time complexities
            if (functionName.equals("sort") || functionName.contains("Sort")) {
                return new TimeComplexity("Function Call", TimeComplexity.ComplexityType.LINEARITHMIC, 1);
            }
            if (functionName.equals("binarySearch") || functionName.contains("binarySearch")) {
                return new TimeComplexity("Function Call", TimeComplexity.ComplexityType.LOGARITHMIC, 1);
            }
        }

        return new TimeComplexity("Function Call", TimeComplexity.ComplexityType.CONSTANT, 1);
    }

    private ASTNode findFunctionDefinition(String functionName) {
        return null;
    }
}
