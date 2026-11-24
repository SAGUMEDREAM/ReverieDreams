package cc.thonly.reverie_dreams.creative_tab.content;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.registry.content.danmaku.DanmakuTypes;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;

public class DanmakuCreativeTab implements ItemGroupContentHelper {
    public static final ResourceKey<CreativeModeTab> BULLET_ITEM_GROUP_KEY = ResourceKey.create(Registries.CREATIVE_MODE_TAB, ReverieDreams.id("02_item_group_bullet"));

    public static void bootstrap() {
        ItemGroupContentHelper.registerGroup(DanmakuCreativeTab.BULLET_ITEM_GROUP_KEY, builder -> new CreativeModeTab.Builder(CreativeModeTab.Row.TOP, 0)
                .icon(RDItems.DANMAKU::createStack)
                .title(Component.translatable("item_group.touhou.bullet"))
                .displayItems((parameters, output) ->
                        DanmakuTypes.allColor().forEach(stack -> output.accept(stack.create()))));
    }
}
