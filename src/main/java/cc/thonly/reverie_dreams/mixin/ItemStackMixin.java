package cc.thonly.reverie_dreams.mixin;

import cc.thonly.mystias_izakaya.entity.villager.TavernVillager;
import cc.thonly.mystias_izakaya.item.base.DrinkItem;
import cc.thonly.mystias_izakaya.item.base.FoodItem;
import cc.thonly.reverie_dreams.interfaces.IItemStack;
import cc.thonly.reverie_dreams.server.ItemDescriptionManager;
import net.fabricmc.fabric.api.item.v1.FabricItemStack;
import net.minecraft.component.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin<T> implements IItemStack,
        ComponentHolder,
        FabricItemStack {
    @Shadow
    public abstract Item getItem();

    @Shadow
    public abstract boolean isEmpty();

    @Shadow public abstract void decrementUnlessCreative(int amount, @Nullable LivingEntity entity);

    @Shadow public abstract ComponentMap getComponents();

    @Shadow @Final public MergedComponentMap components;

    @Inject(method = "useOnEntity", at = @At("HEAD"), cancellable = true)
    public void useOnVillager(PlayerEntity user, LivingEntity entity, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        if (entity instanceof VillagerEntity villager && this.getItem() == Items.BARREL) {
            if (user.getWorld() instanceof ServerWorld world) {
                BlockPos blockPos = entity.getBlockPos();
                Vec3d pos = villager.getPos();
                Text name = villager.getName();
                boolean hasCN = villager.hasCustomName();
                villager.discard();
                TavernVillager sellerVillager = new TavernVillager(villager.getVillagerData(), world);
                sellerVillager.setPos(pos.getX(), pos.getY(), pos.getZ());
                if (hasCN) {
                    sellerVillager.setCustomName(name);
                }
                world.spawnEntity(sellerVillager);
                world.playSound(null, blockPos, SoundEvents.BLOCK_ANVIL_FALL, SoundCategory.PLAYERS);

                this.decrementUnlessCreative(1, user);
                user.swingHand(hand);

                cir.setReturnValue(ActionResult.SUCCESS_SERVER);
            } else {
                cir.setReturnValue(ActionResult.SUCCESS);
            }
        }
    }

    @Inject(method = "getTooltip", at = @At("RETURN"), cancellable = true)
    public void appendTooltip(Item.TooltipContext context, @Nullable PlayerEntity player, TooltipType type, CallbackInfoReturnable<List<Text>> cir) {
        if (this.isEmpty()) {
            return;
        }
        Item item = this.getItem();
        List<MutableText> texts = ItemDescriptionManager.get(item);
        try {
            if (!texts.isEmpty()) {
                List<Text> returnValue = cir.getReturnValue();
                returnValue.addAll(texts);
            }
        } catch (Exception ignored) {
        }
    }

    @Unique
    @Override
    public boolean isFood() {
        Item item = this.getItem();
        if (item instanceof FoodItem || item instanceof DrinkItem) {
            return true;
        }
//        System.out.println(item);
//        this.components.forEach(component -> System.out.println(component.type()));
        return this.components.contains(DataComponentTypes.FOOD);
    }
}
