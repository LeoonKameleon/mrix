package mrix.interpreter;

import mrix.typechecker.DataType;

public class Value {
    private Object value;
    private final DataType type;
    public Value(Object value, DataType type) {
        this.value = value;
        this.type = type;
    }

    public Object getValue() {
        return value;
    }

    public DataType getType() {
        return type;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
