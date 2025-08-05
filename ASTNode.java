import java.util.*;

public class ASTNode {
    public enum Type {
        PROGRAM,
        FUNCTION,
        STATEMENT,
        EXPRESSION,
        LOOP,
        CONDITIONAL,
        VARIABLE_DECLARATION,
        ARRAY_DECLARATION,
        FUNCTION_CALL,
        RETURN_STATEMENT,
        BLOCK,
        CLASS_OR_INTERFACE,
        NEW_INSTANCE,
        SPECIAL_REFERENCE,
        TYPE_DECLARATION,
        TYPEDEF,
        SIZEOF,
        IF_STATEMENT,
        RECURSIVE_CALL,
        LITERAL,
        FUNCTION_DECLARATION,
        FOR_LOOP,
        WHILE_LOOP,
        DO_WHILE_LOOP,
        BINARY_SEARCH,
        MERGE_SORT,
        QUICK_SORT,
        VARIABLE,
        OPERATION,
        ASSIGNMENT,
        FUNCTION_DEFINITION,
        UNKNOWN,
        CONDITION,
        PARAMETERS,
        RETURN,
        INCREMENT,
        RECURSION,
        INITIALIZATION,
        UPDATE
    }

    public enum ComplexityType {
        CONSTANT,
        LOGARITHMIC,
        LINEAR,
        LINEARITHMIC,
        QUADRATIC,
        CUBIC,
        EXPONENTIAL
    }

    private final Type type;
    private final String name;
    private String code;
    private String incrementExpression;
    private String condition;
    private String initialization;
    private String update;
    private final List<ASTNode> children;


    private List<Integer> dimensions;
    private String varName;
    private String varType;
    private boolean isArray;
    private boolean isTwoDimensional;
    private ASTNode parent;

    private int complexityDegree;
    private String functionName;

    public ASTNode(Type type) {
        this(type, null);
    }

    public ASTNode(Type type, String name) {
        this.type = type;
        this.name = name;
        this.children = new ArrayList<>();
        this.condition = null;
        this.initialization = null;
        this.update = null;
        this.dimensions = new ArrayList<>();
        this.isArray = false;
        this.isTwoDimensional = false;
        this.complexityDegree = 0;
    }

    public void addChild(ASTNode child) {
        children.add(child);
    }

    public Type getType() {
        return type;
    }

    public String getValue() {
        return name;
    }

    public List<ASTNode> getChildren() {
        return children;
    }

    public String getFunctionName() {
        return this.functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    public String getIncrement() {
        return incrementExpression;
    }

    public void setIncrement(String incrementExpression) {
        this.incrementExpression = incrementExpression;
    }

    public ASTNode getParent() {
        return parent;
    }

    public void setParent(ASTNode parent) {
        this.parent = parent;
    }

    public ASTNode getBody() {
        if (this.type == Type.FUNCTION) {
            for (ASTNode child : children) {
                if (child.getType() == Type.BLOCK) {
                    return child; 
                }
            }
        }
        throw new IllegalStateException("Function body not found for function: " + this.name);
    }

    public boolean isLoop() {
        return type == Type.FOR_LOOP || type == Type.WHILE_LOOP || type == Type.DO_WHILE_LOOP;
    }

    public boolean variableInCondition() {
        return type == Type.CONDITION; 
    }

    public boolean simpleIncrement() {
        return type == Type.INCREMENT; 
    }

    public List<Integer> getDimensions() {
        return dimensions;
    }

    public void addDimension(int dimension) {
        dimensions.add(dimension);
    }

    public String getVarName() {
        return varName;
    }

    public void setVarName(String varName) {
        this.varName = varName;
    }

    public String getVarType() {
        return varType;
    }

    public void setVarType(String varType) {
        this.varType = varType;
    }

    public boolean isArray() {
        return isArray;
    }

    public void setArray(boolean isArray) {
        this.isArray = isArray;
    }

    public boolean twoDimensional() {
        return isTwoDimensional;
    }

    public void setTwoDimensional(boolean isTwoDimensional) {
        this.isTwoDimensional = isTwoDimensional;
    }


    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getInitialization() {
        return initialization;
    }

    public void setInitialization(String initialization) {
        this.initialization = initialization;
    }

    public String getUpdate() {
        return update;
    }

    public void setUpdate(String update) {
        this.update = update;
    }

    public String getCondition() {
        return condition;
    }

    public int getSize() {
        int size = 1;
        for (int dimension : dimensions) {
            size *= dimension;
        }
        return size;
    }

    public boolean size() {
        return !dimensions.isEmpty();
    }

    public int getComplexityDegree() {
        return complexityDegree;
    }

    public void setComplexityDegree(int complexityDegree) {
        this.complexityDegree = complexityDegree;
    }

    public void addChildNodes(List<ASTNode> nodes) {
        children.addAll(nodes);
    }

    public List<ASTNode> getParameters() {
        List<ASTNode> parameters = new ArrayList<>();
        if (type == Type.FUNCTION_CALL) {
            for (ASTNode child : children) {
                if (child.getType() == Type.EXPRESSION || child.getType() == Type.VARIABLE) {
                    parameters.add(child);
                }
            }
        }
        return parameters;
    }

    public String getCode() {
        return name;
    }

    public void setCode(String code) {
        this.code = code;
    }


    @Override
    public String toString() {
        return type + ": " + (name != null ? name : "");
    }

}
