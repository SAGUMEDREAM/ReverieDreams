package cc.thonly.reverie_dreams.world.gen.feature;

import cc.thonly.reverie_dreams.block.ModBlocks;
import cc.thonly.reverie_dreams.datagen.ModChestLootTableProvider;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

public class DreamTrialRoom extends Feature<DreamTrialRoomConfig> {
    public DreamTrialRoom(Codec<DreamTrialRoomConfig> configCodec) {
        super(configCodec);
    }

    @Override
    public boolean place(FeaturePlaceContext<DreamTrialRoomConfig> context) {
        WorldGenLevel world = context.level();
        RandomSource random = context.random();
        DreamTrialRoomConfig config = context.config();

        int originX = context.origin().getX();
        int originY = context.origin().getY();
        int originZ = context.origin().getZ();

        int sizeX = 16;
        int sizeY = 16;
        int sizeZ = 16;

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
                        world.setBlock(
                                new BlockPos(worldX, worldY, worldZ),
                                ModBlocks.DREAM_STONE_BRICK.block().defaultBlockState(),
                                Block.UPDATE_KNOWN_SHAPE
                        );
                    } else {
                        // 内部留空
                        continue;
                    }
                }
            }
        }

        // 放置刷怪笼在中心
        for (int i = 0; i < 2; i++) {
            int i1 = random.nextIntBetweenInclusive(1, 2 + i);
            BlockPos spawnerPos = new BlockPos(
                    i1 + originX + sizeX / 2,
                    originY + 1,
                    i1 + originZ + sizeZ / 2
            );

            world.setBlock(spawnerPos,
                    Blocks.SPAWNER.defaultBlockState(),
                    Block.UPDATE_KNOWN_SHAPE
            );

            if (world.getBlockEntity(spawnerPos) instanceof SpawnerBlockEntity spawner) {
                EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.getValue(config.entityTypeId());
                spawner.getSpawner().setEntityId(entityType, null, random, spawnerPos);
            }
        }

        BlockPos boxPos = new BlockPos(originX + sizeX / 2 + random.nextIntBetweenInclusive(1, 2), originY + 1, originZ + sizeZ / 2 + random.nextIntBetweenInclusive(1, 2));
        world.setBlock(boxPos,
                Blocks.CHEST.defaultBlockState(),
                Block.UPDATE_KNOWN_SHAPE);
        if (world.getBlockEntity(boxPos) instanceof ChestBlockEntity chestBlockEntity) {
            chestBlockEntity.setLootTable(ModChestLootTableProvider.DREAM_CHEST);
        }

        return true;
    }

}
