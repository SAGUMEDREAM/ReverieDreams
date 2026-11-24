package cc.thonly.reverie_dreams.world.gensokyo.tree;

import com.google.gson.GsonBuilder;
import cc.thonly.reverie_dreams.world.gensokyo.rock.*;
import cc.thonly.reverie_dreams.world.gensokyo.understory.*;
import net.minecraft.world.level.levelgen.feature.Feature;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.minecraft.SharedConstants;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.data.registries.RegistryPatchGenerator;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import com.google.gson.JsonElement;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.HugeMushroomFeatureConfiguration;

/** Optional headless asset export. Does not instantiate a server, dimension or save. */
public final class GensokyoForestPreviewExporter {
    private static HolderLookup.Provider registries;

    static synchronized HolderLookup.Provider registries() {
        if (registries != null) return registries;
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        MaplePreviewFixture.registerBlocks();
        var featureId = GensokyoForestBootstrap.id("gensokyo_tree");
        var feature = BuiltInRegistries.FEATURE.containsKey(featureId)
                ? (GensokyoTreeFeature) BuiltInRegistries.FEATURE.getValue(featureId)
                : new GensokyoTreeFeature(GensokyoTreeConfiguration.CODEC);
        if (!BuiltInRegistries.FEATURE.containsKey(featureId)) registerHeadlessFeature("gensokyo_tree", feature);
        var understoryId = GensokyoForestBootstrap.id("gensokyo_understory");
        var understory = BuiltInRegistries.FEATURE.containsKey(understoryId)
                ? (GensokyoUnderstoryFeature) BuiltInRegistries.FEATURE.getValue(understoryId)
                : new GensokyoUnderstoryFeature(GensokyoUnderstoryConfiguration.CODEC);
        if (!BuiltInRegistries.FEATURE.containsKey(understoryId)) registerHeadlessFeature("gensokyo_understory", understory);
        var mushroomId = GensokyoForestBootstrap.id("magic_huge_mushroom");
        var mushrooms = BuiltInRegistries.FEATURE.containsKey(mushroomId) ? (MagicHugeMushroomFeature) BuiltInRegistries.FEATURE.getValue(mushroomId)
                : new MagicHugeMushroomFeature(HugeMushroomFeatureConfiguration.CODEC);
        if (!BuiltInRegistries.FEATURE.containsKey(mushroomId)) registerHeadlessFeature("magic_huge_mushroom", mushrooms);
        var rockId = GensokyoForestBootstrap.id("gensokyo_rock");
        var rocks = BuiltInRegistries.FEATURE.containsKey(rockId) ? (GensokyoRockFeature) BuiltInRegistries.FEATURE.getValue(rockId)
                : new GensokyoRockFeature(GensokyoRockConfiguration.CODEC);
        if (!BuiltInRegistries.FEATURE.containsKey(rockId)) registerHeadlessFeature("gensokyo_rock", rocks);
        var vanilla = VanillaRegistries.createLookup();
        var jungleVines = new cc.thonly.reverie_dreams.world.gensokyo.jungle.GensokyoJungleDecoration(
                net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration.CODEC, false);
        var jungleFallen = new cc.thonly.reverie_dreams.world.gensokyo.jungle.GensokyoJungleDecoration(
                net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration.CODEC, true);
        registerHeadlessFeature("gensokyo_jungle_vines", jungleVines);
        registerHeadlessFeature("gensokyo_jungle_fallen_log", jungleFallen);
        var builder = new RegistrySetBuilder()
                .add(Registries.CONFIGURED_FEATURE, context -> { MaplePreviewFixture.configured(context); GensokyoForestBootstrap.configured(context, feature); GensokyoUnderstoryBootstrap.configured(context, understory); MagicMushroomBootstrap.configured(context, mushrooms); GensokyoRockBootstrap.configured(context, rocks); cc.thonly.reverie_dreams.world.gensokyo.jungle.GensokyoJungleDecoration.configured(context, jungleVines, jungleFallen); })
                .add(Registries.PLACED_FEATURE, context -> { MaplePreviewFixture.placed(context); GensokyoForestBootstrap.placed(context); GensokyoUnderstoryBootstrap.placed(context); MagicMushroomBootstrap.placed(context); GensokyoRockBootstrap.placed(context); cc.thonly.reverie_dreams.world.gensokyo.jungle.GensokyoJungleDecoration.placed(context); })
                .add(Registries.BIOME, context -> { MaplePreviewFixture.biome(context); GensokyoForestBootstrap.biomes(context); });
        registries = RegistryPatchGenerator.createLookup(CompletableFuture.completedFuture(vanilla), builder).join().full();
        return registries;
    }

