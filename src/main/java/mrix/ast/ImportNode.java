package mrix.ast;

import mrix.interpreter.value.Value;
import mrix.scanner.token.Token;
import mrix.typing.type.DataType;

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
