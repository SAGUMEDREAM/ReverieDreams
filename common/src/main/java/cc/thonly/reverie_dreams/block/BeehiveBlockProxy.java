package cc.thonly.reverie_dreams.block;

import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.entity.npc.NPCSimpleEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import net.minecraft.world.InteractionHand;

public interface BeehiveBlockProxy {
    default void reverie_dreams$onInteractUse(BaseNPCLikeEntity npc,
                                              ServerLevel level, BlockState state,
                                              BlockPos pos, InteractionHand hand) {
        reverie_dreams$onInteractUse(npc, level, state, pos);
    }

    void reverie_dreams$onInteractUse(BaseNPCLikeEntity roleEntity, ServerLevel serverLevel, BlockState blockState, @Nullable BlockPos currentTarget);
}
