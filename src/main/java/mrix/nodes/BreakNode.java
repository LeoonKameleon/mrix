package mrix.nodes;

import mrix.DataType;

public class BreakNode implements Node {
    public DataType accept(NodeVisitor visitor) {
        return visitor.visitBreakNode(this);
    }
}