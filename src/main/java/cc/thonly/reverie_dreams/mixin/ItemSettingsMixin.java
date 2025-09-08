package cc.thonly.reverie_dreams.mixin;

import cc.thonly.reverie_dreams.component.ModDataComponentTypes;
import net.minecraft.component.ComponentType;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.Settings.class)
public abstract class ItemSettingsMixin {
    @Shadow public abstract <T> Item.Settings component(ComponentType<T> type, T value);

    @Inject(method = "registryKey", at = @At("RETURN"))
    public void setRegistryKey(RegistryKey<Item> registryKey, CallbackInfoReturnable<Item.Settings> cir) {
        this.component(ModDataComponentTypes.REGISTRY_KEY, registryKey.getValue());
    }
}
