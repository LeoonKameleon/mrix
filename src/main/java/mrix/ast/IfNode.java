package mrix.ast;

import mrix.interpreter.value.Value;
import mrix.typing.type.DataType;

public class IfNode extends AbstractNode {
    private final Node condition;
    private final Node thenNode;
    private final Node elseNode;
    public IfNode(Node expression, Node instruction1, Node instruction2, int line) {
        super(line);
        this.condition = expression;
        this.thenNode = instruction1;
        this.elseNode = instruction2;
    }
    public DataType accept(NodeVisitor visitor) {
        return visitor.visitIfNode(this);
    }

    public Value accept(InterpreterVisitor visitor) {
        return visitor.visitIfNode(this);
    }

    public Node getCondition() {
        return condition;
    }

    public Node getThenNode() {
        return thenNode;
    }

    public Node getElseNode() {
        return elseNode;
    }
}