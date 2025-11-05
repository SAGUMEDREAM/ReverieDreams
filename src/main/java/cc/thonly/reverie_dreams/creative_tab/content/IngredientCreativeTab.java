package cc.thonly.reverie_dreams.creative_tab.content;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.registry.content.item.RDIngredientItems;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class IngredientCreativeTab implements ItemGroupContentHelper {

    public static final ResourceKey<CreativeModeTab> ITEM_GROUP_KEY = ResourceKey.create(Registries.CREATIVE_MODE_TAB, ReverieDreams.id("ingredients_item_group"));
    public static final CreativeModeTab ITEM_GROUP = ItemGroupContentHelper.builder()
            .icon(() -> new ItemStack(RDIngredientItems.BLACK_PORK))
            .title(Component.translatable("item_group.ingredients_item_group"))
            .build();

    public static void bootstrap() {
        ItemGroupEvents.modifyEntriesEvent(IngredientCreativeTab.ITEM_GROUP_KEY).register(itemGroup -> {
            for (Item item : RDIngredientItems.INGREDIENTS) {
                itemGroup.accept(item);
            }
        });
        ItemGroupContentHelper.registerGroup(IngredientCreativeTab.ITEM_GROUP_KEY, IngredientCreativeTab.ITEM_GROUP);

    }
}
