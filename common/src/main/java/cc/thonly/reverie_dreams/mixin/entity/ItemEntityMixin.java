package cc.thonly.reverie_dreams.mixin.entity;

import cc.thonly.reverie_dreams.util.item.ItemUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("resource")
@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends Entity {

    @Shadow
    public abstract ItemStack getItem();

    public ItemEntityMixin(EntityType<?> type, Level level) {
        super(type, level);
    }

    @Inject(
            method = "merge(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;I)Lnet/minecraft/world/item/ItemStack;",
            at = @At("RETURN")
    )
    private static void reverie_dreams$appendItemData(ItemStack toStack, ItemStack fromStack, int maxCount, CallbackInfoReturnable<ItemStack> cir) {
        ItemStack itemStack = cir.getReturnValue();
        if (itemStack == null) {
            return;
        }
        ItemUtils.updateItemStackTag(itemStack);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    public void reverie_dreams$appendItemData$tick(CallbackInfo ci) {
        if (this.level().isClientSide()) {
            return;
        }
        ItemStack itemStack = this.getItem();
        if (itemStack.isEmpty()) {
            return;
        }
        ItemUtils.updateItemStackTag(itemStack);
    }
}
