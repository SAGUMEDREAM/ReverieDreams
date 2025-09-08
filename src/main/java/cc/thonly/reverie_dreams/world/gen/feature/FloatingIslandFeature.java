package cc.thonly.reverie_dreams.world.gen.feature;

import cc.thonly.mystias_izakaya.block.MIBlocks;
import com.mojang.serialization.Codec;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class FloatingIslandFeature extends Feature<DefaultFeatureConfig> {
    public FloatingIslandFeature(Codec<DefaultFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public boolean generate(FeatureContext<DefaultFeatureConfig> ctx) {
        WorldAccess world = ctx.getWorld();
        // 顶面海拔：100~160
        BlockPos topPlane = ctx.getOrigin().up(100 + world.getRandom().nextInt(60));

        // 生成几个“半椭球”并叠加，顶面统一用同一平面（oy = 0 保证平顶）
        for (int i = 0; i < 5; i++) {
            int rx = 8 + world.getRandom().nextInt(6);
            int ry = 4 + world.getRandom().nextInt(3);
            int rz = 8 + world.getRandom().nextInt(6);

            int ox = world.getRandom().nextInt(6) - 3;
            int oz = world.getRandom().nextInt(6) - 3;

            // 顶面中心位置（同一个水平面）
            BlockPos lobeTopCenter = topPlane.add(ox, 0, oz);
            generateHemisphereDownFlatTop(world, lobeTopCenter, rx, ry, rz);
            decorateTop(world, lobeTopCenter, rx, rz, world.getRandom());
        }
        return true;
    }

    private void sprinkleGrass(WorldAccess world, BlockPos topCenter, int rx, int rz, Random random) {
        for (int x = -rx; x <= rx; x++) {
            for (int z = -rz; z <= rz; z++) {
                if (random.nextFloat() < 0.1f) { // 20% 概率额外放草
                    BlockPos pos = topCenter.add(x, 0, z);
                    if (world.getBlockState(pos).isOf(Blocks.GRASS_BLOCK) && world.isAir(pos.up())) {
                        world.setBlockState(pos.up(), Blocks.TALL_GRASS.getDefaultState(), 3);
                    }
                }
            }
        }
    }


    private void decorateTop(WorldAccess world, BlockPos topCenter, int rx, int rz, Random random) {
        int decorations = 5 + random.nextInt(8); // 每个岛 5~12 个装饰
        int saplingCount = 0; // 限制树苗数量

        for (int i = 0; i < decorations; i++) {
            int x = random.nextInt(rx * 2 + 1) - rx;
            int z = random.nextInt(rz * 2 + 1) - rz;
            BlockPos pos = topCenter.add(x, 0, z);
            if (!world.getBlockState(pos).isOf(Blocks.GRASS_BLOCK)) continue;
            BlockPos above = pos.up();
            if (!world.isAir(above)) continue;

            int roll = random.nextInt(4);
            switch (roll) {
                case 0 -> {
                }
                case 1 -> {
                    world.setBlockState(above,
                            random.nextBoolean() ? MIBlocks.UDUMBARA_FLOWER.getDefaultState() : Blocks.POPPY.getDefaultState(), 3);
                }
                case 2 -> {
                    if (saplingCount < 2) {
                        world.setBlockState(above, Blocks.OAK_SAPLING.getDefaultState(), 3);
                        saplingCount++;
                    }
                }
                case 3 -> {
                    // 小水塘（2×2 或 3×3）
                    int size = random.nextBoolean() ? 2 : 3;
                    for (int dx = 0; dx < size; dx++) {
                        for (int dz = 0; dz < size; dz++) {
                            BlockPos pondPos = pos.add(dx, 0, dz);
                            if (world.getBlockState(pondPos).isOf(Blocks.GRASS_BLOCK)) {
                                world.setBlockState(pondPos, Blocks.WATER.getDefaultState(), 3);
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
    private void generateHemisphereDownFlatTop(WorldAccess world, BlockPos topCenter, int rx, int ry, int rz) {
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
                    BlockPos pos = topCenter.add(x, dy, z);

                    if (dy == 0) {
                        // 顶面：整片草；如果你想避免覆盖已有方块，可先判断 isAir
                        world.setBlockState(pos, Blocks.GRASS_BLOCK.getDefaultState(), 3);
                    } else if (dy >= -2) {
                        world.setBlockState(pos, Blocks.DIRT.getDefaultState(), 3);
                    } else {
                        world.setBlockState(pos, Blocks.STONE.getDefaultState(), 3);
                    }
                }
            }
        }
    }
}
