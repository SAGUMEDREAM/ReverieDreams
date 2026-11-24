package cc.thonly.reverie_dreams.entity;

import cc.thonly.reverie_dreams.entity.variant.YoukaiRabbitAppearance;
import cc.thonly.reverie_dreams.entity.variant.WalkingLegs;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.geckolib.loading.loader.GeckoLibGsonLoader;
import com.geckolib.loading.math.MathParser;
import net.minecraft.resources.Identifier;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.*;

class YoukaiRabbitResourcesTest {
    @BeforeAll static void bootstrap() { SharedConstants.tryDetectVersion(); Bootstrap.bootStrap(); }

    private static byte[] bytes(String path) throws Exception {
        try (var stream = Objects.requireNonNull(YoukaiRabbitResourcesTest.class.getClassLoader().getResourceAsStream(path), path)) {
            return stream.readAllBytes();
        }
    }
    private static JsonObject json(String path) throws Exception {
        return JsonParser.parseString(new String(bytes(path), StandardCharsets.UTF_8)).getAsJsonObject();
    }

    @Test void savedVariantIdsCoverTheSixApprovedPalettes() {
        assertEquals(6, YoukaiRabbitAppearance.COUNT);
        assertSame(YoukaiRabbitAppearance.GREEN, YoukaiRabbitAppearance.byId(-1));
        assertSame(YoukaiRabbitAppearance.MILK, YoukaiRabbitAppearance.byId(1));
        assertSame(YoukaiRabbitAppearance.MOON, YoukaiRabbitAppearance.byId(2));
        assertSame(YoukaiRabbitAppearance.RED_WHITE, YoukaiRabbitAppearance.byId(3));
        assertSame(YoukaiRabbitAppearance.AMBER, YoukaiRabbitAppearance.byId(4));
        assertSame(YoukaiRabbitAppearance.NIGHT_SAKURA, YoukaiRabbitAppearance.byId(5));
        assertSame(YoukaiRabbitAppearance.NIGHT_SAKURA, YoukaiRabbitAppearance.byId(99));
    }

    @Test void sharedGeometryAndEveryEntityStateHaveValidBones() throws Exception {
        var geometry = json("assets/reverie_dreams/geckolib/models/entity/youkai_rabbit.geo.json")
                .getAsJsonArray("minecraft:geometry").get(0).getAsJsonObject().getAsJsonArray("bones");
        Set<String> bones = new HashSet<>();
        int cubes = 0;
        for (var value : geometry) {
            var bone = value.getAsJsonObject();
            String name = bone.get("name").getAsString();
            assertTrue(bones.add(name));
            if (bone.has("cubes")) cubes += bone.getAsJsonArray("cubes").size();
            if (name.equals("head")) assertEquals(2, bone.getAsJsonArray("cubes").size());
        }
        assertEquals(17, bones.size());
        assertTrue(bones.containsAll(Set.of("right_hand", "left_hand")));
        assertEquals(34, cubes);
        var clips = json("assets/reverie_dreams/geckolib/animations/entity/youkai_rabbit.animation.json").getAsJsonObject("animations");
        assertTrue(clips.keySet().containsAll(Set.of("idle", "walk", "look", "accept", "talk", "observe", "gather", "melee", "ranged", "hurt", "jump", "fall", "land", "death")));
        for (var clip : clips.entrySet()) assertTrue(bones.containsAll(clip.getValue().getAsJsonObject().getAsJsonObject("bones").keySet()), clip.getKey());
        assertEquals(1, clips.getAsJsonObject("walk").get("animation_length").getAsDouble());
        var loader = new GeckoLibGsonLoader();
        var id = Identifier.parse("reverie_dreams:youkai_rabbit");
        var baked = loader.bakeGeckoLibModelFile(id, json("assets/reverie_dreams/geckolib/models/entity/youkai_rabbit.geo.json"));
        assertTrue(baked.getBone("head").isPresent());
        assertTrue(baked.getBone("right_hand").isPresent());
        assertTrue(baked.getBone("left_hand").isPresent());
        var bakedClips = loader.bakeGeckoLibAnimationsFile(id,
                json("assets/reverie_dreams/geckolib/animations/entity/youkai_rabbit.animation.json"), MathParser.create());
        for (String name : clips.keySet()) assertNotNull(bakedClips.getAnimation(name), name);
        var legs = clips.getAsJsonObject(WalkingLegs.ANIMATION).getAsJsonObject("bones");
        assertEquals(Set.of("right_leg", "left_leg"), legs.keySet());
        var walk = clips.getAsJsonObject("walk").getAsJsonObject("bones");
        var ranged = clips.getAsJsonObject("ranged").getAsJsonObject("bones");
        for (String side : List.of("right", "left")) {
            assertEquals(walk.get(side + "_leg"), legs.get(side + "_leg"));
            assertTrue(ranged.getAsJsonObject(side + "_arm").getAsJsonObject("rotation")
                    .getAsJsonObject("0.3").getAsJsonArray("vector").get(0).getAsFloat() < 0);
        }
    }

