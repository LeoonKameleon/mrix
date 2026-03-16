package mrix.nodes;

import mrix.interpreter.Value;
import mrix.tokens.Token;
import mrix.typechecker.DataType;

public class ImportNode extends AbstractNode {
    private final Token path;

    public ImportNode(Token path, int line) {
        super(line);
        this.path = path;
    }
    public DataType accept(NodeVisitor visitor) {
        return visitor.visitImportNode(this);
    }

    public Value accept(InterpreterVisitor visitor) {
        return visitor.visitImportNode(this);
    }

    public Token getPath() {
        return path;
    }
}
