package cc.thonly.reverie_dreams.polymer.item;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.item.base.IDanmakuItem;
import cc.thonly.reverie_dreams.item.danmaku.SpellcardArt;
import cc.thonly.reverie_dreams.item.danmaku.SpellcardItem;
import cc.thonly.reverie_dreams.item.prop.GensokyoCompassItem;
import cc.thonly.reverie_dreams.item.prop.Knife;
import cc.thonly.reverie_dreams.item.weapon.TrumpetGun;
import cc.thonly.reverie_dreams.item.weapon.WeaponOfTheMoon;
import cc.thonly.reverie_dreams.item.weapon.YoumuSwordUsing;
import cc.thonly.reverie_dreams.networking.ServerNetworkingHandlers;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponentTypes;
import eu.pb4.polymer.common.api.PolymerCommonUtils;
import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.core.api.utils.PolymerClientDecoded;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PolymerItemImpl implements PolymerItem, PolymerClientDecoded {
    private final Item item;

    public PolymerItemImpl(Item item) {
        this.item = item;
    }

    @Override
    public @Nullable Identifier getPolymerItemModel(ItemStack stack, PacketContext context, HolderLookup.Provider lookup) {
        var currentModel = stack.get(DataComponents.ITEM_MODEL);
        if (this.item instanceof SpellcardItem && currentModel != null
                && currentModel.equals(this.item.components().get(DataComponents.ITEM_MODEL))) {
            var art = SpellcardArt.itemModel(stack.get(RDDataComponentTypes.DANMAKU_PATTERN.value()));
            if (art != null) {
                return art;
            }
        }
        return currentModel;
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, PacketContext context) {
        ServerPlayer player = PolymerCommonUtils.getPlayer(context);
        if (ServerNetworkingHandlers.hasModOnClient(player) && ServerNetworkingHandlers.hasModWithVersion(player)) {
            return itemStack.getItem();
        }
        if (this.item instanceof WeaponOfTheMoon) {
            return Items.TRIAL_KEY;
        }
        if (this.item instanceof MobBucketItem) {
            return Items.COD_BUCKET;
        }
        if (this.item instanceof TrumpetGun || this.item instanceof Knife) {
            return Items.TRIAL_KEY;
        }
        if (this.item instanceof GensokyoCompassItem) {
            return Items.COMPASS;
        }
        if (this.item instanceof IDanmakuItem && ReverieDreams.config().enableDanmakuGlow) {
            return Items.TORCH;
        }
        // Keep ShieldItem identity: vanilla applies a different blocking pose to other items.
        if (this.item instanceof BlockItem) {
            return Items.RABBIT_FOOT;
        }
        return this.item instanceof ShieldItem ? Items.SHIELD : Items.TRIAL_KEY;
    }

    @Override
    public boolean isPolymerBlockInteraction(BlockState state, ServerPlayer player, InteractionHand hand, ItemStack stack, ServerLevel world, BlockHitResult blockHitResult, InteractionResult actionResult) {
        return actionResult.consumesAction();
    }

    @Override
    public boolean isIgnoringBlockInteractionPlaySoundExceptedEntity(BlockState state, ServerPlayer player, InteractionHand hand, ItemStack stack, ServerLevel world, BlockHitResult blockHitResult) {
        return this.item instanceof BlockItem;
    }

    @Override
    public void modifyBasePolymerItemStack(ItemStack out, ItemStack stack, PacketContext context, HolderLookup.Provider lookup) {
        PolymerItem.super.modifyBasePolymerItemStack(out, stack, context, lookup);
        if (this.item instanceof WeaponOfTheMoon weapon) {
            // Vanilla clients use the replacement item's durability bar for ammunition.
            out.set(DataComponents.MAX_DAMAGE, weapon.capacity());
            out.set(DataComponents.DAMAGE, weapon.capacity() - weapon.getRounds(stack));
            // A vanilla consumable can hold use without the bow item's hard-coded drawing pose.
            out.set(DataComponents.CONSUMABLE, new Consumable(3600, weapon.getUseAnimation(stack),
                    Holder.direct(SoundEvents.EMPTY), false, List.of()));
        }
        if (this.item instanceof YoumuSwordUsing) {
            out.set(DataComponents.CONSUMABLE, new Consumable(3600, this.item.getUseAnimation(stack),
                    Holder.direct(SoundEvents.EMPTY), false, List.of()));
        }
        var food = stack.get(DataComponents.FOOD);
        if (food != null) {
            out.set(DataComponents.FOOD, food);
        }
    }
}
