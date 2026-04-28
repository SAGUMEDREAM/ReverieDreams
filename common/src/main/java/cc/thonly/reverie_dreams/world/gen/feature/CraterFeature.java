package cc.thonly.reverie_dreams.world.gen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

import java.util.function.Consumer;

public class CraterFeature extends Feature<CraterFeatureConfig> {
    public CraterFeature(Codec<CraterFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<CraterFeatureConfig> context) {
        WorldGenLevel structureWorldAccess = context.level();
        BlockPos blockPos = structureWorldAccess.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, context.origin()).below();
        RandomSource random = context.random();
        CraterFeatureConfig craterFeatureConfig = (CraterFeatureConfig) context.config();
        int i = craterFeatureConfig.radius().sample(random);
        int j = craterFeatureConfig.depth().sample(random);
        if (j > i) {
            return false;
        } else {
            int k = (j * j + i * i) / (2 * j);
            BlockPos blockPos2 = blockPos.above(k - j);
            BlockPos.MutableBlockPos mutable = blockPos.mutable();
            Consumer<LevelAccessor> consumer = (world) -> {
                for (int kx = -j; kx <= k; ++kx) {
                    boolean bl = false;

                    for (int l = -k; l <= k; ++l) {
                        for (int m = -k; m <= k; ++m) {
                            mutable.setWithOffset(blockPos, l, kx, m);
                            if (mutable.distSqr(blockPos2) < (double) (k * k) && !world.getBlockState(mutable).isAir()) {
                                bl = true;
                                world.setBlock(mutable, Blocks.AIR.defaultBlockState(), 3);
                            }
                        }
                    }

                    if (!bl && kx > 0) {
                        break;
                    }
                }

            };
            if (k < 15) {
                consumer.accept(structureWorldAccess);
            } else {
                ServerLevel serverWorld = structureWorldAccess.getLevel();
                serverWorld.getServer().execute(() -> {
                    consumer.accept(serverWorld);
                });
            }

            return true;
        }
    }
}
