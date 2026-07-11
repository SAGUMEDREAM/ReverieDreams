package cc.thonly.reverie_dreams.creative_tab.content;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.data.danmaku.DanmakuShape;
import cc.thonly.reverie_dreams.registry.RegistryImpls;
import cc.thonly.reverie_dreams.registry.content.danmaku.DanmakuTemplates;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;

import java.util.Map;
import java.util.Set;

public class TemplateCreativeTab implements ItemGroupContentHelper {
    public static final ResourceKey<CreativeModeTab> ITEM_GROUP_KEY = ResourceKey.create(Registries.CREATIVE_MODE_TAB, ReverieDreams.id("03_item_group_template"));

    public static void bootstrap() {
        ItemGroupContentHelper.registerGroup(TemplateCreativeTab.ITEM_GROUP_KEY, builder -> new CreativeModeTab.Builder(CreativeModeTab.Row.TOP, 0)
                .icon(() -> new ItemStack(RDItems.SPELL_CARD_TEMPLATE.asItem()))
                .title(Component.translatable("item_group.touhou.template"))
                .displayItems((parameters, output) -> {
                    Map<Identifier, ItemStackTemplate> registryView = DanmakuTemplates.getRegistryItemStackView();
                    Set<Map.Entry<Identifier, ItemStackTemplate>> views = registryView.entrySet();
                    for (Map.Entry<Identifier, ItemStackTemplate> view : views) {
                        ItemStackTemplate stack = view.getValue();
                        output.accept(stack.create());
                    }
                    for (DanmakuShape shape : RegistryImpls.DANMAKU_SHAPE) {
                        if (shape.getType().isDeleteFromList()) {
                            continue;
                        }
                        output.accept(shape.getItemStackOrThrow());
                    }
                }));
    }
}
