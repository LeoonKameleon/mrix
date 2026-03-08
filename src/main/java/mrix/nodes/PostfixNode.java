package mrix.nodes;

import mrix.TokenType;

public class PostfixNode implements Node {
    public Node primary;
    public TokenType op;
    public PostfixNode(Node primary, TokenType op) {
        this.primary = primary;
        this.op = op;
    }
}