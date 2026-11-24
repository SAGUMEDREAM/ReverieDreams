package cc.thonly.reverie_dreams.fabric.datagen;

import cc.thonly.reverie_dreams.ReverieDreams;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Shared JSON geometry helpers for atlas-based Japanese furniture models.
 * Every cuboid face samples a 16x16 material tile from the caller's atlas,
 * and the item display transforms are derived from the geometry bounds.
 */
final class JapaneseModelKit {
    static final List<String> SIDES = List.of("north", "south", "east", "west", "up", "down");

    record Model(List<JsonObject> elements, String particle, boolean panel) {}

    private JapaneseModelKit() {}

    static JsonObject model(String atlas, Model model) {
        cull(model.elements());
        return build(atlas, model);
    }

    static JsonObject model(String atlas, Model model, JapaneseUvLayout layout) {
        cull(model.elements());
        layout.assign(model.elements());
        return build(atlas, model);
    }

    private static JsonObject build(String atlas, Model model) {
        JsonObject root = new JsonObject();
        root.addProperty("parent", "minecraft:block/block");
        JsonObject textures = new JsonObject();
        textures.addProperty("atlas", atlas);
        textures.addProperty("particle", model.particle());
        root.add("textures", textures);
        JsonArray array = new JsonArray();
        model.elements().forEach(array::add);
        root.add("elements", array);
        return root;
    }

    static void item(BlockModelGenerators generator, Block block, String name, Model model) {
        item(generator, block, name, name, model);
    }

    static void item(BlockModelGenerators generator, Block block, String name, String modelName, Model model) {
        JsonObject item = new JsonObject();
        item.addProperty("parent", blockId(modelName).toString());
        item.add("display", display(model));
        Identifier itemModel = ReverieDreams.id("item/" + name);
        generator.modelOutput.accept(itemModel, () -> item);
        generator.registerSimpleItemModel(block, itemModel);
    }

    static JsonArray array(double... values) {
        JsonArray array = new JsonArray();
        for (double value : values) {
            array.add(value);
        }
        return array;
    }

    static Identifier blockId(String name) {
        return ReverieDreams.id("block/" + name);
    }

    private static JsonObject display(Model model) {
        List<double[]> points = new ArrayList<>();
        for (JsonObject element : model.elements()) {
            double[] from = values(element.getAsJsonArray("from"));
            double[] to = values(element.getAsJsonArray("to"));
            for (int flags = 0; flags < 8; flags++) {
                double[] point = new double[3];
                for (int axis = 0; axis < 3; axis++) {
                    point[axis] = (flags & (1 << axis)) != 0 ? to[axis] : from[axis];
                }
                points.add(point);
            }
        }
        double[] min = new double[3];
        double[] max = new double[3];
        for (int axis = 0; axis < 3; axis++) {
            double low = Double.MAX_VALUE, high = -Double.MAX_VALUE;
            for (double[] point : points) {
                low = Math.min(low, point[axis]);
                high = Math.max(high, point[axis]);
            }
            min[axis] = low;
            max[axis] = high;
        }
        double span = 0;
        for (int axis = 0; axis < 3; axis++) {
            span = Math.max(span, max[axis] - min[axis]);
        }
        double[] guiRotation = model.panel() ? new double[]{10, 200, 0} : new double[]{30, 225, 0};
        double[] guiBounds = rotateBounds(points, guiRotation, 1.0, false);
        double guiScale = round(Math.min(1.25, 14.6 / Math.max(guiBounds[3] - guiBounds[0], guiBounds[4] - guiBounds[1])));
        double holdScale = round(Math.min(0.65, 6.4 / span));
        double thirdScale = round(Math.min(0.55, 6.0 / span));

        JsonObject display = new JsonObject();
        display.add("gui", transform(guiRotation, guiScale, new double[]{0, 0, 0}, false, false, points));
        display.add("firstperson_righthand", transform(new double[]{0, 45, 0}, holdScale, new double[]{0, 0, 0}, false, false, points));
        display.add("firstperson_lefthand", transform(new double[]{0, 225, 0}, holdScale, new double[]{0, 0, 0}, true, false, points));
        display.add("thirdperson_righthand", transform(new double[]{75, 45, 0}, thirdScale, new double[]{0, 2.5, 0}, false, false, points));
        display.add("thirdperson_lefthand", transform(new double[]{75, 45, 0}, thirdScale, new double[]{0, 2.5, 0}, true, false, points));
        display.add("ground", transform(new double[]{0, 0, 0}, round(Math.min(0.4, 4.0 / span)),
                new double[]{0, 1, 0}, false, true, points));
        display.add("fixed", transform(new double[]{0, 180, 0}, round(Math.min(0.75, 8.0 / span)),
                new double[]{0, 0, 0}, false, false, points));
        display.add("on_shelf", transform(new double[]{0, 180, 0}, round(Math.min(0.55, 6.0 / span)),
                new double[]{0, 0, 0}, false, true, points));
        return display;
    }

