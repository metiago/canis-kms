package io.canis.jpaw.pojo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayOutputStream;
import java.io.NotSerializableException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class EntryTest {
    @Test
    void clientMetadataContainsOnlyPublicFieldsAndCannotBeSerialized() {
        Entry entry = new Entry("serviceA", "public-key");
        assertEquals(2, Entry.class.getRecordComponents().length);
        assertEquals("public-key", entry.toMap().get("publicKey"));
        assertFalse(entry.toMap().containsKey("privateKey"));
        assertFalse(Serializable.class.isAssignableFrom(Entry.class));
        assertFalse(Arrays.stream(Entry.class.getMethods())
                .anyMatch(method -> method.getName().toLowerCase().contains("private")));
        assertThrows(NotSerializableException.class, () -> {
            try (ObjectOutputStream out = new ObjectOutputStream(new ByteArrayOutputStream())) {
                out.writeObject(entry);
            }
        });
    }
}