    @Test void nativeTexturesAndPolymerModelsContainTheCurrentMouthAndActions() throws Exception {
        var loader = new GeckoLibGsonLoader();
        for (var appearance : YoukaiRabbitAppearance.values()) {
            String id = appearance.resourceId().getPath();
            var geometry = json("assets/reverie_dreams/geckolib/models/entity/" + appearance.geometryId().getPath() + ".geo.json");
            assertTrue(loader.bakeGeckoLibModelFile(appearance.geometryId(), geometry).getBone("head").isPresent(), id);
            byte[] png = bytes("assets/reverie_dreams/textures/entity/" + id + ".png");
            var image = ImageIO.read(new ByteArrayInputStream(png));
            assertEquals(128, image.getWidth()); assertEquals(128, image.getHeight());
            assertNotEquals(image.getRGB(3, 8), image.getRGB(4, 8), id + " mouth");
            var bb = json("model/reverie_dreams/" + id + ".bbmodel");
            long cubes = 0;
            JsonObject outerHair = null;
            Set<String> locators = new HashSet<>();
            for (var value : bb.getAsJsonArray("elements")) {
                var element = value.getAsJsonObject();
                if (element.get("type").getAsString().equals("cube")) cubes++;
                if (element.get("type").getAsString().equals("locator")) locators.add(element.get("name").getAsString());
                if (element.get("name").getAsString().equals("hair_outer_layer")) outerHair = element;
            }
            assertEquals(34, cubes);
            assertNotNull(outerHair, id);
            var fringeUv = outerHair.getAsJsonObject("faces").getAsJsonObject("north").getAsJsonArray("uv");
            Set<Integer> fringeAlpha = new HashSet<>();
            for (int y = fringeUv.get(1).getAsInt(); y < fringeUv.get(3).getAsInt(); y++) {
                for (int x = fringeUv.get(0).getAsInt(); x < fringeUv.get(2).getAsInt(); x++) {
                    fringeAlpha.add(image.getRGB(x, y) >>> 24);
                }
            }
            assertEquals(Set.of(0, 255), fringeAlpha, id + " fringe");
            assertEquals(Set.of("right_item_locator", "left_item_locator"), locators);
            assertEquals(17, bb.getAsJsonArray("groups").size());
            var texture = bb.getAsJsonArray("textures").get(0).getAsJsonObject().get("source").getAsString();
            assertArrayEquals(png, Base64.getDecoder().decode(texture.substring(texture.indexOf(',') + 1)));
            Set<String> names = new HashSet<>(); bb.getAsJsonArray("animations").forEach(a -> names.add(a.getAsJsonObject().get("name").getAsString()));
            assertTrue(names.containsAll(Set.of("accept", "talk", "walk", "hurt", "melee", "ranged", "death", WalkingLegs.ANIMATION)), id);
        }
    }

    @Test void handAnchorsAndPolymerLocatorsFollowTheCorrectArm() throws Exception {
        var bb = json("model/reverie_dreams/youkai_rabbit.bbmodel");
        Map<String, String> parents = new HashMap<>();
        collectParents(bb.getAsJsonArray("outliner"), null, parents);
        for (String side : List.of("right", "left")) {
            JsonObject arm = null, hand = null, skin = null, locator = null;
            for (var value : bb.getAsJsonArray("groups")) {
                var g = value.getAsJsonObject();
                if (g.get("name").getAsString().equals(side + "_arm")) arm = g;
                if (g.get("name").getAsString().equals(side + "_hand")) hand = g;
            }
            for (var value : bb.getAsJsonArray("elements")) {
                var e = value.getAsJsonObject();
                if (e.get("name").getAsString().equals(side + "_arm_skin")) skin = e;
                if (e.get("name").getAsString().equals(side + "_item_locator")) locator = e;
            }
            assertNotNull(arm); assertNotNull(hand); assertNotNull(skin); assertNotNull(locator);
            assertEquals(arm.get("uuid").getAsString(), parents.get(hand.get("uuid").getAsString()));
            assertEquals(hand.get("uuid").getAsString(), parents.get(locator.get("uuid").getAsString()));
            for (int axis = 0; axis < 3; axis++) {
                double position = hand.getAsJsonArray("origin").get(axis).getAsDouble();
                assertTrue(position >= skin.getAsJsonArray("from").get(axis).getAsDouble());
                assertTrue(position <= skin.getAsJsonArray("to").get(axis).getAsDouble());
                assertEquals(position, locator.getAsJsonArray("position").get(axis).getAsDouble(), 1e-6);
            }
        }
    }

    private static void collectParents(JsonArray children, String parent, Map<String, String> result) {
        for (var value : children) {
            if (value.isJsonPrimitive()) result.put(value.getAsString(), parent);
            else {
                var group = value.getAsJsonObject(); String id = group.get("uuid").getAsString();
                result.put(id, parent); collectParents(group.getAsJsonArray("children"), id, result);
            }
        }
    }
}
