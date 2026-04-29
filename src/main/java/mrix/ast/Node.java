package mrix.ast;

import mrix.interpreter.value.Value;
import mrix.typing.type.DataType;

public interface Node {
    DataType accept(NodeVisitor visitor);
    Value accept(InterpreterVisitor visitor);
    int getLine();
}