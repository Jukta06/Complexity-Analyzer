import java.util.*;

public class JavaParser implements Parser {
    private List<Token> tokens;
    private int currentIndex = 0;
    private ASTNode pendingDoWhileNode;
    private Map<String, ASTNode> functionDefinitions = new HashMap<>();
    private Set<String> declaredFunctions = new HashSet<>();

    @Override
    public ASTNode parse(List<Token> tokens) {
        this.tokens = tokens;
        this.pendingDoWhileNode = null;
        this.functionDefinitions.clear();
        this.declaredFunctions.clear();
        Stack<ASTNode> stack = new Stack<>();
        ASTNode root = new ASTNode(ASTNode.Type.PROGRAM, "program");
        stack.push(root);

        while (currentIndex < tokens.size()) {
            Token token = tokens.get(currentIndex);
            switch (token.getType()) {
                case KEYWORD:
                    handleKeyword(token, stack);
                    break;
                case IDENTIFIER:
                    handleIdentifier(token, stack);
                    break;
                case OPERATOR:
                case LITERAL:
                case SEPARATOR:
                    handleOtherTokens(token, stack);
                    break;
                default:
                    break;
            }
            currentIndex++;
        }

        return root;
    }

    private void handleKeyword(Token token, Stack<ASTNode> stack) {
        switch (token.getValue()) {
            case "for":
                handleForLoop(token, stack);
                break;
            case "while":
                handleWhileLoop(token, stack);
                break;
            case "do":
                handleDoWhileLoop(token, stack);
                break;
            case "if":
            case "else":
            case "switch":
            case "case":
                handleConditional(token, stack);
                break;
            case "int":
            case "double":
            case "float":
            case "char":
            case "boolean":
            case "byte":
            case "short":
            case "long":
                handleArrayOrTypeDeclaration(token, stack);
                break;
            case "void":
            case "class":
            case "interface":
                handleTypeDeclaration(token, stack);
                break;
            case "return":
                handleReturn(token, stack);
                break;
            case "new":
                handleNewInstance(token, stack);
                break;
            case "this":
            case "super":
                handleSpecialReference(token, stack);
                break;
            default:
                handleOtherTokens(token, stack);
                break;
        }
    }

    private void handleForLoop(Token token, Stack<ASTNode> stack) {
        ASTNode loopNode = new ASTNode(ASTNode.Type.FOR_LOOP, token.getValue());
        stack.peek().addChild(loopNode);
        stack.push(loopNode);

        parseLoopBounds(loopNode);
    }

    private void parseLoopBounds(ASTNode loopNode) {
        currentIndex++;
        if (currentIndex < tokens.size() && tokens.get(currentIndex).getValue().equals("(")) {
            currentIndex++;

            // Parse initialization
            StringBuilder initialization = new StringBuilder();
            while (currentIndex < tokens.size()) {
                Token token = tokens.get(currentIndex);
                if (token.getValue().equals(";")) {
                    currentIndex++;
                    break;
                }
                initialization.append(token.getValue()).append(" ");
                currentIndex++;
            }

            // Parse condition
            StringBuilder condition = new StringBuilder();
            while (currentIndex < tokens.size()) {
                Token token = tokens.get(currentIndex);
                if (token.getValue().equals(";")) {
                    currentIndex++;
                    break;
                }
                condition.append(token.getValue()).append(" ");
                currentIndex++;
            }

            // Parse update
            StringBuilder update = new StringBuilder();
            while (currentIndex < tokens.size()) {
                Token token = tokens.get(currentIndex);
                if (token.getValue().equals(")")) {
                    currentIndex++;
                    break;
                }
                update.append(token.getValue()).append(" ");
                currentIndex++;
            }

            // Set the condition on the loop node
            loopNode.setInitialization(initialization.toString().trim());
            loopNode.setCondition(condition.toString().trim());
            loopNode.setUpdate(update.toString().trim());
        }
    }

    private void parseExpression(ASTNode parentNode) {
        while (currentIndex < tokens.size()) {
            Token token = tokens.get(currentIndex);
            if (token.getValue().equals(";") || token.getValue().equals(")")) {
                currentIndex++;
                break;
            }
            parentNode.addChild(new ASTNode(ASTNode.Type.EXPRESSION, token.getValue()));
            currentIndex++;
        }
    }

