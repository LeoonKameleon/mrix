package mrix.nodes;

import java.util.List;

import mrix.DataType;

public class MatrixNode implements Node {
    private final List<List<Node>> rows;
    public MatrixNode(List<List<Node>> rows) {
        this.rows = rows;
    }
    public DataType accept(NodeVisitor visitor) {
        return visitor.visitMatrixNode(this);
    }

    public List<List<Node>> getRows() {
        return rows;
    }
}