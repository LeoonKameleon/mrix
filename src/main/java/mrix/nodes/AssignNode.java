package mrix.nodes;

import mrix.DataType;
import mrix.TokenType;

public class AssignNode implements Node {
    private final Node variable;
    private final TokenType op;
    private final Node expression;
    public AssignNode(Node variable, TokenType op, Node expression) {
        this.variable = variable;
        this.op = op;
        this.expression = expression;
    }
    public DataType accept(NodeVisitor visitor) {
        return visitor.visitAssignNode(this);
    }

    public Node getVariable() {
        return variable;
    }

    public TokenType getOp() {
        return op;
    }

    public Node getExpression() {
        return expression;
    }
}