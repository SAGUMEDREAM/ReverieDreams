package cc.thonly.reverie_dreams.registry.content.villager.offer;

import cc.thonly.reverie_dreams.registry.content.item.RDIngredientItems;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import cc.thonly.reverie_dreams.registry.content.villager.RDVillagerTrades;
import cc.thonly.reverie_dreams.registry.tag.RDItemTags;
import cc.thonly.reverie_dreams.registry.tag.RDVillagerTradeTags;
import cc.thonly.reverie_dreams.util.trading.TradeCost;
import cc.thonly.reverie_dreams.util.trading.VillagerTrade;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import cc.thonly.keine.item.ItemStackTemplate;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import java.util.List;
import java.util.Optional;

public class FarmerModOffers {
    public static void makeOffers(RDVillagerTrades.PreparingTradeInfo builder) {
        HolderGetter<Item> itemRegistry = builder.getItemRegistry();
        if (!(itemRegistry instanceof Registry<Item> registry)) {
            return;
        }
        // FARMER_LEVEL_3
        for (Holder<Item> itemHolder : registry.getTagOrEmpty(RDItemTags.INGREDIENT)) {
            {
                Item item = itemHolder.value();
                ResourceKey<VillagerTrade> key = builder.keyInstance(
                        RDVillagerTradeTags.FARMER_LEVEL_3,
                        "farmer_level_%s_result_item_%s",
                        3,
                        item
                );

                builder.add(key, new VillagerTrade(
                        new TradeCost(RDItems.COPPER_COIN, UniformGenerator.between(11, 17)),
                        Optional.empty(),
                        new ItemStackTemplate(RDIngredientItems.VENISON.asItem()),
                        4,
                        14,
                        0.05f,
                        Optional.empty(),
                        List.of(
                                SetItemCountFunction.setCount(UniformGenerator.between(5, 8)).build()
                        )
                ));
            }
        }
    }
}