    private static void registerHeadlessFeature(String path, Feature<?> feature) {
        // Vanilla Bootstrap freezes static registries; a real mod loader supplies its registration window.
        // Recreate that window only in this test/export JVM, never in production code.
        try {
            var frozen = MappedRegistry.class.getDeclaredField("frozen");
            frozen.setAccessible(true);
            boolean wasFrozen = frozen.getBoolean(BuiltInRegistries.FEATURE);
            frozen.setBoolean(BuiltInRegistries.FEATURE, false);
            try {
                Registry.register(BuiltInRegistries.FEATURE, GensokyoForestBootstrap.id(path), feature);
                var holder = BuiltInRegistries.FEATURE.getOrThrow(ResourceKey.create(Registries.FEATURE, GensokyoForestBootstrap.id(path)));
                var bind = Holder.Reference.class.getDeclaredMethod("bindValue", Object.class);
                bind.setAccessible(true);
                bind.invoke(holder, feature);
            } finally {
                frozen.setBoolean(BuiltInRegistries.FEATURE, wasFrozen);
            }
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot open the headless-only feature registration window", e);
        }
    }

    public static void main(String[] args) throws IOException {
        registries();
        Path resources = Path.of(args[0]);
        Path previews = Path.of(args[1]);
        exportRegistry(resources, Registries.CONFIGURED_FEATURE, ConfiguredFeature.DIRECT_CODEC, "configured_feature");
        exportRegistry(resources, Registries.PLACED_FEATURE, PlacedFeature.DIRECT_CODEC, "placed_feature");
        exportRegistry(resources, Registries.BIOME, Biome.DIRECT_CODEC, "biome");
        JsonArray samples = new JsonArray();
        for (var profile : GensokyoTreeProfile.values()) {
            Set<GensokyoTreeShape.Form> forms = new HashSet<>();
            int expectedForms = profile.settings().species() == GensokyoTreeShape.Species.SPRUCE ? 1 : 4;
            for (int variant = 0; variant < 128 && (variant < 3 || forms.size() < expectedForms); variant++) {
                long seed = 20261103L + variant * 104729L;
                var tree = GensokyoTreeShape.create(profile.settings(), new Random(seed));
                boolean firstForm = forms.add(tree.form());
                if (variant >= 3 && !firstForm) continue;
                JsonObject sample = new JsonObject();
                sample.addProperty("profile", profile.name());
                sample.addProperty("species", profile.settings().species().name());
                sample.addProperty("form", tree.form().name());
                sample.addProperty("crown_style", profile.settings().crownStyle().name());
                sample.addProperty("seed", seed);
                sample.addProperty("height", tree.height());
                sample.addProperty("min_height", profile.settings().minHeight());
                sample.addProperty("max_height", profile.settings().maxHeight());
                JsonArray voxels = new JsonArray();
                for (var voxel : tree.voxels()) {
                    JsonArray cell = new JsonArray();
                    cell.add(voxel.point().x()); cell.add(voxel.point().y()); cell.add(voxel.point().z());
                    cell.add(voxel.log() ? 1 : 0); cell.add(voxel.axis().name()); cell.add(voxel.leafDistance());
                    voxels.add(cell);
                }
                sample.add("voxels", voxels);
                samples.add(sample);
            }
        }
        write(previews.resolve("tree-geometry.json"), samples);
        exportUnderstory(previews);
        exportUnderstoryDistribution(previews);
        GensokyoRockPreview.export(Path.of("tools/gensokyo-rocks/preview"));
    }

