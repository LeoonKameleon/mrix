package mrix.nodes;

import java.util.List;

import mrix.interpreter.Value;
import mrix.typechecker.DataType;

public class PrintNode implements Node {
    private final List<Node> expressionList;
    public PrintNode(List<Node> expressionList) {
        this.expressionList = expressionList;
    }
    public DataType accept(NodeVisitor visitor) {
        return visitor.visitPrintNode(this);
    }

    public Value accept(InterpreterVisitor visitor) {
        return visitor.visitPrintNode(this);
    }

    public List<Node> getExpressionList() {
        return expressionList;
    }
}