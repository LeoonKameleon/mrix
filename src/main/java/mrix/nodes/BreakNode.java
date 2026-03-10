package mrix.nodes;

import mrix.DataType;
import mrix.Value;

public class BreakNode implements Node {
    public DataType accept(NodeVisitor visitor) {
        return visitor.visitBreakNode(this);
    }

    public Value accept(InterpreterVisitor visitor) {
        return visitor.visitBreakNode(this);
    }
}