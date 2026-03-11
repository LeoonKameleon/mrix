package mrix.nodes;

import java.util.List;

import mrix.interpreter.Value;
import mrix.tokens.Token;
import mrix.typechecker.DataType;

public class VariableNode extends AbstractNode {
    private final Token id;
    private final List<Node> expressionList;
    public VariableNode(Token id, List<Node> expressionList, int line) {
        super(line);
        this.id = id;
        this.expressionList = expressionList;
    }
    public DataType accept(NodeVisitor visitor) {
        return visitor.visitVariableNode(this);
    }

    public Value accept(InterpreterVisitor visitor) {
        return visitor.visitVariableNode(this);
    }

    public Token getId() {
        return id;
    }

    public List<Node> getExpressionList() {
        return expressionList;
    }
}