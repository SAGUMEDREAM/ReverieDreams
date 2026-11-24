package cc.thonly.reverie_dreams.fabric.datagen;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.block.decor.ChabudaiBlock;
import cc.thonly.reverie_dreams.block.decor.TatamiBlock;
import cc.thonly.reverie_dreams.block.decor.TeaSetBlock;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.blockstates.ConditionBuilder;
import net.minecraft.client.data.models.blockstates.MultiPartGenerator;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import java.util.Locale;

import static net.minecraft.client.data.models.BlockModelGenerators.plainVariant;
import static net.minecraft.client.data.models.BlockModelGenerators.*;

/** Static JSON geometry shared by native clients and Polymer's model loader. */
final class JapaneseInteriorModels {
    private JapaneseInteriorModels() { }

    static void tatami(BlockModelGenerators generator, Block block, String prefix, int height) {
        MultiPartGenerator state = MultiPartGenerator.multiPart(block);
        for (Direction.Axis axis : new Direction.Axis[]{Direction.Axis.X, Direction.Axis.Z}) {
            for (int mask = 0; mask < 16; mask++) {
                String name = prefix + axis.getName() + "_" + mask;
                JsonObject model = tatamiModel(mask, height, axis);
                generator.modelOutput.accept(ReverieDreams.id("block/" + name), () -> model);
                ConditionBuilder condition = new ConditionBuilder().term(TatamiBlock.AXIS, axis)
                        .term(TatamiBlock.NORTH, (mask & 1) != 0).term(TatamiBlock.EAST, (mask & 2) != 0)
                        .term(TatamiBlock.SOUTH, (mask & 4) != 0).term(TatamiBlock.WEST, (mask & 8) != 0);
                state.with(condition, plainVariant(ReverieDreams.id("block/" + name)));
            }
        }
        for (TatamiBlock.Corner corner : TatamiBlock.Corner.values()) {
            boolean east = corner.first == Direction.EAST || corner.second == Direction.EAST;
            boolean south = corner.first == Direction.SOUTH || corner.second == Direction.SOUTH;
            int x = east ? 15 : 0, z = south ? 15 : 0;
            JsonObject model = tatamiRoot();
            JsonArray elements = new JsonArray();
            plane(elements, x, z, x + 1, z + 1, height + .003, "frame", 0);
            model.add("elements", elements);
            String name = prefix + "inner_" + corner.name().toLowerCase(Locale.ROOT);
            generator.modelOutput.accept(ReverieDreams.id("block/" + name), () -> model);
            state.with(new ConditionBuilder().term(TatamiBlock.property(corner.first), true)
                            .term(TatamiBlock.property(corner.second), true).term(corner.property, false),
                    plainVariant(ReverieDreams.id("block/" + name)));
        }
        generator.blockStateOutput.accept(state);
    }

    private static JsonObject tatamiRoot() {
        JsonObject model = new JsonObject();
        model.addProperty("parent", "minecraft:block/block");
        JsonObject textures = new JsonObject();
        textures.addProperty("particle", "reverie_dreams:block/tatami_top_15");
        textures.addProperty("grass", "reverie_dreams:block/tatami_top_15");
        textures.addProperty("frame", "reverie_dreams:block/tatami_top_0");
        textures.addProperty("seam", "reverie_dreams:block/tatami_seam");
        textures.addProperty("side", "reverie_dreams:block/tatami_side");
        model.add("textures", textures);
        return model;
    }

    private static JsonObject tatamiModel(int mask, int height, Direction.Axis axis) {
        JsonObject model = tatamiRoot();
        JsonArray elements = new JsonArray();
        JsonObject body = box(0, 0, 0, 16, height, 16, "side");
        body.getAsJsonObject("faces").remove("up");
        elements.add(body);
        int rotation = axis == Direction.Axis.X ? 90 : 0;
        plane(elements, 1, 1, 15, 15, height, "grass", rotation);
        boolean n = (mask & 1) != 0, e = (mask & 2) != 0, s = (mask & 4) != 0, w = (mask & 8) != 0;
        plane(elements, 1, 0, 15, 1, height, n ? "grass" : "frame", n ? rotation : 0);
        plane(elements, 15, 1, 16, 15, height, e ? "grass" : "frame", e ? rotation : 0);
        plane(elements, 1, 15, 15, 16, height, s ? "grass" : "frame", s ? rotation : 0);
        plane(elements, 0, 1, 1, 15, height, w ? "grass" : "frame", w ? rotation : 0);
        plane(elements, 0, 0, 1, 1, height, n && w ? "grass" : "frame", n && w ? rotation : 0);
        plane(elements, 15, 0, 16, 1, height, n && e ? "grass" : "frame", n && e ? rotation : 0);
        plane(elements, 15, 15, 16, 16, height, s && e ? "grass" : "frame", s && e ? rotation : 0);
        plane(elements, 0, 15, 1, 16, height, s && w ? "grass" : "frame", s && w ? rotation : 0);
        // Narrow internal seat-mat seams, distinct from the one-pixel outer binding.
        seam(elements, 0, 0, 16, .125, height);
        seam(elements, 0, 15.875, 16, 16, height);
        seam(elements, 0, .125, .125, 15.875, height);
        seam(elements, 15.875, .125, 16, 15.875, height);
        model.add("elements", elements);
        return model;
    }

