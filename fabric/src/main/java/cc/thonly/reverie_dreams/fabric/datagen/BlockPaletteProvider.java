package cc.thonly.reverie_dreams.fabric.datagen;

import cc.thonly.reverie_dreams.fabric.datagen.generator.AbstractJsonElementWriterProvider;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.MapColor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Exports the registered block list for the map editor's block palette.
 * Colors are map colors, used only for editor previews; block textures/IDs remain authoritative.
 */
@SuppressWarnings({"deprecation", "ConstantValue"})
public class BlockPaletteProvider extends AbstractJsonElementWriterProvider {
    public BlockPaletteProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> future) {
        super(output, future);
    }

    @Override
    protected void configured(HolderLookup.Provider provider) {
        JsonArray blocks = new JsonArray();
        JsonObject states = new JsonObject();
        BuiltInRegistries.BLOCK.stream()
                .sorted(Comparator.comparing(block -> block.builtInRegistryHolder().key().identifier().toString()))
                .forEach(block -> {
                    Identifier id = block.builtInRegistryHolder().key().identifier();
                    JsonObject entry = new JsonObject();
                    entry.addProperty("id", id.toString());
                    entry.addProperty("color", colorOf(block));
                    blocks.add(entry);

                    JsonObject stateEntry = new JsonObject();
                    stateEntry.add("properties", propertiesOf(block));
                    stateEntry.add("defaults", defaultsOf(block));
                    states.add(id.toString(), stateEntry);
                });

        JsonObject root = new JsonObject();
        root.addProperty("format_version", 1);
        root.add("blocks", blocks);
        this.path2JsonElement.put("data/reverie_dreams/gensokyo/blocks.json", root);

        JsonObject statesRoot = new JsonObject();
        statesRoot.addProperty("format_version", 1);
        statesRoot.add("blocks", states);
        this.path2JsonElement.put("data/reverie_dreams/gensokyo/block_states.json", statesRoot);
    }

    private static String colorOf(Block block) {
        MapColor mapColor = block.defaultMapColor();
        int argb = mapColor == null
                ? 0xFFFFFFFF
                : mapColor.calculateARGBColor(MapColor.Brightness.NORMAL);
        return String.format("#%06x", argb & 0xFFFFFF);
    }

    private static JsonObject propertiesOf(Block block) {
        JsonObject result = new JsonObject();
        block.getStateDefinition().getProperties().stream()
                .sorted(Comparator.comparing(Property::getName))
                .forEach(property -> addProperty(result, property));
        return result;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void addProperty(JsonObject target, Property<?> property) {
        Property raw = property;
        List<String> names = new ArrayList<>();
        for (Object value : raw.getPossibleValues()) {
            names.add(raw.getName((Comparable) value));
        }
        names.sort(String::compareTo);
        JsonArray values = new JsonArray();
        names.forEach(values::add);
        target.add(property.getName(), values);
    }

    private static JsonObject defaultsOf(Block block) {
        JsonObject result = new JsonObject();
        BlockState defaultState = block.defaultBlockState();
        block.getStateDefinition().getProperties().stream()
                .sorted(Comparator.comparing(Property::getName))
                .forEach(property -> result.addProperty(property.getName(), valueName(property, defaultState)));
        return result;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static String valueName(Property<?> property, BlockState state) {
        Property raw = property;
        Object value = state.getValue(raw);
        return raw.getName((Comparable) value);
    }

    @Override
    public String getName() {
        return "Gensokyo block palette";
    }
}
