package cc.thonly.reverie_dreams.registry.content.villager.offer;

import cc.thonly.reverie_dreams.registry.content.villager.RDVillagerProfessions;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import cc.thonly.reverie_dreams.util.ListMaker;
import net.blay09.mods.balm.world.entity.npc.villager.BalmVillagerTradeRegistrar;
import net.minecraft.world.entity.npc.villager.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;

import java.util.*;

public class MoneyShopClerkOffers {
    private static final float PRICE_MULTIPLIER = 0.13f;
    public static final int COPPER_PER_SILVER = 5;
    public static final int SILVER_PER_GOLD = 10;

    public static int toCopper(int gold, int silver, int copper) {
        return gold * SILVER_PER_GOLD * COPPER_PER_SILVER + silver * COPPER_PER_SILVER + copper;
    }


    public static void registers(BalmVillagerTradeRegistrar tradeRegistrar) {
        // 等级 1：铜 <-> 银
        tradeRegistrar.registerTrade(
                RDVillagerProfessions.MONEY_SHOP_CLERK_KEY,
                1,
                ListMaker.of(() -> List.of(
                        (VillagerTrades.ItemListing) (level, entity, random) -> new MerchantOffer(
                                new ItemCost(RDItems.COPPER_COIN, COPPER_PER_SILVER),
                                Optional.empty(),
                                new ItemStack(RDItems.SILVER_COIN.asItem(), 1),
                                16,
                                2,
                                PRICE_MULTIPLIER
                        ),
                        (level, entity, random) -> new MerchantOffer(
                                new ItemCost(RDItems.SILVER_COIN, 1),
                                Optional.empty(),
                                new ItemStack(RDItems.COPPER_COIN.asItem(), COPPER_PER_SILVER),
                                16,
                                2,
                                PRICE_MULTIPLIER
                        )
                )).toArray(new VillagerTrades.ItemListing[0])
        );

        // 等级 2：银 <-> 金
        tradeRegistrar.registerTrade(
                RDVillagerProfessions.MONEY_SHOP_CLERK_KEY,
                2,
                ListMaker.of(() -> List.of(
                        (VillagerTrades.ItemListing) (level, entity, random) -> new MerchantOffer(
                                new ItemCost(RDItems.SILVER_COIN, SILVER_PER_GOLD),
                                Optional.empty(),
                                new ItemStack(RDItems.GOLD_COIN.asItem(), 1),
                                12,
                                5,
                                PRICE_MULTIPLIER
                        ),
                        (level, entity, random) -> new MerchantOffer(
                                new ItemCost(RDItems.GOLD_COIN, 1),
                                Optional.empty(),
                                new ItemStack(RDItems.SILVER_COIN.asItem(), SILVER_PER_GOLD),
                                12,
                                5,
                                PRICE_MULTIPLIER
                        )
                )).toArray(new VillagerTrades.ItemListing[0])
        );
    }

}
