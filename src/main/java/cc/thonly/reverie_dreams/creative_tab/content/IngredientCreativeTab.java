package cc.thonly.reverie_dreams.creative_tab.content;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.registry.content.item.RDIngredientItems;
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
            .displayItems((parameters, output) -> {
                for (Item item : RDIngredientItems.INGREDIENTS) {
                    output.accept(item);
                }
            })
            .build();

    public static void bootstrap() {
        ItemGroupContentHelper.registerGroup(IngredientCreativeTab.ITEM_GROUP_KEY, IngredientCreativeTab.ITEM_GROUP);

    }
}
