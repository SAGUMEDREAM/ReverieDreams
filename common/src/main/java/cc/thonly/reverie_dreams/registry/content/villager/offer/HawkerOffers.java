package cc.thonly.reverie_dreams.registry.content.villager.offer;

import cc.thonly.reverie_dreams.block.bundle.CropBlockBundle;
import cc.thonly.reverie_dreams.data.FoodProperty;
import cc.thonly.reverie_dreams.registry.content.FoodProperties;
import cc.thonly.reverie_dreams.registry.content.item.RDIngredientItems;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import cc.thonly.reverie_dreams.registry.content.villager.RDVillagerTrades;
import cc.thonly.reverie_dreams.registry.delegate.ItemDelegate;
import cc.thonly.reverie_dreams.registry.tag.RDVillagerTradeTags;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.TradeCost;
import net.minecraft.world.item.trading.VillagerTrade;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class HawkerOffers {
    public static final String TEMPLATE = RDVillagerTrades.HAWKERS_LEVEL_TEMPLATE;
    public static void makeOffers(RDVillagerTrades.PreparingTradeInfo builder) {
        // Level 1
        Item[] arr1 = new Item[]{
                Items.WHEAT,
                Items.POTATO,
                Items.CARROT
        };
        for (Item item : arr1) {
            ResourceKey<VillagerTrade> key = builder.keyInstance(RDVillagerTradeTags.HAWKERS_LEVEL_1, TEMPLATE, 1, item);
            builder.add(key, new VillagerTrade(
                    new TradeCost(RDItems.COPPER_COIN, UniformGenerator.between(8, 10)),
                    Optional.empty(),
                    new ItemStackTemplate(item),
                    7,
                    3,
                    0.2f,
                    Optional.empty(),
                    List.of(SetItemCountFunction.setCount(UniformGenerator.between(3, 4)).build())
            ));
        }

        // Level 1 - 2
        for (ItemDelegate ingredient : RDIngredientItems.INGREDIENTS) {
            Item item = ingredient.asItem();
            {
                ResourceKey<VillagerTrade> key = builder.keyInstance(RDVillagerTradeTags.HAWKERS_LEVEL_1, TEMPLATE, 1, item);
                makeCommon(builder, ingredient, key);
            }
            {
                ResourceKey<VillagerTrade> key = builder.keyInstance(RDVillagerTradeTags.HAWKERS_LEVEL_2, TEMPLATE, 2, item);
                makeCommon(builder, ingredient, key);
            }
            {
                ResourceKey<VillagerTrade> key = builder.keyInstance(RDVillagerTradeTags.HAWKERS_LEVEL_3, TEMPLATE, 3, item);
                makeCommon(builder, ingredient, key);
            }
        }
        // Level 3
        Item[] arr3 = new Item[]{
                Items.SALMON,
                Items.COD,
                Items.TROPICAL_FISH
        };
        for (Item item : arr3) {
            ResourceKey<VillagerTrade> key = builder.keyInstance(RDVillagerTradeTags.HAWKERS_LEVEL_3, TEMPLATE, 3, item);
            builder.add(key, new VillagerTrade(
                    new TradeCost(RDItems.COPPER_COIN,
                            UniformGenerator.between(5, 6)
                    ),
                    Optional.empty(),
                    new ItemStackTemplate(item),
                    7,
                    5,
                    0.2f,
                    Optional.empty(),
                    List.of(
                            SetItemCountFunction.setCount(UniformGenerator.between(3, 4)).build()
                    )
            ));
        }
        // Level 4
        for (Map.Entry<Identifier, CropBlockBundle.Entry> view : CropBlockBundle.getViews()) {
            CropBlockBundle.Entry entry = view.getValue();
            Item seed = entry.getSeed().asItem();
            ResourceKey<VillagerTrade> key = builder.keyInstance(RDVillagerTradeTags.HAWKERS_LEVEL_4, TEMPLATE, 4, seed);
            builder.add(key, new VillagerTrade(
                    new TradeCost(RDItems.COPPER_COIN,
                            UniformGenerator.between(7, 15)
                    ),
                    Optional.empty(),
                    new ItemStackTemplate(seed),
                    4,
                    3,
                    0.2f,
                    Optional.empty(),
                    List.of(
                            SetItemCountFunction.setCount(UniformGenerator.between(3, 6)).build()
                    )
            ));
        }
        // Level 5
        for (ItemDelegate ingredient : RDIngredientItems.INGREDIENTS) {
            Item item = ingredient.asItem();
            ResourceKey<VillagerTrade> key = builder.keyInstance(RDVillagerTradeTags.HAWKERS_LEVEL_5, TEMPLATE, 5, item);
            makeCommon(builder, ingredient, key);
        }
    }

    private static void makeCommon(RDVillagerTrades.PreparingTradeInfo builder, ItemDelegate ingredient, ResourceKey<VillagerTrade> key) {
        Collection<FoodProperty> ingredientProperties = FoodProperties.get(new ItemStackTemplate(ingredient.asItem()));
        int base = ingredientProperties.size();
        int min = (int) (base * 1.25 * 1);
        int max = (int) (base * 1.25 * 2);
        builder.add(key, new VillagerTrade(
                new TradeCost(RDItems.COPPER_COIN,
                        UniformGenerator.between(3 + min, 3 + max)
                ),
                Optional.empty(),
                new ItemStackTemplate(ingredient.asItem()),
                11,
                5,
                0.2f,
                Optional.empty(),
                List.of(
                        SetItemCountFunction.setCount(UniformGenerator.between(3, 4)).build()
                )
        ));
    }
}