package mrix.interpreter.value;

import java.util.List;

import static mrix.typing.type.DataType.MATRIX;

public class TupleValue {
    private final List<Value> values;

    public TupleValue(List<Value> values) {
        this.values = values;
    }

    public List<Value> getValues() {
        return values;
    }

    public Value get(int index) {
        return values.get(index);
    }

    public int size() {
        return values.size();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("(");
        for (int i = 0; i < values.size(); i++) {
            Value v = values.get(i);
            if (v.getType() == MATRIX) {
                double[][] matrix = v.toMatrix();
                sb.append("<matrix ").append(matrix.length).append("x").append(matrix[0].length).append(">");
                continue;
            } else {
                sb.append(v);
            }
            if (i < values.size() - 1) sb.append(", ");
        }
        sb.append(")");
        return sb.toString();
    }

    @Override
    public int hashCode() {
        return values.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TupleValue other)) return false;
        return this.values.equals(other.values);
    }
}
