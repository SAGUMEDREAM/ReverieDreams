package cc.thonly.reverie_dreams.entity.projectile;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.SharedConstants;
import net.minecraft.advancements.criterion.EntityFlagsPredicate;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import sun.misc.Unsafe;

import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;

import static org.junit.jupiter.api.Assertions.*;

class BaguaFireballCookedLootTest {
    private static Unsafe unsafe;
    private static Method hurtWithFire;
    private static final List<EntityFlagsPredicate> COOKING_CONDITIONS = new ArrayList<>();

    @BeforeAll static void bootstrap() throws Exception {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        var field = Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        unsafe = (Unsafe) field.get(null);
        hurtWithFire = BaguaFireball.class.getDeclaredMethod("hurtWithFire", ServerLevel.class, Entity.class, DamageSource.class, float.class);
        hurtWithFire.setAccessible(true);
        // Use the actual vanilla cooking predicates rather than recreating the loot rule.
        try (var jar = new JarFile(Path.of(SmallFireball.class.getProtectionDomain().getCodeSource().getLocation().toURI()).toFile())) {
            for (String animal : List.of("cow", "pig", "chicken", "rabbit", "sheep")) {
                var entry = jar.getJarEntry("data/minecraft/loot_table/entities/" + animal + ".json");
                assertNotNull(entry, animal);
                try (var reader = new InputStreamReader(jar.getInputStream(entry), StandardCharsets.UTF_8)) {
                    JsonObject smelting = findObject(JsonParser.parseReader(reader), "function", "minecraft:furnace_smelt");
                    assertNotNull(smelting, animal);
                    JsonObject flags = findObject(smelting, "is_on_fire", "true");
                    assertNotNull(flags, animal);
                    COOKING_CONDITIONS.add(EntityFlagsPredicate.CODEC.parse(JsonOps.INSTANCE, flags).getOrThrow());
                }
            }
        }
    }

    @Test void lethalDirectDamageSatisfiesCookingConditionsWhileDeathLootIsEvaluated() throws Exception {
        FixtureTarget target = target(4, 0, true);
        assertFalse(COOKING_CONDITIONS.getFirst().matches(target));
        assertTrue(hurt(target, 5));
        assertTrue(target.cookedAtDeath, "Death must see burning before hurtServer returns");
        assertEquals(40, target.fireTicksAtDamage);
    }

    @Test void lethalLowSplashDamageAlsoSatisfiesCookingConditions() throws Exception {
        FixtureTarget target = target(0.25f, 0, true);
        assertTrue(hurt(target, 0.5f));
        assertTrue(target.cookedAtDeath);
    }

    @Test void rejectedDamageRestoresNegativeFireCooldown() throws Exception {
        FixtureTarget target = target(10, -20, false);
        assertFalse(hurt(target, 5));
        assertEquals(-20, target.getRemainingFireTicks());
        assertFalse(target.isOnFire());
    }

    @Test void rejectedDamagePreservesAnExistingLongerBurn() throws Exception {
        FixtureTarget target = target(10, 100, false);
        assertFalse(hurt(target, 5));
        assertEquals(100, target.getRemainingFireTicks());
    }

    @Test void nonlethalHitKeepsTheOriginalDamageAndDoesNotShortenExistingFire() throws Exception {
        FixtureTarget target = target(10, 100, true);
        assertTrue(hurt(target, 5));
        assertEquals(5, target.health);
        assertEquals(100, target.getRemainingFireTicks());
        assertFalse(target.cookedAtDeath);
    }

    private static boolean hurt(FixtureTarget target, float damage) throws Exception {
        return (boolean) hurtWithFire.invoke(null, null, target, null, damage);
    }

    private static FixtureTarget target(float health, int fireTicks, boolean acceptsDamage) throws Exception {
        FixtureTarget target = (FixtureTarget) unsafe.allocateInstance(FixtureTarget.class);
        target.health = health;
        target.acceptsDamage = acceptsDamage;
        target.setRemainingFireTicks(fireTicks);
        return target;
    }

    private static JsonObject findObject(JsonElement node, String key, String value) {
        if (node.isJsonObject()) {
            JsonObject object = node.getAsJsonObject();
            if (object.has(key) && object.get(key).isJsonPrimitive() && object.get(key).getAsString().equals(value)) return object;
            for (JsonElement child : object.asMap().values()) {
                JsonObject found = findObject(child, key, value);
                if (found != null) return found;
            }
        } else if (node.isJsonArray()) {
            for (JsonElement child : node.getAsJsonArray()) {
                JsonObject found = findObject(child, key, value);
                if (found != null) return found;
            }
        }
        return null;
    }

    // Real Entity fire-tick storage and vanilla predicates; only health/death and
    // unrelated freeze metadata are fixtures. The constructor is never invoked.
    private static class FixtureTarget extends Entity {
        float health;
        boolean acceptsDamage;
        boolean cookedAtDeath;
        int fireTicksAtDamage;
        private FixtureTarget() { super(EntityType.PIG, null); }
        @Override public boolean fireImmune() { return false; }
        @Override public void clearFreeze() { }
        @Override public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
            fireTicksAtDamage = getRemainingFireTicks();
            if (!acceptsDamage) return false;
            health -= amount;
            if (health <= 0) cookedAtDeath = COOKING_CONDITIONS.stream().allMatch(condition -> condition.matches(this));
            return true;
        }
        @Override protected void defineSynchedData(SynchedEntityData.Builder builder) { }
        @Override protected void readAdditionalSaveData(ValueInput input) { }
        @Override protected void addAdditionalSaveData(ValueOutput output) { }
    }
}
