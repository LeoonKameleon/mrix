package mrix.ast;

import mrix.interpreter.value.Value;
import mrix.typing.type.DataType;

import java.util.List;

public class HMapNode extends AbstractNode {
    private final List<Node> keys;
    private final List<Node> values;

    public HMapNode(List<Node> keys, List<Node> values, int line) {
        super(line);
        this.keys = keys;
        this.values = values;
    }

    public List<Node> getKeys() {
        return keys;
    }

    public List<Node> getValues() {
        return values;
    }

    @Override
    public DataType accept(NodeVisitor visitor) {
        return visitor.visitHMapNode(this);
    }

    @Override
    public Value accept(InterpreterVisitor visitor) {
        return visitor.visitHMapNode(this);
    }
}
