package cc.thonly.reverie_dreams.polymer.entity;

import cc.thonly.reverie_dreams.entity.variant.OniAppearance;
import cc.thonly.reverie_dreams.entity.variant.OniEquipment;
import com.geckolib.loading.loader.GeckoLibGsonLoader;
import com.geckolib.loading.math.MathParser;
import com.google.gson.JsonParser;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import com.google.gson.JsonElement;
import java.util.Arrays;
import java.util.stream.StreamSupport;
import static org.junit.jupiter.api.Assertions.*;

class OniModelTest {
    @BeforeAll static void bootstrap() { SharedConstants.tryDetectVersion(); Bootstrap.bootStrap(); }

    @Test void allThreeVariantsBakeAndHaveUniqueIdentifiersAndMatchingTextures() throws Exception {
        var resources = Path.of("../common/src/main/resources");
        var loader = new GeckoLibGsonLoader();
        for (var appearance : OniAppearance.values()) {
            String name = appearance.modelId().getPath();
            var model = loader.bakeGeckoLibModelFile(appearance.resourceId(), JsonParser.parseString(Files.readString(
                    resources.resolve("assets/reverie_dreams/geckolib/models/entity/" + name + ".geo.json"))).getAsJsonObject());
            for (String bone : Set.of("right_hand", "left_hand", "right_lower_leg", "left_lower_leg", "right_foot", "left_foot")) {
                assertTrue(model.getBone(bone).isPresent(), name + ": " + bone);
            }
            for (String locator : Set.of("right_item_locator", "left_item_locator", "helmet_locator")) assertTrue(model.getLocator(locator).isPresent(), name);
            var bb = JsonParser.parseString(Files.readString(Path.of("src/main/resources/model/reverie_dreams/" + name + ".bbmodel"))).getAsJsonObject();
            var identifiers = new HashSet<String>();
            for (String array : Set.of("elements", "groups")) for (var item : bb.getAsJsonArray(array)) {
                assertTrue(identifiers.add(item.getAsJsonObject().get("uuid").getAsString()), "Duplicate UUID " + name);
            }
            byte[] embedded = Base64.getDecoder().decode(bb.getAsJsonArray("textures").get(0).getAsJsonObject().get("source").getAsString().split(",",2)[1]);
            assertArrayEquals(Files.readAllBytes(resources.resolve("assets/reverie_dreams/textures/entity/" + name + ".png")), embedded);
            var atlas = ImageIO.read(new ByteArrayInputStream(embedded));
            var original = ImageIO.read(resources.resolve("assets/reverie_dreams/textures/entity/player/skin/" + name + ".png").toFile());
            assertEquals(256, atlas.getWidth());
            assertEquals(256, atlas.getHeight());
            var headFaces = StreamSupport.stream(bb.getAsJsonArray("elements").spliterator(), false)
                    .map(JsonElement::getAsJsonObject).filter(e -> e.get("name").getAsString().equals("head"))
                    .findFirst().orElseThrow().getAsJsonObject("faces");
            var originalUv = Map.of("north", new int[]{8,8}, "east", new int[]{0,8}, "south", new int[]{24,8},
                    "west", new int[]{16,8}, "up", new int[]{8,0}, "down", new int[]{16,0});
            for (var face : originalUv.entrySet()) {
                var uv = headFaces.getAsJsonObject(face.getKey()).getAsJsonArray("uv");
                for (int y=0;y<8;y++) for (int x=0;x<8;x++) assertEquals(
                        original.getRGB(face.getValue()[0]+x, face.getValue()[1]+y),
                        atlas.getRGB(uv.get(0).getAsInt()+x, uv.get(1).getAsInt()+y), name + ": " + face.getKey());
            }
            assertEquals(appearance, OniAppearance.of(appearance.modelId()));
        }
    }

