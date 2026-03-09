package mrix.nodes;

import java.util.List;

import mrix.DataType;
import mrix.Token;

public class VariableNode implements Node {
    private final Token id;
    private final List<Node> expressionList;
    public VariableNode(Token id, List<Node> expressionList) {
        this.id = id;
        this.expressionList = expressionList;
    }
    public DataType accept(NodeVisitor visitor) {
        return visitor.visitVariableNode(this);
    }

    public Token getId() {
        return id;
    }

    public List<Node> getExpressionList() {
        return expressionList;
    }
}