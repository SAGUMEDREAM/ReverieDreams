package cc.thonly.reverie_dreams.world.gen.feature;

import cc.thonly.reverie_dreams.server.DelayedTask;
import com.mojang.serialization.Codec;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class FloatingSphereFeature extends Feature<FloatingSphereFeatureConfig> {
    public FloatingSphereFeature(Codec<FloatingSphereFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public boolean generate(FeatureContext<FloatingSphereFeatureConfig> context) {
        StructureWorldAccess world = context.getWorld();
        BlockPos origin = context.getOrigin();
        Random random = context.getRandom();
        FloatingSphereFeatureConfig config = context.getConfig();

        BlockPos center = origin.up(random.nextInt(config.maxY() - config.minY() + 1) + config.minY());

        int radius = Math.min(config.minRadius() + random.nextInt(config.maxRadius() - config.minRadius() + 1), 6);
        Block block = Registries.BLOCK.get(config.blockId());

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (dx * dx + dy * dy + dz * dz <= radius * radius) {
                        BlockPos pos = center.add(dx, dy, dz);
                        if (!world.isChunkLoaded(pos)) {
                            DelayedTask.whenTick(world.getServer(), () -> world.isChunkLoaded(pos),2, ()-> {
                                if (world.getBlockState(pos).isAir()) {
                                    world.setBlockState(pos, block.getDefaultState(), Block.FORCE_STATE);
                                }
                            }, ()->{

                            });
                            continue;
                        }
                        if (world.getBlockState(pos).isAir()) {
                            world.setBlockState(pos, block.getDefaultState(), Block.FORCE_STATE);
                        }
                    }
                }
            }
        }

        return true;
    }
}
