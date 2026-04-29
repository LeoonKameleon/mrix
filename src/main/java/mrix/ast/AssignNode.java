package mrix.ast;

import mrix.interpreter.value.Value;
import mrix.scanner.token.Token;
import mrix.typing.type.DataType;

public class AssignNode extends AbstractNode {
    private final Node variable;
    private final Token op;
    private final Node expression;
    public AssignNode(Node variable, Token op, Node expression, int line) {
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

    public Token getOp() {
        return op;
    }

    public Node getExpression() {
        return expression;
    }
}