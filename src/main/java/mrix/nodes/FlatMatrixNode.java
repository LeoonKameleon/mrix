package mrix.nodes;

import java.util.List;

public class FlatMatrixNode implements Node {
    public List<Node> expressionList;
    public FlatMatrixNode(List<Node> expressionList) {
        this.expressionList = expressionList;
    }
}