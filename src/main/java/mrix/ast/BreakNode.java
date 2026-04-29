package mrix.ast;

import mrix.interpreter.value.Value;
import mrix.typing.type.DataType;

public class BreakNode extends AbstractNode {

    public BreakNode(int line) {
        super(line);
    }

    public DataType accept(NodeVisitor visitor) {
        return visitor.visitBreakNode(this);
    }

    public Value accept(InterpreterVisitor visitor) {
        return visitor.visitBreakNode(this);
    }
}