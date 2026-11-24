package cc.thonly.reverie_dreams.world.dream.scene;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A deterministic, origin-relative scene. The top of the walking floor is at local y = 0.
 */
public record DreamScene(String id, Map<BlockPos, BlockState> blocks, BlockPos landing,
                         Map<Direction, BlockPos> exits, Map<BlockPos, ResourceKey<LootTable>> lootChests) {
    public DreamScene(String id, Map<BlockPos, BlockState> blocks, BlockPos landing, Map<Direction, BlockPos> exits) {
        this(id, blocks, landing, exits, Map.of());
    }

    public DreamScene {
        Objects.requireNonNull(id);
        Objects.requireNonNull(landing);
        if (blocks.isEmpty())
            throw new IllegalArgumentException("A dream scene must contain blocks");
        Map<BlockPos, BlockState> blockCopy = new LinkedHashMap<>();
        blocks.forEach((pos, state) -> blockCopy.put(pos.immutable(), Objects.requireNonNull(state)));
        blocks = Collections.unmodifiableMap(blockCopy);
        landing = landing.immutable();
        Map<Direction, BlockPos> exitCopy = new EnumMap<>(Direction.class);
        exits.forEach((direction, pos) -> {
            if (direction.getAxis().isVertical())
                throw new IllegalArgumentException("Scene exits must be horizontal");
            exitCopy.put(direction, pos.immutable());
        });
        exits = Collections.unmodifiableMap(exitCopy);
        Map<BlockPos, ResourceKey<LootTable>> chestCopy = new LinkedHashMap<>();
        for (var entry : lootChests.entrySet()) {
            BlockPos pos = entry.getKey().immutable();
            BlockState state = blockCopy.get(pos);
            if (state == null || !state.is(Blocks.CHEST) || state.getValue(ChestBlock.TYPE) != ChestType.SINGLE)
                throw new IllegalArgumentException("Loot markers must refer to single chests: " + pos);
            BlockPos front = pos.relative(state.getValue(ChestBlock.FACING));
            if (!airAt(blockCopy, pos.above()) || !airAt(blockCopy, front) || !airAt(blockCopy, front.above())
                    || airAt(blockCopy, pos.below()) || airAt(blockCopy, front.below()))
                throw new IllegalArgumentException("Dream chests need lid clearance and a supported approach: " + pos);
            chestCopy.put(pos, Objects.requireNonNull(entry.getValue()));
        }
        lootChests = Collections.unmodifiableMap(chestCopy);
    }

    private static boolean airAt(Map<BlockPos, BlockState> blocks, BlockPos pos) {
        BlockState state = blocks.get(pos);
        return state == null || state.isAir();
    }

    public BlockPos min() {
        int x = Integer.MAX_VALUE, y = Integer.MAX_VALUE, z = Integer.MAX_VALUE;
        for (BlockPos pos : blocks.keySet()) {
            x = Math.min(x, pos.getX());
            y = Math.min(y, pos.getY());
            z = Math.min(z, pos.getZ());
        }
        return new BlockPos(x, y, z);
    }

    public BlockPos max() {
        int x = Integer.MIN_VALUE, y = Integer.MIN_VALUE, z = Integer.MIN_VALUE;
        for (BlockPos pos : blocks.keySet()) {
            x = Math.max(x, pos.getX());
            y = Math.max(y, pos.getY());
            z = Math.max(z, pos.getZ());
        }
        return new BlockPos(x, y, z);
    }
}
