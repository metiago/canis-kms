package io.canis.server.store;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import io.canis.protocol.Converter;
import io.canis.store.Entry;
import org.junit.jupiter.api.Test;

public class EntryTest {

    @Test
    void testProtocolMapContainsOnlyPublicMetadata() {
        Entry entry = new Entry("app", "public-key");

        String result = Converter.toMap(entry);

        assertEquals("|ms>name:app|ms>publicKey:public-key", result);
        assertFalse(result.contains("privateKey"));
    }
}
