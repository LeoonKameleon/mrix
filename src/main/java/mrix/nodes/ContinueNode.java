package mrix.nodes;

import mrix.interpreter.Value;
import mrix.typechecker.DataType;

public class ContinueNode implements Node {
    public DataType accept(NodeVisitor visitor) {
        return visitor.visitContinueNode(this);
    }

    public Value accept(InterpreterVisitor visitor) {
        return visitor.visitContinueNode(this);
    }
}