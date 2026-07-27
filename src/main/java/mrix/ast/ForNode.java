package mrix.ast;

import mrix.interpreter.value.Value;
import mrix.scanner.token.Token;
import mrix.typing.type.DataType;

public class ForNode extends AbstractNode {
    private final Token id;
    private final Node rangeStart;
    private final Node rangeEnd;
    private final Node instruction;

    public ForNode(Token id, Node expression1, Node expression2, Node instruction, int line) {
        super(line);
        this.id = id;
        this.rangeStart = expression1;
        this.rangeEnd = expression2;
        this.instruction = instruction;
    }

    public DataType accept(NodeVisitor visitor) {
        return visitor.visitForNode(this);
    }

    public Value accept(InterpreterVisitor visitor) {
        return visitor.visitForNode(this);
    }

    public Token getId() {
        return id;
    }

    public Node getRangeStart() {
        return rangeStart;
    }

    public Node getRangeEnd() {
        return rangeEnd;
    }

    public Node getInstruction() {
        return instruction;
    }
}