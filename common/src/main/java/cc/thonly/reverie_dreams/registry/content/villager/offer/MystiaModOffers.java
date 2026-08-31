package cc.thonly.reverie_dreams.registry.content.villager.offer;

import cc.thonly.reverie_dreams.registry.content.item.RDIngredientItems;
import cc.thonly.reverie_dreams.registry.content.villager.RDVillagerTrades;
import cc.thonly.reverie_dreams.registry.tag.RDVillagerTradeTags;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.TradeCost;
import net.minecraft.world.item.trading.VillagerTrade;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import java.util.List;
import java.util.Optional;

public class MystiaModOffers {

    public static void makeOffers(RDVillagerTrades.PreparingTradeInfo builder) {
        // BUTCHER Lv.2
        {
            ResourceKey<VillagerTrade> key = builder.keyInstance(
                    RDVillagerTradeTags.BUTCHER_LEVEL_2,
                    "butcher_level_%s_result_item_%s",
                    2,
                    RDIngredientItems.VENISON
            );

            builder.add(key, new VillagerTrade(
                    new TradeCost(Items.EMERALD, ConstantValue.exactly(6)),
                    Optional.empty(),
                    new ItemStackTemplate(RDIngredientItems.VENISON.asItem()),
                    4,
                    10,
                    0.05f,
                    Optional.empty(),
                    List.of(
                            SetItemCountFunction.setCount(ConstantValue.exactly(5)).build()
                    )
            ));
        }

        // BUTCHER Lv.3
        // wagyu
        {
            ResourceKey<VillagerTrade> key = builder.keyInstance(
                    RDVillagerTradeTags.BUTCHER_LEVEL_3,
                    "butcher_level_%s_result_item_%s",
                    3,
                    RDIngredientItems.WAGYU_BEEF
            );
            builder.add(key, new VillagerTrade(
                    new TradeCost(Items.EMERALD, ConstantValue.exactly(10)),
                    Optional.empty(),
                    new ItemStackTemplate(RDIngredientItems.WAGYU_BEEF.asItem()),
                    4,
                    10,
                    0.05f,
                    Optional.empty(),
                    List.of(
                            SetItemCountFunction.setCount(ConstantValue.exactly(6)).build()
                    )
            ));
        }

        // boar
        {
            ResourceKey<VillagerTrade> key = builder.keyInstance(
                    RDVillagerTradeTags.BUTCHER_LEVEL_3,
                    "butcher_level_%s_result_item_%s",
                    3,
                    RDIngredientItems.WILD_BOAR_MEAT
            );

            builder.add(key, new VillagerTrade(
                    new TradeCost(Items.EMERALD, ConstantValue.exactly(8)),
                    Optional.empty(),
                    new ItemStackTemplate(RDIngredientItems.WILD_BOAR_MEAT.asItem()),
                    4,
                    10,
                    0.05f,
                    Optional.empty(),
                    List.of(
                            SetItemCountFunction.setCount(ConstantValue.exactly(5)).build()
                    )
            ));
        }
    }
}
