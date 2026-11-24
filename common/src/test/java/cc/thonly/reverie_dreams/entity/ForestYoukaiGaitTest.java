package cc.thonly.reverie_dreams.entity;

import cc.thonly.reverie_dreams.entity.variant.WalkingLegs;
import com.geckolib.animation.AnimationProcessor;
import com.geckolib.animation.object.LoopType;
import com.geckolib.animation.state.AnimationPoint;
import com.geckolib.animation.state.BoneSnapshot;
import com.geckolib.animation.state.ControllerState;
import com.geckolib.cache.animation.Animation;
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
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ForestYoukaiGaitTest {
    @BeforeAll static void bootstrap() { SharedConstants.tryDetectVersion(); Bootstrap.bootStrap(); }
    private double advance(double distance, double scale, double swing) {
        var gait = new WalkingLegs();
        gait.update(distance, scale, true, true, swing);
        assertTrue(gait.active());
        return gait.phase(1);
    }

    @Test void movementDistanceSetsCadenceAndPartialTickInterpolatesIt() {
        assertEquals(2 * this.advance(.05, 1, 12), this.advance(.1, 1, 12), 1e-9);
        var gait = new WalkingLegs();
        gait.update(.1, 1, true, true, 12);
        assertEquals(gait.phase(1) / 2, gait.phase(.5), 1e-9);
        double end = gait.phase(1);
        gait.update(.1, 1, true, true, 12);
        assertEquals(end, gait.phase(0), 1e-9);
    }

    @Test void authoredStrideAndEntityScaleChangeCadence() {
        assertTrue(this.advance(.1, 1, 16) < this.advance(.1, 1, 12));
        assertEquals(this.advance(.1, 1, 12) / 2, this.advance(.1, 2, 12), 1e-9);
        assertEquals(.1 / (3 * Math.sin(Math.toRadians(12))), this.advance(.1, 1, 12), 1e-9);
    }

    @Test void stoppedAirborneAndInvalidMovementDoNotWalk() {
        var gait = new WalkingLegs();
        gait.update(.1, 1, true, true, 12);
        gait.update(0, 1, true, true, 12);
        assertFalse(gait.active());
        assertEquals(gait.phase(0), gait.phase(1));
        gait.update(.1, 1, false, true, 12);
        assertFalse(gait.active());
        gait.update(.1, 1, true, false, 12);
        assertFalse(gait.active());
        gait.update(Double.NaN, 1, true, true, 12);
        assertFalse(gait.active());
        assertTrue(Double.isFinite(gait.phase(1)));
    }

    @Test void existingWalkingLegsCallKeepsItsOriginalTwentyFiveDegreeStride() {
        var gait = new WalkingLegs();
        gait.update(.1, 1, true, true);
        assertEquals(this.advance(.1, 1, 25), gait.phase(1), 1e-9);
    }

    @Test void actualGeckoActionLayerPreservesWalkingLegsForEveryAppearanceAndHand() throws Exception {
        var loader = new GeckoLibGsonLoader();
        for (String species : List.of("cat", "tanuki", "fox")) for (int variant = 1; variant <= 3; variant++) {
            String name = "youkai_" + species + "_female_0" + variant;
            var id = Identifier.parse("reverie_dreams:" + name);
            var geometry = loader.bakeGeckoLibModelFile(id, json("geckolib/models/entity/" + name + ".geo.json"));
            var clips = loader.bakeGeckoLibAnimationsFile(id, json("geckolib/animations/entity/" + name + ".animation.json"), MathParser.create());
            for (String action : List.of("melee", "melee_left", "ranged", "ranged_left", "use_item", "use_item_left")) {
                double minimum = Double.POSITIVE_INFINITY, maximum = Double.NEGATIVE_INFINITY;
                for (int frame = 0; frame < 20; frame++) {
                    Map<String, BoneSnapshot> snapshots = new HashMap<>();
                    for (String bone : List.of("root", "body", "head", "right_leg", "left_leg", "right_lower_leg", "left_lower_leg"))
                        geometry.getBone(bone).ifPresent(b -> snapshots.put(bone, BoneSnapshot.create(b)));
                    apply(clips.getAnimation("animation.walk"), frame / 20.0, snapshots);
                    float leg = snapshots.get("right_leg").getRotX();
                    apply(clips.getAnimation("animation." + action), frame / 40.0, snapshots);
                    assertEquals(leg, snapshots.get("right_leg").getRotX(), 1e-6, name + "/" + action);
                    minimum = Math.min(minimum, leg); maximum = Math.max(maximum, leg);
                }
                assertTrue(maximum - minimum > .35, name + "/" + action + " must retain moving legs");
            }
        }
    }

    private static JsonObject json(String path) throws Exception {
        try (var stream = ForestYoukaiGaitTest.class.getClassLoader().getResourceAsStream("assets/reverie_dreams/" + path)) {
            assertNotNull(stream, path);
            return JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
        }
    }

    private static void apply(Animation clip, double time, Map<String, BoneSnapshot> snapshots) {
        GeoRenderState render = new GeoRenderState() {
            private final Map<DataTicket<?>, Object> data = new HashMap<>();
            @Override public Map<DataTicket<?>, Object> getDataMap() { return this.data; }
        };
        var state = new ControllerState(AnimationPoint.createFor(clip, null, LoopType.LOOP, time), null,
                -1, 0, false, null, render, new Reference2DoubleOpenHashMap<>());
        AnimationProcessor.createBoneSnapshots(state, name -> Optional.ofNullable(snapshots.get(name)));
    }
}
