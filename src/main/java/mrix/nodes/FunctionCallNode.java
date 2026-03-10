package mrix.nodes;

import mrix.interpreter.Value;
import mrix.tokens.Token;
import mrix.typechecker.DataType;

import java.util.List;

public class FunctionCallNode implements Node{
    private final Token id;
    private final List<Node> expressionList;
    public FunctionCallNode(Token id, List<Node> expressionList) {
        this.id = id;
        this.expressionList = expressionList;
    }
    public DataType accept(NodeVisitor visitor) {
        return visitor.visitFunctionCallNode(this);
    }

    public Value accept(InterpreterVisitor visitor) {
        return visitor.visitFunctionCallNode(this);
    }

    public Token getId() {
        return id;
    }

    public List<Node> getExpressionList() {
        return expressionList;
    }
}