package cc.thonly.reverie_dreams.world.gen.feature;

import cc.thonly.reverie_dreams.registry.content.block.RDWoodBlocks;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class FloatingIslandFeature extends Feature<NoneFeatureConfiguration> {
    public FloatingIslandFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        LevelAccessor world = ctx.level();
        // 顶面海拔：100~160
        BlockPos topPlane = ctx.origin().above(100 + world.getRandom().nextInt(60));

        // 生成几个“半椭球”并叠加，顶面统一用同一平面（oy = 0 保证平顶）
        for (int i = 0; i < 5; i++) {
            int rx = 8 + world.getRandom().nextInt(6);
            int ry = 4 + world.getRandom().nextInt(3);
            int rz = 8 + world.getRandom().nextInt(6);

            int ox = world.getRandom().nextInt(6) - 3;
            int oz = world.getRandom().nextInt(6) - 3;

            // 顶面中心位置（同一个水平面）
            BlockPos lobeTopCenter = topPlane.offset(ox, 0, oz);
            generateHemisphereDownFlatTop(world, lobeTopCenter, rx, ry, rz);
            decorateTop(world, lobeTopCenter, rx, rz, world.getRandom());
        }
        return true;
    }

    private void sprinkleGrass(LevelAccessor world, BlockPos topCenter, int rx, int rz, RandomSource random) {
        for (int x = -rx; x <= rx; x++) {
            for (int z = -rz; z <= rz; z++) {
                if (random.nextFloat() < 0.1f) { // 20% 概率额外放草
                    BlockPos pos = topCenter.offset(x, 0, z);
                    if (world.getBlockState(pos).is(Blocks.GRASS_BLOCK) && world.isEmptyBlock(pos.above())) {
                        world.setBlock(pos.above(), Blocks.TALL_GRASS.defaultBlockState(), 3);
                    }
                }
            }
        }
    }


    private void decorateTop(LevelAccessor world, BlockPos topCenter, int rx, int rz, RandomSource random) {
        int decorations = 5 + random.nextInt(8); // 每个岛 5~12 个装饰
        int saplingCount = 0; // 限制树苗数量

        for (int i = 0; i < decorations; i++) {
            int x = random.nextInt(rx * 2 + 1) - rx;
            int z = random.nextInt(rz * 2 + 1) - rz;
            BlockPos pos = topCenter.offset(x, 0, z);
            if (!world.getBlockState(pos).is(Blocks.GRASS_BLOCK)) continue;
            BlockPos above = pos.above();
            if (!world.isEmptyBlock(above)) continue;

            int roll = random.nextInt(4);
            switch (roll) {
                case 0 -> {
                }
                case 1 -> {
                    world.setBlock(above,
                            random.nextBoolean() ? RDWoodBlocks.UDUMBARA_FLOWER.defaultBlockState() : Blocks.POPPY.defaultBlockState(), 3);
                }
                case 2 -> {
                    if (saplingCount < 2) {
                        world.setBlock(above, Blocks.OAK_SAPLING.defaultBlockState(), 3);
                        saplingCount++;
                    }
                }
                case 3 -> {
                    // 小水塘（2×2 或 3×3）
                    int size = random.nextBoolean() ? 2 : 3;
                    for (int dx = 0; dx < size; dx++) {
                        for (int dz = 0; dz < size; dz++) {
                            BlockPos pondPos = pos.offset(dx, 0, dz);
                            if (world.getBlockState(pondPos).is(Blocks.GRASS_BLOCK)) {
                                world.setBlock(pondPos, Blocks.WATER.defaultBlockState(), 3);
                            }
                        }
                    }
                }
            }
        }
        sprinkleGrass(world, topCenter, rx, rz, random);
    }



    /**
     * 以 topCenter 为顶面中心（y=0），向“下方”生成半椭球。
     * 顶面整片为草；往下 2 层为泥土；更下为石头。
     */
    private void generateHemisphereDownFlatTop(LevelAccessor world, BlockPos topCenter, int rx, int ry, int rz) {
        // 遍历顶面投影的椭圆盘
        for (int x = -rx; x <= rx; x++) {
            for (int z = -rz; z <= rz; z++) {
                // 先判断 (x,z) 是否落在椭圆投影内： (x/rx)^2 + (z/rz)^2 <= 1
                double proj = (x * x) / (double) (rx * rx) + (z * z) / (double) (rz * rz);
                if (proj > 1.0) continue;

                // 该 (x,z) 方向上半椭球的最大“深度”（向下的 y 范围）
                // 椭球方程：x^2/rx^2 + y^2/ry^2 + z^2/rz^2 <= 1
                // 给定 x,z，得到 |y| <= ry * sqrt(1 - proj)
                int maxDepth = (int) Math.floor(ry * Math.sqrt(1.0 - proj));
                if (maxDepth <= 0) continue;

                // 从顶面 y=0 向下铺到 y = -maxDepth
                for (int dy = 0; dy >= -maxDepth; dy--) {
                    BlockPos pos = topCenter.offset(x, dy, z);

                    if (dy == 0) {
                        // 顶面：整片草；如果你想避免覆盖已有方块，可先判断 isAir
                        world.setBlock(pos, Blocks.GRASS_BLOCK.defaultBlockState(), 3);
                    } else if (dy >= -2) {
                        world.setBlock(pos, Blocks.DIRT.defaultBlockState(), 3);
                    } else {
                        world.setBlock(pos, Blocks.STONE.defaultBlockState(), 3);
                    }
                }
            }
        }
    }
}
