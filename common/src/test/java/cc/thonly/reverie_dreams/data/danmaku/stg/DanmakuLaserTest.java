package cc.thonly.reverie_dreams.data.danmaku.stg;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DanmakuLaserTest {
    @Test
    void diagonalBeamRejectsTargetsInTheBroadPhaseCorners() {
        LaserVolume beam = new LaserVolume(Vec3.ZERO, 0, -45, 10, 0.8f);
        AABB beside = new AABB(3.6, 0.1, 4.4, 3.7, 0.35, 4.5);

        assertTrue(beam.bounds().intersects(beside));
        assertFalse(beam.intersects(beside));
        assertTrue(beam.intersects(new AABB(3.6, 0.1, 3.6, 3.7, 0.35, 3.7)));
    }

    @Test
    void verticalBeamStopsAtBothEndCaps() {
        LaserVolume beam = new LaserVolume(new Vec3(7, 12, 9), -90, 72, 4, 0.6f);

        assertTrue(beam.intersects(new AABB(6.9, 15.9, 8.9, 7.1, 16.1, 9.1)));
        assertFalse(beam.intersects(new AABB(6.9, 16.01, 8.9, 7.1, 16.2, 9.1)));
        assertFalse(beam.intersects(new AABB(6.9, 11.8, 8.9, 7.1, 11.99, 9.1)));
    }

    @Test
    void oldCuesCanStillBeReadAndAimingPreservesAnExplicitColor() {
        var oldJson = JsonParser.parseString("""
                {"at":40,"yaw":67.5,"length":24,"width":1.35,"warn_ticks":10,"active_ticks":40}
                """);
        var oldCue = DanmakuLaser.CODEC.parse(JsonOps.INSTANCE, oldJson).getOrThrow();
        assertEquals(-1, oldCue.color());
        assertEquals(Vec3.ZERO, oldCue.offset());
        assertEquals(0f, oldCue.sweepHalfAngle());
        var oldEncoded = DanmakuLaser.CODEC.encodeStart(JsonOps.INSTANCE, oldCue).getOrThrow().getAsJsonObject();
        assertFalse(oldEncoded.has("color"));
        assertFalse(oldEncoded.has("offset"));
        assertFalse(oldEncoded.has("sweep_half_angle"));

        var blue = new DanmakuLaser(40, 0, 67.5f, 24, 1.35f, 10, 40, -0.7f, 0x60A8FF)
                .withOffset(new Vec3(3, 1, -2)).withSweepLimit(3.75f);
        var turned = blue.withYaw(22.5f);
        assertEquals(22.5f, turned.yaw());
        assertEquals(blue.color(), turned.color());
        assertEquals(blue.offset(), turned.offset());
        assertEquals(3.75f, turned.sweepHalfAngle());
        var encoded = DanmakuLaser.CODEC.encodeStart(JsonOps.INSTANCE, turned).getOrThrow();
        assertEquals(turned, DanmakuLaser.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow());
    }

    @Test
    void boundedSweepReflectsOvershootInBothDirections() {
        assertEquals(0f, DanmakuLaser.sweepOffset(3f, 8f, 0));
        assertEquals(7f, DanmakuLaser.sweepOffset(3f, 8f, 3));
        assertEquals(-8f, DanmakuLaser.sweepOffset(3f, 8f, 8));
        assertEquals(8f, DanmakuLaser.sweepOffset(-3f, 8f, 8));
        assertEquals(-24f, DanmakuLaser.sweepOffset(-3f, 0f, 8));
    }

    @Test
    void growingMovingBeamKeepsItsTipSpeedAndTranslatesWhenFull() {
        var spec = new MovingLaserSpec(0.35f, 1f, 1.5f, 0.5f, 100, 0x597CFF, true);
        float tail = 0f;
        float length = spec.initialLength();
        for (int tick = 1; tick <= 4; tick++) {
            float grown = spec.nextLength(length);
            tail += spec.tailAdvance(length, grown);
            length = grown;
            assertEquals(1f + tick * spec.speed(), tail + length, 0.0001f);
        }
        assertEquals(1.5f, length, 0.0001f);
        assertEquals(0.9f, tail, 0.0001f);
    }
}
