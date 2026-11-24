package cc.thonly.reverie_dreams.paper.world;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Explicit target-version configurations; the public Paper API cannot inspect ConfiguredFeature registries. */
public record GensokyoOreFeatures(Map<String, Feature> features, Map<String, Set<String>> biomeRules) {
    public record Target(String predicate, String match, String state) {}
    public record Feature(int size, float discardChance, List<Target> targets) {
        public Feature { targets = List.copyOf(targets); }
    }
    public GensokyoOreFeatures {
        features = Map.copyOf(features);
        var copy = new LinkedHashMap<String, Set<String>>();
        biomeRules.forEach((key, value) -> copy.put(key, Set.copyOf(value)));
        biomeRules = Map.copyOf(copy);
    }

    public static GensokyoOreFeatures bundled() throws IOException {
        var resource = GensokyoOreFeatures.class.getResourceAsStream("/worldgen/ore-features.json");
        if (resource == null) throw new IOException("Missing version-locked worldgen/ore-features.json");
        try (var reader = new InputStreamReader(resource, StandardCharsets.UTF_8)) { return read(reader); }
    }
    public static GensokyoOreFeatures read(Reader reader) throws IOException {
        try {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            if (!root.get("minecraft").getAsString().equals("26.1.2")) throw new IOException("Ore catalog must target Minecraft26.1.2");
            var result = new LinkedHashMap<String, Feature>();
            for (var entry : root.getAsJsonObject("features").entrySet()) {
                JsonObject object = entry.getValue().getAsJsonObject();
                if (!object.get("type").getAsString().equals("minecraft:ore")) throw new IOException("Unsupported configured feature kind: " + entry.getKey());
                JsonObject config = object.getAsJsonObject("config");
                int size = config.get("size").getAsInt();
                float discard = config.get("discard_chance_on_air_exposure").getAsFloat();
                if (size < 0 || size > 64 || !Float.isFinite(discard) || discard < 0 || discard > 1) throw new IOException("Invalid ore envelope/discard probability");
                var targets = new ArrayList<Target>();
                for (var target : config.getAsJsonArray("targets")) {
                    JsonObject t = target.getAsJsonObject(), test = t.getAsJsonObject("target"), state = t.getAsJsonObject("state");
                    String predicate = test.get("predicate_type").getAsString();
                    String match = switch (predicate) {
                        case "minecraft:tag_match" -> test.get("tag").getAsString();
                        case "minecraft:block_match" -> test.get("block").getAsString();
                        default -> throw new IOException("Unsupported ore RuleTest: " + predicate);
                    };
                    String encoded = state.get("Name").getAsString();
                    if (state.has("Properties")) {
                        var props = state.getAsJsonObject("Properties").entrySet().stream().sorted(Map.Entry.comparingByKey())
                                .map(p -> p.getKey() + "=" + p.getValue().getAsString()).toList();
                        encoded += "[" + String.join(",", props) + "]";
                    }
                    targets.add(new Target(predicate, match, encoded));
                }
                if (targets.isEmpty()) throw new IOException("Ore configuration has no targets");
                result.put(entry.getKey(), new Feature(size, discard, targets));
            }
            for (var band : GensokyoOreGenerator.BANDS) if (!result.containsKey(band.feature())) throw new IOException("Missing source band feature " + band.feature());
            var rules = new LinkedHashMap<String, Set<String>>();
            for (String key : List.of("overworld", "badlands", "mountain")) {
                var values = new LinkedHashSet<String>();
                root.getAsJsonObject("biome_rules").getAsJsonArray(key).forEach(value -> values.add(value.getAsString()));
                rules.put(key, values);
            }
            return new GensokyoOreFeatures(result, rules);
        } catch (IllegalArgumentException | IllegalStateException | NullPointerException error) {
            throw new IOException("Invalid explicit ore feature catalog", error);
        }
    }
    Set<String> states() {
        var result = new LinkedHashSet<String>();
        features.values().forEach(feature -> feature.targets().forEach(target -> result.add(target.state())));
        return Set.copyOf(result);
    }
    Set<String> tags() {
        var result = new LinkedHashSet<String>();
        features.values().forEach(feature -> feature.targets().stream().filter(t -> t.predicate().equals("minecraft:tag_match")).forEach(t -> result.add(t.match())));
        return Set.copyOf(result);
    }
}
