package cc.thonly.reverie_dreams.creative_tab.content;

import cc.thonly.reverie_dreams.Touhou;
import cc.thonly.reverie_dreams.danmaku.SpellCardTemplates;
import cc.thonly.reverie_dreams.item.ModItems;
import eu.pb4.polymer.core.api.item.PolymerItemGroupUtils;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Map;
import java.util.Set;

public class TemplateCreativeTab implements ItemGroupContent {
    public static final RegistryKey<ItemGroup> ITEM_GROUP_KEY = RegistryKey.of(RegistryKeys.ITEM_GROUP, Touhou.id("item_group_template"));
    public static final ItemGroup ITEM_GROUP = ItemGroupContent.builder()
            .icon(() -> new ItemStack(ModItems.SPELL_CARD_TEMPLATE))
            .displayName(Text.translatable("item_group.touhou.template"))
            .build();

    public static void bootstrap() {
        ItemGroupEvents.modifyEntriesEvent(TemplateCreativeTab.ITEM_GROUP_KEY).register(itemGroup -> {
            Map<Identifier, ItemStack> registryView = SpellCardTemplates.getRegistryItemStackView();
            Set<Map.Entry<Identifier, ItemStack>> views = registryView.entrySet();
            for (Map.Entry<Identifier, ItemStack> view : views) {
                ItemStack stack = view.getValue();
                itemGroup.add(stack.copy());
            }
        });
        ItemGroupContent.registerGroup(TemplateCreativeTab.ITEM_GROUP_KEY, TemplateCreativeTab.ITEM_GROUP);
    }
}
