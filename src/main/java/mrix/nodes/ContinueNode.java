package mrix.nodes;

import mrix.DataType;
import mrix.Value;

public class ContinueNode implements Node {
    public DataType accept(NodeVisitor visitor) {
        return visitor.visitContinueNode(this);
    }

    public Value accept(InterpreterVisitor visitor) {
        return visitor.visitContinueNode(this);
    }
}