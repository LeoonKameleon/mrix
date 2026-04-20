package mrix.interpreter;

import java.util.List;

public class TupleValue {
    private List<Value> values;

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
            sb.append(values.get(i));
            if (i < values.size() - 1) sb.append(", ");
        }
        sb.append(")");
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TupleValue other)) return false;
        return this.values.equals(other.values);
    }
}
