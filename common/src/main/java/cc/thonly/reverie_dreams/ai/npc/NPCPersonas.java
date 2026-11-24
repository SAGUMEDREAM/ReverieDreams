package cc.thonly.reverie_dreams.ai.npc;

import com.google.gson.JsonParser;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import cc.thonly.reverie_dreams.ReverieDreams;

/**
 * Data pack path: data/<namespace>/npc_persona/<role>.json.
 */
public final class NPCPersonas {
    private record Cards(Map<Identifier, String> descriptions, Map<Identifier, NPCBehavior> behaviors) {
    }

    private static volatile Cards cards = new Cards(Map.of(), Map.of());

    private NPCPersonas() {
    }

    public static void reload(ResourceManager manager) {
        Map<Identifier, String> loaded = new HashMap<>();
        Map<Identifier, NPCBehavior> behaviors = new HashMap<>();
        for (var resource : manager.listResources("npc_persona", id -> id.getPath().endsWith(".json")).entrySet()) {
            try (var reader = new InputStreamReader(resource.getValue().open(), StandardCharsets.UTF_8)) {
                var data = JsonParser.parseReader(reader).getAsJsonObject();
                String description = data.get("persona").getAsString();
                if (description.isBlank() || description.length() > 4096)
                    throw new IllegalArgumentException("Invalid persona length");
                String path = resource.getKey().getPath();
                var role = Identifier.fromNamespaceAndPath(resource.getKey().getNamespace(), path.substring("npc_persona/".length(), path.length() - 5));
                loaded.put(role, description);
                try {
                    behaviors.put(role, NPCBehavior.fromCard(data));
                } catch (RuntimeException invalid) {
                    ReverieDreams.LOGGER.warn("Invalid NPC behavior {}; using neutral behavior", resource.getKey(), invalid);
                }
            } catch (Exception exception) {
                ReverieDreams.LOGGER.warn("Invalid NPC persona {}", resource.getKey(), exception);
            }
        }
        cards = new Cards(Map.copyOf(loaded), Map.copyOf(behaviors));
    }

    public static String describe(Identifier role) {
        if (role == null)
            return "Maintain a consistent personality and admit unknown lore or game mechanics.";
        return cards.descriptions.getOrDefault(role, "Maintain a consistent, reserved personality. Base relationships on observed events; admit unknown lore and game mechanics.");
    }

    public static NPCBehavior behavior(Identifier role) {
        return role == null ? NPCBehavior.DEFAULT : cards.behaviors.getOrDefault(role, NPCBehavior.DEFAULT);
    }
}
