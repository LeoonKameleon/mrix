package mrix.nodes;

import mrix.interpreter.Value;
import mrix.tokens.TokenType;
import mrix.typechecker.DataType;

public class AssignNode extends AbstractNode {
    private final Node variable;
    private final TokenType op;
    private final Node expression;
    public AssignNode(Node variable, TokenType op, Node expression, int line) {
        super(line);
        this.variable = variable;
        this.op = op;
        this.expression = expression;
    }
    public DataType accept(NodeVisitor visitor) {
        return visitor.visitAssignNode(this);
    }

    public Value accept(InterpreterVisitor visitor) {
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