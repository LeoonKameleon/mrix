package mrix.nodes;

import mrix.interpreter.Value;
import mrix.typechecker.DataType;

public class BreakNode implements Node {
    public DataType accept(NodeVisitor visitor) {
        return visitor.visitBreakNode(this);
    }

    public Value accept(InterpreterVisitor visitor) {
        return visitor.visitBreakNode(this);
    }
}