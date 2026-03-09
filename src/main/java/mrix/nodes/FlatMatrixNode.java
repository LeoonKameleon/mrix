package mrix.nodes;

import java.util.List;

import mrix.DataType;

public class FlatMatrixNode implements Node {
    private final List<Node> expressionList;
    public FlatMatrixNode(List<Node> expressionList) {
        this.expressionList = expressionList;
    }
    public DataType accept(NodeVisitor visitor) {
        return visitor.visitFlatMatrixNode(this);
    }

    public List<Node> getExpressionList() {
        return expressionList;
    }
}