package cc.thonly.reverie_dreams.entity.villager;

import cc.thonly.mystias_izakaya.component.FoodProperty;
import cc.thonly.mystias_izakaya.item.MIItems;
import cc.thonly.reverie_dreams.block.CropBlockCreator;
import cc.thonly.reverie_dreams.item.ModItems;
import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOffers;
import net.minecraft.village.TradedItem;

import java.util.*;

public class Hawkers {
    public static void registers() {
        Map<Integer, List<TradeOffers.Factory>> hawkers = new HashMap<>();
        hawkers.put(1, getHawkersLevelFactories1());
        hawkers.put(2, getHawkersLevelFactories2());
        hawkers.put(3, getHawkersLevelFactories3());
        hawkers.put(4, getHawkersLevelFactories4());
        hawkers.put(5, getHawkersLevelFactories2());
        hawkers.forEach((level, list) -> {
            TradeOfferHelper.registerVillagerOffers(ModVillagerProfessions.HAWKERS, level, factories -> {
                factories.addAll(list);
            });
        });
    }

    private static List<TradeOffers.Factory> getHawkersLevelFactories1() {
        List<TradeOffers.Factory> list = new ArrayList<>();
        Item[] arr =new Item[] {
                Items.WHEAT,
                Items.POTATO,
                Items.CARROT
        };
        for (Item item : arr) {
            list.add(((entity, random) -> new TradeOffer(
                    new TradedItem(ModItems.COPPER_COIN, (int) (7 + (1.25 * random.nextBetween(1, 3)))),
                    Optional.empty(),
                    new ItemStack(item, 2 + random.nextBetween(1, 2)),
                    7,
                    3,
                    0.2f
            )));
        }
        return list;
    }

    private static List<TradeOffers.Factory> getHawkersLevelFactories2() {
        List<TradeOffers.Factory> list = new ArrayList<>();
        for (Item ingredient : MIItems.INGREDIENTS) {
            list.add((entity, random) -> {
                List<FoodProperty> ingredientProperties = FoodProperty.getIngredientProperties(ingredient);
                int val0 = (int) (ingredientProperties.size() * 1.25 * random.nextBetween(1, 2));
                return new TradeOffer(
                        new TradedItem(ModItems.COPPER_COIN, 3 + val0),
                        Optional.empty(),
                        new ItemStack(ingredient, 2 + random.nextBetween(1, 2)),
                        11,
                        5,
                        0.2f
                );
            });
        }
        return list;
    }

    private static List<TradeOffers.Factory> getHawkersLevelFactories3() {
        Item[] arr = new Item[] {
               Items.SALMON,
               Items.COD,
               Items.TROPICAL_FISH
        };
        List<TradeOffers.Factory> list = new ArrayList<>();
        for (Item item : arr) {
            list.add(((entity, random) -> new TradeOffer(
                    new TradedItem(ModItems.COPPER_COIN, (int) (4 + (1.25 * random.nextBetween(1, 2)))),
                    Optional.empty(),
                    new ItemStack(item, 2 + random.nextBetween(1, 2)),
                    7,
                    5,
                    0.2f
            )));
        }
        return list;
    }

    private static List<TradeOffers.Factory> getHawkersLevelFactories4() {
        List<TradeOffers.Factory> list = new ArrayList<>();
        for (Map.Entry<Identifier, CropBlockCreator.Instance> view : CropBlockCreator.getViews()) {
            CropBlockCreator.Instance instance = view.getValue();
            Item seed = instance.getSeed();
            list.add((entity, random) -> new TradeOffer(
                    new TradedItem(ModItems.COPPER_COIN, 7 + random.nextBetween(0, 8)),
                    Optional.empty(),
                    new ItemStack(seed, 3 + random.nextBetween(0, 3)),
                    4,
                    3,
                    0.2f
            ));
        }
        return list;
    }
}