    private static void seam(JsonArray elements, double x1, double z1, double x2, double z2, int height) {
        JsonObject part = new JsonObject();
        part.add("from", array(x1, height + .001, z1)); part.add("to", array(x2, height + .001, z2));
        JsonObject faces = new JsonObject(); faces.add("up", face("seam", array(0, 0, 1, 1)));
        part.add("faces", faces); elements.add(part);
    }

    static void tableModels(BlockModelGenerators generator) {
        for (int mask = 0; mask < 16; mask++) {
            JsonObject model = new JsonObject(); model.addProperty("parent", "minecraft:block/block");
            JsonObject textures = new JsonObject();
            textures.addProperty("particle", "reverie_dreams:block/chabudai_top_15");
            textures.addProperty("top", "reverie_dreams:block/chabudai_top_15");
            textures.addProperty("side", "reverie_dreams:block/chabudai_side");
            textures.addProperty("trim", "minecraft:block/dark_oak_planks");
            model.add("textures", textures);
            JsonArray elements = new JsonArray();
            for (ChabudaiBlock.Part part : ChabudaiBlock.parts(mask))
                elements.add(box(part.x1(), part.y1(), part.z1(), part.x2(), part.y2(), part.z2(), part.texture()));
            model.add("elements", elements);
            generator.modelOutput.accept(ReverieDreams.id("block/chabudai_table_" + mask), () -> model);
        }
    }

    static void tableState(BlockModelGenerators generator, Block block) {
        MultiPartGenerator state = MultiPartGenerator.multiPart(block);
        for (int mask = 0; mask < 16; mask++) {
            state.with(new ConditionBuilder().term(TatamiBlock.NORTH, (mask & 1) != 0)
                            .term(TatamiBlock.EAST, (mask & 2) != 0).term(TatamiBlock.SOUTH, (mask & 4) != 0)
                            .term(TatamiBlock.WEST, (mask & 8) != 0),
                    plainVariant(ReverieDreams.id("block/chabudai_table_" + mask)));
        }
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            var model = plainVariant(ReverieDreams.id("block/tea_set_on_table"));
            model = switch (direction) {
                case EAST -> model.with(Y_ROT_90); case SOUTH -> model.with(Y_ROT_180);
                case WEST -> model.with(Y_ROT_270); default -> model;
            };
            state.with(new ConditionBuilder().term(ChabudaiBlock.HAS_TEA_SET, true)
                    .term(ChabudaiBlock.TEA_FACING, direction), model);
        }
        generator.blockStateOutput.accept(state);
    }

    static void teaModels(BlockModelGenerators generator) {
        for (boolean onTable : new boolean[]{false, true}) {
            JsonObject model = new JsonObject(); model.addProperty("parent", "minecraft:block/block");
            JsonObject textures = new JsonObject();
            textures.addProperty("particle", "minecraft:block/dark_oak_planks");
            textures.addProperty("tray", "minecraft:block/dark_oak_planks");
            textures.addProperty("porcelain", "minecraft:block/quartz_block_side");
            textures.addProperty("rim", "minecraft:block/yellow_terracotta");
            textures.addProperty("tea", "minecraft:block/brown_terracotta");
            model.add("textures", textures);
            JsonArray elements = new JsonArray();
            double height = onTable ? ChabudaiBlock.TOP_SURFACE * 16 : 0;
            for (TeaSetBlock.Part part : TeaSetBlock.parts())
                elements.add(box(part.x1(), part.y1() + height, part.z1(), part.x2(), part.y2() + height, part.z2(), part.texture()));
            model.add("elements", elements);
            generator.modelOutput.accept(ReverieDreams.id("block/tea_set" + (onTable ? "_on_table" : "")), () -> model);
        }
    }

    private static void plane(JsonArray elements, double x1, double z1, double x2, double z2, double y, String texture, int rotation) {
        JsonObject element = new JsonObject(); element.add("from", array(x1, y, z1)); element.add("to", array(x2, y, z2));
        JsonObject faces = new JsonObject(); JsonObject up = face(texture, rotation == 90
                ? array(z1, 16 - x2, z2, 16 - x1) : array(x1, z1, x2, z2));
        if (rotation != 0) up.addProperty("rotation", rotation);
        faces.add("up", up); element.add("faces", faces); elements.add(element);
    }
    private static JsonObject box(double x1, double y1, double z1, double x2, double y2, double z2, String texture) {
        JsonObject element = new JsonObject(); element.add("from", array(x1, y1, z1)); element.add("to", array(x2, y2, z2));
        JsonObject faces = new JsonObject();
        for (String face : new String[]{"up", "down", "north", "east", "south", "west"})
            faces.add(face, face(texture, face.equals("up") || face.equals("down") ? array(x1, z1, x2, z2)
                    : array(0, 0, x2 - x1, y2 - y1)));
        element.add("faces", faces); return element;
    }
    private static JsonObject face(String texture, JsonArray uv) {
        JsonObject face = new JsonObject(); face.addProperty("texture", "#" + texture); face.add("uv", uv); return face;
    }
    private static JsonArray array(double... values) {
        JsonArray array = new JsonArray(); for (double value : values) array.add(value); return array;
    }
}
