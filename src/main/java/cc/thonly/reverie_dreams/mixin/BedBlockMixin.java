package cc.thonly.reverie_dreams.mixin;

import cc.thonly.reverie_dreams.entity.npc.NPCEntityImpl;
import cc.thonly.reverie_dreams.interfaces.IDreamPillowManager;
import cc.thonly.reverie_dreams.item.prop.DreamPillowItem;
import cc.thonly.reverie_dreams.server.DreamPillowManager;
import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(BedBlock.class)
public abstract class BedBlockMixin extends HorizontalFacingBlock implements BlockEntityProvider {
    protected BedBlockMixin(Settings settings) {
        super(settings);
    }

    @Inject(method = "onUse", at = @At("HEAD"))
    public void onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir) {
        if (state.get(BedBlock.OCCUPIED)) {
            this.wakeNpc(world, pos);
        }
    }

    @Unique
    public boolean wakeNpc(World world, BlockPos pos) {
        List<NPCEntityImpl> list = world.getEntitiesByClass(NPCEntityImpl.class, new Box(pos), LivingEntity::isSleeping);
        if (list.isEmpty()) {
            return false;
        }
        list.getFirst().wakeUp();
        return true;
    }

    @Inject(method = "onBreak", at = @At("HEAD"))
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player, CallbackInfoReturnable<BlockState> cir) {
        if (!world.isClient && world instanceof ServerWorld serverWorld) {
            MinecraftServer server = serverWorld.getServer();
            IDreamPillowManager iDreamPillowManager = (IDreamPillowManager) server;
            DreamPillowManager dreamPillowManager = iDreamPillowManager.getDreamPillowManager();
            DreamPillowManager.WorldEntry worldEntry = dreamPillowManager.get(serverWorld);
            Pair<Boolean, BlockPos> bedHead = DreamPillowItem.getBedHead(serverWorld, pos);
            if (bedHead.getLeft()) {
                worldEntry.remove(bedHead.getRight());
            }
        }
    }
}
