package mrix.nodes;

public class BlockNode implements Node {
    public Node instructions;
    public BlockNode(Node instructions) {
        this.instructions = instructions;
    }
}