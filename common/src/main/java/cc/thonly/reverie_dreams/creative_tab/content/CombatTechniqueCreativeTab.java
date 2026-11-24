package cc.thonly.reverie_dreams.creative_tab.content;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.item.danmaku.CombatTechniqueItem;
import cc.thonly.reverie_dreams.registry.content.danmaku.soku.SokuMoves;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;

public final class CombatTechniqueCreativeTab {
    public static final String TITLE_KEY = "item_group.touhou.combat_technique";
    public static final ResourceKey<CreativeModeTab> ITEM_GROUP_KEY = ResourceKey.create(
            Registries.CREATIVE_MODE_TAB, ReverieDreams.id("02_item_group_combat_technique"));

    private CombatTechniqueCreativeTab() {
    }

    public static void bootstrap() {
        ItemGroupContentHelper.registerGroup(ITEM_GROUP_KEY, builder -> new CreativeModeTab.Builder(CreativeModeTab.Row.TOP, 0)
                .icon(RDItems.COMBAT_TECHNIQUE::createStack)
                .title(Component.translatable(TITLE_KEY))
                .displayItems((parameters, output) -> SokuMoves.catalog().forEach(
                        move -> output.accept(CombatTechniqueItem.createTechnique(move.id())))));
    }
}
