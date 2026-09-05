package cc.thonly.reverie_dreams.registry.content.villager.offer;

import cc.thonly.reverie_dreams.registry.content.item.RDIngredientItems;
import cc.thonly.reverie_dreams.registry.content.villager.RDVillagerTrades;
import cc.thonly.reverie_dreams.registry.tag.RDVillagerTradeTags;
import net.minecraft.resources.ResourceKey;
import cc.thonly.keine.item.ItemStackTemplate;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.npc.villager.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import cc.thonly.reverie_dreams.util.trading.TradeCost;
import cc.thonly.reverie_dreams.util.trading.VillagerTrade;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MystiaModOffers {

    public static List<Tuple<Integer, VillagerTrades.ItemListing>> makeOffers() {
        List<Tuple<Integer, VillagerTrades.ItemListing>> offers = new ArrayList<>();

        // BUTCHER Lv.2 - Venison
        offers.add(new Tuple<>(
                2,
                (level, entity, random) -> new MerchantOffer(
                        new ItemCost(
                                Items.EMERALD,
                                6
                        ),
                        new ItemStack(
                                RDIngredientItems.VENISON.get(),
                                5
                        ),
                        4,
                        10,
                        0.05f
                )
        ));

        // BUTCHER Lv.3 - Wagyu Beef
        offers.add(new Tuple<>(
                3,
                (level, entity, random) -> new MerchantOffer(
                        new ItemCost(
                                Items.EMERALD,
                                10
                        ),
                        new ItemStack(
                                RDIngredientItems.WAGYU_BEEF.get(),
                                6
                        ),
                        4,
                        10,
                        0.05f
                )
        ));

        // BUTCHER Lv.3 - Wild Boar Meat
        offers.add(new Tuple<>(
                3,
                (level, entity, random) -> new MerchantOffer(
                        new ItemCost(
                                Items.EMERALD,
                                8
                        ),
                        new ItemStack(
                                RDIngredientItems.WILD_BOAR_MEAT.get(),
                                5
                        ),
                        4,
                        10,
                        0.05f
                )
        ));

        return offers;
    }

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