    private static void exportUnderstoryDistribution(Path previews) throws IOException {
        JsonArray samples = new JsonArray();
        for (var theme : GensokyoForestType.values()) {
            var result = GensokyoUnderstoryLandscape.generate(theme, 20261105, 8);
            JsonObject sample = new JsonObject();
            sample.addProperty("theme", theme.name()); sample.addProperty("seed", 20261105);
            sample.addProperty("size", result.size()); sample.addProperty("woody_columns", result.woodyColumns());
            sample.addProperty("floor_columns", result.floorColumns()); sample.addProperty("largest_open_area", result.largestOpenArea());
            JsonObject counts = new JsonObject();
            result.patches().forEach((kind, count) -> counts.addProperty(kind.name(), count));
            sample.add("patches", counts);
            Map<Integer, Integer> columns = new TreeMap<>();
            result.world().states.forEach((pos, state) -> {
                if (pos.getX() < 0 || pos.getX() >= result.size() || pos.getZ() < 0 || pos.getZ() >= result.size()) return;
                int key = pos.getZ() * result.size() + pos.getX();
                int role = state.is(BlockTags.LOGS) || state.is(BlockTags.LEAVES) ? 2 : 1;
                columns.merge(key, role, Math::max);
            });
            JsonArray cells = new JsonArray();
            columns.forEach((index, role) -> {
                JsonArray cell = new JsonArray(); cell.add(index % result.size()); cell.add(index / result.size()); cell.add(role); cells.add(cell);
            });
            sample.add("columns", cells); samples.add(sample);
        }
        write(previews.resolve("understory-distribution.json"), samples);
    }

    private static void exportUnderstory(Path previews) throws IOException {
        VanillaTreeTags.bind();
        JsonArray samples = new JsonArray();
        for (var profile : GensokyoUnderstoryBootstrap.PROFILES) for (int variant = 0; variant < 3; variant++) {
            long seed = 20261105L + variant * 104729L;
            var world = new GensokyoTreeFeatureTest.MemoryWorld();
            var config = GensokyoUnderstoryBootstrap.configuration(profile);
            if (!GensokyoUnderstoryFeature.placePatch(world.level, new BlockPos(0, 64, 0), config, seed))
                throw new IllegalStateException("Flat-ground preview failed: " + profile + " seed=" + seed);
            JsonObject sample = new JsonObject();
            sample.addProperty("theme", profile.theme().name());
            sample.addProperty("kind", profile.kind().name());
            sample.addProperty("seed", seed);
            JsonArray voxels = new JsonArray();
            world.states.entrySet().stream().sorted(Comparator.comparingInt((Map.Entry<BlockPos, BlockState> e) -> e.getKey().getY())
                    .thenComparingInt(e -> e.getKey().getX()).thenComparingInt(e -> e.getKey().getZ())).forEach(entry -> {
                var pos = entry.getKey(); var state = entry.getValue();
                JsonArray cell = new JsonArray();
                cell.add(pos.getX()); cell.add(pos.getY() - 64); cell.add(pos.getZ());
                cell.add(BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString());
                cell.add(state.hasProperty(LeavesBlock.DISTANCE) ? "leaves"
                        : config.logs().stream().anyMatch(log -> log.is(state.getBlock())) ? "log" : "floor");
                voxels.add(cell);
            });
            sample.add("voxels", voxels); samples.add(sample);
        }
        write(previews.resolve("understory-geometry.json"), samples);
    }

    private static <T> void exportRegistry(Path root, ResourceKey<? extends Registry<T>> key, Codec<T> codec, String folder) throws IOException {
        var provider = registries();
        var ops = provider.createSerializationContext(JsonOps.INSTANCE);
        for (var entry : provider.lookupOrThrow(key).listElements().toList()) {
            var id = entry.key().identifier();
            if (!id.getNamespace().equals("reverie_dreams")) continue;
            if (!id.getPath().startsWith("gensokyo/") && !id.getPath().startsWith("gensokyo_")) continue;
            write(root.resolve("data/reverie_dreams/worldgen/" + folder + "/" + id.getPath() + ".json"),
                    codec.encodeStart(ops, entry.value()).getOrThrow());
        }
    }

    private static void write(Path path, JsonElement value) throws IOException {
        Files.createDirectories(path.getParent());
        Files.writeString(path, new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(value) + "\n");
    }
}
