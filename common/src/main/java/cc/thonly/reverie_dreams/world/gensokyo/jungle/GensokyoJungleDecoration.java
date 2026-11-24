package cc.thonly.reverie_dreams.world.gensokyo.jungle;

import cc.thonly.reverie_dreams.world.gensokyo.deadwood.GensokyoDeadwoodBootstrap;
import cc.thonly.reverie_dreams.world.gensokyo.deadwood.GensokyoDeadwoodConfiguration;
import cc.thonly.reverie_dreams.world.gensokyo.deadwood.GensokyoDeadwoodFeature;
import cc.thonly.reverie_dreams.world.gensokyo.road.GensokyoRoadProtection;
import cc.thonly.reverie_dreams.world.gensokyo.tree.GensokyoForestBootstrap;
import cc.thonly.reverie_dreams.world.gensokyo.tree.GensokyoForestType;
import cc.thonly.reverie_dreams.world.gensokyo.understory.GensokyoUnderstoryFeature;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.*;

import java.util.List;

/**
 * Sparse accents, wired only into the independent jungle; other forests keep preview-only deadwood.
 */
public final class GensokyoJungleDecoration extends Feature<NoneFeatureConfiguration> {
    public static final ResourceKey<PlacedFeature> VINES = GensokyoForestBootstrap.placed("gensokyo/jungle/vines");
    public static final ResourceKey<PlacedFeature> FALLEN_LOG = GensokyoForestBootstrap.placed("gensokyo/jungle/fallen_log");
    private final boolean fallenLog;

    public GensokyoJungleDecoration(Codec<NoneFeatureConfiguration> codec, boolean fallenLog) {
        super(codec);
        this.fallenLog = fallenLog;
    }

    public static void configured(BootstrapContext<ConfiguredFeature<?, ?>> context,
                                  Feature<NoneFeatureConfiguration> vines, Feature<NoneFeatureConfiguration> fallen) {
        context.register(GensokyoForestBootstrap.configured(VINES.identifier().getPath()),
                new ConfiguredFeature<>(vines, NoneFeatureConfiguration.INSTANCE));
        context.register(GensokyoForestBootstrap.configured(FALLEN_LOG.identifier().getPath()),
                new ConfiguredFeature<>(fallen, NoneFeatureConfiguration.INSTANCE));
    }

    public static void placed(BootstrapContext<PlacedFeature> context) {
        var features = context.lookup(Registries.CONFIGURED_FEATURE);
        for (var key : List.of(VINES, FALLEN_LOG)) {
            PlacementModifier density = key == FALLEN_LOG ? RarityFilter.onAverageOnceEvery(24) : CountPlacement.of(2);
            context.register(key, new PlacedFeature(features.getOrThrow(
                    GensokyoForestBootstrap.configured(key.identifier().getPath())),
                    List.of(density, InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP_OCEAN_FLOOR, BiomeFilter.biome())));
        }
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        var level = context.level();
        BlockPos root = GensokyoUnderstoryFeature.findSoilSurface(level, context.origin().getX(), context.origin().getZ());
        if (root == null || GensokyoUnderstoryFeature.isNearProtectedGround(level, root))
            return false;
        if (fallenLog) {
            var config = GensokyoDeadwoodBootstrap.configuration(new GensokyoDeadwoodBootstrap.Profile(
                    GensokyoForestType.JUNGLE, GensokyoDeadwoodConfiguration.Kind.FALLEN_LOG));
            return GensokyoDeadwoodFeature.placeDeadwood(level, root, config, context.random().nextLong());
        }
        int placed = 0;
        for (int attempt = 0; attempt < 18 && placed < 20; attempt++) {
            BlockPos start = root.offset(context.random().nextInt(13) - 6,
                    5 + context.random().nextInt(26), context.random().nextInt(13) - 6);
            if (!writable(level, start) || !level.getBlockState(start).isAir())
                continue;
            for (Direction face : Direction.Plane.HORIZONTAL) {
                BlockPos support = start.relative(face);
                if (!writable(level, support))
                    continue;
                var neighbour = level.getBlockState(support);
                if (!neighbour.is(BlockTags.LOGS) && !neighbour.is(BlockTags.LEAVES))
                    continue;
                var vine = Blocks.VINE.defaultBlockState().setValue(VineBlock.getPropertyForFace(face), true);
                int length = 1 + context.random().nextInt(4);
                for (int n = 0; n < length && placed < 20; n++) {
                    BlockPos pos = start.below(n);
                    if (pos.getY() < root.getY() + 3 || !writable(level, pos)
                            || !level.getBlockState(pos).isAir() || !level.getFluidState(pos).isEmpty()
                            || level.getBlockEntity(pos) != null || !vine.canSurvive(level, pos))
                        break;
                    level.setBlock(pos, vine, Block.UPDATE_CLIENTS);
                    placed++;
                }
                break;
            }
        }
        return placed > 0;
    }

    private static boolean writable(WorldGenLevel level, BlockPos pos) {
        return !level.isOutsideBuildHeight(pos) && level.ensureCanWrite(pos) && !GensokyoRoadProtection.reserved(level, pos);
    }
}
