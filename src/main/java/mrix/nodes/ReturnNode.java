package mrix.nodes;

public class ReturnNode implements Node {
    public Node expression;
    public ReturnNode(Node expression) {
        this.expression = expression;
    }
}
