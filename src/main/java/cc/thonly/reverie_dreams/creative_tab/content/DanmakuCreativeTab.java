package cc.thonly.reverie_dreams.creative_tab.content;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponentTypes;
import cc.thonly.reverie_dreams.registry.content.danmaku.DanmakuTypes;
import cc.thonly.reverie_dreams.data.danmaku.SpellcardRenderer;
import cc.thonly.reverie_dreams.data.danmaku.spellcard.SpellCardFrameConfigs;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import eu.pb4.polymer.core.api.item.PolymerItemGroupUtils;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class DanmakuCreativeTab implements ItemGroupContentHelper {
    public static final ResourceKey<CreativeModeTab> BULLET_ITEM_GROUP_KEY = ResourceKey.create(Registries.CREATIVE_MODE_TAB, ReverieDreams.id("item_group_bullet"));
    public static final CreativeModeTab ITEM_GROUP_BULLET = ItemGroupContentHelper.builder()
            .icon(() -> new ItemStack(RDItems.DANMAKU))
            .title(Component.translatable("item_group.touhou.bullet"))
            .build();

    public static void bootstrap() {
        PolymerItemGroupUtils.registerPolymerItemGroup(DanmakuCreativeTab.BULLET_ITEM_GROUP_KEY, DanmakuCreativeTab.ITEM_GROUP_BULLET);
        ItemGroupEvents.modifyEntriesEvent(DanmakuCreativeTab.BULLET_ITEM_GROUP_KEY).register(itemGroup -> {
            List<ItemStack> color = DanmakuTypes.allColor();
            color.forEach(itemGroup::accept);
            SpellCardFrameConfigs.MAP.values().forEach((frames) -> {
                ItemStack itemStack = new ItemStack(RDItems.SPELLCARD);
                itemStack.set(RDDataComponentTypes.SPELL_CARD_COMPONENT, new SpellcardRenderer(frames));
                itemGroup.accept(itemStack);
            });
        });
    }
}
