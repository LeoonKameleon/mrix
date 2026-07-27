package mrix.ast;

import mrix.interpreter.value.Value;
import mrix.scanner.token.Token;
import mrix.typing.type.DataType;

import java.util.List;

public class FunctionNode extends AbstractNode {
    private final Token id;
    private final List<Token> parameterList;
    private final Node instruction;

    public FunctionNode(Token id, List<Token> parameterList, Node instruction, int line) {
        super(line);
        this.id = id;
        this.parameterList = parameterList;
        this.instruction = instruction;
    }

    public DataType accept(NodeVisitor visitor) {
        return visitor.visitFunctionNode(this);
    }

    public Value accept(InterpreterVisitor visitor) {
        return visitor.visitFunctionNode(this);
    }

    public Token getId() {
        return id;
    }

    public List<Token> getParameterList() {
        return parameterList;
    }

    public Node getInstruction() {
        return instruction;
    }
}