    private void handleWhileLoop(Token token, Stack<ASTNode> stack) {
        // Check if this 'while' is part of a do-while loop
        if (pendingDoWhileNode != null) {
            parseWhileCondition(pendingDoWhileNode);
            pendingDoWhileNode = null;
        } else {
            ASTNode loopNode = new ASTNode(ASTNode.Type.WHILE_LOOP, token.getValue());
            stack.peek().addChild(loopNode);
            stack.push(loopNode);

            // Parse while loop condition
            parseWhileCondition(loopNode);
        }
    }

    private void parseWhileCondition(ASTNode loopNode) {
        currentIndex++;
        if (currentIndex < tokens.size() && tokens.get(currentIndex).getValue().equals("(")) {
            currentIndex++;

            StringBuilder condition = new StringBuilder();
            while (currentIndex < tokens.size()) {
                Token t = tokens.get(currentIndex);
                if (t.getValue().equals(")")) {
                    currentIndex++;
                    break;
                }
                condition.append(t.getValue()).append(" ");
                currentIndex++;
            }
            loopNode.setCondition(condition.toString().trim());
        }
    }

    private void handleDoWhileLoop(Token token, Stack<ASTNode> stack) {
        ASTNode loopNode = new ASTNode(ASTNode.Type.DO_WHILE_LOOP, token.getValue());
        stack.peek().addChild(loopNode);
        stack.push(loopNode);
    }

    private void handleConditional(Token token, Stack<ASTNode> stack) {
        ASTNode conditionalNode = new ASTNode(ASTNode.Type.CONDITIONAL, token.getValue());
        stack.peek().addChild(conditionalNode);
        stack.push(conditionalNode);
    }

    private void handleTypeDeclaration(Token token, Stack<ASTNode> stack) {
        ASTNode typeDeclNode = new ASTNode(ASTNode.Type.TYPE_DECLARATION, token.getValue());
        stack.peek().addChild(typeDeclNode);
    }

    private void handleArrayOrTypeDeclaration(Token token, Stack<ASTNode> stack) {
        ASTNode currentNode = stack.peek();
        ASTNode typeDeclNode = new ASTNode(ASTNode.Type.TYPE_DECLARATION, token.getValue());
        currentNode.addChild(typeDeclNode);

        currentIndex++;
        if (currentIndex < tokens.size()) {
            Token nextToken = tokens.get(currentIndex);
            if (nextToken.getValue().equals("[")) {
                ASTNode arrayDeclNode = new ASTNode(ASTNode.Type.ARRAY_DECLARATION, token.getValue());
                typeDeclNode.addChild(arrayDeclNode);

                currentIndex++;
                if (currentIndex < tokens.size()) {
                    nextToken = tokens.get(currentIndex);
                    if (nextToken.getType() == Token.Type.LITERAL) {
                        ASTNode sizeNode = new ASTNode(ASTNode.Type.LITERAL, nextToken.getValue());
                        arrayDeclNode.addChild(sizeNode);

                        currentIndex++;
                        if (currentIndex < tokens.size()) {
                            nextToken = tokens.get(currentIndex);
                            if (nextToken.getValue().equals("]")) {
                                currentIndex++;
                            }
                        }
                    }
                }
            }
        }
    }

    private void handleReturn(Token token, Stack<ASTNode> stack) {
        ASTNode returnNode = new ASTNode(ASTNode.Type.RETURN_STATEMENT, token.getValue());
        stack.peek().addChild(returnNode);
    }

    private void handleNewInstance(Token token, Stack<ASTNode> stack) {
        ASTNode newInstanceNode = new ASTNode(ASTNode.Type.NEW_INSTANCE, token.getValue());
        stack.peek().addChild(newInstanceNode);
    }

    private void handleSpecialReference(Token token, Stack<ASTNode> stack) {
        ASTNode specialRefNode = new ASTNode(ASTNode.Type.SPECIAL_REFERENCE, token.getValue());
        stack.peek().addChild(specialRefNode);
    }

    private void handleIdentifier(Token token, Stack<ASTNode> stack) {
        if (currentIndex + 1 < tokens.size()) {
            Token nextToken = tokens.get(currentIndex + 1);
            if (nextToken.getValue().equals("(")) {
                if (isFunctionDeclaration(currentIndex)) {
                    handleFunctionDeclaration(token, stack);
                } else {
                    handleFunctionCall(token, stack);
                }
                return;
            }
        }
        handleOtherTokens(token, stack);
    }

