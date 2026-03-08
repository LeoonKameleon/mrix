package mrix.nodes;

import mrix.TokenType;

public class UnaryOpNode implements Node {
    public TokenType op;
    public Node unaryExpression;
    public UnaryOpNode(TokenType op, Node unaryExpression) {
        this.op = op;
        this.unaryExpression = unaryExpression;
    }
}
