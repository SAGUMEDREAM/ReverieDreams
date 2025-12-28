package cc.thonly.reverie_dreams.creative_tab.content;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.data.danmaku.SpellcardRenderer;
import cc.thonly.reverie_dreams.data.danmaku.spellcard.SpellCardFrameConfigs;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponents;
import cc.thonly.reverie_dreams.registry.content.danmaku.DanmakuTypes;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;

import java.util.List;

public class DanmakuCreativeTab implements ItemGroupContentHelper {
    public static final ResourceKey<CreativeModeTab> BULLET_ITEM_GROUP_KEY = ResourceKey.create(Registries.CREATIVE_MODE_TAB, ReverieDreams.id("item_group_bullet"));
    public static final CreativeModeTab ITEM_GROUP_BULLET = ItemGroupContentHelper.builder()
            .icon(() -> new ItemStack(RDItems.DANMAKU))
            .title(Component.translatable("item_group.touhou.bullet"))
            .displayItems((parameters, output) -> {
                List<ItemStack> color = DanmakuTypes.allColor();
                color.forEach(output::accept);
                SpellCardFrameConfigs.BUILTIN_ITEMS.forEach((s, frames) -> {
                    ItemStack itemStack = new ItemStack(RDItems.SPELLCARD);
                    itemStack.set(RDDataComponents.SPELL_CARD_COMPONENT, new SpellcardRenderer(frames));
                    itemStack.set(DataComponents.LORE, new ItemLore(List.of(Component.empty().append("§eBuiltIn Id: ").append(Component.translatable(s)))));
                    output.accept(itemStack);
                });
            })
            .build();

    public static void bootstrap() {
        ItemGroupContentHelper.registerGroup(DanmakuCreativeTab.BULLET_ITEM_GROUP_KEY, DanmakuCreativeTab.ITEM_GROUP_BULLET);
    }
}
