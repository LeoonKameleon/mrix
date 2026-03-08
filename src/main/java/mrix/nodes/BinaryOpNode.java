package mrix.nodes;

import mrix.TokenType;

public class BinaryOpNode implements Node{
    public Node left;
    public Node right;
    public TokenType op;
    public BinaryOpNode(Node left, TokenType op, Node right) {
        this.left = left;
        this.op = op;
        this.right = right;
    }
}