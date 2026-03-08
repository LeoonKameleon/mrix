package mrix.nodes;

import java.util.List;

public class MatrixNode implements Node {
    public List<List<Node>> rows;
    public MatrixNode(List<List<Node>> rows) {
        this.rows = rows;
    }
}