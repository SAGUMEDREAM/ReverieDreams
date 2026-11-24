package cc.thonly.reverie_dreams.fabric.datagen;

import cc.thonly.reverie_dreams.ReverieDreams;

import cc.thonly.reverie_dreams.fabric.datagen.generator.AbstractJsonElementWriterProvider;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;

import java.util.Comparator;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

/**
 * Exports the complete biome registry for the map editor's biome palette.
 * This includes vanilla biomes as well as biomes registered by mods.
 */
public class BiomePaletteProvider extends AbstractJsonElementWriterProvider {
    public BiomePaletteProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> future) {
        super(output, future);
    }

    @Override
    protected void configured(HolderLookup.Provider provider) {
        JsonArray biomes = new JsonArray();
        provider.lookupOrThrow(Registries.BIOME).listElements()
                .sorted(Comparator.comparing(holder -> holder.unwrapKey()
                        .orElseThrow()
                        .identifier()
                        .toString()))
                .forEach(holder -> {
                    Identifier id = holder.unwrapKey().orElseThrow().identifier();
                    JsonObject entry = new JsonObject();
                    entry.addProperty("id", id.toString());
                    entry.addProperty("color", colorOf(id));
                    biomes.add(entry);
                });

        JsonObject root = new JsonObject();
        root.addProperty("format_version", 1);
        root.add("biomes", biomes);
        this.path2JsonElement.put("data/reverie_dreams/gensokyo/biomes.json", root);
    }

    private static String colorOf(Identifier id) {
        String landmark = switch (id.toString()) {
            case ReverieDreams.MOD_ID + ":gensokyo_phantom_meadow" -> "#9bc46b";
            case ReverieDreams.MOD_ID + ":gensokyo_muenzuka" -> "#af89ba";
            case ReverieDreams.MOD_ID + ":gensokyo_garden_of_the_sun" -> "#e4bd42";
            case ReverieDreams.MOD_ID + ":gensokyo_bamboo_forest" -> "#79a646";
            case ReverieDreams.MOD_ID + ":gensokyo_jungle" -> "#4d7e3b";
            default -> null;
        };
        if (landmark != null) return landmark;
        int hash = id.toString().hashCode();
        float hue = Math.floorMod(hash, 360) / 360.0f;
        int rgb = hslToRgb(hue, 0.42f, 0.52f);
        return String.format(Locale.ROOT, "#%06x", rgb);
    }

    private static int hslToRgb(float hue, float saturation, float lightness) {
        float q = lightness < 0.5f
                ? lightness * (1.0f + saturation)
                : lightness + saturation - lightness * saturation;
        float p = 2.0f * lightness - q;
        float r = hueToRgb(p, q, hue + 1.0f / 3.0f);
        float g = hueToRgb(p, q, hue);
        float b = hueToRgb(p, q, hue - 1.0f / 3.0f);
        int red = Math.round(r * 255.0f);
        int green = Math.round(g * 255.0f);
        int blue = Math.round(b * 255.0f);
        return (red << 16) | (green << 8) | blue;
    }

    private static float hueToRgb(float p, float q, float t) {
        float value = t;
        if (value < 0.0f) {
            value += 1.0f;
        }
        if (value > 1.0f) {
            value -= 1.0f;
        }
        if (value < 1.0f / 6.0f) {
            return p + (q - p) * 6.0f * value;
        }
        if (value < 1.0f / 2.0f) {
            return q;
        }
        if (value < 2.0f / 3.0f) {
            return p + (q - p) * (2.0f / 3.0f - value) * 6.0f;
        }
        return p;
    }

    @Override
    public String getName() {
        return "Gensokyo biome palette";
    }
}
