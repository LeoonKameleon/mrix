package mrix.ast;

import java.util.List;

import mrix.interpreter.value.Value;
import mrix.typing.type.DataType;

public class MatrixNode extends AbstractNode {
    private final List<List<Node>> rows;
    public MatrixNode(List<List<Node>> rows, int line) {
        super(line);
        this.rows = rows;
    }
    public DataType accept(NodeVisitor visitor) {
        return visitor.visitMatrixNode(this);
    }

    public Value accept(InterpreterVisitor visitor) {
        return visitor.visitMatrixNode(this);
    }

    public List<List<Node>> getRows() {
        return rows;
    }
}