package cc.thonly.reverie_dreams.ai;

import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * AI-only routing. Neither JVM proxy settings nor other mod HTTP clients are modified.
 */
public final class AIHttpClient {
    private static final OkHttpClient BASE = new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .callTimeout(150, TimeUnit.SECONDS)
            .retryOnConnectionFailure(false)
            .followRedirects(false)
            .followSslRedirects(false)
            .build();
    private static Settings cachedSettings;
    private static OkHttpClient cachedClient;

    private AIHttpClient() {
    }

    public static synchronized OkHttpClient forProxy(String url) {
        Settings settings = Settings.parse(url);
        if (settings.equals(cachedSettings))
            return cachedClient;
        OkHttpClient next = build(settings, BASE);
        OkHttpClient previous = cachedClient;
        cachedSettings = settings;
        cachedClient = next;
        // Evict idle connections only. Requests already in flight keep their original route.
        if (previous != null)
            previous.connectionPool().evictAll();
        return next;
    }

    public static OkHttpClient build(Settings settings, OkHttpClient base) {
        var builder = base.newBuilder().connectionPool(new ConnectionPool());
        if (settings.scheme().equals("direct"))
            return builder.proxy(Proxy.NO_PROXY).build();
        var type = settings.scheme().equals("socks5") ? Proxy.Type.SOCKS : Proxy.Type.HTTP;
        builder.proxy(new Proxy(type, InetSocketAddress.createUnresolved(settings.host(), settings.port())));
        if (settings.scheme().equals("https")) {
            builder.socketFactory(new HttpsProxySocketFactory(settings.host(), settings.port(), base.sslSocketFactory()));
        }
        return builder.build();
    }

    public record Settings(String scheme, String host, int port) {
        public static Settings parse(String text) {
            if (text == null || text.isBlank())
                return new Settings("direct", "", 0);
            try {
                URI uri = new URI(text.strip());
                String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
                int defaultPort = switch (scheme) {
                    case "http" -> 80;
                    case "https" -> 443;
                    case "socks5" -> 1080;
                    default -> throw invalid();
                };
                String host = uri.getHost();
                int port = uri.getPort() == -1 ? defaultPort : uri.getPort();
                if (host == null || host.isBlank() || port < 1 || port > 65535 || uri.getRawUserInfo() != null
                        || uri.getRawQuery() != null || uri.getRawFragment() != null
                        || (uri.getRawPath() != null && !uri.getRawPath().isEmpty() && !uri.getRawPath().equals("/")))
                    throw invalid();
                if (host.startsWith("[") && host.endsWith("]"))
                    host = host.substring(1, host.length() - 1);
                return new Settings(scheme, host.toLowerCase(Locale.ROOT), port);
            } catch (URISyntaxException exception) {
                // Do not include the supplied URL (which may contain credentials) in error logs.
                throw invalid();
            }
        }

        public static IllegalArgumentException invalid() {
            return new IllegalArgumentException("Invalid AI proxy URL: use http://host:port, https://host:port or socks5://host:port without credentials, path, query or fragment; leave blank for direct access.");
        }
    }
}
