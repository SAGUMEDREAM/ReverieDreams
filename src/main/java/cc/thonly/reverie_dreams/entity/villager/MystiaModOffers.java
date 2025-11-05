package cc.thonly.reverie_dreams.entity.villager;

import cc.thonly.reverie_dreams.registry.content.item.RDIngredientItems;
import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;

public class MystiaModOffers {
    public static void registers() {
        TradeOfferHelper.registerVillagerOffers(VillagerProfession.BUTCHER, 2, factories -> {
            // Lv.3
            factories.add((e, r) -> new MerchantOffer(
                    new ItemCost(Items.EMERALD, 6),
                    new ItemStack(RDIngredientItems.VENISON, 5),
                    4, 10, 0.05f
            ));

        });
        TradeOfferHelper.registerVillagerOffers(VillagerProfession.BUTCHER, 3, factories -> {
            // Lv.4
            factories.add((e, r) -> new MerchantOffer(
                    new ItemCost(Items.EMERALD, 10),
                    new ItemStack(RDIngredientItems.WAGYU_BEEF, 6),
                    4, 10, 0.05f
            ));
            factories.add((e, r) -> new MerchantOffer(
                    new ItemCost(Items.EMERALD, 8),
                    new ItemStack(RDIngredientItems.WILD_BOAR_MEAT, 5),
                    4, 10, 0.05f
            ));

        });
        TradeOfferHelper.registerVillagerOffers(VillagerProfession.FARMER, 2, factories -> {
            // Lv.2：常见农作物，便宜交易
            factories.add((e, r) -> new MerchantOffer(
                    new ItemCost(Items.EMERALD, 2),
                    new ItemStack(RDIngredientItems.STICKY_RICE, 4),
                    6, 3, 0.04f
            ));
            factories.add((e, r) -> new MerchantOffer(
                    new ItemCost(Items.EMERALD, 3),
                    new ItemStack(RDIngredientItems.PLUM, 4),
                    6, 3, 0.04f
            ));
            factories.add((e, r) -> new MerchantOffer(
                    new ItemCost(Items.EMERALD, 7),
                    new ItemStack(RDIngredientItems.PINE_NUT, 3),
                    6, 3, 0.04f
            ));
        });
        TradeOfferHelper.registerVillagerOffers(VillagerProfession.FARMER, 3, factories -> {
            // Lv.3：中等水果
            factories.add((e, r) -> new MerchantOffer(
                    new ItemCost(Items.EMERALD, 4),
                    new ItemStack(RDIngredientItems.CHESTNUT, 5),
                    5, 5, 0.05f
            ));
            factories.add((e, r) -> new MerchantOffer(
                    new ItemCost(Items.EMERALD, 5),
                    new ItemStack(RDIngredientItems.PLUM, 6),
                    5, 5, 0.05f
            ));
        });
        TradeOfferHelper.registerVillagerOffers(VillagerProfession.FARMER, 4, factories -> {
            // Lv.4：稀有水果
            factories.add((e, r) -> new MerchantOffer(
                    new ItemCost(Items.EMERALD, 11),
                    new ItemStack(RDIngredientItems.PUFF_YO_FRUIT, 4),
                    3, 10, 0.05f
            ));
            factories.add((e, r) -> new MerchantOffer(
                    new ItemCost(Items.EMERALD, 15),
                    new ItemStack(RDIngredientItems.FICUS_MICROCARPA, 3),
                    2, 15, 0.05f
            ));
        });
    }
}
