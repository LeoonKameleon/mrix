package mrix.nodes;

import mrix.DataType;
import mrix.TokenType;

public class PostfixNode implements Node {
    private final Node primary;
    private final TokenType op;
    public PostfixNode(Node primary, TokenType op) {
        this.primary = primary;
        this.op = op;
    }
    public DataType accept(NodeVisitor visitor) {
        return visitor.visitPostfixNode(this);
    }

    public Node getPrimary() {
        return primary;
    }

    public TokenType getOp() {
        return op;
    }
}