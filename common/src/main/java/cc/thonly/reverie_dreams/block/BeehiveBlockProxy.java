package cc.thonly.reverie_dreams.block;

import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.entity.npc.NPCSimpleEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public interface BeehiveBlockProxy {
    void reverie_dreams$onInteractUse(BaseNPCLikeEntity roleEntity, ServerLevel serverLevel, BlockState blockState, @Nullable BlockPos currentTarget);
}
