package cc.thonly.reverie_dreams.neoforge.mixin;

import cc.thonly.reverie_dreams.creative_tab.content.ItemGroupContentHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.neoforge.event.EventHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EventHooks.class)
public class EventHooksMixin {
//    private static boolean reverie_dreams$$order_lock = false;
//    @Inject(method = "onCreativeModeTabBuildContents", at = @At("HEAD"))
//    private static void orderBef(CreativeModeTab tab, ResourceKey<CreativeModeTab> tabKey, CreativeModeTab.DisplayItemsGenerator originalGenerator, CreativeModeTab.ItemDisplayParameters params, CreativeModeTab.Output output, CallbackInfo ci) {
//        reverie_dreams$$order_lock = true;
//    }
//
//    @Inject(method = "onCreativeModeTabBuildContents", at = @At("TAIL"))
//    private static void orderAft(CreativeModeTab tab, ResourceKey<CreativeModeTab> tabKey, CreativeModeTab.DisplayItemsGenerator originalGenerator, CreativeModeTab.ItemDisplayParameters params, CreativeModeTab.Output output, CallbackInfo ci) {
//        ResourceKey<CreativeModeTab> prev = null;
//        for (var entry : ItemGroupContentHelper.REGISTRIES.entrySet()) {
//            var key = entry.getKey();
//            var value = BuiltInRegistries.CREATIVE_MODE_TAB.getValue(key);
//
//            if (value == null) continue;
//
//            if (prev != null) {
//                if (!value.tabsAfter.contains(prev.identifier())) {
//                    value.tabsAfter.add(prev.identifier());
//                }
//            }
//
//            prev = key;
//        }
//        reverie_dreams$$order_lock = false;
//    }
}
