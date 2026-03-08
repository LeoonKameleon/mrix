package mrix.nodes;

import java.util.ArrayList;
import java.util.List;

public class ProgramNode implements Node {
    public List<Node> instructions = new ArrayList<Node>();
    public ProgramNode(List<Node> instructions) {
        this.instructions = instructions;
    }
}
