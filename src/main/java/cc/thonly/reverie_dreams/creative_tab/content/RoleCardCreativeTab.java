package cc.thonly.reverie_dreams.creative_tab.content;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.registry.RegistryHandlers;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class RoleCardCreativeTab implements ItemGroupContentHelper {
    public static final ResourceKey<CreativeModeTab> ITEM_GROUP_KEY = ResourceKey.create(Registries.CREATIVE_MODE_TAB, ReverieDreams.id("item_group_role_card"));
    public static final CreativeModeTab ITEM_GROUP = ItemGroupContentHelper.builder()
            .icon(() -> new ItemStack(RDItems.ROLE_CARD))
            .title(Component.translatable("item_group.touhou.role_card"))
            .displayItems((parameters, output) -> {
                output.accept(Items.CAKE);
                output.accept(RDItems.OWNER_STICK);
                output.accept(RDItems.ROLE_CARD);
                RegistryHandlers.ROLE_CARD.values().forEach(instance -> output.accept(instance.itemStack()));
            })
            .build();

    public static void bootstrap() {
        ItemGroupContentHelper.registerGroup(RoleCardCreativeTab.ITEM_GROUP_KEY, RoleCardCreativeTab.ITEM_GROUP);
    }
}
