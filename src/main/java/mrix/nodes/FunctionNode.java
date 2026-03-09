package mrix.nodes;

import java.util.List;

import mrix.DataType;
import mrix.Token;

public class FunctionNode implements Node {
    private final Token id;
    private final List<Token> parameterList;
    private final Node instruction;
    public FunctionNode(Token id, List<Token> parameterList, Node instruction) {
        this.id = id;
        this.parameterList = parameterList;
        this.instruction = instruction;
    }
    public DataType accept(NodeVisitor visitor) {
        return visitor.visitFunctionNode(this);
    }

    public Token getId() {
        return id;
    }

    public List<Token> getParameterList() {
        return parameterList;
    }

    public Node getInstruction() {
        return instruction;
    }
}