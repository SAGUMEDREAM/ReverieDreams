package cc.thonly.reverie_dreams.registry.content.villager.offer;

import cc.thonly.reverie_dreams.registry.content.item.RDIngredientItems;
import cc.thonly.reverie_dreams.util.ListMaker;
import net.blay09.mods.balm.world.entity.npc.villager.BalmVillagerTradeRegistrar;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.entity.npc.villager.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;

import java.util.*;

public class MystiaModOffers {
    public static void registers(BalmVillagerTradeRegistrar tradeRegistrar) {

        // BUTCHER Lv.2
        tradeRegistrar.registerTrade(
                VillagerProfession.BUTCHER,
                2,
                ListMaker.of(() -> List.of(
                        (VillagerTrades.ItemListing) (level, entity, random) -> new MerchantOffer(
                                new ItemCost(Items.EMERALD, 6),
                                new ItemStack(RDIngredientItems.VENISON.asItem(), 5),
                                4, 10, 0.05f
                        )
                )).toArray(new VillagerTrades.ItemListing[0])
        );

        // BUTCHER Lv.3
        tradeRegistrar.registerTrade(
                VillagerProfession.BUTCHER,
                3,
                ListMaker.of(() -> List.of(
                        (VillagerTrades.ItemListing) (level, entity, random) -> new MerchantOffer(
                                new ItemCost(Items.EMERALD, 10),
                                new ItemStack(RDIngredientItems.WAGYU_BEEF.asItem(), 6),
                                4, 10, 0.05f
                        ),
                        (level, entity, random) -> new MerchantOffer(
                                new ItemCost(Items.EMERALD, 8),
                                new ItemStack(RDIngredientItems.WILD_BOAR_MEAT.asItem(), 5),
                                4, 10, 0.05f
                        )
                )).toArray(new VillagerTrades.ItemListing[0])
        );

        // FARMER Lv.2
        tradeRegistrar.registerTrade(
                VillagerProfession.FARMER,
                2,
                ListMaker.of(() -> List.of(
                        (VillagerTrades.ItemListing) (level, entity, random) -> new MerchantOffer(
                                new ItemCost(Items.EMERALD, 2),
                                new ItemStack(RDIngredientItems.STICKY_RICE.asItem(), 4),
                                6, 3, 0.04f
                        ),
                        (level, entity, random) -> new MerchantOffer(
                                new ItemCost(Items.EMERALD, 3),
                                new ItemStack(RDIngredientItems.PLUM.asItem(), 4),
                                6, 3, 0.04f
                        ),
                        (level, entity, random) -> new MerchantOffer(
                                new ItemCost(Items.EMERALD, 7),
                                new ItemStack(RDIngredientItems.PINE_NUT.asItem(), 3),
                                6, 3, 0.04f
                        )
                )).toArray(new VillagerTrades.ItemListing[0])
        );

        // FARMER Lv.3
        tradeRegistrar.registerTrade(
                VillagerProfession.FARMER,
                3,
                ListMaker.of(() -> List.of(
                        (VillagerTrades.ItemListing) (level, entity, random) -> new MerchantOffer(
                                new ItemCost(Items.EMERALD, 4),
                                new ItemStack(RDIngredientItems.CHESTNUT.asItem(), 5),
                                5, 5, 0.05f
                        ),
                        (level, entity, random) -> new MerchantOffer(
                                new ItemCost(Items.EMERALD, 5),
                                new ItemStack(RDIngredientItems.PLUM.asItem(), 6),
                                5, 5, 0.05f
                        )
                )).toArray(new VillagerTrades.ItemListing[0])
        );

        // FARMER Lv.4
        tradeRegistrar.registerTrade(
                VillagerProfession.FARMER,
                4,
                ListMaker.of(() -> List.of(
                        (VillagerTrades.ItemListing) (level, entity, random) -> new MerchantOffer(
                                new ItemCost(Items.EMERALD, 11),
                                new ItemStack(RDIngredientItems.PUFF_YO_FRUIT.asItem(), 4),
                                3, 10, 0.05f
                        ),
                        (level, entity, random) -> new MerchantOffer(
                                new ItemCost(Items.EMERALD, 15),
                                new ItemStack(RDIngredientItems.FICUS_MICROCARPA.asItem(), 3),
                                2, 15, 0.05f
                        )
                )).toArray(new VillagerTrades.ItemListing[0])
        );
    }
}
