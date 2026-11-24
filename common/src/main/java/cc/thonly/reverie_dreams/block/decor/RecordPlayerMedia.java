package cc.thonly.reverie_dreams.block.decor;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

/**
 * Shared server/client validation. Never pass a player supplied URL to a file/resource loader.
 */
public final class RecordPlayerMedia {
    public static final String DEFAULT_URL = "https://radio.zyghit.cn/radio/touhou/320k";
    public static final int MAX_URL_LENGTH = 2048;

    private RecordPlayerMedia() {
    }

    public static String normalize(String value) {
        if (value == null)
            throw new IllegalArgumentException("Missing audio URL");
        String url = value.strip();
        if (url.isEmpty() || url.length() > MAX_URL_LENGTH || url.chars().anyMatch(Character::isISOControl))
            throw new IllegalArgumentException("Invalid audio URL");
        try {
            URI uri = new URI(url);
            String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
            if ((!scheme.equals("http") && !scheme.equals("https")) || uri.isOpaque()
                    || uri.getHost() == null || uri.getRawUserInfo() != null
                    || uri.getPort() < -1 || uri.getPort() == 0 || uri.getPort() > 65535)
                throw new IllegalArgumentException("Only HTTP/HTTPS audio URLs are supported");
            int port = uri.getPort();
            String host = uri.getHost().toLowerCase(Locale.ROOT);
            if ((scheme.equals("http") && port == 80) || (scheme.equals("https") && port == 443))
                port = -1;
            String path = uri.getRawPath();
            return scheme + "://" + host + (port < 0 ? "" : ":" + port)
                    + (path == null || path.isEmpty() ? "/" : path)
                    + (uri.getRawQuery() == null ? "" : "?" + uri.getRawQuery());
        } catch (URISyntaxException invalid) {
            throw new IllegalArgumentException("Invalid audio URL", invalid);
        }
    }
}
