package cc.thonly.reverie_dreams.mixin;

import cc.thonly.reverie_dreams.entity.villager.TavernVillager;
import cc.thonly.reverie_dreams.item.base.DrinkItem;
import cc.thonly.reverie_dreams.item.base.FoodItem;
import cc.thonly.reverie_dreams.interfaces.IItemStack;
import cc.thonly.reverie_dreams.registry.content.RDEnchantments;
import cc.thonly.reverie_dreams.server.ItemDescriptionManager;
import net.fabricmc.fabric.api.item.v1.FabricItemStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
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
        DataComponentHolder,
        FabricItemStack {
    @Shadow
    public abstract Item getItem();

    @Shadow
    public abstract boolean isEmpty();

    @Shadow public abstract void consume(int amount, @Nullable LivingEntity entity);

    @Shadow public abstract DataComponentMap getComponents();

    @Shadow @Final public PatchedDataComponentMap components;

    @Shadow @Final @Deprecated private @Nullable Item item;

    @Inject(method = "interactLivingEntity", at = @At("HEAD"), cancellable = true)
    public void useOnVillager(Player user, LivingEntity entity, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if (entity instanceof Villager villager && this.getItem() == Items.BARREL) {
            if (user.level() instanceof ServerLevel world) {
                BlockPos blockPos = entity.blockPosition();
                Vec3 pos = villager.position();
                Component name = villager.getName();
                boolean hasCN = villager.hasCustomName();
                villager.discard();
                TavernVillager sellerVillager = new TavernVillager(villager.getVillagerData(), world);
                sellerVillager.setPosRaw(pos.x(), pos.y(), pos.z());
                if (hasCN) {
                    sellerVillager.setCustomName(name);
                }
                world.addFreshEntity(sellerVillager);
                world.playSound(null, blockPos, SoundEvents.ANVIL_FALL, SoundSource.PLAYERS);

                this.consume(1, user);
                user.swing(hand);

                cir.setReturnValue(InteractionResult.SUCCESS_SERVER);
            } else {
                cir.setReturnValue(InteractionResult.SUCCESS);
            }
        }
    }

    @Inject(method = "getTooltipLines", at = @At("RETURN"), cancellable = true)
    public void appendTooltip(Item.TooltipContext context, @Nullable Player player, TooltipFlag type, CallbackInfoReturnable<List<Component>> cir) {
        if (this.isEmpty()) {
            return;
        }
        Item item = this.getItem();
        List<MutableComponent> texts = ItemDescriptionManager.getDescription(item);
        try {
            if (!texts.isEmpty()) {
                List<Component> textList = cir.getReturnValue();
                textList.addAll(texts);
            }
        } catch (Exception ignored) {
        }
    }

    @Unique
    @Override
    public boolean isFood() {
        Item item = this.getItem();
        if (item instanceof FoodItem) {
            return true;
        }
//        System.out.println(item);
//        this.components.forEach(component -> System.out.println(component.type()));
        return this.components.has(DataComponents.FOOD);
    }

    @Unique
    @Override
    public boolean isDrink() {
        Item item = this.getItem();
        return item instanceof DrinkItem;
    }
}
