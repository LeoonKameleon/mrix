package mrix.ast;

import java.util.List;

import mrix.interpreter.value.Value;
import mrix.typing.type.DataType;

public class PrintNode extends AbstractNode {
    private final List<Node> expressionList;
    public PrintNode(List<Node> expressionList, int line) {
        super(line);
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