package mrix.nodes;

import java.util.List;

import mrix.DataType;
import mrix.Value;

public class ProgramNode implements Node {
    private final List<Node> instructions;
    public ProgramNode(List<Node> instructions) {
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
