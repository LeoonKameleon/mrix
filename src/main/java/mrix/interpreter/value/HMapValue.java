package mrix.interpreter.value;

import mrix.typing.type.DataType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class HMapValue {
    private final Map<Value, Value> map;

    public HMapValue() {
        this.map = new LinkedHashMap<>();
    }

    public HMapValue(Map<Value, Value> map) {
        this.map = map;
    }

    public Value get(Value key) {
        return map.get(key);
    }

    public void put(Value key, Value value) {
        map.put(key, value);
    }

    public boolean containsKey(Value key) {
        return map.containsKey(key);
    }

    public int size() {
        return map.size();
    }

    public void remove(Value key) {
        map.remove(key);
    }

    public List<Value> getKeys() {
        return new ArrayList<>(map.keySet());
    }

    public Map<Value, Value> getMap() {
        return map;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("hmap{");
        int i = 0;
        for (Map.Entry<Value, Value> e : map.entrySet()) {
            sb.append(e.getKey());
            sb.append(": ");
            if (e.getValue().getType() == DataType.MATRIX) {
                double[][] m = e.getValue().toMatrix();
                sb.append("<matrix ").append(m.length).append("x").append(m[0].length).append(">");
            } else {
                sb.append(e.getValue());
            }
            if (i++ < map.size() - 1) sb.append(", ");
        }
        sb.append("}");
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof HMapValue other)) return false;
        return this.map.equals(other.map);
    }
}
