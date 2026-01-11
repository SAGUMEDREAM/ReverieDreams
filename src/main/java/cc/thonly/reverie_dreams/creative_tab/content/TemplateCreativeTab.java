package cc.thonly.reverie_dreams.creative_tab.content;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.data.danmaku.DanmakuShape;
import cc.thonly.reverie_dreams.registry.RegistryHandlers;
import cc.thonly.reverie_dreams.registry.content.danmaku.DanmakuTemplates;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.Set;

public class TemplateCreativeTab implements ItemGroupContentHelper {
    public static final ResourceKey<CreativeModeTab> ITEM_GROUP_KEY = ResourceKey.create(Registries.CREATIVE_MODE_TAB, ReverieDreams.id("item_group_template"));
    public static final CreativeModeTab ITEM_GROUP = ItemGroupContentHelper.builder()
            .icon(() -> new ItemStack(RDItems.SPELL_CARD_TEMPLATE))
            .title(Component.translatable("item_group.touhou.template"))
            .displayItems((parameters, output) -> {
                Map<Identifier, ItemStack> registryView = DanmakuTemplates.getRegistryItemStackView();
                Set<Map.Entry<Identifier, ItemStack>> views = registryView.entrySet();
                for (Map.Entry<Identifier, ItemStack> view : views) {
                    ItemStack stack = view.getValue();
                    output.accept(stack.copy());
                }
                for (DanmakuShape shape : RegistryHandlers.DANMAKU_SHAPE) {
                    if (shape.getType().isDeleteFromList()) {
                        continue;
                    }
                    output.accept(shape.getItemStack());
                }
            })
            .build();

    public static void bootstrap() {
        ItemGroupContentHelper.registerGroup(TemplateCreativeTab.ITEM_GROUP_KEY, TemplateCreativeTab.ITEM_GROUP);
    }
}
