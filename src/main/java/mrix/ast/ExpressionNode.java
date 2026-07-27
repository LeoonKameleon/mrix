package mrix.ast;

import mrix.interpreter.value.Value;
import mrix.typing.type.DataType;

public class ExpressionNode extends AbstractNode {
    private final Node orExpression;

    public ExpressionNode(Node orExpression, int line) {
        super(line);
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