package cc.thonly.reverie_dreams.entity.variant;

import cc.thonly.reverie_dreams.client.model.entity.GensokyoFairyModel;
import cc.thonly.reverie_dreams.entity.Yousei;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.loading.loader.GeckoLibGsonLoader;
import com.geckolib.loading.math.MathParser;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.SharedConstants;
import net.minecraft.resources.Identifier;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class GensokyoFairyResourcesTest {
    @BeforeAll
    static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void hoveringUsesFlightAndLandingRestoresGroundAnimations() {
        assertEquals(GensokyoFairyAnimation.FLY, GensokyoFairyAnimation.select(false, false));
        assertEquals(GensokyoFairyAnimation.FLY, GensokyoFairyAnimation.select(false, true));
        assertEquals(GensokyoFairyAnimation.IDLE, GensokyoFairyAnimation.select(true, false));
        assertEquals(GensokyoFairyAnimation.WALK, GensokyoFairyAnimation.select(true, true));
    }

    @Test
    void skinLookupUsesFullIdentifierAndFallsBackForCustomSkins() {
        assertEquals(GensokyoFairySkin.ORANGE, GensokyoFairySkin.of(Identifier.parse("reverie_dreams:yousei_02")));
        assertEquals(GensokyoFairySkin.GREEN, GensokyoFairySkin.of(Identifier.parse("reverie_dreams:yousei_03")));
        assertEquals(GensokyoFairySkin.SUNFLOWER, GensokyoFairySkin.of(Identifier.parse("reverie_dreams:sunflower_yousei")));
        assertEquals(GensokyoFairySkin.BLUE, GensokyoFairySkin.of(Identifier.parse("another_mod:yousei_02")));
        assertEquals(GensokyoFairySkin.BLUE, GensokyoFairySkin.of(null));
    }

    @Test
    void geckoLibActuallyBakesModelAndEverySelectedAnimation() throws Exception {
        var loader = new GeckoLibGsonLoader();
        var id = Identifier.parse("reverie_dreams:entity/gensokyo_fairy");
        JsonObject geometry = json("assets/reverie_dreams/geckolib/models/entity/gensokyo_fairy.geo.json");
        var baked = loader.bakeGeckoLibModelFile(id, geometry);
        assertEquals(1, baked.topLevelBones().length);
        assertEquals("body", baked.topLevelBones()[0].name());
        for (String bone : new String[]{"body", "head", "wing_right", "wing_left", "yousei_wing", "yousei_wing3",
                "right_forearm", "left_forearm", "right_calf", "left_calf"}) {
            assertTrue(baked.getBone(bone).isPresent(), bone);
        }
        var animations = loader.bakeGeckoLibAnimationsFile(id,
                json("assets/reverie_dreams/geckolib/animations/entity/gensokyo_fairy.animation.json"), MathParser.create());
        for (var state : GensokyoFairyAnimation.values()) {
            assertNotNull(animations.getAnimation(state.animationName()), state.animationName());
        }
    }

    @Test
    void polymerModelsEmbedMatchingAtlasesAndKeepAnimationBoneReferences() throws Exception {
        for (var skin : GensokyoFairySkin.values()) {
            JsonObject model = json("model/reverie_dreams/" + skin.modelId().getPath() + ".bbmodel");
            model.getAsJsonArray("elements").forEach(entry -> {
                JsonObject cube = entry.getAsJsonObject();
                if (cube.has("type") && !cube.get("type").getAsString().equals("cube")) return;
                if (!cube.get("name").getAsString().startsWith("yousei_wing")) {
                    assertTrue(cube.getAsJsonArray("to").get(1).getAsDouble()
                            > cube.getAsJsonArray("from").get(1).getAsDouble(), "Collapsed limb: " + cube.get("name"));
                }
            });
            Set<String> bones = new HashSet<>();
            model.getAsJsonArray("groups").forEach(group -> bones.add(group.getAsJsonObject().get("uuid").getAsString()));
            Set<String> animations = new HashSet<>();
            model.getAsJsonArray("animations").forEach(entry -> {
                JsonObject animation = entry.getAsJsonObject();
                animations.add(animation.get("name").getAsString());
                var state = Arrays.stream(GensokyoFairyAnimation.values())
                        .filter(value -> value.animationName().equals(animation.get("name").getAsString())).findFirst().orElseThrow();
                assertEquals(state.looping() ? "loop" : "once", animation.get("loop").getAsString());
                assertTrue(bones.containsAll(animation.getAsJsonObject("animators").keySet()));
            });
            for (var state : GensokyoFairyAnimation.values()) {
                assertTrue(animations.contains(state.animationName()));
            }
            String embedded = model.getAsJsonArray("textures").get(0).getAsJsonObject().get("source").getAsString();
            byte[] png = Base64.getDecoder().decode(embedded.substring(embedded.indexOf(',') + 1));
            try (var stream = getClass().getClassLoader().getResourceAsStream("assets/reverie_dreams/" + skin.textureId().getPath())) {
                assertNotNull(stream);
                assertArrayEquals(stream.readAllBytes(), png);
            }
            var image = ImageIO.read(new ByteArrayInputStream(png));
            assertEquals(512, image.getWidth());
            assertEquals(256, image.getHeight());
            assertEquals(128, model.getAsJsonObject("resolution").get("width").getAsInt());
            assertEquals(64, model.getAsJsonObject("resolution").get("height").getAsInt());
            assertEquals(128, model.getAsJsonArray("textures").get(0).getAsJsonObject().get("uv_width").getAsInt());
            assertEquals(0, image.getRGB(500, 250) >>> 24);
        }
    }

    private static JsonObject json(String path) throws Exception {
        try (var stream = GensokyoFairyResourcesTest.class.getClassLoader().getResourceAsStream(path)) {
            assertNotNull(stream, path);
            return JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
        }
    }

    @Test
    void formalBlueSelectionExcludesOtherSpeciesColorsAndUnknownSkins() {
        var blue = Identifier.parse("reverie_dreams:yousei_01");
        assertTrue(GensokyoFairySkin.usesFormalBlueModel(true, blue));
        assertFalse(GensokyoFairySkin.usesFormalBlueModel(false, blue));
        assertFalse(GensokyoFairySkin.usesFormalBlueModel(true, Identifier.parse("reverie_dreams:yousei_02")));
        assertFalse(GensokyoFairySkin.usesFormalBlueModel(true, Identifier.parse("custom:yousei_01")));
        assertFalse(GensokyoFairySkin.usesFormalBlueModel(true, null));
    }

    @Test
    void modelAndTextureSwitchWithoutChangingLegacyDefaults() {
        var model = new GensokyoFairyModel<Yousei>();
        Map<DataTicket<?>,Object> data = new HashMap<>();
        GeoRenderState state = () -> data;
        assertEquals(Identifier.parse("reverie_dreams:entity/gensokyo_fairy"), model.getModelResource(state));
        for (var appearance : GensokyoFairyAppearance.values()) {
            state.addGeckolibData(GensokyoFairyModel.APPEARANCE, appearance);
            assertEquals(appearance.resourceId(), model.getModelResource(state));
            assertEquals(appearance.textureId(), model.getTextureResource(state));
        }
        data.remove(GensokyoFairyModel.APPEARANCE);
        state.addGeckolibData(GensokyoFairyModel.TEXTURE, GensokyoFairySkin.ORANGE.textureId());
        assertEquals(GensokyoFairySkin.ORANGE.textureId(), model.getTextureResource(state));
    }

    @Test
    void formalBlueBakesAndPreservesItsAtlasAnimationReferencesAndFaceSkin() throws Exception {
        var loader = new GeckoLibGsonLoader();
        var id = Identifier.parse("reverie_dreams:entity/gensokyo_fairy_blue");
        var baked = loader.bakeGeckoLibModelFile(id, json("assets/reverie_dreams/geckolib/models/entity/gensokyo_fairy_blue.geo.json"));
        for (String bone : new String[]{"dress_skirt", "bloomers", "bloomer_right", "bloomer_left",
                "upper_wing_right", "lower_wing_right", "upper_wing_left", "lower_wing_left"}) {
            assertTrue(baked.getBone(bone).isPresent(), bone);
        }
        var animations = loader.bakeGeckoLibAnimationsFile(id,
                json("assets/reverie_dreams/geckolib/animations/entity/gensokyo_fairy_blue.animation.json"), MathParser.create());
        for (var a : GensokyoFairyAnimation.values()) assertNotNull(animations.getAnimation(a.animationName()));
        var model = json("model/reverie_dreams/gensokyo_fairy_blue.bbmodel");
        Set<String> bones = new HashSet<>();
        model.getAsJsonArray("groups").forEach(g -> bones.add(g.getAsJsonObject().get("uuid").getAsString()));
        model.getAsJsonArray("animations").forEach(a -> a.getAsJsonObject().getAsJsonObject("animators").entrySet().forEach(e -> {
            assertTrue(bones.contains(e.getKey()));
            Set<String> keys = new HashSet<>();
            e.getValue().getAsJsonObject().getAsJsonArray("keyframes").forEach(k -> {
                var key = k.getAsJsonObject();
                assertTrue(keys.add(key.get("channel").getAsString() + "@" + key.get("time").getAsDouble()), "Duplicate keyframe");
            });
        }));
        String embedded = model.getAsJsonArray("textures").get(0).getAsJsonObject().get("source").getAsString();
        byte[] png = Base64.getDecoder().decode(embedded.substring(embedded.indexOf(',') + 1));
        try (var stream = getClass().getClassLoader().getResourceAsStream("assets/reverie_dreams/textures/entity/gensokyo_fairy_blue.png")) {
            assertNotNull(stream);
            assertArrayEquals(stream.readAllBytes(), png);
        }
        var image = ImageIO.read(new ByteArrayInputStream(png));
        assertEquals(128, image.getWidth());
        assertEquals(128, image.getHeight());
        assertEquals(0xffffd9be, image.getRGB(11, 14));
        int hair = image.getRGB(8, 8);
        assertTrue(((hair >>> 16) & 255) - ((hair >>> 8) & 255) >= 18);
        assertTrue(((image.getRGB(10, 13) >>> 8) & 255) < 128);
    }

    @Test
    void allFairyAppearancesSelectBySpeciesAndExactSkinIdentifier() {
        var kinds = GensokyoFairyAppearance.Kind.values();
        var expected = new GensokyoFairyAppearance[][] {
                {GensokyoFairyAppearance.YOUSEI_BLUE, GensokyoFairyAppearance.YOUSEI_ORANGE, GensokyoFairyAppearance.YOUSEI_GREEN},
                {GensokyoFairyAppearance.MAID_BLUE, GensokyoFairyAppearance.MAID_ORANGE, GensokyoFairyAppearance.MAID_GREEN}
        };
        for (int k = 0; k < 2; k++) {
            for (int color = 0; color < 3; color++) {
                assertEquals(expected[k][color], GensokyoFairyAppearance.of(kinds[k],
                        Identifier.parse("reverie_dreams:yousei_0" + (color + 1))));
            }
            assertNull(GensokyoFairyAppearance.of(kinds[k], Identifier.parse("reverie_dreams:sunflower_yousei")));
        }
        assertEquals(GensokyoFairyAppearance.SUNFLOWER, GensokyoFairyAppearance.of(kinds[2], Identifier.parse("reverie_dreams:sunflower_yousei")));
        for (var kind : kinds) {
            assertNull(GensokyoFairyAppearance.of(kind, null));
            assertNull(GensokyoFairyAppearance.of(kind, Identifier.parse("custom:yousei_01")));
            assertNull(GensokyoFairyAppearance.of(kind, Identifier.parse("reverie_dreams:ice_fairy")));
        }
        assertNull(GensokyoFairyAppearance.of(kinds[2], Identifier.parse("reverie_dreams:yousei_01")));
    }

    @Test
    void allFairyAppearancesActuallyBakeAndEmbedTheSamePixelAtlas() throws Exception {
        var loader = new GeckoLibGsonLoader();
        for (var appearance : GensokyoFairyAppearance.values()) {
            String name = appearance.modelId().getPath();
            var baked = loader.bakeGeckoLibModelFile(appearance.resourceId(),
                    json("assets/reverie_dreams/geckolib/models/entity/" + name + ".geo.json"));
            for (String bone : new String[]{"dress_skirt", "bloomers", "upper_wing_right", "lower_wing_right", "upper_wing_left", "lower_wing_left"}) {
                assertTrue(baked.getBone(bone).isPresent(), appearance + ":" + bone);
            }
            if (appearance.name().startsWith("MAID")) assertTrue(baked.getBone("maid_apron").isPresent());
            if (appearance == GensokyoFairyAppearance.SUNFLOWER) assertTrue(baked.getBone("sunflower_ornaments").isPresent());
            var animations = loader.bakeGeckoLibAnimationsFile(appearance.resourceId(),
                    json("assets/reverie_dreams/geckolib/animations/entity/" + name + ".animation.json"), MathParser.create());
            for (var state : GensokyoFairyAnimation.values()) assertNotNull(animations.getAnimation(state.animationName()), appearance.toString());
            var model = json("model/reverie_dreams/" + name + ".bbmodel");
            String embedded = model.getAsJsonArray("textures").get(0).getAsJsonObject().get("source").getAsString();
            byte[] png = Base64.getDecoder().decode(embedded.substring(embedded.indexOf(',') + 1));
            try (var stream = getClass().getClassLoader().getResourceAsStream("assets/reverie_dreams/" + appearance.textureId().getPath())) {
                assertNotNull(stream);
                assertArrayEquals(stream.readAllBytes(), png, appearance.toString());
            }
            var image = ImageIO.read(new ByteArrayInputStream(png));
            assertEquals(128, image.getWidth());
            assertEquals(128, image.getHeight());
            Set<String> bones = new HashSet<>();
            model.getAsJsonArray("groups").forEach(g -> bones.add(g.getAsJsonObject().get("uuid").getAsString()));
            model.getAsJsonArray("animations").forEach(a -> assertTrue(bones.containsAll(a.getAsJsonObject().getAsJsonObject("animators").keySet())));
            for (String sleeve : new String[]{"sleeve_right_arm", "sleeve_left_arm"}) {
                model.getAsJsonArray("elements").forEach(e -> {
                    var cube = e.getAsJsonObject();
                    if (cube.get("name").getAsString().equals(sleeve)) {
                        assertFalse(cube.getAsJsonObject("faces").getAsJsonObject("up").get("texture").isJsonNull());
                    }
                });
            }
        }
    }
}
