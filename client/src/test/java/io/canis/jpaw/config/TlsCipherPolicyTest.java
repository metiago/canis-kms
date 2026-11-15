package io.canis.jpaw.config;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.List;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import org.junit.jupiter.api.Test;

class TlsCipherPolicyTest {
    @Test
    void selectsOnlyApprovedSuitesInPolicyOrder() {
        assertArrayEquals(
                new String[] {"TLS_AES_256_GCM_SHA384", "TLS_CHACHA20_POLY1305_SHA256"},
                TlsCipherPolicy.enabledSuites(new String[] {
                    "TLS_RSA_WITH_AES_128_CBC_SHA", "TLS_CHACHA20_POLY1305_SHA256", "TLS_AES_256_GCM_SHA384"
                }));
    }

    @Test
    void rejectsProviderWithoutApprovedSuites() {
        assertThrows(
                TlsCipherPolicy.NoApprovedCipherSuiteException.class,
                () -> TlsCipherPolicy.enabledSuites(new String[0]));
        assertThrows(
                TlsCipherPolicy.NoApprovedCipherSuiteException.class,
                () -> TlsCipherPolicy.enabledSuites(new String[] {"TLS_RSA_WITH_AES_128_CBC_SHA"}));
    }

    @Test
    void configuresOnlyProviderEnabledApprovedSuitesOnRealSocket() throws Exception {
        try (SSLSocket socket =
                (SSLSocket) SSLContext.getDefault().getSocketFactory().createSocket()) {
            String approved = TlsCipherPolicy.enabledSuites(socket.getEnabledCipherSuites())[0];
            socket.setEnabledCipherSuites(new String[] {approved});
            TlsCipherPolicy.configure(socket);
            assertArrayEquals(new String[] {"TLSv1.3"}, socket.getEnabledProtocols());
            assertArrayEquals(new String[] {approved}, socket.getEnabledCipherSuites());
        }
    }

    @Test
    void rejectsRealSocketWithoutEligibleSuiteBeforeHandshake() throws Exception {
        List<String> approved =
                List.of("TLS_AES_128_GCM_SHA256", "TLS_AES_256_GCM_SHA384", "TLS_CHACHA20_POLY1305_SHA256");
        try (SSLSocket socket =
                (SSLSocket) SSLContext.getDefault().getSocketFactory().createSocket()) {
            String disallowed = Arrays.stream(socket.getSupportedCipherSuites())
                    .filter(suite -> !approved.contains(suite))
                    .findFirst()
                    .orElseThrow();
            socket.setEnabledCipherSuites(new String[] {disallowed});
            assertThrows(TlsCipherPolicy.NoApprovedCipherSuiteException.class, () -> TlsCipherPolicy.configure(socket));
            assertFalse(socket.isConnected());
        }
    }
}
