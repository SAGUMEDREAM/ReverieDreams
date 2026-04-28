package cc.thonly.reverie_dreams.registry.content.villager.offer;

import cc.thonly.reverie_dreams.data.FoodProperty;
import cc.thonly.reverie_dreams.block.bundle.CropBlockBundle;
import cc.thonly.reverie_dreams.registry.content.FoodProperties;
import cc.thonly.reverie_dreams.registry.content.villager.RDVillagerProfessions;
import cc.thonly.reverie_dreams.registry.content.item.RDIngredientItems;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import net.blay09.mods.balm.world.entity.npc.villager.BalmVillagerTradeRegistrar;
import net.blay09.mods.balm.world.item.DeferredItem;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.npc.villager.VillagerTrades;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;

import java.util.*;

public class HawkerOffers {
    public static void registers(BalmVillagerTradeRegistrar tradeRegistrar) {
        Map<Integer, List<VillagerTrades.ItemListing>> hawkers = new HashMap<>();
        hawkers.put(1, getHawkersLevelFactories1());
        hawkers.put(2, getHawkersLevelFactories2());
        hawkers.put(3, getHawkersLevelFactories3());
        hawkers.put(4, getHawkersLevelFactories4());
        hawkers.put(5, getHawkersLevelFactories2());
        hawkers.forEach((level, list) -> {
            tradeRegistrar.registerTrade(
                    RDVillagerProfessions.HAWKERS_KEY,
                    level,
                    list.toArray(new VillagerTrades.ItemListing[0])
            );
        });
    }

    private static List<VillagerTrades.ItemListing> getHawkersLevelFactories1() {
        List<VillagerTrades.ItemListing> list = new ArrayList<>();
        Item[] arr = new Item[]{
                Items.WHEAT,
                Items.POTATO,
                Items.CARROT
        };
        for (Item item : arr) {
            list.add(((level, entity, random) -> new MerchantOffer(
                    new ItemCost(RDItems.COPPER_COIN, (int) (7 + (1.25 * random.nextIntBetweenInclusive(1, 3)))),
                    Optional.empty(),
                    new ItemStack(item, 2 + random.nextIntBetweenInclusive(1, 2)),
                    7,
                    3,
                    0.2f
            )));
        }
        return list;
    }

    private static List<VillagerTrades.ItemListing> getHawkersLevelFactories2() {
        List<VillagerTrades.ItemListing> list = new ArrayList<>();
        for (DeferredItem ingredient : RDIngredientItems.INGREDIENTS) {
            list.add((level, entity, random) -> {
                Collection<FoodProperty> ingredientProperties = FoodProperties.get(ingredient.createStack());
                int val0 = (int) (ingredientProperties.size() * 1.25 * random.nextIntBetweenInclusive(1, 2));
                return new MerchantOffer(
                        new ItemCost(RDItems.COPPER_COIN, 3 + val0),
                        Optional.empty(),
                        new ItemStack(ingredient.asItem(), 2 + random.nextIntBetweenInclusive(1, 2)),
                        11,
                        5,
                        0.2f
                );
            });
        }
        return list;
    }

    private static List<VillagerTrades.ItemListing> getHawkersLevelFactories3() {
        Item[] arr = new Item[]{
                Items.SALMON,
                Items.COD,
                Items.TROPICAL_FISH
        };
        List<VillagerTrades.ItemListing> list = new ArrayList<>();
        for (Item item : arr) {
            list.add(((level, entity, random) -> new MerchantOffer(
                    new ItemCost(RDItems.COPPER_COIN, (int) (4 + (1.25 * random.nextIntBetweenInclusive(1, 2)))),
                    Optional.empty(),
                    new ItemStack(item, 2 + random.nextIntBetweenInclusive(1, 2)),
                    7,
                    5,
                    0.2f
            )));
        }
        return list;
    }

    private static List<VillagerTrades.ItemListing> getHawkersLevelFactories4() {
        List<VillagerTrades.ItemListing> list = new ArrayList<>();
        for (Map.Entry<Identifier, CropBlockBundle.Entry> view : CropBlockBundle.getViews()) {
            CropBlockBundle.Entry entry = view.getValue();
            Item seed = entry.getSeed().asItem();
            list.add((level, entity, random) -> new MerchantOffer(
                    new ItemCost(RDItems.COPPER_COIN, 7 + random.nextIntBetweenInclusive(0, 8)),
                    Optional.empty(),
                    new ItemStack(seed, 3 + random.nextIntBetweenInclusive(0, 3)),
                    4,
                    3,
                    0.2f
            ));
        }
        return list;
    }
}