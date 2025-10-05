package cc.thonly.reverie_dreams.entity.villager;

import cc.thonly.reverie_dreams.item.ModItems;
import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.random.Random;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOffers;
import net.minecraft.village.TradedItem;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class MoneyShopClerk {
    private static final float PRICE_MULTIPLIER = 0.15f;
    public static final int COPPER_PER_SILVER = 10;
    public static final int SILVER_PER_GOLD = 20;

    public static int toCopper(int gold, int silver, int copper) {
        return gold * SILVER_PER_GOLD * COPPER_PER_SILVER + silver * COPPER_PER_SILVER + copper;
    }
    public static void registers() {
        // 等级 1：铜 <-> 银
        TradeOfferHelper.registerVillagerOffers(ModVillagerProfessions.MONEY_SHOP_CLERK, 1, factories -> {
            // 铜 → 银
            factories.add((entity, random) -> new TradeOffer(
                    new TradedItem(ModItems.COPPER_COIN, COPPER_PER_SILVER),
                    Optional.empty(),
                    new ItemStack(ModItems.SILVER_COIN, 1),
                    16, // 最大交易次数
                    2,  // 村民经验
                    PRICE_MULTIPLIER
            ));
            // 银 → 铜
            factories.add((entity, random) -> new TradeOffer(
                    new TradedItem(ModItems.SILVER_COIN, 1),
                    Optional.empty(),
                    new ItemStack(ModItems.COPPER_COIN, COPPER_PER_SILVER),
                    16,
                    2,
                    PRICE_MULTIPLIER
            ));
        });

        // 等级 2：银 <-> 金
        TradeOfferHelper.registerVillagerOffers(ModVillagerProfessions.MONEY_SHOP_CLERK, 2, factories -> {
            // 银 → 金
            factories.add((entity, random) -> new TradeOffer(
                    new TradedItem(ModItems.SILVER_COIN, SILVER_PER_GOLD),
                    Optional.empty(),
                    new ItemStack(ModItems.GOLD_COIN, 1),
                    12,
                    5,
                    PRICE_MULTIPLIER
            ));
            // 金 → 银
            factories.add((entity, random) -> new TradeOffer(
                    new TradedItem(ModItems.GOLD_COIN, 1),
                    Optional.empty(),
                    new ItemStack(ModItems.SILVER_COIN, SILVER_PER_GOLD),
                    12,
                    5,
                    PRICE_MULTIPLIER
            ));
        });
    }
}
