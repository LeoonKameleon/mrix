package mrix.nodes;

import mrix.DataType;
import mrix.Token;
import java.util.List;

public class CreateMatrixNode implements Node {
    private final Token fun;
    private final List<Node> expressionList;
    public CreateMatrixNode(Token fun, List<Node> expressionList) {
        this.fun = fun;
        this.expressionList = expressionList;
    }
    public DataType accept(NodeVisitor visitor) {
        return visitor.visitCreateMatrixNode(this);
    }

    public Token getFun() {
        return fun;
    }

    public List<Node> getExpressionList() {
        return expressionList;
    }
}