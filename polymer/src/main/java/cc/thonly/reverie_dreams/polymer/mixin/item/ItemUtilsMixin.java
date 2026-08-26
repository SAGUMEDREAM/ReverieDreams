package cc.thonly.reverie_dreams.polymer.mixin.item;

import cc.thonly.reverie_dreams.util.item.ItemUtils;
import eu.pb4.polymer.common.api.PolymerCommonUtils;
import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.core.api.item.PolymerItemUtils;
import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import eu.pb4.polymer.rsm.api.RegistrySyncUtils;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("deprecation")
@Pseudo
@Mixin(ItemUtils.class)
public class ItemUtilsMixin {
//    @Inject(method = "getHolderItem", at= @At("HEAD"), cancellable = true)
    private static void reverie_dreams$polymer$getHolderItem(Identifier itemId, CallbackInfoReturnable<Holder<Item>> cir) {
        Holder<Item> holder = cir.getReturnValue();
        Item item = holder.value();
        if (PolymerCommonUtils.isServerNetworkingThread()) {
            return;
        } else if (RegistrySyncUtils.isServerEntry(BuiltInRegistries.ITEM, item)) {
            PolymerSyncedObject<Item> syncedObject = PolymerSyncedObject.getSyncedObject(BuiltInRegistries.ITEM, item);
            if (syncedObject instanceof PolymerItem polymerItem) {
                Item vanillaItem = polymerItem.getPolymerItem(item.getDefaultInstance(), PacketContext.orElseThrow());
                cir.setReturnValue(vanillaItem.builtInRegistryHolder());
            }
        }
    }
}
