package cc.thonly.reverie_dreams.registry.content.villager.offer;

import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import cc.thonly.reverie_dreams.registry.content.villager.RDVillagerTrades;
import cc.thonly.reverie_dreams.registry.tag.RDVillagerTradeTags;
import cc.thonly.reverie_dreams.util.trading.TradeCost;
import cc.thonly.reverie_dreams.util.trading.VillagerTrade;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import cc.thonly.keine.item.ItemStackTemplate;
import net.minecraft.world.entity.npc.villager.VillagerTrades;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.functions.SetEnchantmentsFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import java.util.List;
import java.util.Optional;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.util.Tuple;

import java.util.ArrayList;

public class PriestOffers {
    public static final String TEMPLATE = RDVillagerTrades.PRIEST_LEVEL_TEMPLATE;

    public static List<Tuple<Integer, VillagerTrades.ItemListing>> makeOffers() {
        List<Tuple<Integer, VillagerTrades.ItemListing>> offers = new ArrayList<>();

        // Level 1
        {
            offers.add(new Tuple<>(
                    1,
                    (level, entity, random) -> new MerchantOffer(
                            new ItemCost(
                                    RDItems.POWER.get(),
                                    11
                            ),
                            new ItemStack(
                                    RDItems.COPPER_COIN.get(),
                                    3
                            ),
                            7,
                            5,
                            0.2f
                    )
            ));
        }

        {
            offers.add(new Tuple<>(
                    1,
                    (level, entity, random) -> new MerchantOffer(
                            new ItemCost(
                                    RDItems.POINT.get(),
                                    12
                            ),
                            new ItemStack(
                                    RDItems.COPPER_COIN.get(),
                                    3
                            ),
                            7,
                            5,
                            0.13f
                    )
            ));
        }

        // Level 2
        {
            offers.add(new Tuple<>(
                    2,
                    (level, entity, random) -> new MerchantOffer(
                            new ItemCost(
                                    RDItems.POWER.get(),
                                    38
                            ),
                            Optional.of(new ItemCost(
                                    RDItems.COPPER_COIN.get(),
                                    24
                            )),
                            new ItemStack(
                                    RDItems.EXORCISM_PAPER.get(),
                                    6
                            ),
                            4,
                            5,
                            0.2f
                    )
            ));
        }

        {
            offers.add(new Tuple<>(
                    2,
                    (level, entity, random) -> {
                        ItemStack stack = new ItemStack(Items.WOODEN_SWORD);

                        stack.enchant(
                                level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow( Enchantments.SMITE),
                                4
                        );

                        stack.enchant(
                                level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.UNBREAKING),
                                2
                        );

                        return new MerchantOffer(
                                new ItemCost(
                                        RDItems.POINT.get(),
                                        29
                                ),
                                Optional.of(new ItemCost(
                                        RDItems.COPPER_COIN.get(),
                                        31
                                )),
                                stack,
                                7,
                                5,
                                0.13f
                        );
                    }
            ));
        }

        {
            offers.add(new Tuple<>(
                    2,
                    (level, entity, random) -> new MerchantOffer(
                            new ItemCost(
                                    RDItems.COPPER_COIN.get(),
                                    29
                            ),
                            new ItemStack(
                                    RDItems.HAKUREI_CANE.get(),
                                    1
                            ),
                            7,
                            5,
                            0.13f
                    )
            ));
        }

        return offers;
    }

    public static void makeOffers(RDVillagerTrades.PreparingTradeInfo builder) {
        HolderGetter<Enchantment> enchantments = builder.getEnchantmentRegistry();
        // Level 1
        {
            ResourceKey<VillagerTrade> key = builder.keyInstance(
                    RDVillagerTradeTags.PRIEST_LEVEL_1,
                    TEMPLATE,
                    1,
                    RDItems.POWER
            );

            builder.add(key, new VillagerTrade(
                    new TradeCost(RDItems.POWER, ConstantValue.exactly(11)),
                    Optional.empty(),
                    new ItemStackTemplate(RDItems.COPPER_COIN.asItem()),
                    7,
                    5,
                    0.2f,
                    Optional.empty(),
                    List.of(
                            SetItemCountFunction.setCount(ConstantValue.exactly(3)).build()
                    )
            ));
        }
        {
            ResourceKey<VillagerTrade> key = builder.keyInstance(
                    RDVillagerTradeTags.PRIEST_LEVEL_1,
                    TEMPLATE,
                    1,
                    RDItems.POINT
            );

            builder.add(key, new VillagerTrade(
                    new TradeCost(RDItems.POINT, ConstantValue.exactly(12)),
                    Optional.empty(),
                    new ItemStackTemplate(RDItems.COPPER_COIN.asItem()),
                    7,
                    5,
                    0.13f,
                    Optional.empty(),
                    List.of(
                            SetItemCountFunction.setCount(ConstantValue.exactly(3)).build()
                    )
            ));
        }

        // Level 2
        {
            ResourceKey<VillagerTrade> key = builder.keyInstance(
                    RDVillagerTradeTags.PRIEST_LEVEL_2,
                    TEMPLATE,
                    2,
                    RDItems.EXORCISM_PAPER
            );

            builder.add(key, new VillagerTrade(
                    new TradeCost(RDItems.POWER, ConstantValue.exactly(38)),
                    Optional.of(new TradeCost(RDItems.COPPER_COIN, ConstantValue.exactly(24))),
                    new ItemStackTemplate(RDItems.EXORCISM_PAPER.asItem()),
                    4,
                    5,
                    0.2f,
                    Optional.empty(),
                    List.of(
                            SetItemCountFunction.setCount(ConstantValue.exactly(6)).build()
                    )
            ));
        }
        {
            ResourceKey<VillagerTrade> key = builder.keyInstance(
                    RDVillagerTradeTags.PRIEST_LEVEL_2,
                    TEMPLATE,
                    2,
                    Items.WOODEN_SWORD
            );

            builder.add(key, new VillagerTrade(
                    new TradeCost(RDItems.POINT, ConstantValue.exactly(29)),
                    Optional.of(new TradeCost(RDItems.COPPER_COIN, ConstantValue.exactly(31))),
                    new ItemStackTemplate(Items.WOODEN_SWORD),
                    7,
                    5,
                    0.13f,
                    Optional.empty(),
                    List.of(
                            new SetEnchantmentsFunction.Builder()
                                    .withEnchantment(enchantments.getOrThrow(Enchantments.SMITE), ConstantValue.exactly(4))
                                    .withEnchantment(enchantments.getOrThrow(Enchantments.UNBREAKING), ConstantValue.exactly(2))
                                    .build()
                    )
            ));
        }
        // Level 2
        {
            ResourceKey<VillagerTrade> key = builder.keyInstance(
                    RDVillagerTradeTags.PRIEST_LEVEL_2,
                    TEMPLATE,
                    2,
                    RDItems.HAKUREI_CANE
            );

            builder.add(key, new VillagerTrade(
                    new TradeCost(RDItems.COPPER_COIN, ConstantValue.exactly(29)),
                    Optional.empty(),
                    new ItemStackTemplate(RDItems.HAKUREI_CANE.asItem()),
                    7,
                    5,
                    0.13f,
                    Optional.empty(),
                    List.of() // 无额外处理
            ));
        }
    }
}