package cc.thonly.reverie_dreams.creative_tab.content;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.data.FoodProperty;
import cc.thonly.reverie_dreams.registry.content.FoodProperties;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponents;
import cc.thonly.reverie_dreams.registry.content.item.RDFoodItems;
import net.blay09.mods.balm.world.item.BalmCreativeModeTabRegistrar;
import net.blay09.mods.balm.world.item.DeferredItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Unit;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Collection;

public class FoodCreativeTab implements ItemGroupContentHelper {
    public static final ResourceKey<CreativeModeTab> ITEM_GROUP_KEY = ResourceKey.create(Registries.CREATIVE_MODE_TAB, ReverieDreams.id("08_food_item_group"));

    public static void bootstrap(BalmCreativeModeTabRegistrar registrar) {
        ItemGroupContentHelper.registerGroup(registrar,FoodCreativeTab.ITEM_GROUP_KEY, builder -> new CreativeModeTab.Builder(CreativeModeTab.Row.TOP, 0)
                .icon(FoodCreativeTab::getFoodItemIcon)
                .title(Component.translatable("item_group.food_item_group"))
                .displayItems((parameters, output) -> {
                    for (DeferredItem item : RDFoodItems.FOOD_ITEMS) {
                        ItemStack itemStack = item.createStack();
                        Collection<FoodProperty> foodProperties = FoodProperties.get(itemStack);
                        itemStack.set(RDDataComponents.FOOD_ITEM_TYPE.value(), Unit.INSTANCE);
                        itemStack.set(RDDataComponents.FOOD_PROPERTIES.value(), foodProperties.stream().toList());
                        output.accept(itemStack);
                    }
                })
        );
    }

    public static ItemStack getFoodItemIcon() {
        for (DeferredItem foodItem : RDFoodItems.FOOD_ITEMS) {
            return foodItem.createStack();
        }
        return new ItemStack(Items.COOKED_BEEF);
    }
}
