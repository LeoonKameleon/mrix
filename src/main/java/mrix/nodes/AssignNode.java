package mrix.nodes;

import mrix.TokenType;

public class AssignNode implements Node {
    public Node variable;
    public TokenType op;
    public Node expression;
    public AssignNode(Node variable, TokenType op, Node expression) {
        this.variable = variable;
        this.op = op;
        this.expression = expression;
    }
}