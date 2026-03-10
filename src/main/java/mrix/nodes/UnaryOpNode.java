package mrix.nodes;

import mrix.DataType;
import mrix.Token;
import mrix.Value;

public class UnaryOpNode implements Node {
    private final Token op;
    private final Node unaryExpression;
    public UnaryOpNode(Token op, Node unaryExpression) {
        this.op = op;
        this.unaryExpression = unaryExpression;
    }
    public DataType accept(NodeVisitor visitor) {
        return visitor.visitUnaryOpNode(this);
    }

    public Value accept(InterpreterVisitor visitor) {
        return visitor.visitUnaryOpNode(this);
    }

    public Token getOp() {
        return op;
    }

    public Node getUnaryExpression() {
        return unaryExpression;
    }
}
