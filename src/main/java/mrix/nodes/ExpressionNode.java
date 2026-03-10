package mrix.nodes;

import mrix.DataType;
import mrix.Value;

public class ExpressionNode implements Node {
    private final Node orExpression;
    public ExpressionNode(Node orExpression) {
        this.orExpression = orExpression;
    }
    public DataType accept(NodeVisitor visitor) {
        return visitor.visitExpressionNode(this);
    }

    public Value accept(InterpreterVisitor visitor) {
        return visitor.visitExpressionNode(this);
    }

    public Node getOrExpression() {
        return orExpression;
    }
}