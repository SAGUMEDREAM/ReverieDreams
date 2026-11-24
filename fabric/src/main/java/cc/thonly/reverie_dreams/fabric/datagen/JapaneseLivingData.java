package cc.thonly.reverie_dreams.fabric.datagen;

import cc.thonly.reverie_dreams.fabric.datagen.generator.AbstractJsonElementWriterProvider;
import com.google.gson.JsonParser;
import net.minecraft.resources.Identifier;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.io.IOException;

/** The reproducible batch manifest shares exactly the checked-in blockstates and recipes. */
final class JapaneseLivingData {
    static void write(JsonElementWriterProvider provider) {
        try (var reader = new InputStreamReader(Objects.requireNonNull(JapaneseLivingData.class.getResourceAsStream(
                "/reverie_dreams/datagen/japanese_living_batch1.json")), StandardCharsets.UTF_8)) {
            for (var entry : JsonParser.parseReader(reader).getAsJsonObject().entrySet()) {
                String path = entry.getKey();
                if (path.contains("/loot_table/")) continue; // Owned by BlockLootTableProvider.
                String[] parts = path.split("/", 3);
                int slash = parts[2].lastIndexOf('/');
                String subPath = parts[2].substring(0, slash);
                String name = parts[2].substring(slash + 1, parts[2].length() - 5);
                provider.addElement(parts[0].equals("assets") ? AbstractJsonElementWriterProvider.Type.ASSETS
                        : AbstractJsonElementWriterProvider.Type.DATA, Identifier.fromNamespaceAndPath(parts[1], name), subPath, entry.getValue());
            }
        } catch (IOException exception) { throw new IllegalStateException("Japanese living datagen manifest", exception); }
    }
}
