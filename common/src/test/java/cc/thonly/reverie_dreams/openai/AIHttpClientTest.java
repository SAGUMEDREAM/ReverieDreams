package cc.thonly.reverie_dreams.openai;

import cc.thonly.reverie_dreams.ai.AIHttpClient;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Proxy;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import cc.thonly.reverie_dreams.ReverieDreamsConfiguration;
import com.google.gson.Gson;
import java.io.OutputStream;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

/** Real sockets, TLS and CONNECT/SOCKS handshakes, using loopback only and no API credentials. */
@Timeout(15)
class AIHttpClientTest {
    private static SSLContext serverTls;
    private static SSLContext clientTls;
    private static X509TrustManager testTrust;
    private static InetAddress loopback;

    @BeforeAll static void testCertificates() throws Exception {
        loopback = InetAddress.getByName("127.0.0.1");
        // Deliberately public test fixture, trusted only by these test clients.
        char[] password = "test-only-not-a-secret".toCharArray();
        var store = KeyStore.getInstance("PKCS12");
        try (var in = AIHttpClientTest.class.getResourceAsStream("proxy-loopback-test.p12")) {
            assertNotNull(in); store.load(in, password);
        }
        var keys = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm()); keys.init(store, password);
        var trust = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm()); trust.init(store);
        testTrust = (X509TrustManager) trust.getTrustManagers()[0];
        serverTls = SSLContext.getInstance("TLS"); serverTls.init(keys.getKeyManagers(), null, null);
        clientTls = SSLContext.getInstance("TLS"); clientTls.init(null, trust.getTrustManagers(), null);
    }

    @Test void parsesDefaultsProtocolsAndIpv6WithoutCredentialsInErrors() {
        assertEquals("direct", AIHttpClient.Settings.parse("").scheme());
        assertEquals("direct", AIHttpClient.Settings.parse(null).scheme());
        assertEquals(80, AIHttpClient.Settings.parse("http://localhost").port());
        assertEquals(443, AIHttpClient.Settings.parse("https://localhost").port());
        assertEquals(1080, AIHttpClient.Settings.parse("socks5://localhost").port());
        assertEquals("socks5", AIHttpClient.Settings.parse(" SOCKS5://localhost:1080 ").scheme());
        assertEquals("::1", AIHttpClient.Settings.parse("http://[::1]:7890").host());
        for (String invalid : List.of("localhost:7890", "ftp://localhost:21", "http://localhost:0", "http://localhost:65536",
                "http://localhost/path", "http://localhost?key=secret", "http://localhost#fragment", "http://user:secret@localhost", "http://bad host")) {
            var error = assertThrows(IllegalArgumentException.class, () -> AIHttpClient.Settings.parse(invalid));
            assertFalse(error.toString().contains("secret"));
        }
    }

    @Test void configurationChangesRebuildOnlyTheAiClient() {
        var direct = AIHttpClient.forProxy("");
        assertEquals(Proxy.NO_PROXY, direct.proxy());
        assertSame(direct, AIHttpClient.forProxy("  "));
        var http = AIHttpClient.forProxy("http://127.0.0.1:7890");
        assertNotSame(direct, http);
        assertEquals(Proxy.Type.HTTP, http.proxy().type());
        assertSame(http, AIHttpClient.forProxy("HTTP://127.0.0.1:7890/"));
        assertEquals(Proxy.Type.SOCKS, AIHttpClient.forProxy("socks5://127.0.0.1:1080").proxy().type());
        assertThrows(IllegalArgumentException.class, () -> AIHttpClient.forProxy("invalid"));
        assertEquals(Proxy.NO_PROXY, AIHttpClient.forProxy("").proxy());
    }

    @Test void httpProxyReceivesAbsoluteHttpRequest() throws Exception {
        try (var fixture = new Network()) {
            int proxy = fixture.httpProxy(false, -1);
            var client = fixture.client("http://127.0.0.1:" + proxy);
            assertEquals("ok", request(client, "http://api.agent.test/chat"));
            assertEquals("POST http://api.agent.test/chat HTTP/1.1", fixture.proxyRequest.get());
        }
    }

    @Test void httpProxyTunnelsHttpsApi() throws Exception {
        try (var fixture = new Network()) {
            int origin = fixture.origin(true);
            int proxy = fixture.httpProxy(false, origin);
            assertEquals("ok", request(fixture.client("http://127.0.0.1:" + proxy), "https://api.agent.test:" + origin + "/chat"));
            assertEquals("CONNECT api.agent.test:" + origin + " HTTP/1.1", fixture.proxyRequest.get());
            assertEquals(1, fixture.originRequests.get());
        }
    }

    @Test void socks5ProxyReceivesUnresolvedApiHostnameAndTunnelsTls() throws Exception {
        try (var fixture = new Network()) {
            int origin = fixture.origin(true);
            int proxy = fixture.socksProxy(origin);
            assertEquals("ok", request(fixture.client("socks5://127.0.0.1:" + proxy), "https://api.agent.test:" + origin + "/chat"));
            assertEquals("api.agent.test", fixture.socksTarget.get());
            assertEquals(1, fixture.originRequests.get());
        }
    }

    @Test void httpsProxyUsesTlsBeforeConnectAndIndependentOriginTls() throws Exception {
        try (var fixture = new Network()) {
            int origin = fixture.origin(true);
            int proxy = fixture.httpProxy(true, origin);
            assertEquals("ok", request(fixture.client("https://127.0.0.1:" + proxy), "https://api.agent.test:" + origin + "/chat"));
            assertEquals("CONNECT api.agent.test:" + origin + " HTTP/1.1", fixture.proxyRequest.get());
            assertEquals(1, fixture.originRequests.get());
        }
    }

    @Test void httpsProxyAlsoForwardsHttpApiInsideTls() throws Exception {
        try (var fixture = new Network()) {
            int proxy = fixture.httpProxy(true, -1);
            assertEquals("ok", request(fixture.client("https://127.0.0.1:" + proxy), "http://api.agent.test/chat"));
            assertEquals("POST http://api.agent.test/chat HTTP/1.1", fixture.proxyRequest.get());
        }
    }

    @Test void httpsProxyRejectsUntrustedCertificateBeforeSendingHttp() throws Exception {
        try (var fixture = new Network()) {
            int proxy = fixture.httpProxy(true, -1);
            var base = new OkHttpClient.Builder().retryOnConnectionFailure(false).build();
            var client = AIHttpClient.build(AIHttpClient.Settings.parse("https://127.0.0.1:" + proxy), base);
            fixture.clients.add(client);
            assertThrows(IOException.class, () -> request(client, "http://api.agent.test/chat"));
            assertNull(fixture.proxyRequest.get());
        }
    }

    @Test void httpsProxyRejectsWrongProxyHostnameWithTrustedCertificate() throws Exception {
        try (var fixture = new Network()) {
            int proxy = fixture.httpProxy(true, -1);
            var base = fixture.base().newBuilder().dns(host -> List.of(loopback)).build();
            var client = AIHttpClient.build(AIHttpClient.Settings.parse("https://wrong-proxy.agent.test:" + proxy), base);
            fixture.clients.add(client);
            assertThrows(IOException.class, () -> request(client, "http://api.agent.test/chat"));
            assertNull(fixture.proxyRequest.get());
        }
    }

    @Test void originCertificateIsStillVerifiedBehindHttpsProxy() throws Exception {
        try (var fixture = new Network()) {
            int origin = fixture.origin(true);
            int proxy = fixture.httpProxy(true, origin);
            assertThrows(IOException.class, () -> request(fixture.client("https://127.0.0.1:" + proxy), "https://wrong-api.agent.test:" + origin + "/chat"));
            assertNotNull(fixture.proxyRequest.get());
        }
    }

    @Test void failedProxyNeverFallsBackToReachableDirectApi() throws Exception {
        try (var fixture = new Network()) {
            int origin = fixture.origin(false);
            int unavailable;
            try (var unused = new ServerSocket(0, 1, loopback)) { unavailable = unused.getLocalPort(); }
            assertThrows(IOException.class, () -> request(fixture.client("http://127.0.0.1:" + unavailable), "http://127.0.0.1:" + origin + "/chat"));
            assertEquals(0, fixture.originRequests.get());
        }
    }

    @Test void callCancellationInterruptsAnUnresponsiveProxyTlsHandshake() throws Exception {
        try (var fixture = new Network()) {
            int proxy = fixture.listen(false, socket -> socket.getInputStream().transferTo(OutputStream.nullOutputStream()));
            var client = fixture.client("https://127.0.0.1:" + proxy).newBuilder().callTimeout(250, TimeUnit.MILLISECONDS).build();
            fixture.clients.add(client);
            assertTimeoutPreemptively(Duration.ofSeconds(3), () ->
                    assertThrows(IOException.class, () -> request(client, "http://api.agent.test/chat")));
        }
    }

    @Test void oldConfigurationWithoutProxyStillDefaultsToDirect() {
        var config = new Gson().fromJson("{}", ReverieDreamsConfiguration.class);
        assertEquals("", config.aiProxyUrl);
        assertEquals("direct", AIHttpClient.Settings.parse(config.aiProxyUrl).scheme());
    }

    private static String request(OkHttpClient client, String url) throws IOException {
        var request = new Request.Builder().url(url).post(RequestBody.create("{\"test\":true}", MediaType.parse("application/json"))).build();
        try (var response = client.newCall(request).execute()) {
            assertEquals(200, response.code());
            return response.body().string();
        }
    }

    private static String readLine(InputStream input) throws IOException {
        var text = new StringBuilder();
        for (int value; (value = input.read()) != -1;) {
            if (value == '\n') return text.toString().replace("\r", "");
            if (text.length() >= 8192) throw new IOException("Test HTTP line too long");
            text.append((char) value);
        }
        throw new IOException("Test connection closed before HTTP headers");
    }

    private static String readRequest(InputStream input) throws IOException {
        String first = readLine(input);
        int length = 0;
        for (String header; !(header = readLine(input)).isEmpty();) {
            if (header.toLowerCase(Locale.ROOT).startsWith("content-length:")) length = Integer.parseInt(header.substring(15).strip());
        }
        input.skipNBytes(length);
        return first;
    }

    private static final class Network implements AutoCloseable {
        private final ExecutorService workers = Executors.newVirtualThreadPerTaskExecutor();
        private final ConcurrentLinkedQueue<Closeable> sockets = new ConcurrentLinkedQueue<>();
        private final ConcurrentLinkedQueue<OkHttpClient> clients = new ConcurrentLinkedQueue<>();
        private final AtomicReference<String> proxyRequest = new AtomicReference<>();
        private final AtomicReference<String> socksTarget = new AtomicReference<>();
        private final AtomicInteger originRequests = new AtomicInteger();

        OkHttpClient base() {
            return new OkHttpClient.Builder().sslSocketFactory(clientTls.getSocketFactory(), testTrust)
                    .dns(host -> { throw new UnknownHostException("Origin DNS must go through the proxy"); })
                    .connectTimeout(3, TimeUnit.SECONDS).readTimeout(4, TimeUnit.SECONDS).callTimeout(6, TimeUnit.SECONDS)
                    .retryOnConnectionFailure(false).build();
        }

        OkHttpClient client(String proxy) {
            var client = AIHttpClient.build(AIHttpClient.Settings.parse(proxy), base());
            clients.add(client); return client;
        }

        int listen(boolean tls, Handler handler) throws IOException {
            ServerSocket server = tls ? serverTls.getServerSocketFactory().createServerSocket(0, 10, loopback) : new ServerSocket(0, 10, loopback);
            sockets.add(server);
            workers.submit(() -> {
                try (Socket socket = server.accept()) {
                    sockets.add(socket); socket.setSoTimeout(5000); handler.accept(socket);
                } catch (Exception ignored) { /* Expected in negative TLS and connection tests. */ }
            });
            return server.getLocalPort();
        }

        int origin(boolean tls) throws IOException {
            return listen(tls, socket -> {
                readRequest(socket.getInputStream()); originRequests.incrementAndGet(); reply(socket);
            });
        }

        int httpProxy(boolean tls, int origin) throws IOException {
            return listen(tls, socket -> {
                String request = readRequest(socket.getInputStream()); proxyRequest.set(request);
                if (request.startsWith("CONNECT ")) {
                    socket.getOutputStream().write("HTTP/1.1 200 Connection Established\r\n\r\n".getBytes(StandardCharsets.US_ASCII));
                    socket.getOutputStream().flush(); relay(socket, origin);
                } else reply(socket);
            });
        }

        int socksProxy(int origin) throws IOException {
            return listen(false, socket -> {
                var in = socket.getInputStream(); var out = socket.getOutputStream();
                if (in.read() != 5) throw new IOException("Expected SOCKS5");
                in.readNBytes(in.read()); out.write(new byte[]{5,0}); out.flush();
                if (in.read() != 5 || in.read() != 1 || in.read() != 0 || in.read() != 3) throw new IOException("Expected SOCKS5 domain CONNECT");
                socksTarget.set(new String(in.readNBytes(in.read()), StandardCharsets.US_ASCII));
                in.readNBytes(2);
                out.write(new byte[]{5,0,0,1,127,0,0,1,0,0}); out.flush(); relay(socket, origin);
            });
        }

        void relay(Socket client, int origin) throws IOException {
            try (var upstream = new Socket(loopback, origin)) {
                sockets.add(upstream); upstream.setSoTimeout(5000);
                workers.submit(() -> {
                    try { client.getInputStream().transferTo(upstream.getOutputStream()); } catch (IOException ignored) { }
                });
                upstream.getInputStream().transferTo(client.getOutputStream());
            }
        }

        void reply(Socket socket) throws IOException {
            socket.getOutputStream().write("HTTP/1.1 200 OK\r\nContent-Length: 2\r\nConnection: close\r\n\r\nok".getBytes(StandardCharsets.US_ASCII));
            socket.getOutputStream().flush();
        }

        @Override public void close() throws Exception {
            for (var socket : sockets) try { socket.close(); } catch (IOException ignored) { }
            for (var client : clients) { client.connectionPool().evictAll(); client.dispatcher().executorService().shutdownNow(); }
            workers.shutdownNow(); assertTrue(workers.awaitTermination(5, TimeUnit.SECONDS));
        }
        private interface Handler { void accept(Socket socket) throws Exception; }
    }
}
