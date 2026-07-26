package mrix.ast;

import mrix.interpreter.value.Value;
import mrix.typing.type.DataType;

public class IterNode extends AbstractNode {
    private final Node id;
    private final Node iterable;
    private final Node instruction;

    public IterNode(Node id, Node iterable, Node instruction, int line) {
        super(line);
        this.id = id;
        this.iterable = iterable;
        this.instruction = instruction;
    }

    public Node getId() {
        return id;
    }

    public Node getIterable() {
        return iterable;
    }

    public Node getInstruction() {
        return instruction;
    }

    public DataType accept(NodeVisitor visitor) {
        return visitor.visitIterNode(this);
    }

    public Value accept(InterpreterVisitor visitor) {
        return visitor.visitIterNode(this);
    }
}
