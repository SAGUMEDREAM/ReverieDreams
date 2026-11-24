package cc.thonly.reverie_dreams.entity.ai.goal;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import net.minecraft.SharedConstants;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.Bootstrap;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagLoader;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.AnimalPanic;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import sun.misc.Unsafe;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;
import net.minecraft.util.RandomSource;

import static org.junit.jupiter.api.Assertions.*;

/** Runs vanilla's panic decision against real merged damage data, without a server or invented tags. */
class DanmakuAnimalPanicTest {
    private static MappedRegistry<DamageType> merged;
    private static MappedRegistry<DamageType> vanillaOnly;
    private static Unsafe unsafe;

    @BeforeAll static void bootstrap() throws Exception {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        var field = Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        unsafe = (Unsafe) field.get(null);
        merged = load(true);
        vanillaOnly = load(false);
    }

    @Test void normalDanmakuTriggersVanillaAnimalPanic() throws Exception {
        var mob = fixture(source(merged, "reverie_dreams:danmaku_generic"));
        assertTrue(new ProbeGoal(mob, DamageTypeTags.PANIC_CAUSES).canUse());
    }

    @Test void gunBulletsAndRealDanmakuTriggerVanillaAnimalPanic() throws Exception {
        var mob = fixture(source(merged, "reverie_dreams:danmaku_real"));
        assertTrue(new ProbeGoal(mob, DamageTypeTags.PANIC_CAUSES).canUse());
    }

    @Test void originalMissingTagReproducesTheBugDespiteProjectileMembership() throws Exception {
        for (String type : List.of("danmaku_generic", "danmaku_real")) {
            var source = source(vanillaOnly, "reverie_dreams:" + type);
            assertTrue(source.is(DamageTypeTags.IS_PROJECTILE));
            assertFalse(new ProbeGoal(fixture(source), DamageTypeTags.PANIC_CAUSES).canUse());
        }
    }

    @Test void brainBasedAnimalsRecognizeBothDamageTypesToo() throws Exception {
        var behavior = new ProbeBehavior();
        for (String type : List.of("danmaku_generic", "danmaku_real")) {
            assertTrue(behavior.allowed(fixture(source(merged, "reverie_dreams:" + type))));
            assertFalse(behavior.allowed(fixture(source(vanillaOnly, "reverie_dreams:" + type))));
        }
    }

    @Test void mergePreservesVanillaAndDoesNotChangeEnvironmentOnlyPanicOrKnockback() throws Exception {
        assertTrue(new ProbeGoal(fixture(source(merged, "minecraft:arrow")), DamageTypeTags.PANIC_CAUSES).canUse());
        assertFalse(new ProbeGoal(fixture(source(merged, "minecraft:fall")), DamageTypeTags.PANIC_CAUSES).canUse());
        assertFalse(new ProbeGoal(fixture(null), DamageTypeTags.PANIC_CAUSES).canUse());
        for (String type : List.of("danmaku_generic", "danmaku_real")) {
            var source = source(merged, "reverie_dreams:" + type);
            assertFalse(new ProbeGoal(fixture(source), DamageTypeTags.PANIC_ENVIRONMENTAL_CAUSES).canUse());
            assertTrue(source.is(DamageTypeTags.NO_KNOCKBACK));
            assertTrue(source.is(DamageTypeTags.IS_PROJECTILE));
        }
        assertFalse(source(merged, "reverie_dreams:danmaku_generic").is(DamageTypeTags.BYPASSES_ARMOR));
        assertTrue(source(merged, "reverie_dreams:danmaku_real").is(DamageTypeTags.BYPASSES_ARMOR));
    }

