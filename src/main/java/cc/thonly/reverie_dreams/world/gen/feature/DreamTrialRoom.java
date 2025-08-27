package cc.thonly.reverie_dreams.world.gen.feature;

import cc.thonly.reverie_dreams.block.ModBlocks;
import com.mojang.serialization.Codec;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class DreamTrialRoom extends Feature<DreamTrialRoomConfig> {
    public DreamTrialRoom(Codec<DreamTrialRoomConfig> configCodec) {
        super(configCodec);
    }

    @Override
    public boolean generate(FeatureContext<DreamTrialRoomConfig> context) {
        StructureWorldAccess world = context.getWorld();
        Random random = context.getRandom();
        DreamTrialRoomConfig config = context.getConfig();

        // 房间原点
        int originX = context.getOrigin().getX();
        int originY = context.getOrigin().getY();
        int originZ = context.getOrigin().getZ();

        int sizeX = 16;
        int sizeY = 16;
        int sizeZ = 16;

        // 立方体房间
        for (int x = 0; x < sizeX; x++) {
            for (int y = 0; y < sizeY; y++) {
                for (int z = 0; z < sizeZ; z++) {

                    boolean isEdge = x == 0 || x == sizeX - 1 ||
                            y == 0 || y == sizeY - 1 ||
                            z == 0 || z == sizeZ - 1;

                    int worldX = originX + x;
                    int worldY = originY + y;
                    int worldZ = originZ + z;
                    BlockPos blockPos = new BlockPos(worldX, worldY, worldZ);
                    if (isEdge && world.getBlockState(blockPos).isAir()) {
                        // 放房间方块，例如梦境石
                        world.setBlockState(
                                new BlockPos(worldX, worldY, worldZ),
                                ModBlocks.DREAM_STONE_BRICK.block().getDefaultState(),
                                Block.FORCE_STATE
                        );
                    } else {
                        // 内部留空
                        continue;
                    }
                }
            }
        }

        // 放置刷怪笼在中心
        BlockPos spawnerPos = new BlockPos(
                originX + sizeX / 2,
                originY + 1,
                originZ + sizeZ / 2
        );

        world.setBlockState(spawnerPos,
                Blocks.SPAWNER.getDefaultState(),
                Block.FORCE_STATE
        );

        // 设置刷怪类型
        if (world.getBlockEntity(spawnerPos) instanceof MobSpawnerBlockEntity spawner) {
            EntityType<?> entityType = Registries.ENTITY_TYPE.get(config.entityTypeId());
            spawner.getLogic().setEntityId(entityType, null, random, spawnerPos);
        }

        return true;
    }

}
