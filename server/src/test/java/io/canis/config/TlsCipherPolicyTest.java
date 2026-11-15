package io.canis.config;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.List;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;
import org.junit.jupiter.api.Test;

class TlsCipherPolicyTest {
    @Test
    void selectsOnlyApprovedSuitesInPolicyOrder() {
        assertArrayEquals(
                new String[] {"TLS_AES_128_GCM_SHA256", "TLS_CHACHA20_POLY1305_SHA256"},
                TlsCipherPolicy.enabledSuites(new String[] {
                    "TLS_CHACHA20_POLY1305_SHA256", "TLS_RSA_WITH_AES_128_CBC_SHA", "TLS_AES_128_GCM_SHA256"
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
    void configuresOnlyProviderEnabledApprovedSuitesOnRealSockets() throws Exception {
        SSLContext context = SSLContext.getDefault();
        try (SSLServerSocket listener =
                        (SSLServerSocket) context.getServerSocketFactory().createServerSocket();
                SSLSocket peer = (SSLSocket) context.getSocketFactory().createSocket()) {
            String listenerSuite = TlsCipherPolicy.enabledSuites(listener.getEnabledCipherSuites())[0];
            listener.setEnabledCipherSuites(new String[] {listenerSuite});
            TlsCipherPolicy.configure(listener);
            assertArrayEquals(new String[] {"TLSv1.3"}, listener.getEnabledProtocols());
            assertArrayEquals(new String[] {listenerSuite}, listener.getEnabledCipherSuites());

            String peerSuite = TlsCipherPolicy.enabledSuites(peer.getEnabledCipherSuites())[0];
            peer.setEnabledCipherSuites(new String[] {peerSuite});
            TlsCipherPolicy.configure(peer);
            assertArrayEquals(new String[] {"TLSv1.3"}, peer.getEnabledProtocols());
            assertArrayEquals(new String[] {peerSuite}, peer.getEnabledCipherSuites());
        }
    }

    @Test
    void rejectsRealSocketsWithoutEligibleSuiteBeforeBindOrHandshake() throws Exception {
        SSLContext context = SSLContext.getDefault();
        List<String> approved =
                List.of("TLS_AES_128_GCM_SHA256", "TLS_AES_256_GCM_SHA384", "TLS_CHACHA20_POLY1305_SHA256");
        try (SSLServerSocket listener =
                        (SSLServerSocket) context.getServerSocketFactory().createServerSocket();
                SSLSocket peer = (SSLSocket) context.getSocketFactory().createSocket()) {
            String disallowed = Arrays.stream(listener.getSupportedCipherSuites())
                    .filter(suite -> !approved.contains(suite))
                    .findFirst()
                    .orElseThrow();
            listener.setEnabledCipherSuites(new String[] {disallowed});
            peer.setEnabledCipherSuites(new String[] {disallowed});
            assertThrows(
                    TlsCipherPolicy.NoApprovedCipherSuiteException.class, () -> TlsCipherPolicy.configure(listener));
            assertThrows(TlsCipherPolicy.NoApprovedCipherSuiteException.class, () -> TlsCipherPolicy.configure(peer));
            assertFalse(listener.isBound());
            assertFalse(peer.isConnected());
        }
    }
}
