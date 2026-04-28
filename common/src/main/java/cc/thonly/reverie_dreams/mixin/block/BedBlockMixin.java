package cc.thonly.reverie_dreams.mixin.block;

import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(BedBlock.class)
public abstract class BedBlockMixin extends HorizontalDirectionalBlock implements EntityBlock {
    protected BedBlockMixin(Properties settings) {
        super(settings);
    }

    @Inject(method = "useWithoutItem", at = @At("HEAD"))
    public void onUse(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir) {
        if (state.getValue(BedBlock.OCCUPIED)) {
            this.wakeNpc(world, pos);
        }
    }

    @Unique
    public boolean wakeNpc(Level world, BlockPos pos) {
        List<BaseNPCLikeEntity> list = world.getEntitiesOfClass(BaseNPCLikeEntity.class, new AABB(pos), LivingEntity::isSleeping);
        if (list.isEmpty()) {
            return false;
        }
        list.getFirst().stopSleeping();
        return true;
    }

}
