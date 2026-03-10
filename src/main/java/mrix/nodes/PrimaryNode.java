package mrix.nodes;

import mrix.DataType;
import mrix.Token;
import mrix.Value;

public class PrimaryNode implements Node {
    private final Token value;
    public PrimaryNode(Token value) {
        this.value = value;
    }
    public DataType accept(NodeVisitor visitor) {
        return visitor.visitPrimaryNode(this);
    }

    public Value accept(InterpreterVisitor visitor) {
        return visitor.visitPrimaryNode(this);
    }

    public Token getValue() {
        return value;
    }
}