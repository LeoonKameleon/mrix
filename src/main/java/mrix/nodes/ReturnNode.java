package mrix.nodes;

import mrix.DataType;

public class ReturnNode implements Node {
    private final Node expression;
    public ReturnNode(Node expression) {
        this.expression = expression;
    }
    public DataType accept(NodeVisitor visitor) {
        return visitor.visitReturnNode(this);
    }

    public Node getExpression() {
        return expression;
    }
}
