package mrix.nodes;

import mrix.interpreter.Value;
import mrix.tokens.Token;
import mrix.typechecker.DataType;

public class UnaryOpNode extends AbstractNode {
    private final Token op;
    private final Node unaryExpression;
    public UnaryOpNode(Token op, Node unaryExpression, int line) {
        super(line);
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
