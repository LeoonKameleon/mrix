package mrix.ast;

import java.util.List;

import mrix.interpreter.value.Value;
import mrix.scanner.token.Token;
import mrix.typing.type.DataType;

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
