import java.util.*;

public class CParser implements Parser {

    private int currentTokenIndex;
    private List<Token> tokens;
    private ASTNode pendingDoWhileNode;
    private Map<String, ASTNode> functionDefinitions = new HashMap<>();
    private Set<String> declaredFunctions = new HashSet<>();

    @Override
    public ASTNode parse(List<Token> tokens) {
        this.tokens = tokens;
        this.currentTokenIndex = 0;
        this.pendingDoWhileNode = null;
        this.functionDefinitions.clear();
        this.declaredFunctions.clear();

        Stack<ASTNode> stack = new Stack<>();
        ASTNode root = new ASTNode(ASTNode.Type.PROGRAM, "program");
        stack.push(root);

        while (currentTokenIndex < tokens.size()) {
            Token token = tokens.get(currentTokenIndex);
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
            currentTokenIndex++;
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
                handleConditional(token, stack);
                break;
            case "int":
            case "double":
            case "float":
            case "char":
            case "struct":
            case "void":
            case "long":
            case "short":
            case "signed":
            case "unsigned":
                handleTypeDeclaration(token, stack);
                break;
            case "return":
                handleReturn(token, stack);
                break;
            case "typedef":
                handleTypedef(token, stack);
                break;
            case "sizeof":
                handleSizeof(token, stack);
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

        parseLoopBoundExpressions(loopNode);
    }

    private void handleWhileLoop(Token token, Stack<ASTNode> stack) {

        if (pendingDoWhileNode != null) {
            parseLoopCondition(pendingDoWhileNode);
            pendingDoWhileNode = null;
        } else {
            ASTNode loopNode = new ASTNode(ASTNode.Type.WHILE_LOOP, token.getValue());
            stack.peek().addChild(loopNode);
            stack.push(loopNode);

            parseLoopCondition(loopNode);
        }
    }

    private void handleDoWhileLoop(Token token, Stack<ASTNode> stack) {
        ASTNode loopNode = new ASTNode(ASTNode.Type.DO_WHILE_LOOP, token.getValue());
        stack.peek().addChild(loopNode);
        stack.push(loopNode);
    }

    private void parseLoopBoundExpressions(ASTNode loopNode) {

        StringBuilder initialization = new StringBuilder();
        StringBuilder condition = new StringBuilder();
        StringBuilder update = new StringBuilder();

        int section = 0;
        int depth = 0;

        currentTokenIndex++;

        // Parse the three sections separated by semicolons
        while (currentTokenIndex < tokens.size()) {
            Token token = tokens.get(currentTokenIndex);
            String value = token.getValue();

            if (value.equals("(")) {
                depth++;
                if (depth > 1) {
                    // Nested parenthesis, add to current section
                    if (section == 0)
                        initialization.append(value);
                    else if (section == 1)
                        condition.append(value).append(" ");
                    else if (section == 2)
                        update.append(value);
                }
            } else if (value.equals(")")) {
                depth--;
                if (depth == 0) {
                    break; // End of for loop header
                } else {
                    // Nested parenthesis closing
                    if (section == 0)
                        initialization.append(value);
                    else if (section == 1)
                        condition.append(value).append(" ");
                    else if (section == 2)
                        update.append(value);
                }
            } else if (value.equals(";") && depth == 1) {
                section++;
            } else if (depth >= 1) {
                // Append to appropriate section
                if (section == 0) {
                    initialization.append(value).append(" ");
                } else if (section == 1) {
                    condition.append(value).append(" ");
                } else if (section == 2) {
                    update.append(value).append(" ");
                }
            }
            currentTokenIndex++;
        }

        loopNode.setInitialization(initialization.toString().trim());
        loopNode.setCondition(condition.toString().trim());
        loopNode.setUpdate(update.toString().trim());
    }

    private void parseLoopCondition(ASTNode loopNode) {
        StringBuilder condition = new StringBuilder();

        // Skip to opening parenthesis
        while (currentTokenIndex < tokens.size() && !tokens.get(currentTokenIndex).getValue().equals("(")) {
            currentTokenIndex++;
        }
        currentTokenIndex++;

        while (currentTokenIndex < tokens.size()) {
            Token token = tokens.get(currentTokenIndex);
            String value = token.getValue();

            if (value.equals(")")) {
                break;
            }
            condition.append(value).append(" ");
            currentTokenIndex++;
        }

        loopNode.setCondition(condition.toString().trim());
    }

    private Token getNextToken() {
        if (currentTokenIndex + 1 < tokens.size()) {
            return tokens.get(++currentTokenIndex);
        }
        throw new IllegalStateException("Unexpected end of tokens while parsing");
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

    private void handleReturn(Token token, Stack<ASTNode> stack) {
        ASTNode returnNode = new ASTNode(ASTNode.Type.RETURN_STATEMENT, token.getValue());
        stack.peek().addChild(returnNode);
    }

    private void handleTypedef(Token token, Stack<ASTNode> stack) {
        ASTNode typedefNode = new ASTNode(ASTNode.Type.TYPEDEF, token.getValue());
        stack.peek().addChild(typedefNode);
    }

    private void handleSizeof(Token token, Stack<ASTNode> stack) {
        ASTNode sizeofNode = new ASTNode(ASTNode.Type.SIZEOF, token.getValue());
        stack.peek().addChild(sizeofNode);
    }

    private void handleIdentifier(Token token, Stack<ASTNode> stack) {
        // Check if this identifier is followed by '(' which indicates a function call
        // or declaration
        if (currentTokenIndex + 1 < tokens.size()) {
            Token nextToken = tokens.get(currentTokenIndex + 1);
            if (nextToken.getValue().equals("(")) {
                // Check if this is a function declaration or call
                if (isFunctionDeclaration(currentTokenIndex)) {
                    handleFunctionDeclaration(token, stack);
                } else {
                    // This is a function call
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
                        keyword.equals("float") || keyword.equals("char") || keyword.equals("long") ||
                        keyword.equals("short") || keyword.equals("unsigned") || keyword.equals("signed")) {

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

        // Store this function in our map
        functionDefinitions.put(functionName, functionNode);
        declaredFunctions.add(functionName);

        currentTokenIndex++;
        int parenDepth = 0;
        while (currentTokenIndex < tokens.size()) {
            String val = tokens.get(currentTokenIndex).getValue();
            if (val.equals("("))
                parenDepth++;
            else if (val.equals(")")) {
                parenDepth--;
                if (parenDepth == 0)
                    break;
            }
            currentTokenIndex++;
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

        currentTokenIndex++;
        int parenDepth = 0;
        while (currentTokenIndex < tokens.size()) {
            String val = tokens.get(currentTokenIndex).getValue();
            if (val.equals("("))
                parenDepth++;
            else if (val.equals(")")) {
                parenDepth--;
                if (parenDepth == 0)
                    break;
            }
            currentTokenIndex++;
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
