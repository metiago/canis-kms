package io.canis.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;

/** The only TLS protocol and cipher suites accepted by the server. */
public final class TlsCipherPolicy {
    private static final String[] APPROVED_SUITES = {
        "TLS_AES_128_GCM_SHA256", "TLS_AES_256_GCM_SHA384", "TLS_CHACHA20_POLY1305_SHA256"
    };

    private TlsCipherPolicy() {}

    public static void configure(SSLServerSocket socket) {
        socket.setEnabledProtocols(new String[] {"TLSv1.3"});
        socket.setEnabledCipherSuites(enabledSuites(socket.getEnabledCipherSuites()));
    }

    public static void configure(SSLSocket socket) {
        socket.setEnabledProtocols(new String[] {"TLSv1.3"});
        socket.setEnabledCipherSuites(enabledSuites(socket.getEnabledCipherSuites()));
    }

    static String[] enabledSuites(String[] providerEnabledSuites) {
        List<String> providerEnabled = Arrays.asList(providerEnabledSuites);
        List<String> enabled = new ArrayList<>();
        for (String suite : APPROVED_SUITES) {
            if (providerEnabled.contains(suite)) {
                enabled.add(suite);
            }
        }
        if (enabled.isEmpty()) {
            throw new NoApprovedCipherSuiteException();
        }
        return enabled.toArray(String[]::new);
    }

    public static final class NoApprovedCipherSuiteException extends IllegalStateException {
        private NoApprovedCipherSuiteException() {
            super("No approved TLS 1.3 cipher suite is enabled");
        }
    }
}
