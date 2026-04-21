package mrix.nodes;

import java.util.List;

import mrix.interpreter.Value;
import mrix.tokens.Token;
import mrix.typechecker.DataType;

public class TuplePatternNode extends AbstractNode {
    private final List<Token> ids;

    public TuplePatternNode(List<Token> ids, int line) {
        super(line);
        this.ids = ids;
    }

    public List<Token> getIds() {
        return ids;
    }

    public DataType accept(NodeVisitor v) {
        return v.visitTuplePatternNode(this);
    }

    public Value accept(InterpreterVisitor visitor) {
        return visitor.visitTuplePatternNode(this);
    }
}
