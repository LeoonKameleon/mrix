package mrix.nodes;

public class WhileNode implements Node {
    public Node condition;
    public Node thenNode;
    public WhileNode(Node expression, Node instruction) {
        this.condition = expression;
        this.thenNode = instruction;
    }
}