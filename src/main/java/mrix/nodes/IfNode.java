package mrix.nodes;

import mrix.interpreter.Value;
import mrix.typechecker.DataType;

public class IfNode implements Node {
    private final Node condition;
    private final Node thenNode;
    private final Node elseNode;
    public IfNode(Node expression, Node instruction1, Node instruction2) {
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