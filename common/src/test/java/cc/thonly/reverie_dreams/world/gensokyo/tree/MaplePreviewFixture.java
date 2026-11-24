package cc.thonly.reverie_dreams.world.gensokyo.tree;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import cc.thonly.reverie_dreams.world.gen.biome.MapleForestBiomeCreator;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import cc.thonly.reverie_dreams.block.MagicMushroomBlock;
import cc.thonly.reverie_dreams.block.MagicMushroomCapBlock;
import cc.thonly.reverie_dreams.world.gensokyo.understory.MagicMushroomBootstrap;

/** Headless maple subset: same IDs and native block classes as WoodBundle, no world or loader. */
final class MaplePreviewFixture {
    static final List<String> SPECIES = List.of("iroha", "itaya", "hauchiwa");
    private static final Path GENERATED = (Files.isDirectory(Path.of("common/src")) ? Path.of("common") : Path.of("."))
            .resolve("src/generated/resources/data");

    static void registerBlocks() {
        try {
            var frozen = MappedRegistry.class.getDeclaredField("frozen");
            var intrusive = MappedRegistry.class.getDeclaredField("unregisteredIntrusiveHolders");
            frozen.setAccessible(true);
            intrusive.setAccessible(true);
            Object oldIntrusive = intrusive.get(BuiltInRegistries.BLOCK);
            boolean oldFrozen = frozen.getBoolean(BuiltInRegistries.BLOCK);
            frozen.setBoolean(BuiltInRegistries.BLOCK, false);
            intrusive.set(BuiltInRegistries.BLOCK, new IdentityHashMap<>());
            try {
                for (String species : SPECIES) {
                    register(species + "_maple_log", Blocks.OAK_LOG, properties -> new RotatedPillarBlock(properties.noOcclusion()));
                    register(species + "_maple_leaves", Blocks.OAK_LEAVES, properties -> new TintedParticleLeavesBlock(0.01F, properties));
                    register(species + "_maple_sapling", Blocks.OAK_SAPLING, properties -> new SaplingBlock(TreeGrower.OAK, properties));
                    register(species + "_maple_leaf_litter", Blocks.LEAF_LITTER, LeafLitterBlock::new);
                }
                register("blue_mushroom", Blocks.BROWN_MUSHROOM, p -> new MagicMushroomBlock(
                        MagicMushroomBootstrap.HUGE_BLUE, p));
                register("purple_mushroom", Blocks.RED_MUSHROOM, p -> new MagicMushroomBlock(
                        MagicMushroomBootstrap.HUGE_PURPLE, p));
                for (String id : List.of("blue_mushroom_block", "purple_mushroom_block"))
                    register(id, Blocks.BROWN_MUSHROOM_BLOCK, MagicMushroomCapBlock::new);
                register("magic_mushroom_stem", Blocks.MUSHROOM_STEM, HugeMushroomBlock::new);
            } finally {
                intrusive.set(BuiltInRegistries.BLOCK, oldIntrusive);
                frozen.setBoolean(BuiltInRegistries.BLOCK, oldFrozen);
            }
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot initialize the headless maple subset", e);
        }
    }

    private static void register(String path, Block template, Function<BlockBehaviour.Properties, Block> factory) {
        var key = ResourceKey.create(Registries.BLOCK, GensokyoForestBootstrap.id(path));
        if (BuiltInRegistries.BLOCK.containsKey(key)) return;
        Block block = factory.apply(BlockBehaviour.Properties.ofFullCopy(template).setId(key));
        Registry.register(BuiltInRegistries.BLOCK, key, block);
        for (var state : block.getStateDefinition().getPossibleStates()) {
            Block.BLOCK_STATE_REGISTRY.add(state);
            state.initCache();
        }
    }

    static void configured(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        for (String name : List.of("iroha_maple_tree", "itaya_maple_tree", "hauchiwa_maple_tree", "maple_leaf_litter")) {
            context.register(GensokyoForestBootstrap.configured(name), ConfiguredFeature.DIRECT_CODEC.parse(JsonOps.INSTANCE,
                    json("reverie_dreams/worldgen/configured_feature/" + name + ".json")).getOrThrow());
        }
    }

    static void placed(BootstrapContext<PlacedFeature> context) {
        for (String name : List.of("iroha_maple_tree_placed", "itaya_maple_tree_placed", "hauchiwa_maple_tree_placed", "maple_leaf_litter_placed")) {
            var value = json("reverie_dreams/worldgen/placed_feature/" + name + ".json").getAsJsonObject();
            var modifiers = new ArrayList<PlacementModifier>();
            for (var modifier : value.getAsJsonArray("placement")) modifiers.add(PlacementModifier.CODEC.parse(JsonOps.INSTANCE, modifier).getOrThrow());
            var feature = ResourceKey.create(Registries.CONFIGURED_FEATURE, Identifier.parse(value.get("feature").getAsString()));
            context.register(GensokyoForestBootstrap.placed(name), new PlacedFeature(context.lookup(Registries.CONFIGURED_FEATURE).getOrThrow(feature), modifiers));
        }
    }

    static void biome(BootstrapContext<Biome> context) {
        context.register(ResourceKey.create(Registries.BIOME, GensokyoForestBootstrap.id("maple_forest")),
                MapleForestBiomeCreator.createMapleForest(context.lookup(Registries.PLACED_FEATURE), context.lookup(Registries.CONFIGURED_CARVER)));
    }

    static void addBlockTags(Map<TagKey<Block>, List<Holder<Block>>> tags) {
        // Read the real project tag declarations for this subset, rather than assigning guessed memberships.
        for (var tag : List.of(BlockTags.LOGS, BlockTags.PREVENTS_NEARBY_LEAF_DECAY, BlockTags.LEAVES)) {
            var declarations = json("minecraft/tags/block/" + tag.location().getPath() + ".json").getAsJsonObject().getAsJsonArray("values");
            var merged = new ArrayList<Holder<Block>>(tags.getOrDefault(tag, List.of()));
            String suffix = tag.equals(BlockTags.LEAVES) ? "_leaves" : "_log";
            for (String species : SPECIES) {
                String id = "reverie_dreams:" + species + "_maple" + suffix;
                boolean declared = false;
                for (var entry : declarations) if (entry.isJsonPrimitive() && entry.getAsString().equals(id)) declared = true;
                if (!declared) throw new IllegalStateException("Maple material missing from actual tag " + tag + ": " + id);
                merged.add(BuiltInRegistries.BLOCK.getOrThrow(ResourceKey.create(Registries.BLOCK, Identifier.parse(id))));
            }
            tags.put(tag, List.copyOf(merged));
        }
        var replaceable = new ArrayList<Holder<Block>>(tags.get(BlockTags.REPLACEABLE_BY_TREES));
        for (String species : SPECIES) replaceable.add(BuiltInRegistries.BLOCK.getOrThrow(ResourceKey.create(Registries.BLOCK,
                GensokyoForestBootstrap.id(species + "_maple_leaves"))));
        tags.put(BlockTags.REPLACEABLE_BY_TREES, List.copyOf(replaceable));
    }

    private static JsonElement json(String path) {
        try { return JsonParser.parseString(Files.readString(GENERATED.resolve(path))); }
        catch (IOException e) { throw new UncheckedIOException(e); }
    }

    private MaplePreviewFixture() {}
}
