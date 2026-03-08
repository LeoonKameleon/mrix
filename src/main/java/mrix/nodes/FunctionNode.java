package mrix.nodes;

import java.util.List;

import mrix.Token;

public class FunctionNode implements Node {
    public Token id;
    public List<Token> parameterList;
    public Node instruction;
    public FunctionNode(Token id, List<Token> parameterList, Node instruction) {
        this.id = id;
        this.parameterList = parameterList;
        this.instruction = instruction;
    }
}