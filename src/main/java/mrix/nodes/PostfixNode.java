package mrix.nodes;

import mrix.DataType;
import mrix.Token;

public class PostfixNode implements Node {
    private final Node primary;
    private final Token op;
    public PostfixNode(Node primary, Token op) {
        this.primary = primary;
        this.op = op;
    }
    public DataType accept(NodeVisitor visitor) {
        return visitor.visitPostfixNode(this);
    }

    public Node getPrimary() {
        return primary;
    }

    public Token getOp() {
        return op;
    }
}