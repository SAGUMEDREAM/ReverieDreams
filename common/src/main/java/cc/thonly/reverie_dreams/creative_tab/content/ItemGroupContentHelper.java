package cc.thonly.reverie_dreams.creative_tab.content;

import cc.thonly.reverie_dreams.util.PlatformContext;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.blay09.mods.balm.world.item.BalmCreativeModeTabRegistrar;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.CreativeModeTab;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public interface ItemGroupContentHelper {
    Map<ResourceKey<CreativeModeTab>, Function<CreativeModeTab.Builder, CreativeModeTab.Builder>> REGISTRIES = new Object2ObjectLinkedOpenHashMap<>();
    List<Tuple<ResourceKey<CreativeModeTab>, Function<CreativeModeTab.Builder, CreativeModeTab.Builder>>> FABRIC_LATE_INIT = new ArrayList<>();

    static CreativeModeTab.Builder builder() {
        return new CreativeModeTab.Builder(CreativeModeTab.Row.BOTTOM, -1);
    }

    static Holder<CreativeModeTab> registerGroup(BalmCreativeModeTabRegistrar registrar, ResourceKey<CreativeModeTab> key, Function<CreativeModeTab.Builder, CreativeModeTab.Builder> builderFunction) {
        if (!PlatformContext.hasPolymer()) {
            Holder<CreativeModeTab> holder = registrar.register(key.identifier().getPath(), builderFunction).asHolder();
            REGISTRIES.put(key, builderFunction);
            return holder;
        } else {
            CreativeModeTab.Builder builder = builderFunction.apply(registrar.createBuilder());
            REGISTRIES.put(key, builderFunction);
            FABRIC_LATE_INIT.add(new Tuple<>(key, builderFunction));
            return Holder.direct(builderFunction.apply(builder).build());
        }
    }
}
