package cc.thonly.reverie_dreams.world.gen.feature;

import cc.thonly.reverie_dreams.server.DelayedTask;
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

        BlockPos center = origin.above(random.nextInt(config.maxY() - config.minY() + 1) + config.minY());

        int radius = Math.min(config.minRadius() + random.nextInt(config.maxRadius() - config.minRadius() + 1), 6);
        Block block = BuiltInRegistries.BLOCK.getValue(config.blockId());

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (dx * dx + dy * dy + dz * dz <= radius * radius) {
                        BlockPos pos = center.offset(dx, dy, dz);
                        if (!world.hasChunkAt(pos)) {
                            DelayedTask.whenTick(world.getServer(), () -> world.hasChunkAt(pos),2, ()-> {
                                if (world.getBlockState(pos).isAir()) {
                                    world.setBlock(pos, block.defaultBlockState(), Block.UPDATE_KNOWN_SHAPE);
                                }
                            }, ()->{

                            });
                            continue;
                        }
                        if (world.getBlockState(pos).isAir()) {
                            world.setBlock(pos, block.defaultBlockState(), Block.UPDATE_KNOWN_SHAPE);
                        }
                    }
                }
            }
        }

        return true;
    }
}
