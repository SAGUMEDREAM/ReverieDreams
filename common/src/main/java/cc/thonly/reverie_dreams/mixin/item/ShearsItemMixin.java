package cc.thonly.reverie_dreams.mixin.item;

import cc.thonly.reverie_dreams.registry.content.block.RDWoodBlocks;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShearsItem.class)
public class ShearsItemMixin {
    @Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
    public void useOnBlessedSpiritualLog(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        Player player = context.getPlayer();
        if (player == null) {
            return;
        }
        InteractionHand hand = context.getHand();
        ItemStack itemStack = player.getItemInHand(hand);
        Level level = context.getLevel();
        BlockPos blockPos = context.getClickedPos();
        BlockState blockState = level.getBlockState(blockPos);
        if (blockState.is(RDWoodBlocks.BLESSED_SPIRITUAL_LOG)) {
            if (!level.isClientSide()) {
                level.setBlock(blockPos, RDWoodBlocks.SPIRITUAL_BUNDLE.strippedLog().asBlock().withPropertiesOf(blockState), Block.UPDATE_KNOWN_SHAPE);
                itemStack.hurtWithoutBreaking(1, player);
                CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger((ServerPlayer) player, blockPos, itemStack);
                level.playSound(null, blockPos, SoundEvents.GROWING_PLANT_CROP, SoundSource.BLOCKS, 1.0f, 1.0f);
                cir.setReturnValue(InteractionResult.SUCCESS_SERVER);
                cir.cancel();
            } else {
                cir.setReturnValue(InteractionResult.SUCCESS);
                cir.cancel();
            }
        } else {
            return;
        }

    }
}
