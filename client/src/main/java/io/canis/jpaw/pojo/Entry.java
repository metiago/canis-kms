package io.canis.jpaw.pojo;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public record Entry(
        String name,
        String publicKey,
        String privateKey
) implements Serializable {

    public Entry() {
        this(null, null, null);
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", this.name);
        map.put("publicKey", this.publicKey);
        map.put("privateKey", this.privateKey);
        return map;
    }

    @Override
    public String toString() {
        return "Entry{" +
                "name='" + name + '\'' +
                '}';
    }
}
