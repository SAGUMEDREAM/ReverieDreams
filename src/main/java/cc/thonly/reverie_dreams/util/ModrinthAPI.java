package cc.thonly.reverie_dreams.util;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

@Slf4j
public class ModrinthAPI {
    public static final String API = "https://api.modrinth.com/v2/project/gensokyo-reverie-of-lost-dreams/version";
    public static final String USER_AGENT = "SAGUMEDREAM/ReverieDreams (helper@thonly.cc)";

    public static int compareVersion(String v1, String v2) {
        String[] parts1 = v1.split("\\.");
        String[] parts2 = v2.split("\\.");

        int len = Math.max(parts1.length, parts2.length);
        for (int i = 0; i < len; i++) {
            int num1 = i < parts1.length ? Integer.parseInt(parts1[i]) : 0;
            int num2 = i < parts2.length ? Integer.parseInt(parts2[i]) : 0;

            if (num1 != num2) {
                return Integer.compare(num1, num2);
            }
        }
        return 0; // equal
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
                return new Gson().fromJson(jsonArray.get(0), Entry.class);
            }
        } catch (Exception e) {
            log.error("Can't get new version");
        }
        return null;
    }

    @Getter
    @ToString
    public static class Entry {
        String id;
        String name;
        String version_number;
        String changelog;
        String date_published;
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
