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