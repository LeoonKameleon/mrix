package mrix.nodes;

import mrix.DataType;

public interface Node {
    DataType accept(NodeVisitor visitor);
}