    @Test void heavyMeleeMatchesWindupAndDeathKeepsItsGroundingTracks() throws Exception {
        var resources = Path.of("../common/src/main/resources");
        var loader = new GeckoLibGsonLoader();
        for (var appearance : OniAppearance.values()) {
            var json = JsonParser.parseString(Files.readString(resources.resolve("assets/reverie_dreams/geckolib/animations/entity/"
                    + appearance.modelId().getPath() + ".animation.json"))).getAsJsonObject();
            for (var clip : json.getAsJsonObject("animations").entrySet()) {
                for (var bone : clip.getValue().getAsJsonObject().getAsJsonObject("bones").entrySet()) {
                    for (var channel : bone.getValue().getAsJsonObject().entrySet()) {
                        double previous = -1;
                        for (String timestamp : channel.getValue().getAsJsonObject().keySet()) {
                            double time = Double.parseDouble(timestamp);
                            assertTrue(time >= previous, "GeckoLib requires sorted timestamps: " + clip.getKey());
                            previous = time;
                        }
                    }
                }
            }
            var animations = loader.bakeGeckoLibAnimationsFile(appearance.resourceId(), json, MathParser.create());
            for (String clip : Set.of("idle", "walk", "melee", "hurt", "death", "heavy_ready")) assertNotNull(animations.getAnimation("animation." + clip));
            var melee = json.getAsJsonObject("animations").getAsJsonObject("animation.melee");
            assertFalse(melee.has("loop") && melee.get("loop").getAsBoolean());
            assertTrue(melee.getAsJsonObject("bones").getAsJsonObject("right_arm").getAsJsonObject("rotation").has("0.6"));
            assertEquals(1.2, melee.get("animation_length").getAsDouble());
            var death = json.getAsJsonObject("animations").getAsJsonObject("animation.death");
            assertEquals(1, death.get("animation_length").getAsDouble());
            assertFalse(death.getAsJsonObject("bones").getAsJsonObject("body").has("scale"));
            var root = death.getAsJsonObject("bones").getAsJsonObject("root");
            assertEquals(-88, root.getAsJsonObject("rotation").getAsJsonObject("1.0")
                    .getAsJsonArray("vector").get(0).getAsDouble());
            assertTrue(root.getAsJsonObject("position").has("0.12"), "Preserve exact key times after native export");
            assertTrue(death.getAsJsonObject("bones").getAsJsonObject("right_lower_leg").has("rotation"));
            var bakedRoot = Arrays.stream(animations.getAnimation("animation.death").boneAnimations())
                    .filter(bone -> bone.boneName().equals("root")).findFirst().orElseThrow();
            assertTrue(bakedRoot.rotationKeyFrames().xKeyframes().length >= 40, "Root fall must survive animation baking");
            assertTrue(bakedRoot.positionKeyFrames().yKeyframes().length >= 40, "Grounding track must survive animation baking");
        }
    }

    @Test void clubItemDisplayTransformsPlaceActualHandleCenterAtPalm() throws Exception {
        var resources = Path.of("../common/src/main/resources/assets/reverie_dreams/models/item");
        for (String name : Set.of("iron_bar", "oni_iron_bar")) {
            var item = JsonParser.parseString(Files.readString(resources.resolve(name + ".json"))).getAsJsonObject();
            var handle = item.getAsJsonArray("elements").get(0).getAsJsonObject();
            for (String hand : Set.of("thirdperson_righthand", "thirdperson_lefthand")) {
                var display = item.getAsJsonObject("display").getAsJsonObject(hand);
                assertFalse(display.has("rotation"));
                assertFalse(display.has("scale"));
                for (int axis=0;axis<3;axis++) {
                    double center = (handle.getAsJsonArray("from").get(axis).getAsDouble()
                            + handle.getAsJsonArray("to").get(axis).getAsDouble()) / 2;
                    double offset = axis == 1 ? OniEquipment.CLUB_GRIP_OFFSET * 16 : 0;
                    assertEquals(0, center - 8 + display.getAsJsonArray("translation").get(axis).getAsDouble() + offset, 1e-6);
                }
            }
        }
    }
}
