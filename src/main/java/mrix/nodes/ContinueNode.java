package mrix.nodes;

import mrix.DataType;

public class ContinueNode implements Node {
    public DataType accept(NodeVisitor visitor) {
        return visitor.visitContinueNode(this);
    }
}