    private static JsonObject transform(double[] rotation, double scale, double[] anchor,
                                        boolean left, boolean bottom, List<double[]> points) {
        double[] bounds = rotateBounds(points, rotation, scale, left);
        double[] mid = new double[3];
        for (int axis = 0; axis < 3; axis++) {
            mid[axis] = bottom && axis == 1 ? bounds[axis] : (bounds[axis] + bounds[axis + 3]) * 0.5;
        }
        JsonObject entry = new JsonObject();
        entry.add("rotation", array(rotation[0], rotation[1], rotation[2]));
        JsonArray translation = new JsonArray();
        for (int axis = 0; axis < 3; axis++) {
            translation.add(round((anchor[axis] - mid[axis]) * (left && axis == 0 ? -1 : 1)));
        }
        entry.add("translation", translation);
        entry.add("scale", array(round(scale), round(scale), round(scale)));
        return entry;
    }

    /** Returns min x/y/z and max x/y/z of the rotated, scaled corners. */
    private static double[] rotateBounds(List<double[]> points, double[] rotation, double scale, boolean left) {
        double[] bounds = {Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE,
                -Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE};
        for (double[] point : points) {
            double[] scaled = new double[3];
            for (int axis = 0; axis < 3; axis++) {
                scaled[axis] = (point[axis] - 8) * scale;
            }
            double[] rotated = rotate(scaled, rotation, left);
            for (int axis = 0; axis < 3; axis++) {
                bounds[axis] = Math.min(bounds[axis], rotated[axis]);
                bounds[axis + 3] = Math.max(bounds[axis + 3], rotated[axis]);
            }
        }
        return bounds;
    }

    private static double[] rotate(double[] point, double[] rotation, boolean left) {
        double[] angle = new double[3];
        for (int axis = 0; axis < 3; axis++) {
            double degrees = left && axis > 0 ? -rotation[axis] : rotation[axis];
            angle[axis] = Math.toRadians(degrees);
        }
        double cx = Math.cos(angle[0]), sx = Math.sin(angle[0]);
        double cy = Math.cos(angle[1]), sy = Math.sin(angle[1]);
        double cz = Math.cos(angle[2]), sz = Math.sin(angle[2]);
        double a = point[0], b = point[1], c = point[2];
        double na = a * cz - b * sz, nb = a * sz + b * cz;
        a = na; b = nb;
        na = a * cy + c * sy; double nc = -a * sy + c * cy;
        a = na; c = nc;
        nb = b * cx - c * sx; nc = b * sx + c * cx;
        return new double[]{a, nb, nc};
    }

    private static double round(double value) {
        return Math.round(value * 10000.0) / 10000.0;
    }

    static JsonObject box(Map<String, Integer> tiles, double x1, double y1, double z1,
                          double x2, double y2, double z2, String material) {
        return box(tiles, x1, y1, z1, x2, y2, z2, material, Map.of());
    }

    static JsonObject box(Map<String, Integer> tiles, double x1, double y1, double z1,
                          double x2, double y2, double z2, String material,
                          Map<String, String> faceMaterials) {
        return box(tiles, x1, y1, z1, x2, y2, z2, material, faceMaterials, Map.of());
    }

