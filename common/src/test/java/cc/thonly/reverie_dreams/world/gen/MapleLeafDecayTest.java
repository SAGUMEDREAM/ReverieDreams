package cc.thonly.reverie_dreams.world.gen;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.Bootstrap;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagLoader;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.jar.JarFile;

import static org.junit.jupiter.api.Assertions.*;

class MapleLeafDecayTest {
    private static final List<String> SPECIES = List.of("iroha", "itaya", "hauchiwa");
    private static final BlockPos ORIGIN = new BlockPos(0, 64, 0);
    private static Method updateDistance;

    @BeforeAll
    static void bootstrap() throws Exception {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        Map<TagKey<Block>, List<Holder<Block>>> tags = new LinkedHashMap<>();
        // Read from Minecraft's own JAR so classpath mod tag fragments cannot replace vanilla data.
        try (var jar = new JarFile(Path.of(Blocks.class.getProtectionDomain().getCodeSource().getLocation().toURI()).toFile())) {
            for (var tag : List.of(BlockTags.PREVENTS_NEARBY_LEAF_DECAY, BlockTags.SUBSTRATE_OVERWORLD,
                    BlockTags.REPLACEABLE_BY_TREES, TagKey.create(Registries.BLOCK, Identifier.withDefaultNamespace("cannot_replace_below_tree_trunk")))) {
                loadTag(jar, tag, tags);
            }
        }
        BuiltInRegistries.BLOCK.prepareTagReload(new TagLoader.LoadResult<>(Registries.BLOCK, tags)).apply();
        assertTrue(Blocks.OAK_LOG.defaultBlockState().is(BlockTags.PREVENTS_NEARBY_LEAF_DECAY));
        assertFalse(Blocks.GRASS_BLOCK.defaultBlockState().is(BlockTags.DIRT));
        // Invoke the real vanilla scheduled-tick kernel without starting a ServerLevel/save.
        updateDistance = LeavesBlock.class.getDeclaredMethod("updateDistance", BlockState.class, LevelAccessor.class, BlockPos.class);
        updateDistance.setAccessible(true);
    }

    private static List<Holder<Block>> loadTag(JarFile jar, TagKey<Block> tag, Map<TagKey<Block>, List<Holder<Block>>> tags) throws Exception {
        if (tags.containsKey(tag)) return tags.get(tag);
        String resource = "data/" + tag.location().getNamespace() + "/tags/block/" + tag.location().getPath() + ".json";
        var entry = jar.getJarEntry(resource);
        assertNotNull(entry, resource);
        var result = new LinkedHashSet<Holder<Block>>();
        try (var reader = new InputStreamReader(jar.getInputStream(entry), StandardCharsets.UTF_8)) {
            for (var value : JsonParser.parseReader(reader).getAsJsonObject().getAsJsonArray("values")) {
                String id = value.isJsonPrimitive() ? value.getAsString() : value.getAsJsonObject().get("id").getAsString();
                if (id.startsWith("#")) result.addAll(loadTag(jar, TagKey.create(Registries.BLOCK, Identifier.parse(id.substring(1))), tags));
                else result.add(BuiltInRegistries.BLOCK.getOrThrow(ResourceKey.create(Registries.BLOCK, Identifier.parse(id))));
            }
        }
        var resolved = List.copyOf(result);
        tags.put(tag, resolved);
        return resolved;
    }

    @Test
    void allThreeGeneratedCrownsSurviveRealVanillaDistanceUpdates() throws Exception {
        Map<String, Integer> unsupported = new LinkedHashMap<>();
        for (String species : SPECIES) {
            int count = 0;
            for (long seed = 0; seed < 64; seed++) {
                MemoryWorld world = grow(species, seed);
                world.settleLeaves();
                count += (int) world.states.values().stream().filter(MapleLeafDecayTest::isLeaf)
                        .filter(state -> state.getValue(LeavesBlock.DISTANCE) == LeavesBlock.DECAY_DISTANCE).count();
            }
            unsupported.put(species, count);
        }
        System.out.println("Unsupported maple leaves after native updates: " + unsupported);
        assertTrue(unsupported.values().stream().allMatch(count -> count == 0), unsupported.toString());
    }

    @Test
    void freshlyGeneratedLeavesAlreadyHaveRealSupportedDistances() throws Exception {
        for (String species : SPECIES) {
            for (long seed = 0; seed < 32; seed++) {
                MemoryWorld world = grow(species, seed);
                assertTrue(world.states.values().stream().filter(MapleLeafDecayTest::isLeaf)
                        .allMatch(state -> state.getValue(LeavesBlock.DISTANCE) < LeavesBlock.DECAY_DISTANCE), species + " / " + seed);
            }
        }
    }

