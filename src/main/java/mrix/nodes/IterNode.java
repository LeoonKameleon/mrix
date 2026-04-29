package mrix.nodes;

import mrix.interpreter.Value;
import mrix.tokens.Token;
import mrix.typechecker.DataType;

public class IterNode extends AbstractNode {
    private final Token id;
    private final Node iterable;
    private final Node instruction;

    public IterNode(Token id, Node iterable, Node instruction, int line) {
        super(line);
        this.id = id;
        this.iterable = iterable;
        this.instruction = instruction;
    }

    public Token getId() {
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
