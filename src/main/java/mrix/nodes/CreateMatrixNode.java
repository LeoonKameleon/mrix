package mrix.nodes;

import mrix.Token;
import java.util.List;

public class CreateMatrixNode implements Node {
    public Token fun;
    public List<Node> expressionList;
    public CreateMatrixNode(Token fun, List<Node> expressionList) {
        this.expressionList = expressionList;
    }
}