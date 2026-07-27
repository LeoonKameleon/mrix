package mrix.ast;

import mrix.interpreter.value.Value;
import mrix.typing.type.DataType;

import java.util.List;

public class TupleNode extends AbstractNode {
    private final List<Node> elements;

    public TupleNode(List<Node> elements, int line) {
        super(line);
        this.elements = elements;
    }

    public List<Node> getElements() {
        return elements;
    }

    public DataType accept(NodeVisitor visitor) {
        return visitor.visitTupleNode(this);
    }

    public Value accept(InterpreterVisitor visitor) {
        return visitor.visitTupleNode(this);
    }
}
