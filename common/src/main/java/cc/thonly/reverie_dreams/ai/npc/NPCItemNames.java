package cc.thonly.reverie_dreams.ai.npc;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.locale.Language;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

import cc.thonly.reverie_dreams.ReverieDreams;

/**
 * Server-side names from packaged language files and reloadable, curated data-pack aliases.
 */
public final class NPCItemNames {
    private static volatile Map<String, Set<String>> names = Map.of();

    private NPCItemNames() {
    }

    public static void reload(ResourceManager resources) {
        Map<String, Set<String>> loaded = new HashMap<>();
        Set<String> namespaces = new TreeSet<>();
        BuiltInRegistries.ITEM.keySet().forEach(id -> namespaces.add(id.getNamespace()));
        for (String namespace : namespaces)
            for (String language : List.of("en_us", "zh_cn", "zh_tw", "ja_jp")) {
                try (var stream = NPCItemNames.class.getClassLoader().getResourceAsStream("assets/" + namespace + "/lang/" + language + ".json")) {
                    if (stream != null)
                        add(loaded, JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject());
                } catch (Exception ignored) { /* Optional third-party language files may be absent. */ }
            }
        for (var resource : resources.listResources("npc_item_names", id -> id.getPath().endsWith(".json")).entrySet()) {
            try (var reader = new InputStreamReader(resource.getValue().open(), StandardCharsets.UTF_8)) {
                add(loaded, JsonParser.parseReader(reader).getAsJsonObject().getAsJsonObject("names"));
            } catch (Exception exception) {
                ReverieDreams.LOGGER.warn("Invalid NPC item names {}", resource.getKey(), exception);
            }
        }
        Map<String, Set<String>> immutable = new HashMap<>();
        loaded.forEach((key, values) -> immutable.put(key, Set.copyOf(values)));
        names = Map.copyOf(immutable);
    }

    private static void add(Map<String, Set<String>> into, JsonObject translations) {
        for (var entry : translations.entrySet()) {
            if (!entry.getKey().startsWith("item.") && !entry.getKey().startsWith("block."))
                continue;
            if (!entry.getValue().isJsonPrimitive() || !entry.getValue().getAsJsonPrimitive().isString())
                continue;
            String value = entry.getValue().getAsString();
            if (!value.isBlank() && value.length() <= 256)
                into.computeIfAbsent(entry.getKey(), _ -> new TreeSet<>()).add(value);
        }
    }

    public static Set<String> of(Item item) {
        Set<String> result = new TreeSet<>(names.getOrDefault(item.getDescriptionId(), Set.of()));
        result.add(Language.getInstance().getOrDefault(item.getDescriptionId()));
        return result;
    }

    static int rank(String query, String id, Collection<String> aliases) {
        String needle = query.strip().toLowerCase(Locale.ROOT);
        if (needle.isEmpty())
            return Integer.MAX_VALUE;
        if (id.equals(needle))
            return 0;
        if (aliases.stream().anyMatch(name -> name.equalsIgnoreCase(needle)))
            return 1;
        if (id.substring(id.indexOf(':') + 1).equals(needle))
            return 2;
        if (aliases.stream().anyMatch(name -> name.toLowerCase(Locale.ROOT).startsWith(needle)))
            return 3;
        if (id.contains(needle) || aliases.stream().anyMatch(name -> name.toLowerCase(Locale.ROOT).contains(needle)))
            return 4;
        return Integer.MAX_VALUE;
    }
}
