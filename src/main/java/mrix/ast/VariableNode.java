package mrix.ast;

import mrix.interpreter.value.Value;
import mrix.scanner.token.Token;
import mrix.typing.type.DataType;

import java.util.List;

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