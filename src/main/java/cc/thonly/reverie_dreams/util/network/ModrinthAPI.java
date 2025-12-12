package cc.thonly.reverie_dreams.util.network;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import net.fabricmc.loader.api.FabricLoader;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;

@Slf4j
public class ModrinthAPI {
    public static final String API = "https://api.modrinth.com/v2/project/gensokyo-reverie-of-lost-dreams/version";
    public static final String USER_AGENT = "SAGUMEDREAM/ReverieDreams (helper@thonly.cc)";

    public static int compareVersion(String v1, String v2) {
        String[] split1 = v1.split("\\+", 2);
        String[] split2 = v2.split("\\+", 2);

        String main1 = split1[0];
        String main2 = split2[0];
        String suffix1 = split1.length > 1 ? split1[1] : "";
        String suffix2 = split2.length > 1 ? split2[1] : "";

        int cmp = compareNumericVersion(main1, main2);
        if (cmp != 0) return cmp;

        String[] mc1 = suffix1.split("-", 2);
        String[] mc2 = suffix2.split("-", 2);

        String mcVer1 = mc1.length > 0 ? mc1[0] : "";
        String mcVer2 = mc2.length > 0 ? mc2[0] : "";

        cmp = compareNumericVersion(mcVer1, mcVer2);
        if (cmp != 0) return cmp;

        String tag1 = mc1.length > 1 ? mc1[1] : "";
        String tag2 = mc2.length > 1 ? mc2[1] : "";

        return compareTag(tag1, tag2);
    }

    private static int compareNumericVersion(String v1, String v2) {
        if (v1.isEmpty() && v2.isEmpty()) return 0;
        if (v1.isEmpty()) return -1;
        if (v2.isEmpty()) return 1;

        String[] parts1 = v1.split("\\.");
        String[] parts2 = v2.split("\\.");

        int len = Math.max(parts1.length, parts2.length);
        for (int i = 0; i < len; i++) {
            int num1 = i < parts1.length ? parseSafe(parts1[i]) : 0;
            int num2 = i < parts2.length ? parseSafe(parts2[i]) : 0;
            if (num1 != num2) return Integer.compare(num1, num2);
        }
        return 0;
    }

    private static int parseSafe(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static int compareTag(String t1, String t2) {
        return Integer.compare(tagRank(t1), tagRank(t2));
    }

    private static int tagRank(String tag) {
        return switch (tag.toLowerCase()) {
            case "alpha" -> 0;
            case "beta" -> 1;
            case "", "stable", "release" -> 2;
            case "lts" -> 3;
            default -> 4;
        };
    }


    public static Entry get() {
        try {
            URI uri = new URI(API);
            URL url = uri.toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", USER_AGENT);
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed to fetch data: HTTP " + conn.getResponseCode());
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            JsonArray jsonArray = JsonParser.parseReader(reader).getAsJsonArray();
            reader.close();
            conn.disconnect();

            if (!jsonArray.isEmpty()) {
                String minecraftVersion = getMinecraftVersion();
                for (JsonElement element : jsonArray) {
                    Entry entry = new Gson().fromJson(element, Entry.class);
                    if (entry.game_versions != null) {
                        for (String gameVersion : entry.game_versions) {
                            if (!gameVersion.startsWith(minecraftVersion)) {
                                continue;
                            }
                            return entry;
                        }
                    }
                }
                return new Gson().fromJson(jsonArray.get(0), Entry.class);
            }
        } catch (Exception e) {
            log.error("Can't get new version");
        }
        return null;
    }

    public static String getMinecraftVersion() {
        return FabricLoader.getInstance()
                .getModContainer("minecraft")
                .map(container -> container.getMetadata().getVersion().getFriendlyString())
                .orElse("unknown");
    }

    @Getter
    @ToString
    public static class Entry {
        String id;
        String name;
        String version_number;
        String changelog;
        String date_published;
        String[] game_versions;
        int downloads;

        FileInfo[] files;

        @Getter
        @ToString
        public static class FileInfo {
            String url;
            String filename;
            long size;
        }
    }
}
