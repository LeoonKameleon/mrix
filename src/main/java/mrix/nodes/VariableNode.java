package mrix.nodes;

import java.util.List;

import mrix.Token;

public class VariableNode implements Node {
    public Token id;
    public List<Node> expressionList;
    public VariableNode(Token id, List<Node> expressionList) {
        this.id = id;
        this.expressionList = expressionList;
    }
}