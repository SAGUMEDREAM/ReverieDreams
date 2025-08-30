package cc.thonly.reverie_dreams.creative_tab.content;

import cc.thonly.reverie_dreams.config.ReverieDreamsConfiguration;
import eu.pb4.polymer.core.api.item.PolymerItemGroupUtils;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;

public interface ItemGroupContent {
    public static ItemGroup.Builder builder() {
        return new ItemGroup.Builder(ItemGroup.Row.BOTTOM, -1);
    }

    public static ItemGroup registerGroup(RegistryKey<ItemGroup> key, ItemGroup group) {
        if (ReverieDreamsConfiguration.POLYMER_PATCH) {
            PolymerItemGroupUtils.registerPolymerItemGroup(key, group);
        } else {
            Registry.register(Registries.ITEM_GROUP, key, group);
        }
        return group;
    }
}
