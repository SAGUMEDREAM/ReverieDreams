package cc.thonly.reverie_dreams.entity.ai.work;

import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.List;

public final class WorkWorld {
    private WorkWorld() {
    }

    public static List<BlockEntity> blockEntities(ServerLevel level, WorkArea area) {
        var result = new ArrayList<BlockEntity>();
        for (int x = SectionPos.blockToSectionCoord(area.center().getX() - area.radius()); x <= SectionPos.blockToSectionCoord(area.center().getX() + area.radius()); x++)
            for (int z = SectionPos.blockToSectionCoord(area.center().getZ() - area.radius()); z <= SectionPos.blockToSectionCoord(area.center().getZ() + area.radius()); z++) {
                var chunk = level.getChunkSource().getChunkNow(x, z);
                if (chunk == null)
                    continue;
                for (var block : List.copyOf(chunk.getBlockEntities().values()))
                    if (!block.isRemoved() && area.contains(block.getBlockPos()))
                        result.add(block);
            }
        return result;
    }
}