    static JsonObject box(Map<String, Integer> tiles, double x1, double y1, double z1,
                          double x2, double y2, double z2, String defaultMaterial,
                          Map<String, String> faceMaterials, Map<String, double[]> uvOverrides) {
        double[] from = {x1, y1, z1};
        double[] to = {x2, y2, z2};
        double[] size = {x2 - x1, y2 - y1, z2 - z1};
        JsonObject element = new JsonObject();
        element.add("from", array(x1, y1, z1));
        element.add("to", array(x2, y2, z2));
        JsonObject faces = new JsonObject();
        for (String side : SIDES) {
            String material = faceMaterials.getOrDefault(side, defaultMaterial);
            int tile = tiles.getOrDefault(material, 0);
            JsonObject face = new JsonObject();
            face.addProperty("texture", "#atlas");
            double[] override = uvOverrides.get(side);
            if (override != null) {
                double baseU = (tile % 8) * 16.0, baseV = (tile / 8) * 16.0;
                face.add("uv", array((baseU + override[0]) / 8, (baseV + override[1]) / 8,
                        (baseU + override[2]) / 8, (baseV + override[3]) / 8));
            } else {
                double[] faceSize;
                double[] origin;
                switch (side) {
                    case "north" -> { faceSize = new double[]{size[0], size[1]}; origin = new double[]{16 - to[0], 16 - to[1]}; }
                    case "south" -> { faceSize = new double[]{size[0], size[1]}; origin = new double[]{from[0], 16 - to[1]}; }
                    case "east" -> { faceSize = new double[]{size[2], size[1]}; origin = new double[]{16 - to[2], 16 - to[1]}; }
                    case "west" -> { faceSize = new double[]{size[2], size[1]}; origin = new double[]{from[2], 16 - to[1]}; }
                    case "up" -> { faceSize = new double[]{size[0], size[2]}; origin = new double[]{from[0], from[2]}; }
                    default -> { faceSize = new double[]{size[0], size[2]}; origin = new double[]{from[0], 16 - to[2]}; }
                }
                double ox = ((origin[0] % 16) + 16) % 16;
                double oy = ((origin[1] % 16) + 16) % 16;
                ox = Math.min(ox, 16 - faceSize[0]);
                oy = Math.min(oy, 16 - faceSize[1]);
                double u = (tile % 8) * 16.0 + ox, v = (tile / 8) * 16.0 + oy;
                face.add("uv", array(u / 8, v / 8, (u + faceSize[0]) / 8, (v + faceSize[1]) / 8));
            }
            faces.add(side, face);
        }
        element.add("faces", faces);
        return element;
    }

    /** Cuboid for {@link JapaneseUvLayout}; the material marker is replaced with a packed UV. */
    static JsonObject layoutBox(double x1, double y1, double z1, double x2, double y2, double z2, String material) {
        JsonObject element = new JsonObject();
        element.add("from", array(x1, y1, z1));
        element.add("to", array(x2, y2, z2));
        JsonObject faces = new JsonObject();
        for (String side : SIDES) {
            JsonObject face = new JsonObject();
            face.addProperty("texture", "#atlas");
            face.addProperty("x-rd-material", material);
            faces.add(side, face);
        }
        element.add("faces", faces);
        return element;
    }

    static void cull(List<JsonObject> elements) {
        for (int i = 0; i < elements.size(); i++) {
            for (int j = i + 1; j < elements.size(); j++) {
                JsonObject first = elements.get(i), second = elements.get(j);
                double[] a1 = values(first.getAsJsonArray("from")), b1 = values(first.getAsJsonArray("to"));
                double[] a2 = values(second.getAsJsonArray("from")), b2 = values(second.getAsJsonArray("to"));
                for (int axis = 0; axis < 3; axis++) {
                    int axis1 = (axis + 1) % 3, axis2 = (axis + 2) % 3;
                    if (a1[axis1] != a2[axis1] || b1[axis1] != b2[axis1]
                            || a1[axis2] != a2[axis2] || b1[axis2] != b2[axis2]) {
                        continue;
                    }
                    String[] names = axis == 0 ? new String[]{"west", "east"}
                            : axis == 1 ? new String[]{"down", "up"} : new String[]{"north", "south"};
                    JsonObject firstFaces = first.getAsJsonObject("faces");
                    JsonObject secondFaces = second.getAsJsonObject("faces");
                    if (b1[axis] == a2[axis]) {
                        firstFaces.remove(names[1]);
                        secondFaces.remove(names[0]);
                    }
                    if (b2[axis] == a1[axis]) {
                        secondFaces.remove(names[1]);
                        firstFaces.remove(names[0]);
                    }
                }
            }
        }
    }

    static double[] values(JsonArray array) {
        double[] values = new double[array.size()];
        for (int index = 0; index < values.length; index++) {
            values[index] = array.get(index).getAsDouble();
        }
        return values;
    }
}
