package cc.thonly.reverie_dreams.polymer.item;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.data.FoodProperty;
import cc.thonly.reverie_dreams.item.base.IDanmakuItem;
import cc.thonly.reverie_dreams.item.prop.Knife;
import cc.thonly.reverie_dreams.item.prop.TenguShieldItem;
import cc.thonly.reverie_dreams.item.weapon.TrumpetGun;
import cc.thonly.reverie_dreams.item.weapon.WeaponOfTheMoon;
import cc.thonly.reverie_dreams.networking.ServerNetworkingHandlers;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponents;
import eu.pb4.polymer.common.api.PolymerCommonUtils;
import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.core.api.utils.PolymerClientDecoded;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.Connection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;

import java.util.List;
import java.util.Objects;

public class PolymerItemImpl implements PolymerItem, PolymerClientDecoded {
    private final Item item;

    public PolymerItemImpl(Item item) {
        this.item = item;
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, PacketContext context) {
        ServerPlayer player = PolymerCommonUtils.getPlayer(context);
        if (ServerNetworkingHandlers.hasModOnClient(player) && ServerNetworkingHandlers.hasModWithVersion(player)) {
            return itemStack.getItem();
        }
        if (this.item instanceof WeaponOfTheMoon) {
            return Items.BOW;
        }
        if (this.item instanceof TrumpetGun || this.item instanceof Knife) {
            return Items.TRIAL_KEY;
        }
        if (this.item instanceof IDanmakuItem && ReverieDreams.config().enableDanmakuGlow) {
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

    @Override
    public boolean isIgnoringBlockInteractionPlaySoundExceptedEntity(BlockState state, ServerPlayer player, InteractionHand hand, ItemStack stack, ServerLevel world, BlockHitResult blockHitResult) {
        return this.item instanceof BlockItem;
    }

    @Override
    public void modifyBasePolymerItemStack(ItemStack out, ItemStack stack, PacketContext context, HolderLookup.Provider lookup) {
        PolymerItem.super.modifyBasePolymerItemStack(out, stack, context, lookup);
        this.modifyForFoodTag(out, stack, context, lookup);
    }

    private void modifyForFoodTag(ItemStack out, ItemStack stack, PacketContext context, HolderLookup.Provider lookup) {
        if (stack.has(RDDataComponents.FOOD_ITEM_TYPE.value())) {
            FoodProperties foodProps = stack.get(DataComponents.FOOD);
            if (foodProps == null) {
                return;
            }
            List<FoodProperty> foodProperties = stack.get(RDDataComponents.FOOD_PROPERTIES.value());
            if (foodProperties == null || foodProperties.isEmpty()) {
                return;
            }
            int size = foodProperties.size();
            out.set(DataComponents.FOOD, new FoodProperties(foodProps.nutrition() + size, foodProps.saturation() + size * 1.5f, foodProps.canAlwaysEat()));
        }
    }
}
