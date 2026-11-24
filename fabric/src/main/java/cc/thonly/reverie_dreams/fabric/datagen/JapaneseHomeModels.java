package cc.thonly.reverie_dreams.fabric.datagen;

import cc.thonly.reverie_dreams.block.decor.GroupedDecorBlock;
import cc.thonly.reverie_dreams.block.decor.KotatsuBlock;
import cc.thonly.reverie_dreams.block.decor.living.HibachiBlock;
import cc.thonly.reverie_dreams.fabric.datagen.JapaneseModelKit.Model;
import cc.thonly.reverie_dreams.registry.content.block.RDJapaneseHomeBlocks;
import com.google.gson.JsonObject;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static cc.thonly.reverie_dreams.fabric.datagen.JapaneseModelKit.blockId;
import static net.minecraft.client.data.models.BlockModelGenerators.Y_ROT_180;
import static net.minecraft.client.data.models.BlockModelGenerators.Y_ROT_270;
import static net.minecraft.client.data.models.BlockModelGenerators.Y_ROT_90;
import static net.minecraft.client.data.models.BlockModelGenerators.plainVariant;

/**
 * Residential furnishing geometry on the shared home atlas. The kotatsu model
 * follows the connection mask of its block state, like the chabudai tables.
 */
final class JapaneseHomeModels {
    private static final String ATLAS = "reverie_dreams:block/japanese_home";

    private JapaneseHomeModels() {}

    static void generate(BlockModelGenerators generator) {
        JapaneseUvLayout layout = new JapaneseUvLayout();
        Map<String, Model> models = new LinkedHashMap<>();
        for (int mask = 0; mask < 16; mask++) {
            models.put("kotatsu_" + mask, new Model(kotatsu(mask), "minecraft:block/blue_wool", false));
        }
        models.put("hibachi", new Model(hibachi(false), "minecraft:block/terracotta", false));
        models.put("hibachi_lit", new Model(hibachi(true), "minecraft:block/terracotta", false));
        models.put("kamidana", new Model(kamidana(), "minecraft:block/oak_planks", true));
        models.put("butsudan_closed", new Model(butsudan(false), "minecraft:block/oak_planks", false));
        models.put("butsudan_open", new Model(butsudan(true), "minecraft:block/oak_planks", false));

        for (Map.Entry<String, Model> entry : models.entrySet()) {
            JsonObject model = JapaneseModelKit.model(ATLAS, entry.getValue(), layout);
            generator.modelOutput.accept(blockId(entry.getKey()), () -> model);
        }
        System.out.println("[datagen] japanese_home uv layout: " + layout.summary());

        generator.blockStateOutput.accept(MultiVariantGenerator.dispatch(RDJapaneseHomeBlocks.KOTATSU.asBlock())
                .with(PropertyDispatch.initial(GroupedDecorBlock.NORTH, GroupedDecorBlock.EAST,
                                GroupedDecorBlock.SOUTH, GroupedDecorBlock.WEST)
                        .generate((north, east, south, west) -> plainVariant(blockId("kotatsu_"
                                + ((north ? 1 : 0) | (east ? 2 : 0) | (south ? 4 : 0) | (west ? 8 : 0)))))));
        generator.blockStateOutput.accept(MultiVariantGenerator.dispatch(RDJapaneseHomeBlocks.HIBACHI.asBlock())
                .with(PropertyDispatch.initial(HibachiBlock.LIT)
                        .generate(lit -> plainVariant(blockId(lit ? "hibachi_lit" : "hibachi")))));
        state(generator, RDJapaneseHomeBlocks.KAMIDANA.asBlock(), blockId("kamidana"));
        generator.blockStateOutput.accept(MultiVariantGenerator.dispatch(RDJapaneseHomeBlocks.BUTSUDAN.asBlock())
                .with(PropertyDispatch.initial(BlockStateProperties.HORIZONTAL_FACING, BlockStateProperties.OPEN)
                        .generate((facing, open) -> rotated(
                                plainVariant(blockId(open ? "butsudan_open" : "butsudan_closed")), facing))));

        JapaneseModelKit.item(generator, RDJapaneseHomeBlocks.KOTATSU.asBlock(), "kotatsu", "kotatsu_0",
                models.get("kotatsu_0"));
        item(generator, RDJapaneseHomeBlocks.HIBACHI.asBlock(), "hibachi", models.get("hibachi"));
        item(generator, RDJapaneseHomeBlocks.KAMIDANA.asBlock(), "kamidana", models.get("kamidana"));
        JapaneseModelKit.item(generator, RDJapaneseHomeBlocks.BUTSUDAN.asBlock(), "butsudan", "butsudan_closed",
                models.get("butsudan_closed"));
    }

