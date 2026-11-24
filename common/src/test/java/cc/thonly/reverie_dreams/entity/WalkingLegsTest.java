package cc.thonly.reverie_dreams.entity;

import cc.thonly.reverie_dreams.entity.variant.WalkingLegs;
import com.geckolib.animation.AnimationProcessor;
import com.geckolib.animation.object.LoopType;
import com.geckolib.animation.state.AnimationPoint;
import com.geckolib.animation.state.BoneSnapshot;
import com.geckolib.animation.state.ControllerState;
import com.geckolib.cache.animation.Animation;
import com.geckolib.cache.model.BakedGeoModel;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.loading.loader.GeckoLibGsonLoader;
import com.geckolib.loading.math.MathParser;
import com.geckolib.renderer.base.GeoRenderState;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import it.unimi.dsi.fastutil.objects.Reference2DoubleOpenHashMap;
import net.minecraft.SharedConstants;
import net.minecraft.resources.Identifier;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.util.*;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.*;

class WalkingLegsTest {
    @BeforeAll static void bootstrap() { SharedConstants.tryDetectVersion(); Bootstrap.bootStrap(); }

    @Test void walkingPhaseContinuesAcrossRepeatedHurtAndRecovery() {
        var clock = new WalkingLegs();
        double stride = 4 * .75 * Math.sin(Math.toRadians(25));
        for (int tick = 0; tick < 15; tick++) {
            // Walking, hurt, another hit and recovery keep the same lower-body clock.
            clock.update(stride / 20, 1, true, true);
            assertTrue(clock.active());
            assertEquals((tick + 1) / 20.0, clock.phase(1), 1e-9);
        }
    }

    @Test void blockedStandingAirborneAndDeathDoNotWalkInPlace() {
        var clock = new WalkingLegs();
        clock.update(.1, 1, true, true);
        double paused = clock.phase(1);
        for (boolean grounded : new boolean[]{false, true}) {
            clock.update(grounded ? 0 : .1, 1, grounded, true);
            assertFalse(clock.active());
            assertEquals(paused, clock.phase(1), 1e-9);
        }
        clock.update(.1, 1, true, false);
        assertFalse(clock.active());
        assertEquals(paused, clock.phase(1), 1e-9);
    }

    @Test void partialTicksInterpolateThroughWrapWithoutRestartAndScaleMatchesStride() {
        var normal = new WalkingLegs();
        var scaled = new WalkingLegs();
        for (int i = 0; i < 12; i++) {
            normal.update(.1, 1, true, true);
            scaled.update(.2, 2, true, true);
            assertEquals(normal.phase(.5), scaled.phase(.5), 1e-9);
        }
        normal.update(Double.NaN, 1, true, true);
        assertFalse(normal.active());
        assertTrue(Double.isFinite(normal.phase(1)));
    }

    @Test void actualGeckoLayerKeepsLegsMovingAndPreservesHurtTorsoForAllAppearances() throws Exception {
        var loader = new GeckoLibGsonLoader();
        for (String name : appearances()) {
            var id = Identifier.parse("reverie_dreams:" + name);
            var geometry = loader.bakeGeckoLibModelFile(id, json("geckolib/models/entity/" + name + ".geo.json"));
            var clips = loader.bakeGeckoLibAnimationsFile(id, json("geckolib/animations/entity/" + name + ".animation.json"), MathParser.create());
            var hurt = clips.getAnimation("animation.hurt");
            var legs = clips.getAnimation(WalkingLegs.ANIMATION);
            assertNotNull(legs, name);
            double minimum = Double.POSITIVE_INFINITY, maximum = Double.NEGATIVE_INFINITY;
            for (int frame = 0; frame < 20; frame++) {
                var before = snapshots(geometry);
                apply(hurt, Math.min(.39, frame / 20.0), before);
                float torso = before.get("body").getRotX();
                var composed = snapshots(geometry);
                apply(hurt, Math.min(.39, frame / 20.0), composed);
                apply(legs, frame / 20.0, composed);
                assertEquals(torso, composed.get("body").getRotX(), 1e-6, name + " lost hurt response");
                double rotation = composed.get("right_leg").getRotX();
                minimum = Math.min(minimum, rotation); maximum = Math.max(maximum, rotation);
            }
            assertTrue(maximum - minimum > .5, name + " feet frozen during hurt");
        }
    }

    private static Map<String, BoneSnapshot> snapshots(BakedGeoModel geometry) {
        var result = new HashMap<String, BoneSnapshot>();
        for (String name : List.of("root", "body", "head", "right_leg", "left_leg", "right_shin", "left_shin", "right_lower_leg", "left_lower_leg"))
            geometry.getBone(name).ifPresent(bone -> result.put(name, BoneSnapshot.create(bone)));
        return result;
    }
    private static void apply(Animation animation, double time, Map<String, BoneSnapshot> snapshots) {
        GeoRenderState renderState = new GeoRenderState() {
            private final Map<DataTicket<?>, Object> data = new HashMap<>();
            @Override public Map<DataTicket<?>, Object> getDataMap() { return this.data; }
        };
        var point = AnimationPoint.createFor(animation, null, LoopType.LOOP, time);
        var state = new ControllerState(point, null, -1, 0, false, null, renderState, new Reference2DoubleOpenHashMap<>());
        AnimationProcessor.createBoneSnapshots(state, name -> Optional.ofNullable(snapshots.get(name)));
    }
    private static List<String> appearances() {
        var names = new ArrayList<>(List.of("rabbit_unit_0", "rabbit_unit_1", "rabbit_unit_2"));
        for (String gender : List.of("", "female_")) for (int i = 1; i <= 4; i++) names.add("white_wolf_tengu_" + gender + "0" + i);
        return names;
    }
    private static JsonObject json(String file) throws Exception {
        try (var stream = WalkingLegsTest.class.getClassLoader().getResourceAsStream("assets/reverie_dreams/" + file)) {
            assertNotNull(stream, file);
            return JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
        }
    }
}
