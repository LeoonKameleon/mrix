package mrix.ast;

import mrix.interpreter.value.Value;
import mrix.typing.type.DataType;

public class ContinueNode extends AbstractNode {

    public ContinueNode(int line) {
        super(line);
    }

    public DataType accept(NodeVisitor visitor) {
        return visitor.visitContinueNode(this);
    }

    public Value accept(InterpreterVisitor visitor) {
        return visitor.visitContinueNode(this);
    }
}