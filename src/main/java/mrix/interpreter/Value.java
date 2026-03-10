package mrix.interpreter;

import mrix.typechecker.DataType;

public class Value {
    private Object value;
    private final DataType type;

    public static final Value TRUE = new Value(Boolean.TRUE, DataType.BOOL);
    public static final Value FALSE = new Value(Boolean.FALSE, DataType.BOOL);
    public static final Value NULL = new Value(null, DataType.UNKNOWN);
    
    private static final Value[] INT_CACHE = new Value[2048];
    static {
        for (int i=0; i <2048; i++) {
            INT_CACHE[i] = new Value(i - 1024, DataType.INT);
        }
    }

    public Value(Object value, DataType type) {
        this.value = value;
        this.type = type;
    }

    public static Value of(boolean b) {
        return b ? TRUE : FALSE;
    }

    public static Value of(int i) {
        if (i >= -1024 && i <= 1023) return INT_CACHE[i + 1024];
        return new Value(i, DataType.INT);
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
