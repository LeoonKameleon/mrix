package mrix.ast;

import mrix.interpreter.value.Value;
import mrix.typing.type.DataType;

public class BlockNode extends AbstractNode {
    private final Node instructions;
    public BlockNode(Node instructions, int line) {
        super(line);
        this.instructions = instructions;
    }
    public DataType accept(NodeVisitor visitor) {
        return visitor.visitBlockNode(this);
    }

    public Value accept(InterpreterVisitor visitor) {
        return visitor.visitBlockNode(this);
    }

    public Node getInstructions() {
        return instructions;
    }
}