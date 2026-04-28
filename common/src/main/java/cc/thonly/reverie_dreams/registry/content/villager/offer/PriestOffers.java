package cc.thonly.reverie_dreams.registry.content.villager.offer;

import cc.thonly.reverie_dreams.registry.content.villager.RDVillagerProfessions;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import cc.thonly.reverie_dreams.util.ListMaker;
import net.blay09.mods.balm.world.entity.npc.villager.BalmVillagerTradeRegistrar;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.npc.villager.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;

import java.util.List;
import java.util.Optional;

public class PriestOffers {
    @SuppressWarnings("DataFlowIssue")
    public static void registers(BalmVillagerTradeRegistrar tradeRegistrar) {
        tradeRegistrar.registerTrade(
                RDVillagerProfessions.PRIEST_KEY,
                1,
                ListMaker.of(() -> List.of(
                        (VillagerTrades.ItemListing) (level, entity, random) -> new MerchantOffer(
                                new ItemCost(RDItems.POWER, 11),
                                Optional.empty(),
                                new ItemStack(RDItems.COPPER_COIN.asItem(), 3),
                                7,
                                5,
                                0.2f
                        ),
                        (level, entity, random) -> new MerchantOffer(
                                new ItemCost(RDItems.POINT, 12),
                                Optional.empty(),
                                new ItemStack(RDItems.COPPER_COIN.asItem(), 3),
                                7,
                                5,
                                0.13f
                        )
                )).toArray(new VillagerTrades.ItemListing[0])
        );
        tradeRegistrar.registerTrade(
                RDVillagerProfessions.PRIEST_KEY,
                2,
                ListMaker.of(() -> List.of(
                        (VillagerTrades.ItemListing) (level, entity, random) -> new MerchantOffer(
                                new ItemCost(RDItems.POWER, 38),
                                Optional.of(new ItemCost(RDItems.COPPER_COIN, 24)),
                                new ItemStack(RDItems.EXORCISM_PAPER.asItem(), 6),
                                4,
                                5,
                                0.2f
                        ),

                        (level, entity, random) -> {
                            RegistryAccess registryManager = entity.registryAccess();
                            ItemStack woodenSwordStack = Items.WOODEN_SWORD.getDefaultInstance();
                            Registry<Enchantment> lookup = registryManager.lookupOrThrow(Registries.ENCHANTMENT);

                            woodenSwordStack.enchant(
                                    lookup.wrapAsHolder(lookup.getValue(Enchantments.SMITE)), 4
                            );
                            woodenSwordStack.enchant(
                                    lookup.wrapAsHolder(lookup.getValue(Enchantments.UNBREAKING)), 2
                            );

                            return new MerchantOffer(
                                    new ItemCost(RDItems.POINT, 29),
                                    Optional.of(new ItemCost(RDItems.COPPER_COIN, 31)),
                                    woodenSwordStack,
                                    7,
                                    5,
                                    0.13f
                            );
                        },

                        (level, entity, random) -> new MerchantOffer(
                                new ItemCost(RDItems.COPPER_COIN, 29),
                                Optional.empty(),
                                new ItemStack(RDItems.HAKUREI_CANE.asItem()),
                                7,
                                5,
                                0.13f
                        )
                )).toArray(new VillagerTrades.ItemListing[0])
        );
    }
}