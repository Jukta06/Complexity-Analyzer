import java.util.*;

public class CParser implements Parser {

    private int currentTokenIndex; 
    private List<Token> tokens;   

    @Override
    public ASTNode parse(List<Token> tokens) {
        this.tokens = tokens;
        this.currentTokenIndex = 0; 

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
        ASTNode loopNode = new ASTNode(ASTNode.Type.WHILE_LOOP, token.getValue());
        stack.peek().addChild(loopNode);
        stack.push(loopNode);

        parseLoopCondition(loopNode);
    }

    private void handleDoWhileLoop(Token token, Stack<ASTNode> stack) {
        ASTNode loopNode = new ASTNode(ASTNode.Type.DO_WHILE_LOOP, token.getValue());
        stack.peek().addChild(loopNode);
        stack.push(loopNode);
    }

    private void parseLoopBoundExpressions(ASTNode loopNode) {
        Token initializationToken = getNextToken();
        Token conditionToken = getNextToken();
        Token updateToken = getNextToken();

        loopNode.setInitialization(initializationToken.getValue());
        loopNode.setCondition(conditionToken.getValue());
        loopNode.setUpdate(updateToken.getValue());
    }

    private void parseLoopCondition(ASTNode loopNode) {
        Token conditionToken = getNextToken();
        loopNode.setCondition(conditionToken.getValue());
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

    private void handleOtherTokens(Token token, Stack<ASTNode> stack) {
        ASTNode currentNode = stack.peek();
        currentNode.addChild(new ASTNode(ASTNode.Type.EXPRESSION, token.getValue()));

        if (token.getValue().equals("}") && currentNode.getType() != ASTNode.Type.PROGRAM) {
            stack.pop();
        }
    }
}
