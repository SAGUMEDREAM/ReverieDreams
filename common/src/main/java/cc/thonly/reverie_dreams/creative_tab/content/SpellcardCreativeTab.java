package cc.thonly.reverie_dreams.creative_tab.content;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.data.danmaku.spellcard.LegacySpellcardDefinition;
import cc.thonly.reverie_dreams.data.danmaku.spellcard.SpellCardFrameConfigs;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuDefaults;
import cc.thonly.reverie_dreams.item.danmaku.SpellcardItem;
import cc.thonly.reverie_dreams.registry.content.danmaku.soku.SokuMoves;
import cc.thonly.reverie_dreams.registry.BuiltInRegistryProviders;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponentTypes;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.component.ItemLore;

import java.util.List;

public final class SpellcardCreativeTab {
    public static final ResourceKey<CreativeModeTab> ITEM_GROUP_KEY =
            ResourceKey.create(Registries.CREATIVE_MODE_TAB, ReverieDreams.id("02_item_group_spellcard"));
    public static final String TITLE_KEY = "item_group.touhou.spellcard";

    private SpellcardCreativeTab() {}

    public static void bootstrap() {
        ItemGroupContentHelper.registerGroup(ITEM_GROUP_KEY, builder -> new CreativeModeTab.Builder(CreativeModeTab.Row.TOP, 0)
                .icon(RDItems.SPELLCARD::createStack)
                .title(Component.translatable(TITLE_KEY))
                .displayItems(SpellcardCreativeTab::displayItems));
    }

    private static void displayItems(CreativeModeTab.ItemDisplayParameters parameters, CreativeModeTab.Output output) {
        output.accept(RDItems.SPELLCARD.createStack());

        // Keep the registration order, including the encounter order of built-in cards.
        for (var entry : BuiltInRegistryProviders.DANMAKU_PATTERN.entrySet()) {
            var id = entry.getKey().identifier();
            if (SokuMoves.find(id) != null) {
                continue;
            }
            var stack = SpellcardItem.createCard(id);
            stack.set(DataComponents.LORE, new ItemLore(List.of(
                    Component.literal("Pattern: ").withStyle(ChatFormatting.AQUA).append(DanmakuDefaults.displayName(id)))));
            output.accept(stack);
        }

        SpellCardFrameConfigs.BUILTIN_ITEMS.forEach((id, frames) -> {
            var stack = RDItems.SPELLCARD.createStack();
            stack.set(RDDataComponentTypes.SPELL_CARD_COMPONENT.value(), new LegacySpellcardDefinition(frames));
            stack.set(DataComponents.LORE, new ItemLore(List.of(
                    Component.literal("BuiltIn Id: ").withStyle(ChatFormatting.YELLOW).append(Component.translatable(id)))));
            output.accept(stack);
        });
    }
}
