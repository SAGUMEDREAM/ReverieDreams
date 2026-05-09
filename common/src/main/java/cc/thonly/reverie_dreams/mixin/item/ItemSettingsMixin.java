package cc.thonly.reverie_dreams.mixin.item;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.Properties.class)
public abstract class ItemSettingsMixin {
    @Unique
    private static final Identifier DEFAULT_KEY = Identifier.parse("minecraft:stone");

    @Shadow
    public abstract <T> Item.Properties component(DataComponentType<T> type, T value);

    @Inject(method = "setId", at = @At("RETURN"))
    public void setRegistryKey(ResourceKey<Item> registryKey, CallbackInfoReturnable<Item.Properties> cir) {
//        if (registryKey == null) {
//            registryKey = RegistryKey.of(RegistryKeys.ITEM, DEFAULT_KEY);
//        }
//        if (registryKey.getValue() == null) {
//            registryKey = RegistryKey.of(RegistryKeys.ITEM, DEFAULT_KEY);
//        }
//        this.component(ModDataComponentTypes.REGISTRY_KEY, registryKey.getValue());
    }
}
