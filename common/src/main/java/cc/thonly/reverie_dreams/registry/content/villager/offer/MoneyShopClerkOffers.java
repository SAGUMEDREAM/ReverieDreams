package cc.thonly.reverie_dreams.registry.content.villager.offer;

import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import cc.thonly.reverie_dreams.registry.content.villager.RDVillagerTrades;
import cc.thonly.reverie_dreams.registry.tag.RDVillagerTradeTags;
import net.minecraft.resources.ResourceKey;
import cc.thonly.keine.item.ItemStackTemplate;
import cc.thonly.reverie_dreams.util.trading.TradeCost;
import cc.thonly.reverie_dreams.util.trading.VillagerTrade;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.npc.villager.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MoneyShopClerkOffers {
    private static final float PRICE_MULTIPLIER = 0.13f;
    public static final String TEMPLATE = RDVillagerTrades.MONEY_SHOP_CLERK_TEMPLATE;
    public static final int COPPER_PER_SILVER = 5;
    public static final int SILVER_PER_GOLD = 10;

    public static int toCopper(int gold, int silver, int copper) {
        return gold * SILVER_PER_GOLD * COPPER_PER_SILVER + silver * COPPER_PER_SILVER + copper;
    }

    public static List<Tuple<Integer, VillagerTrades.ItemListing>> makeOffers() {
        List<Tuple<Integer, VillagerTrades.ItemListing>> offers = new ArrayList<>();

        // ======================
        // Level 1：铜 <-> 银
        // ======================

        // 铜 → 银
        offers.add(new Tuple<>(
                1,
                (level, entity, random) -> new MerchantOffer(
                        new ItemCost(
                                RDItems.COPPER_COIN.get(),
                                COPPER_PER_SILVER
                        ),
                        new ItemStack(
                                RDItems.SILVER_COIN.get(),
                                1
                        ),
                        16,
                        2,
                        PRICE_MULTIPLIER
                )
        ));

        // 银 → 铜
        offers.add(new Tuple<>(
                1,
                (level, entity, random) -> new MerchantOffer(
                        new ItemCost(
                                RDItems.SILVER_COIN.get(),
                                1
                        ),
                        new ItemStack(
                                RDItems.COPPER_COIN.get(),
                                COPPER_PER_SILVER
                        ),
                        16,
                        2,
                        PRICE_MULTIPLIER
                )
        ));

        // ======================
        // Level 2：银 <-> 金
        // ======================

        // 银 → 金
        offers.add(new Tuple<>(
                2,
                (level, entity, random) -> new MerchantOffer(
                        new ItemCost(
                                RDItems.SILVER_COIN.get(),
                                SILVER_PER_GOLD
                        ),
                        new ItemStack(
                                RDItems.GOLD_COIN.get(),
                                1
                        ),
                        12,
                        5,
                        PRICE_MULTIPLIER
                )
        ));

        // 金 → 银
        offers.add(new Tuple<>(
                2,
                (level, entity, random) -> new MerchantOffer(
                        new ItemCost(
                                RDItems.GOLD_COIN.get(),
                                1
                        ),
                        new ItemStack(
                                RDItems.SILVER_COIN.get(),
                                SILVER_PER_GOLD
                        ),
                        12,
                        5,
                        PRICE_MULTIPLIER
                )
        ));

        return offers;
    }

    public static void makeOffers(RDVillagerTrades.PreparingTradeInfo builder) {
        // ======================
        // Level 1：铜 <-> 银
        // ======================

        // 铜 → 银
        {
            ResourceKey<VillagerTrade> key = builder.keyInstance(
                    RDVillagerTradeTags.MONEY_SHOP_CLERK_LEVEL_1,
                    TEMPLATE,
                    1,
                    RDItems.SILVER_COIN
            );

            builder.add(key, new VillagerTrade(
                    new TradeCost(RDItems.COPPER_COIN,
                            ConstantValue.exactly(COPPER_PER_SILVER)
                    ),
                    Optional.empty(),
                    new ItemStackTemplate(RDItems.SILVER_COIN.asItem()),
                    16,
                    2,
                    PRICE_MULTIPLIER,
                    Optional.empty(),
                    List.of(
                            SetItemCountFunction.setCount(ConstantValue.exactly(1)).build()
                    )
            ));
        }

        // 银 → 铜
        {
            ResourceKey<VillagerTrade> key = builder.keyInstance(
                    RDVillagerTradeTags.MONEY_SHOP_CLERK_LEVEL_1,
                    TEMPLATE,
                    1,
                    RDItems.COPPER_COIN
            );

            builder.add(key, new VillagerTrade(
                    new TradeCost(RDItems.SILVER_COIN,
                            ConstantValue.exactly(1)
                    ),
                    Optional.empty(),
                    new ItemStackTemplate(RDItems.COPPER_COIN.asItem()),
                    16,
                    2,
                    PRICE_MULTIPLIER,
                    Optional.empty(),
                    List.of(
                            SetItemCountFunction.setCount(
                                    ConstantValue.exactly(COPPER_PER_SILVER)
                            ).build()
                    )
            ));
        }

        // ======================
        // Level 2：银 <-> 金
        // ======================

        // 银 → 金
        {
            ResourceKey<VillagerTrade> key = builder.keyInstance(
                    RDVillagerTradeTags.MONEY_SHOP_CLERK_LEVEL_2,
                    TEMPLATE,
                    2,
                    RDItems.GOLD_COIN
            );

            builder.add(key, new VillagerTrade(
                    new TradeCost(RDItems.SILVER_COIN,
                            ConstantValue.exactly(SILVER_PER_GOLD)
                    ),
                    Optional.empty(),
                    new ItemStackTemplate(RDItems.GOLD_COIN.asItem()),
                    12,
                    5,
                    PRICE_MULTIPLIER,
                    Optional.empty(),
                    List.of(
                            SetItemCountFunction.setCount(ConstantValue.exactly(1)).build()
                    )
            ));
        }

        // 金 → 银
        {
            ResourceKey<VillagerTrade> key = builder.keyInstance(
                    RDVillagerTradeTags.MONEY_SHOP_CLERK_LEVEL_2,
                    TEMPLATE,
                    2,
                    RDItems.SILVER_COIN
            );

            builder.add(key, new VillagerTrade(
                    new TradeCost(RDItems.GOLD_COIN,
                            ConstantValue.exactly(1)
                    ),
                    Optional.empty(),
                    new ItemStackTemplate(RDItems.SILVER_COIN.asItem()),
                    12,
                    5,
                    PRICE_MULTIPLIER,
                    Optional.empty(),
                    List.of(
                            SetItemCountFunction.setCount(
                                    ConstantValue.exactly(SILVER_PER_GOLD)
                            ).build()
                    )
            ));
        }
    }
}
