package mrix.nodes;

import mrix.interpreter.Value;
import mrix.typechecker.DataType;

public class ReturnNode extends AbstractNode {
    private final Node expression;
    public ReturnNode(Node expression, int line) {
        super(line);
        this.expression = expression;
    }
    public DataType accept(NodeVisitor visitor) {
        return visitor.visitReturnNode(this);
    }

    public Value accept(InterpreterVisitor visitor) {
        return visitor.visitReturnNode(this);
    }

    public Node getExpression() {
        return expression;
    }
}
