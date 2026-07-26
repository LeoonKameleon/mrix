package mrix.ast;

import mrix.interpreter.value.Value;
import mrix.typing.type.DataType;

import java.util.List;

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