package cc.thonly.reverie_dreams.world.dream;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public final class DreamArchipelagoFeature extends Feature<NoneFeatureConfiguration> {
    public DreamArchipelagoFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        var world = context.level();
        var blocks = DreamArchipelagoLayout.blocksInChunk(world.getSeed(), Math.floorDiv(context.origin().getX(), 16),
                Math.floorDiv(context.origin().getZ(), 16));
        for (var block : blocks) {
            if (block.pos().getY() >= world.getMinY() && block.pos().getY() < world.getMaxY()) {
                world.setBlock(block.pos(), block.state(), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
            }
        }
        // Only the generating owner chunk receives metadata. No chunk-load refill or repair hook.
        for (var chest : DreamArchipelagoLayout.chestsInChunk(world.getSeed(), Math.floorDiv(context.origin().getX(), 16),
                Math.floorDiv(context.origin().getZ(), 16))) {
            if (chest.pos().getY() < world.getMinY() || chest.pos().getY() >= world.getMaxY())
                continue;
            if (world.getBlockEntity(chest.pos()) instanceof ChestBlockEntity container) {
                container.setLootTable(chest.table());
                container.setLootTableSeed(chest.seed());
                container.setChanged();
            }
        }
        int[] terrainBlocks = {0};
        DreamTerrainLayout.forEachBlockInChunk(world.getSeed(), Math.floorDiv(context.origin().getX(), 16),
                Math.floorDiv(context.origin().getZ(), 16), (pos, state) -> {
                    if (pos.getY() >= world.getMinY() && pos.getY() < world.getMaxY()) {
                        world.setBlock(pos, state, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
                        terrainBlocks[0]++;
                    }
                });
        return !blocks.isEmpty() || terrainBlocks[0] > 0;
    }
}
