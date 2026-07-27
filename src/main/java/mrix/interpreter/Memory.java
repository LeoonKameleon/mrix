package mrix.interpreter;

import mrix.interpreter.value.Value;

import java.util.HashMap;

public class Memory {
    private final Memory parent;
    private final HashMap<String, Value> memory;

    public Memory(Memory parent) {
        this.parent = parent;
        memory = new HashMap<>();
    }

    public void put(String name, Value value) {
        memory.put(name, value);
    }

    public Value get(String name) {
        if (memory.containsKey(name)) return memory.get(name);
        if (parent != null) return parent.get(name);
        return null;
    }

    public void set(String name, Value value) {
        if (memory.containsKey(name)) {
            memory.put(name, value);
            return;
        }
        if (parent != null) parent.set(name, value);
        else memory.put(name, value);
    }

    public Memory push() {
        return new Memory(this);
    }

    public Memory pop() {
        return parent;
    }
}
