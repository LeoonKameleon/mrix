package mrix.nodes;

public abstract class AbstractNode implements Node {
    protected final int line;

    protected AbstractNode(int line) {
        this.line = line;
    }

    @Override
    public int getLine() {
        return line;
    }
}
