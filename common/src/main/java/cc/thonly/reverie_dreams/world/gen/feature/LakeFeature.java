package cc.thonly.reverie_dreams.world.gen.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

@SuppressWarnings("deprecation")
public class LakeFeature extends Feature<LakeFeature.Configuration> {
    private static final BlockState AIR;

    public LakeFeature(Codec<Configuration> codec) {
        super(codec);
    }

    public boolean place(FeaturePlaceContext<Configuration> context) {
        BlockPos origin = context.origin();
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        Configuration config = (Configuration)context.config();
        if (origin.getY() <= level.getMinY() + 4) {
            return false;
        } else {
            origin = origin.offset(-8, -4, -8);
            boolean[] grid = new boolean[2048];
            int spots = random.nextInt(4) + 4;

            for(int i = 0; i < spots; ++i) {
                double xr = random.nextDouble() * (double)6.0F + (double)3.0F;
                double yr = random.nextDouble() * (double)4.0F + (double)2.0F;
                double zr = random.nextDouble() * (double)6.0F + (double)3.0F;
                double xp = random.nextDouble() * ((double)16.0F - xr - (double)2.0F) + (double)1.0F + xr / (double)2.0F;
                double yp = random.nextDouble() * ((double)8.0F - yr - (double)4.0F) + (double)2.0F + yr / (double)2.0F;
                double zp = random.nextDouble() * ((double)16.0F - zr - (double)2.0F) + (double)1.0F + zr / (double)2.0F;

                for(int xx = 1; xx < 15; ++xx) {
                    for(int zz = 1; zz < 15; ++zz) {
                        for(int yy = 1; yy < 7; ++yy) {
                            double xd = ((double)xx - xp) / (xr / (double)2.0F);
                            double yd = ((double)yy - yp) / (yr / (double)2.0F);
                            double zd = ((double)zz - zp) / (zr / (double)2.0F);
                            double d = xd * xd + yd * yd + zd * zd;
                            if (d < (double)1.0F) {
                                grid[(xx * 16 + zz) * 8 + yy] = true;
                            }
                        }
                    }
                }
            }

            BlockState fluid = config.fluid().getState(level, random, origin);

            for(int xx = 0; xx < 16; ++xx) {
                for(int zz = 0; zz < 16; ++zz) {
                    for(int yyx = 0; yyx < 8; ++yyx) {
                        boolean check = !grid[(xx * 16 + zz) * 8 + yyx] && (xx < 15 && grid[((xx + 1) * 16 + zz) * 8 + yyx] || xx > 0 && grid[((xx - 1) * 16 + zz) * 8 + yyx] || zz < 15 && grid[(xx * 16 + zz + 1) * 8 + yyx] || zz > 0 && grid[(xx * 16 + (zz - 1)) * 8 + yyx] || yyx < 7 && grid[(xx * 16 + zz) * 8 + yyx + 1] || yyx > 0 && grid[(xx * 16 + zz) * 8 + (yyx - 1)]);
                        if (check) {
                            BlockState blockState = level.getBlockState(origin.offset(xx, yyx, zz));
                            if (yyx >= 4 && blockState.liquid()) {
                                return false;
                            }

                            if (yyx < 4 && !blockState.isSolid() && level.getBlockState(origin.offset(xx, yyx, zz)) != fluid) {
                                return false;
                            }
                        }
                    }
                }
            }

            for(int xx = 0; xx < 16; ++xx) {
                for(int zz = 0; zz < 16; ++zz) {
                    for(int yyxx = 0; yyxx < 8; ++yyxx) {
                        if (grid[(xx * 16 + zz) * 8 + yyxx]) {
                            BlockPos placePos = origin.offset(xx, yyxx, zz);
                            if (this.canReplaceBlock(level.getBlockState(placePos))) {
                                boolean placeAir = yyxx >= 4;
                                level.setBlock(placePos, placeAir ? AIR : fluid, 2);
                                if (placeAir) {
                                    level.scheduleTick(placePos, AIR.getBlock(), 0);
                                    this.markAboveForPostProcessing(level, placePos);
                                }
                            }
                        }
                    }
                }
            }

            BlockState barrier = config.barrier().getState(level, random, origin);
            if (!barrier.isAir()) {
                for(int xx = 0; xx < 16; ++xx) {
                    for(int zz = 0; zz < 16; ++zz) {
                        for(int yyxxx = 0; yyxxx < 8; ++yyxxx) {
                            boolean check = !grid[(xx * 16 + zz) * 8 + yyxxx] && (xx < 15 && grid[((xx + 1) * 16 + zz) * 8 + yyxxx] || xx > 0 && grid[((xx - 1) * 16 + zz) * 8 + yyxxx] || zz < 15 && grid[(xx * 16 + zz + 1) * 8 + yyxxx] || zz > 0 && grid[(xx * 16 + (zz - 1)) * 8 + yyxxx] || yyxxx < 7 && grid[(xx * 16 + zz) * 8 + yyxxx + 1] || yyxxx > 0 && grid[(xx * 16 + zz) * 8 + (yyxxx - 1)]);
                            if (check && (yyxxx < 4 || random.nextInt(2) != 0)) {
                                BlockState blockStatex = level.getBlockState(origin.offset(xx, yyxxx, zz));
                                if (blockStatex.isSolid() && !blockStatex.is(BlockTags.LAVA_POOL_STONE_CANNOT_REPLACE)) {
                                    BlockPos barrierPos = origin.offset(xx, yyxxx, zz);
                                    level.setBlock(barrierPos, barrier, 2);
                                    this.markAboveForPostProcessing(level, barrierPos);
                                }
                            }
                        }
                    }
                }
            }

//            if (fluid.getFluidState().is(FluidTags.WATER)) {
//                for(int xx = 0; xx < 16; ++xx) {
//                    for(int zz = 0; zz < 16; ++zz) {
//                        int yyxxxx = 4;
//                        BlockPos offset = origin.offset(xx, 4, zz);
//                        if (((Biome)level.getBiome(offset).value()).shouldFreeze(level, offset, false) && this.canReplaceBlock(level.getBlockState(offset))) {
//                            level.setBlock(offset, Blocks.ICE.defaultBlockState(), 2);
//                        }
//                    }
//                }
//            }

            return true;
        }
    }

    private boolean canReplaceBlock(BlockState state) {
        return !state.is(BlockTags.FEATURES_CANNOT_REPLACE);
    }

    static {
        AIR = Blocks.CAVE_AIR.defaultBlockState();
    }

    public static record Configuration(BlockStateProvider fluid, BlockStateProvider barrier) implements FeatureConfiguration {
        public static final Codec<Configuration> CODEC = RecordCodecBuilder.create((i) -> i.group(BlockStateProvider.CODEC.fieldOf("fluid").forGetter(Configuration::fluid), BlockStateProvider.CODEC.fieldOf("barrier").forGetter(Configuration::barrier)).apply(i, Configuration::new));
    }
}
