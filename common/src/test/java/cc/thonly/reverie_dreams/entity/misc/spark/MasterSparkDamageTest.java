package cc.thonly.reverie_dreams.entity.misc.spark;

import cc.thonly.reverie_dreams.entity.misc.BaguaFurnaceEntity;
import cc.thonly.reverie_dreams.registry.content.RDDamageTypes;
import com.mojang.serialization.Lifecycle;
import net.minecraft.SharedConstants;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import sun.misc.Unsafe;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import net.minecraft.world.scores.PlayerTeam;
import static org.junit.jupiter.api.Assertions.*;

/** Executes the real turret's damage path; only entity health storage and world queries are fixtures. */
class MasterSparkDamageTest {
    private static Unsafe unsafe;
    private static RegistryAccess access;
    private static Method damage;
    private static Method targeting;
    private static Field ownerField, targetField, positionField;

    @BeforeAll static void bootstrap() throws Exception {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        var field = Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        unsafe = (Unsafe) field.get(null);
        var types = new MappedRegistry<DamageType>(Registries.DAMAGE_TYPE, Lifecycle.stable());
        Registry.register(types, RDDamageTypes.DANMAKU_GENERIC, new DamageType("danmaku", 0.1f));
        types.freeze();
        access = new RegistryAccess.ImmutableRegistryAccess(List.of(types));
        damage = BaguaFurnaceEntity.class.getDeclaredMethod("damageBeam", ServerLevel.class);
        damage.setAccessible(true);
        targeting = BaguaFurnaceEntity.class.getDeclaredMethod("tickTargeting", ServerLevel.class);
        targeting.setAccessible(true);
        ownerField = BaguaFurnaceEntity.class.getDeclaredField("owner");
        targetField = BaguaFurnaceEntity.class.getDeclaredField("target");
        positionField = Entity.class.getDeclaredField("position");
        ownerField.setAccessible(true);
        targetField.setAccessible(true);
        positionField.setAccessible(true);
    }

    @Test void actualServerPathHitsCentralAndLargeEdgeTargetsWithSourceAttributedDamage() throws Exception {
        var central = zombie(new AABB(-0.3, -0.8, 10, 0.3, 0.8, 11));
        var large = zombie(new AABB(1.2, -1, 25, 5.2, 1, 27));
        var outside = zombie(new AABB(4, -0.2, 20, 4.4, 0.2, 21));
        FixtureWorld world = world(central, large, outside);
        FixtureFurnace furnace = furnace();
        damage.invoke(furnace, world);
        assertEquals(8, central.damage);
        assertEquals(8, large.damage);
        assertEquals(0, outside.damage);
        assertSame(furnace, central.source.getDirectEntity());
    }

    @Test void neighboringWallAndClippedBeamEndpointPreventDamage() throws Exception {
        var target = zombie(new AABB(-0.3, -0.8, 12, 0.3, 0.8, 13));
        FixtureWorld world = world(target);
        FixtureFurnace furnace = furnace();
        world.blocked = true;
        damage.invoke(furnace, world);
        assertEquals(0, target.damage);
        world.blocked = false;
        furnace.length = 8;
        damage.invoke(furnace, world);
        assertEquals(0, target.damage);
    }

    @Test void fourRealDamageCallsDoNotClearTheVictimsInvulnerabilityTimer() throws Exception {
        var target = zombie(new AABB(-0.3, -0.8, 12, 0.3, 0.8, 13));
        target.invulnerableTime = 17;
        FixtureWorld world = world(target);
        FixtureFurnace furnace = furnace();
        MasterSparkCycle cycle = new MasterSparkCycle();
        while (cycle.phase() != MasterSparkCycle.Phase.FIRE) cycle.tick(true);
        for (int i = 0; i < 40; i++) {
            if (cycle.damagePulse()) damage.invoke(furnace, world);
            cycle.tick(true);
        }
        assertEquals(32, target.damage);
        assertEquals(17, target.invulnerableTime);
    }

    @Test void deadTargetIsReplacedOnTheVeryNextTargetingTick() throws Exception {
        var first = zombie(new AABB(-0.3, 0, 10, 0.3, 1.8, 11));
        var second = zombie(new AABB(-0.3, 0, 15, 0.3, 1.8, 16));
        var world = world(first, second);
        var furnace = ownedFurnace(world);
        targeting.invoke(furnace, world);
        assertSame(first, targetField.get(furnace));
        first.dead = true;
        targeting.invoke(furnace, world);
        assertSame(second, targetField.get(furnace));
        assertEquals(2, world.queries);
    }

