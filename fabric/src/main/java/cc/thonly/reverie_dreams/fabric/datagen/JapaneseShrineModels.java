package cc.thonly.reverie_dreams.fabric.datagen;

import cc.thonly.reverie_dreams.block.decor.shrine.IncenseBurnerBlock;
import cc.thonly.reverie_dreams.fabric.datagen.JapaneseModelKit.Model;
import cc.thonly.reverie_dreams.registry.content.block.RDJapaneseShrineBlocks;
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
 * Shrine furnishing geometry. Every cuboid face samples a 16x16 material tile
 * from the owning block's shrine atlas, and neighbouring faces are culled the
 * same way as the hand-authored set2 models.
 */
final class JapaneseShrineModels {
    private static final String ATLAS_ROOT = "reverie_dreams:block/japanese_shrine/";
    private static final Map<String, Integer> TILES = Map.ofEntries(
            Map.entry("wood", 0), Map.entry("wood_h", 1), Map.entry("dark", 2), Map.entry("endgrain", 3),
            Map.entry("lacquer", 4), Map.entry("lacquer_h", 5), Map.entry("stone", 6), Map.entry("stone_moss", 7),
            Map.entry("rope", 8), Map.entry("paper", 9), Map.entry("ofuda", 10), Map.entry("red", 11),
            Map.entry("brass", 12), Map.entry("iron", 13), Map.entry("ash", 14), Map.entry("ember", 15),
            Map.entry("jizo_face", 16), Map.entry("stone_dark", 17), Map.entry("moss", 18));

    private JapaneseShrineModels() {}

