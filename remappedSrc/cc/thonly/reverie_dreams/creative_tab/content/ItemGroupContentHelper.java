package cc.thonly.reverie_dreams.creative_tab.content;

import eu.pb4.polymer.core.api.item.PolymerItemGroupUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;

public interface ItemGroupContentHelper {
    public static CreativeModeTab.Builder builder() {
        return new CreativeModeTab.Builder(CreativeModeTab.Row.BOTTOM, -1);
    }

    public static CreativeModeTab registerGroup(ResourceKey<CreativeModeTab> key, CreativeModeTab group) {
        PolymerItemGroupUtils.registerPolymerItemGroup(key, group);
        return group;
    }
}
