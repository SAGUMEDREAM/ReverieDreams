package cc.thonly.reverie_dreams.world.gen.feature;

import cc.thonly.reverie_dreams.world.dimension.RDBuiltinLevels;
import cc.thonly.reverie_dreams.world.dream.DreamArchipelagoLayout;
import cc.thonly.reverie_dreams.world.dream.DreamFloatingRockShape;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;


@SuppressWarnings("deprecation")
public class FloatingSphereFeature extends Feature<FloatingSphereFeatureConfig> {
    public FloatingSphereFeature(Codec<FloatingSphereFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<FloatingSphereFeatureConfig> context) {
        WorldGenLevel world = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();
        FloatingSphereFeatureConfig config = context.config();
        boolean protectDreamScenes = world.getLevel().dimension().equals(RDBuiltinLevels.DREAM_WORLD);

        int minRadius = Math.max(1, Math.min(config.minRadius(), 5));
        int maxRadius = Math.max(minRadius, Math.min(config.maxRadius(), 5));
        int radius = minRadius + random.nextInt(maxRadius - minRadius + 1);
        var shape = DreamFloatingRockShape.create(random.nextLong(), radius);
        int minY = Math.max(config.minY() + shape.radiusY(), world.getMinY() + shape.radiusY());
        int maxY = Math.min(config.maxY() - shape.radiusY(), world.getMaxY() - 1 - shape.radiusY());
        if (minY > maxY)
            return false;
        // Keep the entire rock inside its generating chunk. Configuration Y is absolute,
        // rather than added to an already randomized placed-feature height a second time.
        int chunkX = Math.floorDiv(origin.getX(), 16), chunkZ = Math.floorDiv(origin.getZ(), 16);
        BlockPos center = new BlockPos(chunkX * 16 + shape.radiusX() + random.nextInt(16 - 2 * shape.radiusX()),
                minY + random.nextInt(maxY - minY + 1),
                chunkZ * 16 + shape.radiusZ() + random.nextInt(16 - 2 * shape.radiusZ()));
        Block block = BuiltInRegistries.BLOCK.getValue(config.blockId());
        if (block == null)
            return false;
        for (BlockPos offset : shape.blocks()) {
            BlockPos pos = center.offset(offset);
            if ((protectDreamScenes && DreamArchipelagoLayout.isReserved(world.getSeed(), pos))
                    || !world.getBlockState(pos).isAir())
                return false;
        }
        for (BlockPos offset : shape.blocks())
            world.setBlock(center.offset(offset), block.defaultBlockState(), Block.UPDATE_KNOWN_SHAPE);
        return true;
    }
}
