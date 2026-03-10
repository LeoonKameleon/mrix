package mrix.nodes;

import mrix.interpreter.Value;
import mrix.tokens.Token;
import mrix.typechecker.DataType;

public class BinaryOpNode implements Node{
    private final Node left;
    private final Node right;
    private final Token op;
    public BinaryOpNode(Node left, Token op, Node right) {
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