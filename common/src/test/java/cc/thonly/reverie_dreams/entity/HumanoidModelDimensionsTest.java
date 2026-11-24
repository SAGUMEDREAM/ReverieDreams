package cc.thonly.reverie_dreams.entity;

import cc.thonly.reverie_dreams.entity.variant.HumanoidModelDimensions;
import com.google.gson.JsonParser;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class HumanoidModelDimensionsTest {
    @BeforeAll static void bootstrap() { SharedConstants.tryDetectVersion(); Bootstrap.bootStrap(); }

    @Test void actualEntityTypeBuilderUsesTheAuthoredBodySizeAndEyeHeight() throws ReflectiveOperationException {
        // Inspect the real builder before build(): a bootstrapped test registry is already frozen.
        var field = EntityType.Builder.class.getDeclaredField("dimensions");
        field.setAccessible(true);
        for (var profile : HumanoidModelDimensions.values()) {
            var builder = profile.applyTo(EntityType.Builder.of(Pig::new, MobCategory.CREATURE));
            var actual = (EntityDimensions) field.get(builder);
            assertEquals(profile == HumanoidModelDimensions.FAIRY ? 2F : 2.125F, actual.height());
            assertFalse(actual.fixed());
            assertEquals(.6F, actual.width());
            assertEquals(1.75F, actual.eyeHeight());
        }
    }

    @Test void headLevelRayAndLowCeilingNowMeetTheRealBodyRatherThanPassingThrough() {
        for (var profile : HumanoidModelDimensions.values()) {
            var dimensions = profile.dimensions().scale(.9F);
            var box = dimensions.makeBoundingBox(0, 64, 0);
            var old = EntityDimensions.scalable(.6F, 1.8F).scale(.9F).makeBoundingBox(0, 64, 0);
            double headY = 64 + (old.getYsize() + box.getYsize()) / 2;
            assertTrue(box.clip(new Vec3(0, headY, -2), new Vec3(0, headY, 2)).isPresent());
            assertTrue(old.clip(new Vec3(0, headY, -2), new Vec3(0, headY, 2)).isEmpty());
            var lowCeiling = new AABB(-1, headY, -1, 1, headY + .1, 1);
            assertTrue(box.intersects(lowCeiling));
            assertFalse(old.intersects(lowCeiling));
            assertEquals(64, box.minY);
        }
    }

    @Test void customScalesApplyOnceWithUnchangedWidthAndEyesStayInsideTheHead() {
        for (var profile : HumanoidModelDimensions.values()) for (float scale : new float[]{.45F, .9F, 1F, 1.8F}) {
            var scaled = profile.dimensions().scale(scale);
            assertEquals(.6 * scale, scaled.width(), 1e-6);
            assertEquals(profile.dimensions().height() * scale, scaled.height(), 1e-6);
            assertEquals(1.75 * scale, scaled.eyeHeight(), 1e-6);
            assertTrue(scaled.eyeHeight() > 24 / 16.0 * scale);
            assertTrue(scaled.eyeHeight() < 32 / 16.0 * scale);
        }
    }

    @Test void allTenExistingModelsHaveTheirCoreHeadInsideTheConfiguredBody() throws Exception {
        for (String name : List.of("rabbit_unit_0", "rabbit_unit_1", "rabbit_unit_2", "gensokyo_fairy_blue", "gensokyo_fairy_orange", "gensokyo_fairy_green",
                "gensokyo_fairy_maid_blue", "gensokyo_fairy_maid_orange", "gensokyo_fairy_maid_green", "gensokyo_fairy_sunflower")) {
            var profile = name.startsWith("rabbit_unit") ? HumanoidModelDimensions.LUNAR_SOLDIER : HumanoidModelDimensions.FAIRY;
            try (var stream = getClass().getClassLoader().getResourceAsStream("assets/reverie_dreams/geckolib/models/entity/" + name + ".geo.json")) {
                assertNotNull(stream);
                var model = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
                var bones = model.getAsJsonArray("minecraft:geometry").get(0).getAsJsonObject().getAsJsonArray("bones");
                var head = bones.asList().stream().map(b -> b.getAsJsonObject()).filter(b -> b.get("name").getAsString().equals("head")).findFirst().orElseThrow();
                var core = head.getAsJsonArray("cubes").get(0).getAsJsonObject();
                double headTop = (core.getAsJsonArray("origin").get(1).getAsDouble() + core.getAsJsonArray("size").get(1).getAsDouble()) / 16;
                assertTrue(profile.dimensions().height() >= headTop, name + " head outside hitbox");
                assertTrue(profile.dimensions().height() < 2.2, "Decorative ears/flower must not enlarge the body");
            }
        }
    }
}
