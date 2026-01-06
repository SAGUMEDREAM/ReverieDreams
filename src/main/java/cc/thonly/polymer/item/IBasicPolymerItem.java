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

public interface IBasicPolymerItem extends PolymerItem, PolymerClientDecoded, PolymerKeepModel {

    @Override
    default Item getPolymerItem(ItemStack itemStack, PacketContext packetContext) {
        if (this instanceof TrumpetGun || this instanceof Knife) {
            return Items.TRIAL_KEY;
        }
        if (this instanceof IDanmakuItem && ReverieDreamsConfiguration.ENABLE_DANMAKU_GLOW) {
            return Items.TORCH;
        }
        if (this instanceof TenguShieldItem) {
            return Items.TRIAL_KEY;
        }
        if (this instanceof BlockItem) {
            return Items.RABBIT_FOOT;
        }
        return this instanceof ShieldItem ? Items.SHIELD : Items.TRIAL_KEY;
    }

    @Override
    default boolean isPolymerBlockInteraction(BlockState state, ServerPlayer player, InteractionHand hand, ItemStack stack, ServerLevel world, BlockHitResult blockHitResult, InteractionResult actionResult) {
        return actionResult.consumesAction();
    }


}
