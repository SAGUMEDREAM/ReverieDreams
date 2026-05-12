package cc.thonly.reverie_dreams.mixin.item;

import cc.thonly.reverie_dreams.api.item.ItemStackHelper;
import cc.thonly.reverie_dreams.entity.villager.TavernVillager;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponents;
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

@Mixin(ItemStack.class)
public abstract class ItemStackHelperMixin<T> implements ItemStackHelper,
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
        return this.components.has(DataComponents.FOOD) || this.components.has(RDDataComponents.FOOD_ITEM_TYPE.value());
    }

    @Unique
    @Override
    public boolean reverie_dreams$isDrink() {
        return this.components.has(RDDataComponents.DRINK_ITEM_TYPE.value());
    }
}
