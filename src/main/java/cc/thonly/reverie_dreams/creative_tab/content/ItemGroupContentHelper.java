package cc.thonly.reverie_dreams.creative_tab.content;

import eu.pb4.polymer.core.api.item.PolymerItemGroupUtils;
import net.minecraft.item.ItemGroup;
import net.minecraft.registry.RegistryKey;

public interface ItemGroupContentHelper {
    public static ItemGroup.Builder builder() {
        return new ItemGroup.Builder(ItemGroup.Row.BOTTOM, -1);
    }

    public static ItemGroup registerGroup(RegistryKey<ItemGroup> key, ItemGroup group) {
        PolymerItemGroupUtils.registerPolymerItemGroup(key, group);
        return group;
    }
}
