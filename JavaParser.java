import java.util.*;

public class JavaParser implements Parser {
    private List<Token> tokens;
    private int currentIndex = 0;

    @Override
    public ASTNode parse(List<Token> tokens) {
        this.tokens = tokens;
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
        stack.pop();
    }

    private void parseLoopBounds(ASTNode loopNode) {
        currentIndex++;
        if (currentIndex < tokens.size() && tokens.get(currentIndex).getValue().equals("(")) {
            currentIndex++;
            
            ASTNode initNode = new ASTNode(ASTNode.Type.INITIALIZATION, "initialization");
            parseExpression(initNode);
            loopNode.addChild(initNode);

           
            ASTNode conditionNode = new ASTNode(ASTNode.Type.CONDITION, "condition");
            parseExpression(conditionNode);
            loopNode.addChild(conditionNode);

            
            ASTNode updateNode = new ASTNode(ASTNode.Type.UPDATE, "update");
            parseExpression(updateNode);
            loopNode.addChild(updateNode);

          
            if (currentIndex < tokens.size() && tokens.get(currentIndex).getValue().equals(")")) {
                currentIndex++;
            }
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
        ASTNode loopNode = new ASTNode(ASTNode.Type.WHILE_LOOP, token.getValue());
        stack.peek().addChild(loopNode);
        stack.push(loopNode);
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

    private void handleOtherTokens(Token token, Stack<ASTNode> stack) {
        ASTNode currentNode = stack.peek();
        currentNode.addChild(new ASTNode(ASTNode.Type.EXPRESSION, token.getValue()));

        if (token.getValue().equals("}") && currentNode.getType() != ASTNode.Type.PROGRAM) {
            stack.pop();
        }
    }
}
