package mrix.nodes;

import java.util.List;

public class PrintNode implements Node {
    public List<Node> expressionList;
    public PrintNode(List<Node> expressionList) {
        this.expressionList = expressionList;
    }
}