    private static void state(BlockModelGenerators generator, Block block, Identifier model) {
        generator.blockStateOutput.accept(MultiVariantGenerator.dispatch(block)
                .with(PropertyDispatch.initial(BlockStateProperties.HORIZONTAL_FACING)
                        .generate(facing -> rotated(plainVariant(model), facing))));
    }

    private static MultiVariant rotated(MultiVariant variant, Direction facing) {
        return switch (facing) {
            case EAST -> variant.with(Y_ROT_90);
            case SOUTH -> variant.with(Y_ROT_180);
            case WEST -> variant.with(Y_ROT_270);
            default -> variant;
        };
    }

    private static void item(BlockModelGenerators generator, Block block, String name, Model model) {
        JapaneseModelKit.item(generator, block, name, model);
    }

    private static List<JsonObject> kotatsu(int mask) {
        List<JsonObject> elements = new ArrayList<>();
        for (KotatsuBlock.Part part : KotatsuBlock.parts(mask)) {
            elements.add(box(part.x1(), part.y1(), part.z1(), part.x2(), part.y2(), part.z2(), part.texture()));
        }
        return elements;
    }

    private static List<JsonObject> hibachi(boolean lit) {
        List<JsonObject> elements = new ArrayList<>();
        elements.add(box(1, 0, 1, 15, 1.5, 15, "earthenware"));
        elements.add(box(3, 1.5, 3, 13, 7, 13, "earthenware"));
        elements.add(box(2, 7, 2, 14, 8, 4, "earthenware"));
        elements.add(box(2, 7, 12, 14, 8, 14, "earthenware"));
        elements.add(box(2, 7, 4, 4, 8, 12, "earthenware"));
        elements.add(box(12, 7, 4, 14, 8, 12, "earthenware"));
        elements.add(box(4, 7, 4, 12, 7.75, 12, "ash"));
        int[][] lumps = {{5, 5}, {8, 6}, {5, 9}, {9, 8}};
        for (int[] lump : lumps) {
            elements.add(box(lump[0], 7.75, lump[1], lump[0] + 2.5, 8, lump[1] + 2.5, lit ? "ember" : "ink"));
        }
        return elements;
    }

    private static List<JsonObject> kamidana() {
        List<JsonObject> elements = new ArrayList<>();
        // The model faces south; the backboard sits against the wall at z=0.
        elements.add(box(2, 3, 0, 14, 11.5, 0.8, "ginkgo"));
        elements.add(box(1, 3, 0, 15, 4, 8, "ginkgo_h"));
        elements.add(box(4, 4, 1, 5, 11.5, 6, "peach"));
        elements.add(box(11, 4, 1, 12, 11.5, 6, "peach"));
        elements.add(box(5, 4, 1, 11, 10.5, 5.5, "dark"));
        elements.add(box(5.5, 4.6, 5.5, 7, 10, 6, "washi"));
        elements.add(box(9, 4.6, 5.5, 10.5, 10, 6, "washi"));
        // The centre stays open so the enshrined ofuda is readable.
        elements.add(box(7, 4.8, 5.6, 9, 10.4, 5.9, "ofuda"));
        elements.add(box(7, 4, 7, 9, 4.6, 8, "brass"));
        elements.add(box(1, 11.5, 0, 15, 12.8, 8, "dark"));
        elements.add(box(3, 12.8, 0, 13, 13.8, 7, "dark"));
        elements.add(box(7, 13.8, 0.5, 9, 14, 6.5, "brass"));
        elements.add(box(7.5, 10.8, 5.9, 8.5, 11.5, 6.3, "brass"));
        return elements;
    }

