package mrix.interpreter;

import mrix.typechecker.DataType;
import static mrix.typechecker.DataType.*;

public class Value {
    private Object value;
    private final DataType type;

    public static final Value TRUE = new Value(Boolean.TRUE, DataType.BOOL);
    public static final Value FALSE = new Value(Boolean.FALSE, DataType.BOOL);
    public static final Value NULL = new Value(null, DataType.UNKNOWN);
    
    private static final int CACHE_LOW = -4096;
    private static final int CACHE_HIGH = 4095;
    private static final Value[] LONG_CACHE = new Value[(CACHE_HIGH - CACHE_LOW) + 1];

    static {
        for (int i = 0; i < LONG_CACHE.length; i++) {
            LONG_CACHE[i] = new Value((long) (i + CACHE_LOW), DataType.INT);
        }
    }

    public Value(Object value, DataType type) {
        this.value = value;
        this.type = type;
    }

    public static Value of(boolean b) {
        return b ? TRUE : FALSE;
    }

    public static Value of(long l) {
        if (l >= CACHE_LOW && l <= CACHE_HIGH) {
            return LONG_CACHE[(int) (l - CACHE_LOW)];
        }
        return new Value(l, DataType.INT);
    }

    public Object getValue() {
        return value;
    }

    public DataType getType() {
        return type;
    }

    public double toDouble() {
        if (type == INT) return ((Number) value).doubleValue();
        if (type == BOOL) return (boolean) value ? 1.0 : 0.0;
        return (double) value;
    }

    public int toInt() {
        if (type == BOOL) return (boolean) value ? 1 : 0;
        return ((Number) value).intValue();
    }

    public long toLong() {
        if (type == BOOL) return (boolean) value ? 1L : 0L;
        return ((Number) value).longValue();
    }

    public String toString() {
        return (String) value;
    }

    public boolean toBoolean() {
        return (boolean) value;
    }

    public double[][] toMatrix() {
        return (double[][]) value;
    }
}
