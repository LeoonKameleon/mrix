package mrix.nodes;

import mrix.DataType;
import mrix.Value;

public class WhileNode implements Node {
    private final Node condition;
    private final Node thenNode;
    public WhileNode(Node expression, Node instruction) {
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