package cc.thonly.reverie_dreams.data.danmaku.stg.runtime;

import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuBulletSpec;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuEmitter;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuLaser;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuMath;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuPattern;
import cc.thonly.reverie_dreams.data.danmaku.stg.EmitAim;
import cc.thonly.reverie_dreams.data.danmaku.stg.SpellTiming;
import cc.thonly.reverie_dreams.data.danmaku.stg.shape.EmitShape;
import cc.thonly.reverie_dreams.entity.misc.DanmakuLaserEntity;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import java.lang.reflect.Field;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import sun.misc.Unsafe;

import java.util.List;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class SpellCastRuntimeTest {
    private static Unsafe unsafe;
    private static Field position;
    private static Field dimensions;

    @BeforeAll
    static void bootstrap() throws ReflectiveOperationException {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        var field = Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        unsafe = (Unsafe) field.get(null);
        position = Entity.class.getDeclaredField("position");
        position.setAccessible(true);
        dimensions = Entity.class.getDeclaredField("dimensions");
        dimensions.setAccessible(true);
    }

    @Test
    void movingCasterKeepsRelativeOffsetThenDetachesAtLastPosition() throws Exception {
        var source = caster();
        position.set(source, new Vec3(2, 5, 8));
        var runtime = new DanmakuPatternRuntime(quietPattern(), null, source, source.getEyePosition().add(0, -1, 0));
        runtime.tick();
        position.set(source, new Vec3(9, 7, 8));
        runtime.tick();
        assertEquals(new Vec3(9, 7, 8), runtime.getOrigin());
        assertEquals(new Vec3(9, 8, 8), runtime.sourceMuzzlePosition());
        source.removal = FixtureCaster.RemovalReason.UNLOADED_TO_CHUNK;
        position.set(source, new Vec3(18, 10, 8));
        runtime.tick();
        assertNull(runtime.getSource());
        assertEquals(new Vec3(9, 7, 8), runtime.getOrigin(), "Never resample a removed source");
        position.set(source, new Vec3(20, 10, 8));
        runtime.tick();
        assertFalse(runtime.isDone());
        assertEquals(new Vec3(9, 7, 8), runtime.getOrigin());
    }

    @Test
    void defeatStopsCastAndDiscardsOnlyItsRemainingEntities() throws Exception {
        var source = caster();
        position.set(source, Vec3.ZERO);
        var first = new DanmakuPatternRuntime(quietPattern(), null, source, source.getEyePosition());
        var other = new DanmakuPatternRuntime(quietPattern(), null, null, Vec3.ZERO);
        var expired = beam(1);
        var active = beam(2);
        var unrelated = beam(3);
        first.track(expired);
        first.track(active);
        other.track(unrelated);
        expired.discard();
        source.defeated = true;
        first.tick();
        assertTrue(first.isDone());
        assertEquals(1, expired.removals, "Expired beam was untracked at removal");
        assertEquals(1, active.removals, "Remaining beam was cleared");
        assertEquals(0, unrelated.removals);
    }

    @Test
    void dueQueueKeepsCadenceWhenLoopRestartsBetweenShots() {
        var spec = DanmakuBulletSpec.of(Identifier.parse("reverie_dreams:rice"));
        var emitter = new DanmakuEmitterRuntime(DanmakuEmitter.of(EmitShape.aim(), spec).withTiming(3, 10, 4), null, 0);
        assertTrue(emitter.scheduleFrom(0));
        assertEquals(3, emitter.nextFireTick());
        assertTrue(emitter.scheduleFrom(8));
        assertEquals(11, emitter.nextFireTick());
        assertFalse(emitter.scheduleFrom(12));
        assertTrue(emitter.scheduleFrom(4));
        assertEquals(7, emitter.nextFireTick());
    }

    @Test
    void playerAimTurnsTheWholePatternAndRetainsItsLastPoseAfterDetaching() throws Exception {
        var source = caster();
        position.set(source, Vec3.ZERO);
        var origin = new SpellCastOrigin(null, source, source.getEyePosition());
        var forward = new Vec3(0, 0, 1);
        assertEquals(forward, origin.aimFrame().toWorld(forward));

        source.setYRot(90f);
        source.setXRot(-30f);
        assertTrue(origin.update());
        var frame = origin.aimFrame();
        assertEquals(0, frame.toWorld(forward).distanceTo(DanmakuMath.direction(-30f, 90f)), 1.0e-6);
        var authored = DanmakuMath.direction(7f, 45f);
        assertEquals(authored.dot(forward), frame.toWorld(authored).dot(frame.toWorld(forward)), 1.0e-6);
        assertEquals(0, frame.toWorld(new Vec3(2, 0, 0)).distanceTo(new Vec3(0, 0, 2)), 1.0e-6);

        source.removal = Entity.RemovalReason.UNLOADED_TO_CHUNK;
        assertTrue(origin.update());
        source.setYRot(0f);
        assertTrue(origin.update());
        assertSame(frame, origin.aimFrame());
    }

    @Test
    void beamScheduleRewindsToTheLoopStartInStableOrder() {
        var intro = new DanmakuLaser(2, 0, 0, 9, 0.5f, 3, 6, 0);
        var first = new DanmakuLaser(8, 0, 45, 9, 0.5f, 3, 6, 0);
        var second = first.withYaw(-45);
        var schedule = new DanmakuLaserRuntime(List.of(first, intro, second));
        var fired = new ArrayList<DanmakuLaser>();
        schedule.seek(0);
        for (int i = 0; i <= 9; i++) schedule.fire(i, fired::add);
        schedule.seek(6);
        for (int i = 6; i <= 9; i++) schedule.fire(i, fired::add);
        assertEquals(List.of(intro, first, second, first, second), fired);
    }

    @Test
    void offsetBeamTransformsItsMuzzleAndHeadingInTheSameCastFrame() {
        var cue = new DanmakuLaser(0, 0, 0, 9, 0.5f, 3, 6, 0).withOffset(new Vec3(2, 0, 1));
        var anchor = new Vec3(5, 9, 11);
        var facing = DanmakuLaserRuntime.place(cue, anchor, 90f, null);
        assertEquals(0, facing.origin().distanceTo(anchor.add(-1, 0, 2)), 1e-6);
        var player = DanmakuLaserRuntime.place(cue, anchor, 90f, new SpellAimFrame(-30f, 90f));
        assertEquals(0, player.origin().distanceTo(anchor.add(-Math.sqrt(3) / 2, 0.5, 2)), 1e-6);
        assertEquals(-30f, player.cue().pitch(), 1e-6);
        assertEquals(90f, player.cue().yaw(), 1e-6);
    }

    private static DanmakuPattern quietPattern() {
        return new DanmakuPattern(List.of()).withTiming(new SpellTiming(20, 0, 0));
    }

    @Test
    void simultaneousLargeVolleysReachEachSpawnLocationOnTheirScheduledTick() throws Exception {
        var world = (SpawnProbeLevel) unsafe.allocateInstance(SpawnProbeLevel.class);
        world.positions = new ArrayList<>();
        var bullet = DanmakuBulletSpec.of(Identifier.parse("reverie_dreams:rice"));
        var pattern = DanmakuPattern.of(DanmakuEmitter.of(EmitShape.ring(96, 0, true), bullet)
                .withAim(EmitAim.FIXED, 0, 0)
                .withTiming(0, 0, 1));
        DanmakuDirector.add(new DanmakuPatternRuntime(pattern, world, null, new Vec3(0, 70, 0)));
        DanmakuDirector.add(new DanmakuPatternRuntime(pattern, world, null, new Vec3(20, 70, 0)));
        try {
            DanmakuDirector.tick(null);
            assertEquals(192, world.positions.size());
            assertEquals(96, world.positions.stream().filter(pos -> pos.getX() == 0).count());
            assertEquals(96, world.positions.stream().filter(pos -> pos.getX() == 20).count());
            DanmakuDirector.tick(null);
            assertEquals(192, world.positions.size(), "Don't defer shots into later ticks");
        } finally {
            DanmakuDirector.cancelAll();
        }
    }

    private static final class SpawnProbeLevel extends ServerLevel {
        List<BlockPos> positions;

        private SpawnProbeLevel() { super(null, null, null, null, null, null, false, 0, List.of(), false); }

        @Override public boolean isPositionEntityTicking(BlockPos pos) {
            this.positions.add(pos);
            return false; // Inspect the spawn boundary without allocating game entities or loading chunks.
        }
    }

    private static FixtureBeam beam(int id) throws InstantiationException {
        var beam = (FixtureBeam) unsafe.allocateInstance(FixtureBeam.class);
        beam.setId(id);
        return beam;
    }

    private static FixtureCaster caster() throws ReflectiveOperationException {
        var source = (FixtureCaster) unsafe.allocateInstance(FixtureCaster.class);
        // Unsafe skips Entity's constructor; give the fixture real dimensions before sampling its body.
        dimensions.set(source, EntityType.ZOMBIE.getDimensions());
        return source;
    }

    private static final class FixtureCaster extends Zombie {
        RemovalReason removal;
        boolean defeated;

        private FixtureCaster() { super(EntityType.ZOMBIE, null); }

        @Override public double getEyeY() { return this.position().y + 1; }
        @Override public RemovalReason getRemovalReason() { return this.removal; }
        @Override public boolean isDeadOrDying() { return this.defeated; }
    }

    private static final class FixtureBeam extends DanmakuLaserEntity {
        int removals;

        private FixtureBeam() { super(null, null); }

        @Override public void remove(RemovalReason reason) {
            this.removals++;
            this.onRemoval(reason);
        }
    }
}
