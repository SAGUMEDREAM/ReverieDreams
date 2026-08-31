package cc.thonly.reverie_dreams.mixin.item;

import cc.thonly.reverie_dreams.util.item.ProjectileItemHelper;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Predicate;

@SuppressWarnings({"RedundantIfStatement", "DuplicatedCode"})
@Mixin(BowItem.class)
public abstract class BowItemMixin extends ProjectileWeaponItem {
    public BowItemMixin(Properties properties) {
        super(properties);
    }

    @Inject(method = {"getAllSupportedProjectiles",}, at = @At("RETURN"), cancellable = true)
    public void reverie_dreams$addSupportItem(CallbackInfoReturnable<Predicate<ItemStack>> cir) {
        Predicate<ItemStack> predicate = cir.getReturnValue();
        if (predicate == null) {
            return;
        }
        Predicate<ItemStack> merged = itemStack -> {
            if (predicate.test(itemStack)) {
                return true;
            }
            if (ProjectileItemHelper.isThrowableCuisine(itemStack)) {
                return true;
            }
            return false;
        };
        cir.setReturnValue(merged);
    }

}