    static void generate(BlockModelGenerators generator) {
        Map<String, Model> models = new LinkedHashMap<>();
        models.put("shimenawa", new Model(shimenawa(), "minecraft:block/hay_block_side", true));
        models.put("gohei", new Model(gohei(), "minecraft:block/white_wool", true));
        models.put("saisen_bako", new Model(saisenBako(), "minecraft:block/spruce_planks", false));
        models.put("omikuji_rack", new Model(omikujiRack(), "minecraft:block/spruce_planks", true));
        Model incense = new Model(incenseBurner(false), "minecraft:block/stone", false);
        Model incenseLit = new Model(incenseBurner(true), "minecraft:block/stone", false);
        models.put("incense_burner", incense);
        models.put("incense_burner_lit", incenseLit);
        models.put("jizo_statue", new Model(jizoStatue(), "minecraft:block/stone", false));
        models.put("yukimi_lantern", new Model(yukimiLantern(), "minecraft:block/stone", true));
        models.put("hokora", new Model(hokora(), "minecraft:block/spruce_planks", false));

        for (Map.Entry<String, Model> entry : models.entrySet()) {
            JsonObject model = JapaneseModelKit.model(atlas(entry.getKey()), entry.getValue());
            generator.modelOutput.accept(blockId(entry.getKey()), () -> model);
        }

        state(generator, RDJapaneseShrineBlocks.SHIMENAWA.asBlock(), blockId("shimenawa"));
        state(generator, RDJapaneseShrineBlocks.GOHEI.asBlock(), blockId("gohei"));
        state(generator, RDJapaneseShrineBlocks.SAISEN_BAKO.asBlock(), blockId("saisen_bako"));
        state(generator, RDJapaneseShrineBlocks.OMIKUJI_RACK.asBlock(), blockId("omikuji_rack"));
        generator.blockStateOutput.accept(MultiVariantGenerator.dispatch(RDJapaneseShrineBlocks.INCENSE_BURNER.asBlock())
                .with(PropertyDispatch.initial(BlockStateProperties.HORIZONTAL_FACING, IncenseBurnerBlock.LIT)
                        .generate((facing, lit) -> rotated(plainVariant(
                                blockId(lit ? "incense_burner_lit" : "incense_burner")), facing))));
        state(generator, RDJapaneseShrineBlocks.JIZO_STATUE.asBlock(), blockId("jizo_statue"));
        state(generator, RDJapaneseShrineBlocks.YUKIMI_LANTERN.asBlock(), blockId("yukimi_lantern"));
        state(generator, RDJapaneseShrineBlocks.HOKORA.asBlock(), blockId("hokora"));

        item(generator, RDJapaneseShrineBlocks.SHIMENAWA.asBlock(), "shimenawa", models.get("shimenawa"));
        item(generator, RDJapaneseShrineBlocks.GOHEI.asBlock(), "gohei", models.get("gohei"));
        item(generator, RDJapaneseShrineBlocks.SAISEN_BAKO.asBlock(), "saisen_bako", models.get("saisen_bako"));
        item(generator, RDJapaneseShrineBlocks.OMIKUJI_RACK.asBlock(), "omikuji_rack", models.get("omikuji_rack"));
        item(generator, RDJapaneseShrineBlocks.INCENSE_BURNER.asBlock(), "incense_burner", incense);
        item(generator, RDJapaneseShrineBlocks.JIZO_STATUE.asBlock(), "jizo_statue", models.get("jizo_statue"));
        item(generator, RDJapaneseShrineBlocks.YUKIMI_LANTERN.asBlock(), "yukimi_lantern", models.get("yukimi_lantern"));
        item(generator, RDJapaneseShrineBlocks.HOKORA.asBlock(), "hokora", models.get("hokora"));
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

    private static String atlas(String model) {
        // The lit and unlit variants are the same block, so they share one atlas.
        String block = "incense_burner_lit".equals(model) ? "incense_burner" : model;
        return ATLAS_ROOT + block;
    }

    private static void item(BlockModelGenerators generator, Block block, String name, Model model) {
        JapaneseModelKit.item(generator, block, name, model);
    }

    private static List<JsonObject> shimenawa() {
        List<JsonObject> elements = new ArrayList<>();
        elements.add(box(0, 12, 0, 16, 16, 3.5, "rope"));
        elements.add(box(0, 10, 0, 2, 16, 4, "rope"));
        elements.add(box(14, 10, 0, 16, 16, 4, "rope"));
        elements.add(box(6, 10, 0, 10, 16, 4, "rope"));
        elements.add(box(1, 8, 0.5, 3, 11, 3.5, "rope"));
        elements.add(box(13, 8, 0.5, 15, 11, 3.5, "rope"));
        for (int x : new int[]{3, 7, 11}) {
            elements.add(box(x, 10, 4, x + 2, 12, 4.5, "paper"));
            elements.add(box(x + 1, 8, 4, x + 3, 10, 4.5, "paper"));
            elements.add(box(x, 6, 4, x + 2, 8, 4.5, "paper"));
            elements.add(box(x + 1, 4, 4, x + 3, 6, 4.5, "paper"));
        }
        return elements;
    }

    private static List<JsonObject> gohei() {
        List<JsonObject> elements = new ArrayList<>();
        elements.add(box(6, 0, 7, 10, 1, 11, "dark"));
        elements.add(box(7, 1, 8, 9, 15, 10, "wood"));
        elements.add(box(7, 15, 8, 9, 16, 10, "dark"));
        elements.add(box(4, 12, 8, 12, 14, 10, "wood"));
        elements.add(box(6, 11, 7, 10, 12, 11, "rope"));
        elements.add(box(3, 10, 6, 5, 12, 8, "paper"));
        elements.add(box(4, 7, 6, 6, 10, 8, "paper"));
        elements.add(box(3, 4, 6, 5, 7, 8, "paper"));
        elements.add(box(11, 10, 6, 13, 12, 8, "paper"));
        elements.add(box(10, 7, 6, 12, 10, 8, "paper"));
        elements.add(box(11, 4, 6, 13, 7, 8, "paper"));
        return elements;
    }

    private static List<JsonObject> saisenBako() {
        List<JsonObject> elements = new ArrayList<>();
        elements.add(box(2, 0, 2, 4, 2, 4, "dark"));
        elements.add(box(12, 0, 2, 14, 2, 4, "dark"));
        elements.add(box(2, 0, 12, 4, 2, 14, "dark"));
        elements.add(box(12, 0, 12, 14, 2, 14, "dark"));
        elements.add(box(1, 1, 1, 15, 7, 15, "wood_h"));
        elements.add(box(1, 2, 0, 15, 3, 1, "iron"));
        elements.add(box(1, 2, 15, 15, 3, 16, "iron"));
        elements.add(box(0, 2, 1, 1, 3, 15, "iron"));
        elements.add(box(15, 2, 1, 16, 3, 15, "iron"));
        elements.add(box(7, 3, 0, 9, 5, 1, "brass"));
        elements.add(box(1, 7, 1, 15, 9, 6, "wood_h"));
        elements.add(box(1, 7, 10, 15, 9, 15, "wood_h"));
        elements.add(box(1, 7, 6, 4, 9, 10, "wood"));
        elements.add(box(12, 7, 6, 15, 9, 10, "wood"));
        elements.add(box(4, 7, 6, 12, 8, 10, "stone_dark"));
        return elements;
    }

    private static List<JsonObject> omikujiRack() {
        List<JsonObject> elements = new ArrayList<>();
        elements.add(box(1, 3, 13, 3, 15, 15, "wood"));
        elements.add(box(13, 3, 13, 15, 15, 15, "wood"));
        elements.add(box(1, 5, 14, 15, 15, 15, "wood_h"));
        elements.add(box(0, 15, 12, 16, 16, 16, "dark"));
        elements.add(box(1, 3, 12, 15, 5, 16, "wood_h"));
        for (int x : new int[]{4, 7, 10}) {
            elements.add(box(x, 5, 13, x + 2, 14, 14, "paper", Map.of("north", "ofuda")));
        }
        return elements;
    }

    private static List<JsonObject> incenseBurner(boolean lit) {
        List<JsonObject> elements = new ArrayList<>();
        elements.add(box(3, 0, 3, 5, 2, 5, "stone"));
        elements.add(box(11, 0, 3, 13, 2, 5, "stone"));
        elements.add(box(3, 0, 11, 5, 2, 13, "stone"));
        elements.add(box(11, 0, 11, 13, 2, 13, "stone"));
        elements.add(box(2, 2, 2, 14, 3, 14, "stone"));
        elements.add(box(3, 3, 3, 13, 6, 13, "stone"));
        elements.add(box(2, 6, 2, 14, 7, 14, "stone"));
        elements.add(box(4, 7, 4, 12, 8, 12, "ash"));
        elements.add(box(1, 4, 7, 3, 5, 9, "brass"));
        elements.add(box(13, 4, 7, 15, 5, 9, "brass"));
        int[][] sticks = {{5, 7}, {7, 8}, {9, 7}};
        for (int[] stick : sticks) {
            int x = stick[0], z = stick[1];
            elements.add(box(x, 8, z, x + 1, 14, z + 1, "dark"));
            elements.add(box(x, 14, z, x + 1, 15, z + 1, lit ? "ember" : "ash"));
        }
        return elements;
    }

    private static List<JsonObject> jizoStatue() {
        List<JsonObject> elements = new ArrayList<>();
        elements.add(box(3, 0, 3, 13, 2, 13, "stone_moss"));
        elements.add(box(5, 2, 5, 11, 8, 11, "stone"));
        elements.add(box(6, 2, 3.5, 10, 4.5, 5, "stone"));
        elements.add(box(5, 4, 4, 11, 6, 5, "red"));
        elements.add(box(4, 6, 4, 12, 12, 12, "stone"));
        elements.add(box(4, 6, 3, 12, 12, 4, "stone",
                Map.of("north", "jizo_face"), Map.of("north", new double[]{4, 4, 12, 10})));
        elements.add(box(3.7, 11, 3.7, 12.3, 13, 12.3, "red"));
        elements.add(box(7, 13, 7, 9, 14, 9, "red"));
        return elements;
    }

    private static List<JsonObject> yukimiLantern() {
        List<JsonObject> elements = new ArrayList<>();
        elements.add(box(2, 0, 2, 4, 5, 4, "stone"));
        elements.add(box(12, 0, 2, 14, 5, 4, "stone"));
        elements.add(box(7, 0, 12, 9, 5, 14, "stone"));
        elements.add(box(2, 5, 2, 14, 7, 14, "stone"));
        elements.add(box(3, 7, 3, 13, 8, 13, "stone"));
        elements.add(box(4, 8, 4, 12, 11, 12, "ember"));
        elements.add(box(3, 8, 3, 5, 11, 5, "stone"));
        elements.add(box(11, 8, 3, 13, 11, 5, "stone"));
        elements.add(box(3, 8, 11, 5, 11, 13, "stone"));
        elements.add(box(11, 8, 11, 13, 11, 13, "stone"));
        elements.add(box(3, 11, 3, 13, 12, 13, "stone"));
        elements.add(box(1, 12, 1, 15, 13, 15, "stone_dark"));
        elements.add(box(3, 13, 3, 13, 14, 13, "stone_dark"));
        elements.add(box(5, 14, 5, 11, 15, 11, "stone_dark"));
        elements.add(box(7, 15, 7, 9, 16, 9, "stone"));
        return elements;
    }

    private static List<JsonObject> hokora() {
        List<JsonObject> elements = new ArrayList<>();
        elements.add(box(1, 0, 2, 15, 2, 14, "stone"));
        elements.add(box(5, 0, 0, 11, 1, 2, "stone"));
        elements.add(box(3, 2, 4, 5, 10, 12, "wood"));
        elements.add(box(11, 2, 4, 13, 10, 12, "wood"));
        elements.add(box(5, 2, 10, 11, 10, 12, "wood"));
        elements.add(box(5, 2, 4, 11, 3, 10, "dark"));
        elements.add(box(6, 3, 5, 10, 7, 9, "wood_h"));
        elements.add(box(6, 4, 4, 10, 7, 5, "paper", Map.of("north", "ofuda")));
        elements.add(box(2.7, 2, 2.7, 5, 10, 5, "lacquer"));
        elements.add(box(11, 2, 2.7, 13.3, 10, 5, "lacquer"));
        elements.add(box(5, 2, 3, 11, 3, 4, "wood_h"));
        elements.add(box(7, 9, 3.5, 9, 10, 4.5, "rope"));
        elements.add(box(7, 8, 3.5, 9, 9, 4.5, "brass"));
        elements.add(box(6, 6.5, 3.5, 10, 8, 4.5, "paper"));
        elements.add(box(0, 10, 1, 16, 12, 15, "dark"));
        elements.add(box(4, 12, 3, 12, 14, 13, "dark"));
        return elements;
    }

    private static JsonObject box(double x1, double y1, double z1, double x2, double y2, double z2, String material) {
        return JapaneseModelKit.box(TILES, x1, y1, z1, x2, y2, z2, material);
    }

    private static JsonObject box(double x1, double y1, double z1, double x2, double y2, double z2,
                                  String material, Map<String, String> faceMaterials) {
        return JapaneseModelKit.box(TILES, x1, y1, z1, x2, y2, z2, material, faceMaterials);
    }

    private static JsonObject box(double x1, double y1, double z1, double x2, double y2, double z2,
                                  String defaultMaterial, Map<String, String> faceMaterials,
                                  Map<String, double[]> uvOverrides) {
        return JapaneseModelKit.box(TILES, x1, y1, z1, x2, y2, z2, defaultMaterial, faceMaterials, uvOverrides);
    }
}
