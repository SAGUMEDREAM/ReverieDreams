package cc.thonly.polymer.item;

import cc.thonly.reverie_dreams.config.ReverieDreamsConfiguration;
import cc.thonly.reverie_dreams.item.base.IDanmakuItem;
import cc.thonly.reverie_dreams.item.prop.Knife;
import cc.thonly.reverie_dreams.item.prop.TenguShieldItem;
import cc.thonly.reverie_dreams.item.weapon.TrumpetGun;
import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.core.api.utils.PolymerClientDecoded;
import eu.pb4.polymer.core.api.utils.PolymerKeepModel;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import xyz.nucleoid.packettweaker.PacketContext;

public class PolymerItemImpl implements PolymerItem, PolymerClientDecoded, PolymerKeepModel {
    private final Item item;

    public PolymerItemImpl(Item item) {
        this.item = item;
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, PacketContext packetContext) {
        if (this.item instanceof TrumpetGun || this.item instanceof Knife) {
            return Items.TRIAL_KEY;
        }
        if (this.item instanceof IDanmakuItem && ReverieDreamsConfiguration.ENABLE_DANMAKU_GLOW) {
            return Items.TORCH;
        }
        if (this.item instanceof TenguShieldItem) {
            return Items.TRIAL_KEY;
        }
        if (this.item instanceof BlockItem) {
            return Items.RABBIT_FOOT;
        }
        return this.item instanceof ShieldItem ? Items.SHIELD : Items.TRIAL_KEY;
    }

    @Override
    public boolean isPolymerBlockInteraction(BlockState state, ServerPlayer player, InteractionHand hand, ItemStack stack, ServerLevel world, BlockHitResult blockHitResult, InteractionResult actionResult) {
        return actionResult.consumesAction();
    }

//    @Override
//    public boolean isIgnoringBlockInteractionPlaySoundExceptedEntity(BlockState state, ServerPlayer player, InteractionHand hand, ItemStack stack, ServerLevel world, BlockHitResult blockHitResult) {
//        return this.item instanceof BlockItem;
//    }

//    @Override
//    public void modifyBasePolymerItemStack(ItemStack out, ItemStack stack, PacketContext context) {
//        out.set(DataComponentTypes.TOOLTIP_DISPLAY, out.getOrDefault(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplayComponent.DEFAULT));
//    }

}
