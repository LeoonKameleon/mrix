package mrix.ast;

import mrix.interpreter.value.Value;
import mrix.scanner.token.Token;
import mrix.typing.type.DataType;

public class BinaryOpNode extends AbstractNode {
    private final Node left;
    private final Node right;
    private final Token op;

    public BinaryOpNode(Node left, Token op, Node right, int line) {
        super(line);
        this.left = left;
        this.op = op;
        this.right = right;
    }

    public DataType accept(NodeVisitor visitor) {
        return visitor.visitBinaryOpNode(this);
    }

    public Value accept(InterpreterVisitor visitor) {
        return visitor.visitBinaryOpNode(this);
    }

    public Node getLeft() {
        return left;
    }

    public Node getRight() {
        return right;
    }

    public Token getOp() {
        return op;
    }
}