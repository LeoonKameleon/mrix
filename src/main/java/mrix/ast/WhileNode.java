package mrix.ast;

import mrix.interpreter.value.Value;
import mrix.typing.type.DataType;

public class WhileNode extends AbstractNode {
    private final Node condition;
    private final Node thenNode;

    public WhileNode(Node expression, Node instruction, int line) {
        super(line);
        this.condition = expression;
        this.thenNode = instruction;
    }

    public DataType accept(NodeVisitor visitor) {
        return visitor.visitWhileNode(this);
    }

    public Value accept(InterpreterVisitor visitor) {
        return visitor.visitWhileNode(this);
    }

    public Node getCondition() {
        return condition;
    }

    public Node getThenNode() {
        return thenNode;
    }
}