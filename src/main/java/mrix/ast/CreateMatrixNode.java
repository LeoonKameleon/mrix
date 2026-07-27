package mrix.ast;

import mrix.interpreter.value.Value;
import mrix.scanner.token.Token;
import mrix.typing.type.DataType;

import java.util.List;

public class CreateMatrixNode extends AbstractNode {
    private final Token fun;
    private final List<Node> expressionList;

    public CreateMatrixNode(Token fun, List<Node> expressionList, int line) {
        super(line);
        this.fun = fun;
        this.expressionList = expressionList;
    }

    public DataType accept(NodeVisitor visitor) {
        return visitor.visitCreateMatrixNode(this);
    }

    public Value accept(InterpreterVisitor visitor) {
        return visitor.visitCreateMatrixNode(this);
    }

    public Token getFun() {
        return fun;
    }

    public List<Node> getExpressionList() {
        return expressionList;
    }
}