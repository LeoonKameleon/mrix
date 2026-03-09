package mrix.nodes;

import mrix.DataType;

public class BlockNode implements Node {
    private final Node instructions;
    public BlockNode(Node instructions) {
        this.instructions = instructions;
    }
    public DataType accept(NodeVisitor visitor) {
        return visitor.visitBlockNode(this);
    }

    public Node getInstructions() {
        return instructions;
    }
}