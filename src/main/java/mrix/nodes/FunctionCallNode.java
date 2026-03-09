package mrix.nodes;

import mrix.Token;
import java.util.List;

public class FunctionCallNode implements Node{
    public Token id;
    public List<Node> expressionList;
    public FunctionCallNode(Token id, List<Node> expressionList) {
        this.id = id;
        this.expressionList = expressionList;
    }
}
