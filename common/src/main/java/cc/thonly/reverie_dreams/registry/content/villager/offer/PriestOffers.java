package cc.thonly.reverie_dreams.registry.content.villager.offer;

import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import cc.thonly.reverie_dreams.registry.content.villager.RDVillagerTrades;
import cc.thonly.reverie_dreams.registry.tag.RDVillagerTradeTags;
import net.minecraft.core.HolderGetter;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.trading.TradeCost;
import net.minecraft.world.item.trading.VillagerTrade;
import net.minecraft.world.level.storage.loot.functions.SetEnchantmentsFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import java.util.List;
import java.util.Optional;

public class PriestOffers {
    public static final String TEMPLATE = RDVillagerTrades.PRIEST_LEVEL_TEMPLATE;

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
                    new ItemStackTemplate(RDItems.EXORCISM_PAPER.asItem(), 16),
                    4,
                    5,
                    0.2f,
                    Optional.empty(),
                    List.of(
                            SetItemCountFunction.setCount(ConstantValue.exactly(16)).build()
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

        // Level 3: fairy dust offerings for exorcism paper
        {
            ResourceKey<VillagerTrade> key = builder.keyInstance(
                    RDVillagerTradeTags.PRIEST_LEVEL_3,
                    TEMPLATE,
                    3,
                    RDItems.EXORCISM_PAPER
            );

            builder.add(key, new VillagerTrade(
                    new TradeCost(RDItems.FAIRY_DUST, ConstantValue.exactly(16)),
                    Optional.of(new TradeCost(RDItems.COPPER_COIN, ConstantValue.exactly(8))),
                    new ItemStackTemplate(RDItems.EXORCISM_PAPER.asItem(), 16),
                    4,
                    10,
                    0.15f,
                    Optional.empty(),
                    List.of(
                            SetItemCountFunction.setCount(ConstantValue.exactly(16)).build()
                    )
            ));
        }

        // Level 4: upgraded silver smite sword
        {
            ResourceKey<VillagerTrade> key = builder.keyInstance(
                    RDVillagerTradeTags.PRIEST_LEVEL_4,
                    TEMPLATE,
                    4,
                    RDItems.SILVER_SWORD
            );

            builder.add(key, new VillagerTrade(
                    new TradeCost(RDItems.POINT, ConstantValue.exactly(48)),
                    Optional.of(new TradeCost(RDItems.COPPER_COIN, ConstantValue.exactly(48))),
                    new ItemStackTemplate(RDItems.SILVER_SWORD.asItem()),
                    3,
                    20,
                    0.1f,
                    Optional.empty(),
                    List.of(
                            new SetEnchantmentsFunction.Builder()
                                    .withEnchantment(enchantments.getOrThrow(Enchantments.SMITE), ConstantValue.exactly(5))
                                    .withEnchantment(enchantments.getOrThrow(Enchantments.UNBREAKING), ConstantValue.exactly(3))
                                    .build()
                    )
            ));
        }

        // Level 5: bulk fairy dust offering for blessed paper
        {
            ResourceKey<VillagerTrade> key = builder.keyInstance(
                    RDVillagerTradeTags.PRIEST_LEVEL_5,
                    TEMPLATE,
                    5,
                    RDItems.EXORCISM_PAPER
            );

            builder.add(key, new VillagerTrade(
                    new TradeCost(RDItems.FAIRY_DUST, ConstantValue.exactly(32)),
                    Optional.of(new TradeCost(RDItems.GOLD_COIN, ConstantValue.exactly(1))),
                    new ItemStackTemplate(RDItems.EXORCISM_PAPER.asItem(), 16),
                    3,
                    30,
                    0.1f,
                    Optional.empty(),
                    List.of(
                            SetItemCountFunction.setCount(ConstantValue.exactly(32)).build()
                    )
            ));
        }

        // Level 4: the priest buys youkai relics — oni horns
        {
            ResourceKey<VillagerTrade> key = builder.keyInstance(
                    RDVillagerTradeTags.PRIEST_LEVEL_4,
                    TEMPLATE,
                    4,
                    RDItems.ONI_HORN
            );

            builder.add(key, new VillagerTrade(
                    new TradeCost(RDItems.ONI_HORN, ConstantValue.exactly(2)),
                    Optional.empty(),
                    new ItemStackTemplate(RDItems.SILVER_COIN.asItem()),
                    6,
                    15,
                    0.2f,
                    Optional.empty(),
                    List.of(
                            SetItemCountFunction.setCount(ConstantValue.exactly(8)).build()
                    )
            ));
        }
    }
}