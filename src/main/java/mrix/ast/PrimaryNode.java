package mrix.ast;

import mrix.interpreter.value.Value;
import mrix.scanner.token.Token;
import mrix.typing.type.DataType;

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