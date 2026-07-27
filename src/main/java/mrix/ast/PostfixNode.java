package mrix.ast;

import mrix.interpreter.value.Value;
import mrix.scanner.token.Token;
import mrix.typing.type.DataType;

public class PostfixNode extends AbstractNode {
    private final Node primary;
    private final Token op;

    public PostfixNode(Node primary, Token op, int line) {
        super(line);
        this.primary = primary;
        this.op = op;
    }

    public DataType accept(NodeVisitor visitor) {
        return visitor.visitPostfixNode(this);
    }

    public Value accept(InterpreterVisitor visitor) {
        return visitor.visitPostfixNode(this);
    }

    public Node getPrimary() {
        return primary;
    }

    public Token getOp() {
        return op;
    }
}