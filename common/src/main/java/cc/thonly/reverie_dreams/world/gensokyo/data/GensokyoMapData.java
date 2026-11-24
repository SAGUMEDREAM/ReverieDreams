package cc.thonly.reverie_dreams.world.gensokyo.data;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

import cc.thonly.reverie_dreams.world.gensokyo.road.GensokyoRoadNetwork;

/**
 * 编译后的固定地图数据。运行时只读，不直接对应源 PNG/开发世界。
 */
public record GensokyoMapData(
        int formatVersion,
        int mapVersion,
        Identifier id,
        int width,
        int depth,
        int originX,
        int originZ,
        int minY,
        int worldHeight,
        int seaLevel,
        int dirtDepth,
        int biomeTransition,
        Spawn spawn,
        List<Identifier> biomePalette,
        List<BlockState> surfacePalette,
        short[] heights,
        byte[] biomes,
        short[] surfaces,
        List<Anchor> anchors,
        Map<Long, List<UndergroundBlock>> underground,
        GensokyoRoadNetwork roads
) {
    public static final int NO_SURFACE = Short.MIN_VALUE;

    public GensokyoMapData(int formatVersion, int mapVersion, Identifier id, int width, int depth, int originX, int originZ,
                           int minY, int worldHeight, int seaLevel, int dirtDepth, int biomeTransition, Spawn spawn,
                           List<Identifier> biomePalette, List<BlockState> surfacePalette, short[] heights, byte[] biomes,
                           short[] surfaces, List<Anchor> anchors, Map<Long, List<UndergroundBlock>> underground) {
        this(formatVersion, mapVersion, id, width, depth, originX, originZ, minY, worldHeight, seaLevel, dirtDepth,
                biomeTransition, spawn, biomePalette, surfacePalette, heights, biomes, surfaces, anchors, underground,
                GensokyoRoadNetwork.EMPTY);
    }

    public int index(int worldX, int worldZ) {
        return (worldX - this.originX) + (worldZ - this.originZ) * this.width;
    }

    public boolean contains(int worldX, int worldZ) {
        int x = worldX - this.originX;
        int z = worldZ - this.originZ;
        return x >= 0 && x < this.width && z >= 0 && z < this.depth;
    }

    public int heightAt(int worldX, int worldZ) {
        return this.contains(worldX, worldZ) ? this.heights[this.index(worldX, worldZ)] : NO_SURFACE;
    }

    public int biomeIndexAt(int worldX, int worldZ) {
        return this.contains(worldX, worldZ) ? this.biomes[this.index(worldX, worldZ)] & 0xFF : 0;
    }

    public int surfaceIndexAt(int worldX, int worldZ) {
        return this.contains(worldX, worldZ) ? this.surfaces[this.index(worldX, worldZ)] & 0xFFFF : 0;
    }

    public BlockState surfaceStateAt(int worldX, int worldZ) {
        if (!this.contains(worldX, worldZ)) {
            return Blocks.AIR.defaultBlockState();
        }
        int index = this.surfaceIndexAt(worldX, worldZ);
        return index >= 0 && index < this.surfacePalette.size()
                ? this.surfacePalette.get(index)
                : Blocks.AIR.defaultBlockState();
    }

    public BlockPos spawnPos() {
        return this.spawn.pos();
    }

    public List<UndergroundBlock> undergroundAt(ChunkPos pos) {
        return this.underground.getOrDefault(pos.pack(), List.of());
    }

    public record UndergroundBlock(int x, int y, int z, BlockState state) {
    }

    public record Spawn(BlockPos pos, float yaw, float pitch) {
    }

    public record Anchor(
            Identifier id,
            String type,
            @Nullable Identifier template,
            @Nullable Identifier structure,
            BlockPos pos,
            String rotation,
            String mirror,
            float integrity,
            float scale
    ) {
    }
}
