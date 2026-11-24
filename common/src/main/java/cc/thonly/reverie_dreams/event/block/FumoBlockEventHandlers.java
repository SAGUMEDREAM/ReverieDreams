package cc.thonly.reverie_dreams.event.block;

import cc.thonly.reverie_dreams.block.base.BaseFumoBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;

/**
 * Fumo callbacks subscribed by the common gameplay event entry point.
 */
public final class FumoBlockEventHandlers {
    private FumoBlockEventHandlers() {
    }

    public static void onChunkLoad(LevelChunk chunk, Level level, boolean newChunk) {
        restoreBlockEntities(chunk);
    }

    // Vanilla bypasses block interaction while sneaking with either hand occupied.
    // Handle it before that bypass so holding a block/item cannot place or consume it instead.
    public static InteractionResult onRightClickBlock(Player player, InteractionHand hand, BlockPos pos, Direction face) {
        if (!player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        Level level = player.level();
        var state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof BaseFumoBlock fumo)) {
            return InteractionResult.PASS;
        }
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.CONSUME;
        }
        return fumo.rotateFumo(state, level, pos, player);
    }

    // Old chunks contain Fumo block states but no saved block entities. Repair only
    // sections containing Fumo, on both server load and receipt of a client chunk.
    public static void restoreBlockEntities(LevelChunk chunk) {
        boolean restored = false;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        LevelChunkSection[] sections = chunk.getSections();
        for (int sectionIndex = 0; sectionIndex < sections.length; sectionIndex++) {
            LevelChunkSection section = sections[sectionIndex];
            if (!section.maybeHas(state -> state.getBlock() instanceof BaseFumoBlock)) {
                continue;
            }
            int baseY = chunk.getSectionYFromSectionIndex(sectionIndex) << 4;
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    for (int x = 0; x < 16; x++) {
                        if (section.getBlockState(x, y, z).getBlock() instanceof BaseFumoBlock) {
                            pos.set(chunk.getPos().getMinBlockX() + x, baseY + y, chunk.getPos().getMinBlockZ() + z);
                            if (chunk.getBlockEntity(pos, LevelChunk.EntityCreationType.CHECK) == null
                                    && chunk.getBlockEntity(pos, LevelChunk.EntityCreationType.IMMEDIATE) != null) {
                                restored = true;
                            }
                        }
                    }
                }
            }
        }
        if (restored && !chunk.getLevel().isClientSide()) {
            chunk.markUnsaved();
        }
    }
}