    @Test
    void removingEveryLogMakesNaturalLeavesDecayEligible() throws Exception {
        for (String species : SPECIES) {
            for (long seed = 0; seed < 8; seed++) {
                MemoryWorld world = grow(species, seed);
                world.settleLeaves();
                world.states.entrySet().removeIf(entry -> entry.getValue().is(Blocks.OAK_LOG));
                world.settleLeaves();
                assertTrue(world.states.values().stream().filter(MapleLeafDecayTest::isLeaf)
                        .allMatch(state -> !state.getValue(LeavesBlock.PERSISTENT)
                                && state.getValue(LeavesBlock.DISTANCE) == LeavesBlock.DECAY_DISTANCE), species + " / " + seed);
            }
        }
    }

    private static MemoryWorld grow(String species, long seed) throws Exception {
        String resource = "data/reverie_dreams/worldgen/configured_feature/" + species + "_maple_tree.json";
        JsonObject config;
        Path file = Path.of("src/generated/resources").resolve(resource);
        if (!Files.isRegularFile(file)) file = Path.of("common/src/generated/resources").resolve(resource);
        try (var reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            config = JsonParser.parseReader(reader).getAsJsonObject().getAsJsonObject("config");
        }
        // Geometry/settings are the shipped config. Alias only mod block identities for headless registries.
        config.getAsJsonObject("trunk_provider").getAsJsonObject("state").addProperty("Name", "minecraft:oak_log");
        config.getAsJsonObject("foliage_provider").getAsJsonObject("state").addProperty("Name", "minecraft:oak_leaves");
        var tree = TreeConfiguration.CODEC.parse(JsonOps.INSTANCE, config).getOrThrow();
        var world = new MemoryWorld();
        assertTrue(Feature.TREE.place(tree, world.level, null, RandomSource.create(seed), ORIGIN), species + " / " + seed);
        assertTrue(world.states.values().stream().anyMatch(MapleLeafDecayTest::isLeaf));
        assertTrue(world.states.values().stream().filter(MapleLeafDecayTest::isLeaf)
                .noneMatch(state -> state.getValue(LeavesBlock.PERSISTENT)), species);
        return world;
    }

    private static boolean isLeaf(BlockState state) { return state.is(Blocks.OAK_LEAVES); }

    private static final class MemoryWorld {
        final Map<BlockPos, BlockState> states = new HashMap<>();
        final RandomSource random = RandomSource.create(0);
        final WorldGenLevel level = (WorldGenLevel) Proxy.newProxyInstance(WorldGenLevel.class.getClassLoader(), new Class[]{WorldGenLevel.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getMinY" -> 0;
                    case "getMaxY", "getHeight" -> 256;
                    case "getRandom" -> random;
                    case "ensureCanWrite" -> true;
                    case "getBlockState" -> state((BlockPos) args[0]);
                    case "getFluidState" -> state((BlockPos) args[0]).getFluidState();
                    case "getBlockEntity" -> null;
                    case "isStateAtPosition" -> ((Predicate<BlockState>) args[1]).test(state((BlockPos) args[0]));
                    case "isFluidAtPosition" -> ((Predicate<Object>) args[1]).test(state((BlockPos) args[0]).getFluidState());
                    case "setBlock" -> { states.put(((BlockPos) args[0]).immutable(), (BlockState) args[1]); yield true; }
                    case "scheduleTick" -> null;
                    case "toString" -> "Maple decay in-memory world";
                    default -> {
                        if (method.isDefault()) yield InvocationHandler.invokeDefault(proxy, method, args);
                        throw new UnsupportedOperationException(method.getName());
                    }
                });

        BlockState state(BlockPos pos) {
            return states.getOrDefault(pos, pos.getY() == 63 ? Blocks.GRASS_BLOCK.defaultBlockState() : Blocks.AIR.defaultBlockState());
        }

        void settleLeaves() throws ReflectiveOperationException {
            for (int pass = 0; pass < 32; pass++) {
                Map<BlockPos, BlockState> updates = new HashMap<>();
                for (var entry : states.entrySet()) {
                    if (!isLeaf(entry.getValue())) continue;
                    BlockState next = (BlockState) updateDistance.invoke(null, entry.getValue(), level, entry.getKey());
                    if (next != entry.getValue()) updates.put(entry.getKey(), next);
                }
                if (updates.isEmpty()) return;
                states.putAll(updates);
            }
            fail("Vanilla leaf distances did not converge");
        }
    }
}
