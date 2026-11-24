package cc.thonly.reverie_dreams.world.sapling;

import cc.thonly.reverie_dreams.world.gensokyo.tree.GensokyoForestBootstrap;
import cc.thonly.reverie_dreams.world.gensokyo.tree.GensokyoForestType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Uses each forest's weighted configured tree pool, without natural placement filters.
 */
public final class GensokyoSaplings {
    public record Entry(GensokyoForestType forest, Block appearance, TreeGrower grower) {
        public String path() {
            return forest.biomePath() + "_sapling";
        }

        public ResourceKey<ConfiguredFeature<?, ?>> pool() {
            return GensokyoForestBootstrap.configured(forest.treesPath());
        }
    }

    private static final List<Entry> ENTRIES = Arrays.stream(GensokyoForestType.values()).map(type -> {
        Block appearance = switch (type) {
            case FOREST -> Blocks.OAK_SAPLING;
            case BIRCH_FOREST -> Blocks.BIRCH_SAPLING;
            case TAIGA -> Blocks.SPRUCE_SAPLING;
            case CHERRY_GROVE, MAPLE_FOREST -> Blocks.CHERRY_SAPLING;
            case DARK_FOREST -> Blocks.DARK_OAK_SAPLING;
            case MAGIC_FOREST -> Blocks.PALE_OAK_SAPLING;
            case JUNGLE -> Blocks.JUNGLE_SAPLING;
        };
        var pool = GensokyoForestBootstrap.configured(type.treesPath());
        var grower = new TreeGrower(GensokyoForestBootstrap.id(type.biomePath() + "_sapling").toString(),
                Optional.empty(), Optional.of(pool), Optional.empty());
        return new Entry(type, appearance, grower);
    }).toList();

    public static List<Entry> entries() {
        return ENTRIES;
    }

    private GensokyoSaplings() {
    }
}
