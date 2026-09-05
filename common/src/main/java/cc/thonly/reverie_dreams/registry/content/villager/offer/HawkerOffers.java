package cc.thonly.reverie_dreams.registry.content.villager.offer;

import cc.thonly.reverie_dreams.block.bundle.CropBlockBundle;
import cc.thonly.reverie_dreams.data.FoodProperty;
import cc.thonly.reverie_dreams.registry.content.FoodProperties;
import cc.thonly.reverie_dreams.registry.content.item.RDIngredientItems;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import cc.thonly.reverie_dreams.registry.content.villager.RDVillagerTrades;
import cc.thonly.reverie_dreams.registry.delegate.ItemDelegate;
import cc.thonly.reverie_dreams.registry.delegate.RegistryDelegate;
import cc.thonly.reverie_dreams.registry.tag.RDVillagerTradeTags;
import cc.thonly.reverie_dreams.util.trading.TradeCost;
import cc.thonly.reverie_dreams.util.trading.VillagerTrade;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.npc.villager.VillagerTrades;
import net.minecraft.world.item.Item;
import cc.thonly.keine.item.ItemStackTemplate;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import java.util.*;

public class HawkerOffers {
    public static final String TEMPLATE = RDVillagerTrades.HAWKERS_LEVEL_TEMPLATE;

    public static List<Tuple<Integer, VillagerTrades.ItemListing>> makeOffers() {
        List<Tuple<Integer, VillagerTrades.ItemListing>> offers = new ArrayList<>();

        // Level 1
        Item[] arr1 = new Item[]{
                Items.WHEAT,
                Items.POTATO,
                Items.CARROT
        };

        for (Item item : arr1) {
            offers.add(new Tuple<>(
                    1,
                    (level, entity, random) -> new MerchantOffer(
                            new ItemCost(
                                    RDItems.COPPER_COIN.get(),
                                    random.nextInt(8, 11)
                            ),
                            new ItemStack(
                                    item,
                                    random.nextInt(3, 5)
                            ),
                            7,
                            3,
                            0.2f
                    )
            ));
        }

        // Level 1 - 2 - 3
        for (RegistryDelegate<Item> ingredient : RDIngredientItems.INGREDIENTS) {
            if (RDIngredientItems.EXISTS.contains(ingredient)) {
                continue;
            }

            Item item = ingredient.get().asItem();

            Collection<FoodProperty> ingredientProperties =
                    FoodProperties.get(new ItemStackTemplate(item));

            int base = ingredientProperties.size();
            int min = (int) (base * 1.25 * 1);
            int max = (int) (base * 1.25 * 2);

            VillagerTrades.ItemListing listing = (level, entity, random) -> new MerchantOffer(
                    new ItemCost(
                            RDItems.COPPER_COIN.get(),
                            random.nextInt(3 + min, 3 + max + 1)
                    ),
                    new ItemStack(
                            item,
                            random.nextInt(3, 5)
                    ),
                    11,
                    5,
                    0.2f
            );

            offers.add(new Tuple<>(1, listing));
            offers.add(new Tuple<>(2, listing));
            offers.add(new Tuple<>(3, listing));
        }

        // Level 3
        Item[] arr3 = new Item[]{
                Items.SALMON,
                Items.COD,
                Items.TROPICAL_FISH
        };

        for (Item item : arr3) {
            offers.add(new Tuple<>(
                    3,
                    (level, entity, random) -> new MerchantOffer(
                            new ItemCost(
                                    RDItems.COPPER_COIN.get(),
                                    random.nextInt(5, 7)
                            ),
                            new ItemStack(
                                    item,
                                    random.nextInt(3, 5)
                            ),
                            7,
                            5,
                            0.2f
                    )
            ));
        }

        // Level 4
        for (Map.Entry<Identifier, CropBlockBundle.Entry> view : CropBlockBundle.getViews()) {
            CropBlockBundle.Entry entry = view.getValue();
            Item seed = entry.getSeed().asItem();

            offers.add(new Tuple<>(
                    4,
                    (level, entity, random) -> new MerchantOffer(
                            new ItemCost(
                                    RDItems.COPPER_COIN.get(),
                                    random.nextInt(7, 16)
                            ),
                            new ItemStack(
                                    seed,
                                    random.nextInt(3, 7)
                            ),
                            4,
                            3,
                            0.2f
                    )
            ));
        }

        // Level 5
        for (RegistryDelegate<Item> ingredient : RDIngredientItems.INGREDIENTS) {
            if (RDIngredientItems.EXISTS.contains(ingredient)) {
                continue;
            }

            Item item = ingredient.get().asItem();

            Collection<FoodProperty> ingredientProperties =
                    FoodProperties.get(new ItemStackTemplate(item));

            int base = ingredientProperties.size();
            int min = (int) (base * 1.25 * 1);
            int max = (int) (base * 1.25 * 2);

            offers.add(new Tuple<>(
                    5,
                    (level, entity, random) -> new MerchantOffer(
                            new ItemCost(
                                    RDItems.COPPER_COIN.get(),
                                    random.nextInt(3 + min, 3 + max + 1)
                            ),
                            new ItemStack(
                                    item,
                                    random.nextInt(3, 5)
                            ),
                            11,
                            5,
                            0.2f
                    )
            ));
        }

        return offers;
    }

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
        for (RegistryDelegate<Item> ingredient : RDIngredientItems.INGREDIENTS) {
            if (RDIngredientItems.EXISTS.contains(ingredient)) {
                continue;
            }
            Item item = ingredient.get().asItem();
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
        for (RegistryDelegate<Item> ingredient : RDIngredientItems.INGREDIENTS) {
            if (RDIngredientItems.EXISTS.contains(ingredient)) {
                continue;
            }
            Item item = ingredient.get().asItem();
            ResourceKey<VillagerTrade> key = builder.keyInstance(RDVillagerTradeTags.HAWKERS_LEVEL_5, TEMPLATE, 5, item);
            makeCommon(builder, ingredient, key);
        }
    }

    private static void makeCommon(RDVillagerTrades.PreparingTradeInfo builder, RegistryDelegate<Item> ingredient, ResourceKey<VillagerTrade> key) {
        Collection<FoodProperty> ingredientProperties = FoodProperties.get(new ItemStackTemplate(ingredient.get().asItem()));
        int base = ingredientProperties.size();
        int min = (int) (base * 1.25 * 1);
        int max = (int) (base * 1.25 * 2);
        builder.add(key, new VillagerTrade(
                new TradeCost(RDItems.COPPER_COIN,
                        UniformGenerator.between(3 + min, 3 + max)
                ),
                Optional.empty(),
                new ItemStackTemplate(ingredient.get().asItem()),
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