package mrix.nodes;
import mrix.Token;

public class ForNode implements Node {
    public Token id;
    public Node rangeStart;
    public Node rangeEnd;
    public Node instruction;
    public ForNode(Token id, Node expression1, Node expression2, Node instruction) {
        this.id = id;
        this.rangeStart = expression1;
        this.rangeEnd = expression2;
        this.instruction = instruction;
    }
}