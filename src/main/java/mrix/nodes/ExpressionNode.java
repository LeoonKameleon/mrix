package mrix.nodes;

public class ExpressionNode implements Node {
    public Node orExpression;
    public ExpressionNode(Node orExpression) {
        this.orExpression = orExpression;
    }
}