    private static DamageSource source(MappedRegistry<DamageType> registry, String id) {
        return new DamageSource(registry.getOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE, Identifier.parse(id))));
    }

    private static FixtureMob fixture(DamageSource source) throws Exception {
        var mob = (FixtureMob) unsafe.allocateInstance(FixtureMob.class);
        mob.damage = source;
        mob.fixtureBrain = new FixtureBrain();
        if (source != null) mob.fixtureBrain.setMemory(MemoryModuleType.HURT_BY, source);
        return mob;
    }

    /** Only supplies damage history/fire state; actual shouldPanic/canUse are inherited vanilla code. */
    private static class FixtureMob extends PathfinderMob {
        DamageSource damage;
        Brain<FixtureMob> fixtureBrain;
        private FixtureMob() { super(EntityType.PIG, null); }
        @Override public DamageSource getLastDamageSource() { return damage; }
        @Override public boolean isOnFire() { return false; }
        @Override public Brain<FixtureMob> getBrain() { return fixtureBrain; }
    }

    private static class ProbeGoal extends PanicGoal {
        ProbeGoal(PathfinderMob mob, TagKey<DamageType> tag) { super(mob, 1.25, tag); }
        // A destination exists in the fixture; path geometry is outside this damage-tag regression.
        @Override protected boolean findRandomPosition() { return true; }
    }

    private static class FixtureBrain extends Brain<FixtureMob> {
        FixtureBrain() {
            super(List.of(MemoryModuleType.HURT_BY, MemoryModuleType.IS_PANICKING), List.of(), List.of(),
                    Brain.Packed.EMPTY.memories(), RandomSource.create(0));
        }
    }

    private static class ProbeBehavior extends AnimalPanic<FixtureMob> {
        ProbeBehavior() { super(1.25f); }
        boolean allowed(FixtureMob body) { return checkExtraStartConditions(null, body); }
    }

    private static Path generated(String relative) {
        Path root = Path.of("common/src/generated/resources");
        if (!Files.isDirectory(root)) root = Path.of("src/generated/resources");
        return root.resolve(relative);
    }

    private static MappedRegistry<DamageType> load(boolean includePanicFix) throws Exception {
        var registry = new MappedRegistry<DamageType>(Registries.DAMAGE_TYPE, Lifecycle.stable());
        Map<TagKey<DamageType>, List<Holder<DamageType>>> tags = new LinkedHashMap<>();
        try (var jar = new JarFile(Path.of(PanicGoal.class.getProtectionDomain().getCodeSource().getLocation().toURI()).toFile())) {
            for (var entry : jar.stream().filter(e -> e.getName().startsWith("data/minecraft/damage_type/") && e.getName().endsWith(".json")).toList()) {
                String name = entry.getName().substring("data/minecraft/damage_type/".length(), entry.getName().length() - 5);
                try (var reader = new InputStreamReader(jar.getInputStream(entry), StandardCharsets.UTF_8)) {
                    Registry.register(registry, Identifier.withDefaultNamespace(name), DamageType.DIRECT_CODEC.parse(JsonOps.INSTANCE, JsonParser.parseReader(reader)).getOrThrow());
                }
            }
            for (String name : List.of("danmaku_generic", "danmaku_real")) {
                try (var reader = Files.newBufferedReader(generated("data/reverie_dreams/damage_type/" + name + ".json"))) {
                    Registry.register(registry, Identifier.parse("reverie_dreams:" + name), DamageType.DIRECT_CODEC.parse(JsonOps.INSTANCE, JsonParser.parseReader(reader)).getOrThrow());
                }
            }
            registry.freeze();
            for (var tag : List.of(DamageTypeTags.PANIC_CAUSES, DamageTypeTags.PANIC_ENVIRONMENTAL_CAUSES,
                    DamageTypeTags.IS_PROJECTILE, DamageTypeTags.NO_KNOCKBACK, DamageTypeTags.BYPASSES_ARMOR)) {
                loadTag(jar, registry, tag, tags, includePanicFix);
            }
        }
        registry.prepareTagReload(new TagLoader.LoadResult<>(Registries.DAMAGE_TYPE, tags)).apply();
        return registry;
    }

    private static List<Holder<DamageType>> loadTag(JarFile jar, MappedRegistry<DamageType> registry, TagKey<DamageType> tag,
            Map<TagKey<DamageType>, List<Holder<DamageType>>> tags, boolean includePanicFix) throws Exception {
        if (tags.containsKey(tag)) return tags.get(tag);
        String relative = "data/" + tag.location().getNamespace() + "/tags/damage_type/" + tag.location().getPath() + ".json";
        var result = new LinkedHashSet<Holder<DamageType>>();
        var base = jar.getJarEntry(relative);
        if (base != null) try (var reader = new InputStreamReader(jar.getInputStream(base), StandardCharsets.UTF_8)) {
            addTag(jar, registry, JsonParser.parseReader(reader).getAsJsonObject(), result, tags, includePanicFix);
        }
        var fragment = generated(relative);
        if (Files.isRegularFile(fragment) && (includePanicFix || !tag.equals(DamageTypeTags.PANIC_CAUSES))) {
            try (var reader = Files.newBufferedReader(fragment)) {
                var json = JsonParser.parseReader(reader).getAsJsonObject();
                assertFalse(json.has("replace") && json.get("replace").getAsBoolean(), relative);
                addTag(jar, registry, json, result, tags, includePanicFix);
            }
        }
        var resolved = List.copyOf(result);
        tags.put(tag, resolved);
        return resolved;
    }

    private static void addTag(JarFile jar, MappedRegistry<DamageType> registry, JsonObject json, LinkedHashSet<Holder<DamageType>> result,
            Map<TagKey<DamageType>, List<Holder<DamageType>>> tags, boolean includePanicFix) throws Exception {
        for (var value : json.getAsJsonArray("values")) {
            String id = value.isJsonPrimitive() ? value.getAsString() : value.getAsJsonObject().get("id").getAsString();
            if (id.startsWith("#")) result.addAll(loadTag(jar, registry, TagKey.create(Registries.DAMAGE_TYPE, Identifier.parse(id.substring(1))), tags, includePanicFix));
            else result.add(registry.getOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE, Identifier.parse(id))));
        }
    }
}
