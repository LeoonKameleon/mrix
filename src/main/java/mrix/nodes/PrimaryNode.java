package mrix.nodes;

import mrix.interpreter.Value;
import mrix.tokens.Token;
import mrix.typechecker.DataType;

public class PrimaryNode extends AbstractNode {
    private final Token value;
    private Value cachedValue;

    public PrimaryNode(Token value, int line) {
        super(line);
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

    public Value getCachedValue() {
        return cachedValue;
    }

    public void setCachedValue(Value value) {
        cachedValue = value;
    }
}