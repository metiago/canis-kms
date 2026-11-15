package io.canis.protocol;

import io.canis.store.Entry;
import java.util.List;

/** Converter utility to convert internal objects to string - canis protocol */
public class Converter {

    public static String toMap(Entry entry) {
        return "|ms>name:" + entry.name() + "|ms>publicKey:" + entry.publicKey();
    }

    public static String toArrayOfMaps(List<Entry> entries) {
        StringBuilder builder = new StringBuilder();

        builder.append("|a>");

        for (Entry entry : entries) {
            builder.append(toMap(entry));

            if (entries.size() > 1) {
                builder.append("|a>");
            }
        }

        return builder.toString();
    }
}
