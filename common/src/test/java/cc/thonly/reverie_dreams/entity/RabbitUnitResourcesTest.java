package cc.thonly.reverie_dreams.entity;

import cc.thonly.reverie_dreams.entity.variant.RabbitUnitAction;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class RabbitUnitResourcesTest {
    private static JsonObject json(String path) throws Exception {
        try (var stream = resource(path)) { return JsonParser.parseString(new String(stream.readAllBytes(), StandardCharsets.UTF_8)).getAsJsonObject(); }
    }
    private static InputStream resource(String path) {
        return Objects.requireNonNull(RabbitUnitResourcesTest.class.getClassLoader().getResourceAsStream(path), path);
    }
    private static JsonObject source(int variant) throws Exception { return json("model/reverie_dreams/rabbit_unit_" + variant + ".bbmodel"); }
    private static double value(JsonArray array, int axis) { return array.get(axis).getAsDouble(); }

    @Test void threeVariantsHaveMatchingSkeletonsAndAllRequiredAnimations() throws Exception {
        Set<String> expected = new HashSet<>();
        for (var action : RabbitUnitAction.values()) expected.add(action.animationName());
        expected.add("animation.walk_legs");
        for (String action : List.of("hold", "gun_walk", "aim", "shoot", "reload", "melee"))
            for (int type = 1; type <= 5; type++) expected.add("animation." + action + "_type_" + type);
        Set<String> referenceBones = null;
        for (int variant = 0; variant < 3; variant++) {
            var geometry = json("assets/reverie_dreams/geckolib/models/entity/rabbit_unit_" + variant + ".geo.json");
            Set<String> bones = new HashSet<>();
            geometry.getAsJsonArray("minecraft:geometry").get(0).getAsJsonObject().getAsJsonArray("bones")
                    .forEach(b -> assertTrue(bones.add(b.getAsJsonObject().get("name").getAsString())));
            if (referenceBones == null) referenceBones = bones; else assertEquals(referenceBones, bones);
            assertTrue(bones.containsAll(Set.of("root", "body", "head", "ear_right", "ear_left", "right_arm", "right_forearm", "right_hand", "left_hand")));
            var animations = json("assets/reverie_dreams/geckolib/animations/entity/rabbit_unit_" + variant + ".animation.json").getAsJsonObject("animations");
            assertEquals(expected, animations.keySet());
            assertEquals(2.4, animations.getAsJsonObject("animation.evacuate").get("animation_length").getAsDouble(), .000001);
            assertEquals("hold_on_last_frame", animations.getAsJsonObject("animation.reload").get("loop").getAsString());
            var root = animations.getAsJsonObject("animation.evacuate").getAsJsonObject("bones").getAsJsonObject("root");
            assertTrue(root.has("position") && root.has("scale"), "Evacuation must ascend and disappear");
        }
    }

    @Test void everyFaceUsesTwoTexelsPerModelUnitWithoutOffGridUvs() throws Exception {
        for (int variant = 0; variant < 3; variant++) {
            try (var png = resource("assets/reverie_dreams/textures/entity/rabbit_unit_" + variant + ".png")) {
                var image = ImageIO.read(png); assertEquals(256, image.getWidth()); assertEquals(256, image.getHeight());
            }
            var model = source(variant);
            for (var item : model.getAsJsonArray("elements")) {
                var cube = item.getAsJsonObject(); if (!cube.get("type").getAsString().equals("cube")) continue;
                double dx = value(cube.getAsJsonArray("to"), 0) - value(cube.getAsJsonArray("from"), 0);
                double dy = value(cube.getAsJsonArray("to"), 1) - value(cube.getAsJsonArray("from"), 1);
                double dz = value(cube.getAsJsonArray("to"), 2) - value(cube.getAsJsonArray("from"), 2);
                for (var entry : cube.getAsJsonObject("faces").entrySet()) {
                    String face = entry.getKey(); var uv = entry.getValue().getAsJsonObject().getAsJsonArray("uv");
                    double w = Set.of("east", "west").contains(face) ? dz : dx;
                    double h = Set.of("up", "down").contains(face) ? dz : dy;
                    assertEquals(w * 2, Math.abs(value(uv, 2) - value(uv, 0)), .000001, cube.get("name") + "/" + face);
                    assertEquals(h * 2, Math.abs(value(uv, 3) - value(uv, 1)), .000001, cube.get("name") + "/" + face);
                    for (var coordinate : uv) { double n = coordinate.getAsDouble(); assertEquals(Math.rint(n), n, .000001); assertTrue(n >= 0 && n <= 256); }
                }
            }
        }
    }

    @Test void repeatedOutwardFacesDoNotOverlapAndFlicker() throws Exception {
        for (int variant = 0; variant < 3; variant++) {
            var cubes = new ArrayList<JsonObject>();
            source(variant).getAsJsonArray("elements").forEach(item -> { var cube = item.getAsJsonObject(); if (cube.get("type").getAsString().equals("cube")) cubes.add(cube); });
            for (int i = 0; i < cubes.size(); i++) for (int j = i + 1; j < cubes.size(); j++) {
                var a = cubes.get(i); var b = cubes.get(j);
                for (int axis = 0; axis < 3; axis++) for (String edge : List.of("from", "to")) {
                    if (Math.abs(value(a.getAsJsonArray(edge), axis) - value(b.getAsJsonArray(edge), axis)) > .000001) continue;
                    int u = (axis + 1) % 3, v = (axis + 2) % 3;
                    double overlapU = Math.min(value(a.getAsJsonArray("to"), u), value(b.getAsJsonArray("to"), u)) - Math.max(value(a.getAsJsonArray("from"), u), value(b.getAsJsonArray("from"), u));
                    double overlapV = Math.min(value(a.getAsJsonArray("to"), v), value(b.getAsJsonArray("to"), v)) - Math.max(value(a.getAsJsonArray("from"), v), value(b.getAsJsonArray("from"), v));
                    if (overlapU <= .000001 || overlapV <= .000001) continue;
                    double startU = Math.max(value(a.getAsJsonArray("from"), u), value(b.getAsJsonArray("from"), u));
                    double startV = Math.max(value(a.getAsJsonArray("from"), v), value(b.getAsJsonArray("from"), v));
                    for (double sampleU = startU; sampleU < startU + overlapU - .000001; sampleU += .5)
                        for (double sampleV = startV; sampleV < startV + overlapV - .000001; sampleV += .5) {
                            double[] point = new double[3];
                            point[axis] = value(a.getAsJsonArray(edge), axis) + (edge.equals("from") ? -.0001 : .0001);
                            point[u] = (sampleU + Math.min(sampleU + .5, startU + overlapU)) / 2;
                            point[v] = (sampleV + Math.min(sampleV + .5, startV + overlapV)) / 2;
                            boolean covered = cubes.stream().anyMatch(c -> c != a && c != b && contains(c, point));
                            assertTrue(covered, "Exposed coplanar faces: " + a.get("name") + " / " + b.get("name") + " axis=" + axis + " point=" + Arrays.toString(point));
                        }
                }
            }
        }
    }

    @Test void polymerEquipmentLocatorsMatchNativeHandBoneOrigins() throws Exception {
        for (int variant = 0; variant < 3; variant++) {
            var model = source(variant); Map<String, JsonObject> elements = new HashMap<>();
            model.getAsJsonArray("elements").forEach(e -> elements.put(e.getAsJsonObject().get("uuid").getAsString(), e.getAsJsonObject()));
            var hierarchy = new ArrayList<JsonObject>(); collect(model.getAsJsonArray("outliner"), hierarchy);
            var groups = model.getAsJsonArray("groups").asList().stream().map(e -> e.getAsJsonObject()).toList();
            for (String hand : List.of("right_hand", "left_hand")) {
                var bone = groups.stream().filter(g -> hand.equals(g.get("name").getAsString())).findFirst().orElseThrow();
                var locator = elements.values().stream().filter(e -> e.get("name").getAsString().equals(hand.replace("_hand", "_item_locator"))).findFirst().orElseThrow();
                var node = hierarchy.stream().filter(g -> g.get("uuid").equals(bone.get("uuid"))).findFirst().orElseThrow();
                assertTrue(node.getAsJsonArray("children").asList().stream().anyMatch(c -> c.isJsonPrimitive() && c.getAsString().equals(locator.get("uuid").getAsString())));
                assertEquals(bone.getAsJsonArray("origin"), locator.getAsJsonArray("position"));
            }
        }
    }
    private static void collect(JsonArray outliner, List<JsonObject> groups) {
        for (var item : outliner) if (item.isJsonObject()) { var group = item.getAsJsonObject(); groups.add(group); collect(group.getAsJsonArray("children"), groups); }
    }
    private static boolean contains(JsonObject cube, double[] point) {
        for (int axis = 0; axis < 3; axis++) if (point[axis] <= value(cube.getAsJsonArray("from"), axis)
                || point[axis] >= value(cube.getAsJsonArray("to"), axis)) return false;
        return true;
    }
}
