package cc.thonly.reverie_dreams.entity.villager;

import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;

import java.util.Optional;

public class MoneyShopClerk {
    private static final float PRICE_MULTIPLIER = 0.13f;
    public static final int COPPER_PER_SILVER = 5;
    public static final int SILVER_PER_GOLD = 10;

    public static int toCopper(int gold, int silver, int copper) {
        return gold * SILVER_PER_GOLD * COPPER_PER_SILVER + silver * COPPER_PER_SILVER + copper;
    }

    public static void registers() {
        // 等级 1：铜 <-> 银
        TradeOfferHelper.registerVillagerOffers(RDVillagerProfessions.MONEY_SHOP_CLERK, 1, factories -> {
            // 铜 → 银
            factories.add((level, entity, random) -> new MerchantOffer(
                    new ItemCost(RDItems.COPPER_COIN, COPPER_PER_SILVER),
                    Optional.empty(),
                    new ItemStack(RDItems.SILVER_COIN, 1),
                    16, // 最大交易次数
                    2,  // 村民经验
                    PRICE_MULTIPLIER
            ));
            // 银 → 铜
            factories.add((level, entity, random) -> new MerchantOffer(
                    new ItemCost(RDItems.SILVER_COIN, 1),
                    Optional.empty(),
                    new ItemStack(RDItems.COPPER_COIN, COPPER_PER_SILVER),
                    16,
                    2,
                    PRICE_MULTIPLIER
            ));
        });

        // 等级 2：银 <-> 金
        TradeOfferHelper.registerVillagerOffers(RDVillagerProfessions.MONEY_SHOP_CLERK, 2, factories -> {
            // 银 → 金
            factories.add((level, entity, random) -> new MerchantOffer(
                    new ItemCost(RDItems.SILVER_COIN, SILVER_PER_GOLD),
                    Optional.empty(),
                    new ItemStack(RDItems.GOLD_COIN, 1),
                    12,
                    5,
                    PRICE_MULTIPLIER
            ));
            // 金 → 银
            factories.add((level, entity, random) -> new MerchantOffer(
                    new ItemCost(RDItems.GOLD_COIN, 1),
                    Optional.empty(),
                    new ItemStack(RDItems.SILVER_COIN, SILVER_PER_GOLD),
                    12,
                    5,
                    PRICE_MULTIPLIER
            ));
        });
    }
}
