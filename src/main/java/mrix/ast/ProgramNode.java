package mrix.ast;

import java.util.List;

import mrix.interpreter.value.Value;
import mrix.typing.type.DataType;

public class ProgramNode extends AbstractNode {
    private final List<Node> instructions;
    public ProgramNode(List<Node> instructions, int line) {
        super(line);
        this.instructions = instructions;
    }
    public DataType accept(NodeVisitor visitor) {
        return visitor.visitProgramNode(this);
    }

    public Value accept(InterpreterVisitor visitor) {
        return visitor.visitProgramNode(this);
    }

    public List<Node> getInstructions() {
        return instructions;
    }
}
