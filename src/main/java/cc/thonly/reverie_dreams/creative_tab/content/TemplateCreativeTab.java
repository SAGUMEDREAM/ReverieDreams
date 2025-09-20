package cc.thonly.reverie_dreams.creative_tab.content;

import cc.thonly.reverie_dreams.Touhou;
import cc.thonly.reverie_dreams.danmaku.DanmakuShape;
import cc.thonly.reverie_dreams.danmaku.DanmakuTypes;
import cc.thonly.reverie_dreams.danmaku.SpellCardTemplates;
import cc.thonly.reverie_dreams.item.ModItems;
import cc.thonly.reverie_dreams.registry.RegistryManager;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Map;
import java.util.Set;

public class TemplateCreativeTab implements ItemGroupContentHelper {
    public static final RegistryKey<ItemGroup> ITEM_GROUP_KEY = RegistryKey.of(RegistryKeys.ITEM_GROUP, Touhou.id("item_group_template"));
    public static final ItemGroup ITEM_GROUP = ItemGroupContentHelper.builder()
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
            for (DanmakuShape shape : RegistryManager.DANMAKU_SHAPE) {
                if (DanmakuTypes.UNLIST.contains(shape.getType())) {
                    continue;
                }
                itemGroup.add(shape.getItemStack());
            }
        });
        ItemGroupContentHelper.registerGroup(TemplateCreativeTab.ITEM_GROUP_KEY, TemplateCreativeTab.ITEM_GROUP);
    }
}
