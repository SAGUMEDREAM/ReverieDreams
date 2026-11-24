package cc.thonly.reverie_dreams.client.pak;

import cc.thonly.pak.PakAsset;
import cc.thonly.pak.PakException;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.resources.Identifier;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Plaintext of the authenticated PAK2 catalog. The shipped catalog itself is encrypted.
 */
public record SkinPackManifest(Map<Identifier, Entry> entries) {
    public static final String OWNER = "reverie_dreams";
    public static final String ROOT = "META-INF/runtime/";
    public static final String CATALOG = ROOT + "catalog.pak";
    public static final String PACK_DIRECTORY = ROOT;
    public static final String CATALOG_SLOT = "catalog-v2";
    public static final PakAsset CATALOG_ASSET =
            new PakAsset(OWNER, OWNER + ":pak/catalog.json", CATALOG_SLOT);
    public static final int MAX_CATALOG_BYTES = 1024 * 1024;
    public static final int MAX_ENTRIES = 4096;

    public record Entry(PakAsset asset, String file) {
        public String packedPath() {
            return PACK_DIRECTORY + file;
        }
    }

    public SkinPackManifest {
        entries = Collections.unmodifiableMap(new LinkedHashMap<>(entries));
    }

    public static SkinPackManifest read(byte[] bytes) throws IOException {
        if (bytes.length < 2 || bytes.length > MAX_CATALOG_BYTES)
            throw new PakException("Invalid PAK catalog size");
        try {
            String json = StandardCharsets.UTF_8.newDecoder().decode(ByteBuffer.wrap(bytes)).toString();
            checkNesting(json);
            JsonObject catalog = JsonParser.parseString(json).getAsJsonObject();
            if (!catalog.keySet().equals(Set.of("version", "owner", "entries"))) {
                throw new PakException("Unknown or missing PAK catalog fields");
            }
            JsonElement version = catalog.get("version");
            if (version == null || !version.isJsonPrimitive() || !version.getAsJsonPrimitive().isNumber()
                    || !version.getAsString().equals("2") || !text(catalog, "owner").equals(OWNER)) {
                throw new PakException("Unsupported PAK catalog");
            }
            JsonArray rows = catalog.getAsJsonArray("entries");
            if (rows == null || rows.isEmpty() || rows.size() > MAX_ENTRIES) {
                throw new PakException("Invalid PAK catalog entry count");
            }
            Map<Identifier, Entry> entries = new LinkedHashMap<>();
            Set<String> files = new HashSet<>();
            for (JsonElement row : rows) {
                JsonObject object = row.getAsJsonObject();
                if (!object.keySet().equals(Set.of("path", "file", "slot"))) {
                    throw new PakException("Unknown PAK catalog entry field");
                }
                PakAsset asset = new PakAsset(OWNER, text(object, "path"), text(object, "slot"));
                String file = text(object, "file");
                if (!file.matches("[0-9a-f]{32}\\.pak") || !files.add(file)) {
                    throw new PakException("Invalid or duplicate packed file name");
                }
                Identifier id = Identifier.parse(asset.resource());
                if (entries.putIfAbsent(id, new Entry(asset, file)) != null) {
                    throw new PakException("Duplicate PAK catalog entry: " + id);
                }
            }
            return new SkinPackManifest(entries);
        } catch (PakException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new PakException("Malformed PAK catalog", e);
        }
    }

    private static String text(JsonObject object, String field) throws PakException {
        JsonElement value = object.get(field);
        if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()) {
            throw new PakException("Missing/string field required in PAK catalog: " + field);
        }
        return value.getAsString();
    }

    private static void checkNesting(String json) throws PakException {
        int depth = 0;
        boolean quoted = false, escaped = false;
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (quoted) {
                if (escaped)
                    escaped = false;
                else if (c == '\\')
                    escaped = true;
                else if (c == '"')
                    quoted = false;
            } else if (c == '"')
                quoted = true;
            else if (c == '{' || c == '[') {
                if (++depth > 16)
                    throw new PakException("PAK catalog nesting exceeds limit");
            } else if (c == '}' || c == ']') {
                if (--depth < 0)
                    throw new PakException("Unbalanced PAK catalog");
            }
        }
        if (quoted || depth != 0)
            throw new PakException("Incomplete PAK catalog");
    }
}
