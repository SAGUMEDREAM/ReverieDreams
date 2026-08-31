package cc.thonly.reverie_dreams.mixin.item;

import cc.thonly.reverie_dreams.api.item.IItemStack;
import cc.thonly.reverie_dreams.entity.villager.TavernVillager;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponentTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
@Mixin(ItemStack.class)
public abstract class ItemStackMixin<T> implements IItemStack,
        DataComponentHolder {
    @Shadow
    public abstract Item getItem();

    @Shadow
    public abstract boolean isEmpty();

    @Shadow
    public abstract void consume(int amount, @Nullable LivingEntity entity);

    @Shadow
    public abstract DataComponentMap getComponents();

    @Shadow
    @Final
    public PatchedDataComponentMap components;

    @Shadow
    @Final
    @Deprecated
    private @Nullable Holder<Item> item;

    @Unique
    private volatile Map<String, Integer> reverie_dreams$keys = new HashMap<>();

    @Unique
    private volatile Object[] reverie_dreams$values = new Object[0];


    @Unique
    private synchronized void reverie_dreams$updateNonPersistentAdditionalDataKey(String name) {
        if (!this.reverie_dreams$keys.containsKey(name)) {
            int index = this.reverie_dreams$keys.size();

            this.reverie_dreams$keys.put(name, index);

            this.reverie_dreams$resizeNonPersistentAdditionalDataKey();
        }
    }


    @Unique
    private synchronized void reverie_dreams$resizeNonPersistentAdditionalDataKey() {
        int size = this.reverie_dreams$keys.size();

        if (this.reverie_dreams$values.length < size) {
            Object[] newValues = new Object[size];

            System.arraycopy(
                    this.reverie_dreams$values,
                    0,
                    newValues,
                    0,
                    this.reverie_dreams$values.length
            );

            this.reverie_dreams$values = newValues;
        }
    }


    @Unique
    @Override
    @SuppressWarnings("unchecked")
    public synchronized <Type> @Nullable Type reverie_dreams$getNonPersistentAdditionalData(String name) {
        Integer idx = this.reverie_dreams$keys.get(name);

        if (idx == null) {
            return null;
        }

        if (idx >= this.reverie_dreams$values.length) {
            return null;
        }

        return (Type) this.reverie_dreams$values[idx];
    }


    @Unique
    @Override
    public synchronized <Type> @Nullable Type reverie_dreams$getNonPersistentAdditionalData(
            String name,
            Class<Type> type
    ) {
        Object value = this.reverie_dreams$getNonPersistentAdditionalData(name);

        if (value == null) {
            return null;
        }

        if (!type.isInstance(value)) {
            return null;
        }

        return type.cast(value);
    }


    @Unique
    @Override
    public synchronized <Type> void reverie_dreams$setNonPersistentAdditionalData(
            String name,
            Type data
    ) {

        this.reverie_dreams$updateNonPersistentAdditionalDataKey(name);

        Integer idx = this.reverie_dreams$keys.get(name);

        this.reverie_dreams$values[idx] = data;
    }

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

    @Unique
    @Override
    public boolean reverie_dreams$isFood() {
        return this.components.has(DataComponents.FOOD) || this.components.has(RDDataComponentTypes.FOOD_ITEM_TYPE.value());
    }

    @Unique
    @Override
    public boolean reverie_dreams$isBeverage() {
        return this.components.has(RDDataComponentTypes.DRINK_ITEM_TYPE.value());
    }
}
