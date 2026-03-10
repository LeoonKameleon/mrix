package mrix;

public class Value {
    private final Object value;
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
}
