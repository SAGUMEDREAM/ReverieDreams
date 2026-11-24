package cc.thonly.reverie_dreams.polymer.entity;

import cc.thonly.reverie_dreams.entity.variant.GensokyoFairyActions;
import cc.thonly.reverie_dreams.entity.variant.GensokyoFairyAnimation;
import cc.thonly.reverie_dreams.entity.variant.GensokyoFairyAppearance;
import com.geckolib.loading.loader.GeckoLibGsonLoader;
import com.geckolib.loading.math.MathParser;
import com.google.gson.JsonParser;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class GensokyoFairyCombatTest {
    @BeforeAll
    static void bootstrap() { SharedConstants.tryDetectVersion(); Bootstrap.bootStrap(); }

    @Test
    void deathAndHurtTakePriorityAndAttacksReturnToMovement() {
        var actions = new GensokyoFairyActions();
        actions.begin(GensokyoFairyAnimation.RANGED, 100);
        assertEquals(GensokyoFairyAnimation.RANGED, actions.select(false, false, 100, true, false));
        assertEquals(GensokyoFairyAnimation.HURT, actions.select(false, true, 101, true, false));
        assertEquals(GensokyoFairyAnimation.DEATH, actions.select(true, true, 101, true, false));
        assertEquals(GensokyoFairyAnimation.FLY, actions.select(false, false, 118, false, true));
        assertEquals(GensokyoFairyAnimation.WALK, actions.select(false, false, 118, true, true));
    }

    @Test
    void shootingHandSwingCannotReplaceTheRangedCastWithAMeleeAttack() {
        var actions = new GensokyoFairyActions();
        actions.begin(GensokyoFairyAnimation.RANGED, 100);
        int revision = actions.revision();
        actions.begin(GensokyoFairyAnimation.MELEE, 101);
        assertEquals(revision, actions.revision());
        assertEquals(GensokyoFairyAnimation.RANGED, actions.select(false, false, 110, true, false));
        actions.begin(GensokyoFairyAnimation.MELEE, 120);
        assertEquals(GensokyoFairyAnimation.MELEE, actions.select(false, false, 121, true, false));
        assertEquals(GensokyoFairyAnimation.IDLE, actions.select(false, false, 132, true, false));
    }

    @Test
    void repeatedAttacksHaveDistinctPlaybackRevisions() {
        var actions = new GensokyoFairyActions();
        actions.begin(GensokyoFairyAnimation.MELEE, 1);
        int first = actions.revision();
        actions.begin(GensokyoFairyAnimation.MELEE, 14);
        assertTrue(actions.revision() > first);
    }

    @Test
    void allAppearancesBakeActionsAndBothHandAndHelmetAttachments() throws Exception {
        var resources = Path.of("../common/src/main/resources");
        var loader = new GeckoLibGsonLoader();
        for (var appearance : GensokyoFairyAppearance.values()) {
            String name = appearance.modelId().getPath();
            var model = loader.bakeGeckoLibModelFile(appearance.resourceId(), JsonParser.parseString(Files.readString(
                    resources.resolve("assets/reverie_dreams/geckolib/models/entity/" + name + ".geo.json"))).getAsJsonObject());
            assertTrue(model.getBone("right_hand").isPresent(), name);
            assertTrue(model.getBone("left_hand").isPresent(), name);
            assertTrue(model.getLocator("helmet_locator").isPresent(), name);
            assertTrue(model.getLocator("right_item_locator").isPresent(), name);
            assertTrue(model.getLocator("left_item_locator").isPresent(), name);
            var json = JsonParser.parseString(Files.readString(resources.resolve(
                    "assets/reverie_dreams/geckolib/animations/entity/" + name + ".animation.json"))).getAsJsonObject();
            var animations = loader.bakeGeckoLibAnimationsFile(appearance.resourceId(), json, MathParser.create());
            for (var action : GensokyoFairyAnimation.values()) {
                assertNotNull(animations.getAnimation(action.animationName()), name + ":" + action);
                var clip = json.getAsJsonObject("animations").getAsJsonObject(action.animationName());
                assertEquals(action.looping(), clip.get("loop").getAsBoolean());
                if (!action.looping()) assertEquals(action.durationTicks() / 20.0, clip.get("animation_length").getAsDouble());
            }
        }
    }

    @Test
    void natureReturnShrinksGentlyWithoutNegativeScaleOrChangingTheFaceTexture() throws Exception {
        var resources = Path.of("../common/src/main/resources");
        var json = JsonParser.parseString(Files.readString(resources.resolve(
                "assets/reverie_dreams/geckolib/animations/entity/gensokyo_fairy_blue.animation.json"))).getAsJsonObject();
        var scale = json.getAsJsonObject("animations").getAsJsonObject("animation.death").getAsJsonObject("bones").getAsJsonObject("body").getAsJsonObject("scale");
        double previous = 1;
        for (var entry : scale.entrySet()) {
            double value = entry.getValue().getAsJsonObject().getAsJsonObject("post").getAsJsonArray("vector").get(0).getAsDouble();
            assertTrue(value > 0 && value <= previous);
            previous = value;
        }
        assertEquals(0.001, previous);
        var source = JsonParser.parseString(Files.readString(Path.of("src/main/resources/model/reverie_dreams/gensokyo_fairy_blue.bbmodel"))).getAsJsonObject();
        long cubes = source.getAsJsonArray("elements").asList().stream().filter(e -> !e.getAsJsonObject().has("type") || e.getAsJsonObject().get("type").getAsString().equals("cube")).count();
        assertEquals(70, cubes);
    }
}
