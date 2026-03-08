package mrix.nodes;

public class IfNode implements Node {
    public Node condition;
    public Node thenNode;
    public Node elseNode;
    public IfNode(Node expression, Node instruction1, Node instruction2) {
        this.condition = expression;
        this.thenNode = instruction1;
        this.elseNode = instruction2;
    }
}