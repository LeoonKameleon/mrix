package mrix.nodes;

import mrix.Token;

public class PrimaryNode implements Node {
    public Token value;
    public PrimaryNode(Token value) {
        this.value = value;
    }
}