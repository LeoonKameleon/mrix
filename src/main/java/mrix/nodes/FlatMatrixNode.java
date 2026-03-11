package mrix.nodes;

import java.util.List;

import mrix.interpreter.Value;
import mrix.typechecker.DataType;

public class FlatMatrixNode extends AbstractNode {
    private final List<Node> expressionList;
    public FlatMatrixNode(List<Node> expressionList, int line) {
        super(line);
        this.expressionList = expressionList;
    }
    public DataType accept(NodeVisitor visitor) {
        return visitor.visitFlatMatrixNode(this);
    }

    public Value accept(InterpreterVisitor visitor) {
        return visitor.visitFlatMatrixNode(this);
    }

    public List<Node> getExpressionList() {
        return expressionList;
    }
}