package cc.thonly.reverie_dreams.creative_tab.content;

import cc.thonly.reverie_dreams.Touhou;
import cc.thonly.reverie_dreams.danmaku.DanmakuTypes;
import cc.thonly.reverie_dreams.item.ModItems;
import eu.pb4.polymer.core.api.item.PolymerItemGroupUtils;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;

import java.util.List;

public class DanmakuCreativeTab implements ItemGroupContent {
    public static final RegistryKey<ItemGroup> BULLET_ITEM_GROUP_KEY = RegistryKey.of(RegistryKeys.ITEM_GROUP, Touhou.id("item_group_bullet"));
    public static final ItemGroup ITEM_GROUP_BULLET = ItemGroupContent.builder()
            .icon(() -> new ItemStack(ModItems.DANMAKU))
            .displayName(Text.translatable("item_group.touhou.bullet"))
            .build();

    public static void bootstrap() {
        PolymerItemGroupUtils.registerPolymerItemGroup(DanmakuCreativeTab.BULLET_ITEM_GROUP_KEY, DanmakuCreativeTab.ITEM_GROUP_BULLET);
        ItemGroupEvents.modifyEntriesEvent(DanmakuCreativeTab.BULLET_ITEM_GROUP_KEY).register(itemGroup -> {
            List<ItemStack> color = DanmakuTypes.allColor();
            color.forEach(itemGroup::add);
        });
    }
}
