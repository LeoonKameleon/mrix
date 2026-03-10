package mrix.nodes;

import mrix.DataType;
import mrix.Value;

public interface Node {
    DataType accept(NodeVisitor visitor);
    Value accept(InterpreterVisitor visitor);
}