    private static List<JsonObject> butsudan(boolean open) {
        List<JsonObject> elements = new ArrayList<>();
        elements.add(box(1, 0, 1, 15, 2, 15, "peach"));
        if (open) {
            // Hollow lower cabinet so the folded-back doors reveal an interior.
            elements.add(box(1.5, 2, 13, 14.5, 10, 14.5, "ginkgo"));
            elements.add(box(1.5, 2, 1.5, 2.5, 10, 13, "ginkgo"));
            elements.add(box(13.5, 2, 1.5, 14.5, 10, 13, "ginkgo"));
            elements.add(box(2.5, 2, 1.5, 13.5, 2.6, 13, "ginkgo"));
            elements.add(box(2.5, 9.4, 1.5, 13.5, 10, 13, "ginkgo"));
            elements.add(box(2.5, 2.6, 12, 13.5, 9.4, 13, "ink"));
            elements.add(box(2.5, 6.2, 1.5, 13.5, 6.8, 12.5, "peach"));
            elements.add(box(0.6, 2.6, 1.0, 1.4, 9.4, 6.8, "dark"));
            elements.add(box(14.6, 2.6, 1.0, 15.4, 9.4, 6.8, "dark"));
            elements.add(box(1.0, 5.6, 0.8, 1.4, 6.4, 1.0, "brass"));
            elements.add(box(14.6, 5.6, 0.8, 15.0, 6.4, 1.0, "brass"));
        } else {
            elements.add(box(1.5, 2, 1.5, 14.5, 10, 14.5, "ginkgo"));
            elements.add(box(2.2, 2.6, 1.2, 7.6, 9.4, 1.5, "dark"));
            elements.add(box(8.4, 2.6, 1.2, 13.8, 9.4, 1.5, "dark"));
            elements.add(box(7.1, 5.6, 1.1, 7.5, 6.4, 1.2, "brass"));
            elements.add(box(8.5, 5.6, 1.1, 8.9, 6.4, 1.2, "brass"));
        }
        // Open shrine niche above the storage cabinet.
        elements.add(box(2, 10, 12, 14, 15, 14, "dark"));
        elements.add(box(2, 10, 2, 3, 15, 12, "dark"));
        elements.add(box(13, 10, 2, 14, 15, 12, "dark"));
        elements.add(box(3, 10, 2, 13, 10.8, 12, "dark"));
        elements.add(box(3, 14.2, 2, 13, 15, 12, "dark"));
        elements.add(box(3, 10.8, 11, 13, 14.2, 12, "brass"));
        elements.add(box(2, 10, 2, 3, 15, 3, "peach"));
        elements.add(box(13, 10, 2, 14, 15, 3, "peach"));
        elements.add(box(7, 11, 5, 9, 12.8, 7, "brass"));
        elements.add(box(7.4, 12.8, 5.4, 8.6, 13.8, 6.6, "brass"));
        elements.add(box(4.5, 10.8, 4, 5.5, 11.8, 5, "brass"));
        elements.add(box(10.5, 10.8, 4, 11.5, 11.8, 5, "brass"));
        elements.add(box(1, 15, 1, 15, 16, 15, "dark"));
        return elements;
    }

    private static JsonObject box(double x1, double y1, double z1, double x2, double y2, double z2, String material) {
        return JapaneseModelKit.layoutBox(x1, y1, z1, x2, y2, z2, material);
    }
}
