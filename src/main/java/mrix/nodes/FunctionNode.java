package mrix.nodes;

import java.util.List;

import mrix.interpreter.Value;
import mrix.tokens.Token;
import mrix.typechecker.DataType;

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