package mrix.nodes;

import mrix.interpreter.Value;
import mrix.typechecker.DataType;

public interface Node {
    DataType accept(NodeVisitor visitor);
    Value accept(InterpreterVisitor visitor);
}