    private boolean isFunctionDeclaration(int identifierIndex) {
        if (identifierIndex > 0) {
            Token prevToken = tokens.get(identifierIndex - 1);
            if (prevToken.getType() == Token.Type.KEYWORD) {
                String keyword = prevToken.getValue();
                if (keyword.equals("int") || keyword.equals("void") || keyword.equals("double") ||
                        keyword.equals("float") || keyword.equals("char") || keyword.equals("boolean") ||
                        keyword.equals("long") || keyword.equals("short") || keyword.equals("byte")) {
                    int tempIndex = identifierIndex + 1;
                    int parenDepth = 0;
                    while (tempIndex < tokens.size()) {
                        String val = tokens.get(tempIndex).getValue();
                        if (val.equals("("))
                            parenDepth++;
                        else if (val.equals(")")) {
                            parenDepth--;
                            if (parenDepth == 0) {
                                tempIndex++;
                                break;
                            }
                        }
                        tempIndex++;
                    }

                    while (tempIndex < tokens.size()) {
                        String val = tokens.get(tempIndex).getValue();
                        if (val.equals("{"))
                            return true;
                        if (val.equals(";"))
                            return false;
                        tempIndex++;
                    }
                }
            }
        }
        return false;
    }

    private void handleFunctionDeclaration(Token token, Stack<ASTNode> stack) {
        String functionName = token.getValue();
        ASTNode functionNode = new ASTNode(ASTNode.Type.FUNCTION_DECLARATION, functionName);
        functionNode.setFunctionName(functionName);
        stack.peek().addChild(functionNode);
        stack.push(functionNode);

        functionDefinitions.put(functionName, functionNode);
        declaredFunctions.add(functionName);

        currentIndex++;
        int parenDepth = 0;
        while (currentIndex < tokens.size()) {
            String val = tokens.get(currentIndex).getValue();
            if (val.equals("("))
                parenDepth++;
            else if (val.equals(")")) {
                parenDepth--;
                if (parenDepth == 0)
                    break;
            }
            currentIndex++;
        }
    }

    private void handleFunctionCall(Token token, Stack<ASTNode> stack) {
        String functionName = token.getValue();
        ASTNode currentFunction = findCurrentFunction(stack);

        // Check if this is a recursive call
        if (currentFunction != null && currentFunction.getFunctionName() != null &&
                currentFunction.getFunctionName().equals(functionName)) {
            // Recursive call
            ASTNode recursiveNode = new ASTNode(ASTNode.Type.RECURSIVE_CALL, functionName);
            recursiveNode.setFunctionName(functionName);
            recursiveNode.setCode(functionName);
            stack.peek().addChild(recursiveNode);
        } else {
            // Regular function call
            ASTNode functionCallNode = new ASTNode(ASTNode.Type.FUNCTION_CALL, functionName);
            functionCallNode.setFunctionName(functionName);
            functionCallNode.setCode(functionName);
            stack.peek().addChild(functionCallNode);
        }

        currentIndex++;
        int parenDepth = 0;
        while (currentIndex < tokens.size()) {
            String val = tokens.get(currentIndex).getValue();
            if (val.equals("("))
                parenDepth++;
            else if (val.equals(")")) {
                parenDepth--;
                if (parenDepth == 0)
                    break;
            }
            currentIndex++;
        }
    }

    private ASTNode findCurrentFunction(Stack<ASTNode> stack) {
        for (int i = stack.size() - 1; i >= 0; i--) {
            ASTNode node = stack.get(i);
            if (node.getType() == ASTNode.Type.FUNCTION_DECLARATION ||
                    node.getType() == ASTNode.Type.FUNCTION_DEFINITION) {
                return node;
            }
        }
        return null;
    }

    private void handleOtherTokens(Token token, Stack<ASTNode> stack) {
        ASTNode currentNode = stack.peek();
        currentNode.addChild(new ASTNode(ASTNode.Type.EXPRESSION, token.getValue()));

        if (token.getValue().equals("}") && currentNode.getType() != ASTNode.Type.PROGRAM) {
            if (currentNode.getType() == ASTNode.Type.DO_WHILE_LOOP) {
                pendingDoWhileNode = currentNode;
            }
            stack.pop();
        }
    }
}