    @Test void keepsAValidTargetInsteadOfThrashingBetweenNearbyMobs() throws Exception {
        var first = zombie(new AABB(-0.3, 0, 10, 0.3, 1.8, 11));
        var world = world(first);
        var furnace = ownedFurnace(world);
        targeting.invoke(furnace, world);
        var closer = zombie(new AABB(-0.3, 0, 2, 0.3, 1.8, 3));
        closer.world = world;
        world.targets = List.of(closer, first);
        for (int i = 0; i < 20; i++) targeting.invoke(furnace, world);
        assertSame(first, targetField.get(furnace));
        assertEquals(1, world.queries);
    }

    @Test void idleSearchPollsEveryFourTicksWithoutScanningEveryFrame() throws Exception {
        var world = world();
        var furnace = ownedFurnace(world);
        targeting.invoke(furnace, world);
        assertEquals(1, world.queries);
        for (int i = 0; i < 3; i++) targeting.invoke(furnace, world);
        assertEquals(1, world.queries);
        targeting.invoke(furnace, world);
        assertEquals(2, world.queries);
        assertNull(targetField.get(furnace));
    }

    private static FixtureZombie zombie(AABB box) throws Exception {
        FixtureZombie zombie = (FixtureZombie) unsafe.allocateInstance(FixtureZombie.class);
        zombie.setUUID(UUID.randomUUID());
        zombie.setBoundingBox(box);
        positionField.set(zombie, box.getCenter());
        return zombie;
    }
    private static FixtureWorld world(FixtureZombie... targets) throws Exception {
        FixtureWorld world = (FixtureWorld) unsafe.allocateInstance(FixtureWorld.class);
        world.targets = List.of(targets);
        for (var target : targets) target.world = world;
        return world;
    }
    private static FixtureFurnace furnace() throws Exception {
        FixtureFurnace furnace = (FixtureFurnace) unsafe.allocateInstance(FixtureFurnace.class);
        furnace.length = 40;
        positionField.set(furnace, Vec3.ZERO);
        furnace.setBoundingBox(new AABB(-0.3, 0, -0.3, 0.3, 1.8, 0.3));
        return furnace;
    }
    private static FixtureFurnace ownedFurnace(FixtureWorld world) throws Exception {
        FixtureFurnace furnace = furnace();
        furnace.world = world;
        ownerField.set(furnace, zombie(new AABB(-0.3, 0, -2, 0.3, 1.8, -1)));
        return furnace;
    }
    private static class FixtureFurnace extends BaguaFurnaceEntity {
        float length;
        FixtureWorld world;
        private FixtureFurnace() { super(EntityType.MARKER, null); }
        @Override public RegistryAccess registryAccess() { return access; }
        @Override public Vec3 getMuzzlePosition() { return Vec3.ZERO; }
        @Override public Vec3 getBeamDirection() { return new Vec3(0, 0, 1); }
        @Override public float getBeamLength() { return length; }
        @Override public float getBeamStrength(float partialTick) { return 1; }
        @Override public boolean isDescending() { return false; }
        @Override public ServerLevel level() { return world; }
    }
    private static class FixtureZombie extends Zombie {
        float damage;
        DamageSource source;
        boolean dead;
        FixtureWorld world;
        private FixtureZombie() { super(EntityType.ZOMBIE, null); }
        @Override public boolean isAlive() { return !dead; }
        @Override public ServerLevel level() { return world; }
        @Override public PlayerTeam getTeam() { return null; }
        @Override public boolean hurtServer(ServerLevel level, DamageSource source, float amount) { this.source = source; damage += amount; return true; }
    }
    private static class FixtureWorld extends ServerLevel {
        List<FixtureZombie> targets;
        boolean blocked;
        int queries;
        private FixtureWorld() { super(null, null, null, null, null, null, false, 0, List.of(), false); }
        @Override public <T extends Entity> List<T> getEntitiesOfClass(Class<T> type, AABB box, Predicate<? super T> predicate) {
            queries++;
            return targets.stream().filter(type::isInstance).map(type::cast).filter(predicate).toList();
        }
        @Override public BlockHitResult clip(ClipContext context) {
            return blocked ? new BlockHitResult(context.getTo().scale(0.5), Direction.NORTH, BlockPos.ZERO, false)
                    : BlockHitResult.miss(context.getTo(), Direction.NORTH, BlockPos.ZERO);
        }
    }
}
