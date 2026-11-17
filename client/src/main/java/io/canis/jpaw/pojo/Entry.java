package io.canis.jpaw.pojo;

import java.util.HashMap;
import java.util.Map;

public record Entry(String name, String publicKey) {

    public Entry() {
        this(null, null);
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", this.name);
        map.put("publicKey", this.publicKey);
        return map;
    }

    @Override
    public String toString() {
        return "Entry{" + "name='" + name + '\'' + '}';
    }
}
