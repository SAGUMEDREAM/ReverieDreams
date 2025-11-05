package cc.thonly.reverie_dreams.entity.villager;

import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import java.util.Optional;

public class Priest {
    public static void registers() {
        TradeOfferHelper.registerVillagerOffers(RDVillagerProfessions.PRIEST, 1, factories -> {
            factories.add((entity, random) -> new MerchantOffer(
                            new ItemCost(RDItems.POWER, 11),
                            Optional.empty(),
                            new ItemStack(RDItems.COPPER_COIN, 3),
                            7,
                            5,
                            0.2f
                    )
            );
            factories.add((entity, random) -> new MerchantOffer(
                            new ItemCost(RDItems.POINT, 12),
                            Optional.empty(),
                            new ItemStack(RDItems.COPPER_COIN, 3),
                            7,
                            5,
                            0.13f
                    )
            );
        });
        TradeOfferHelper.registerVillagerOffers(RDVillagerProfessions.PRIEST, 2, factories -> {
            factories.add((entity, random) -> new MerchantOffer(
                            new ItemCost(RDItems.POWER, 38),
                            Optional.of(new ItemCost(RDItems.COPPER_COIN, 24)),
                            new ItemStack(RDItems.EXORCISM_PAPER, 6),
                            4,
                            5,
                            0.2f
                    )
            );
            factories.add((entity, random) -> {
                RegistryAccess registryManager = entity.registryAccess();
                ItemStack woodenSwordStack = Items.WOODEN_SWORD.getDefaultInstance();
                Registry<Enchantment> lookup = registryManager.lookupOrThrow(Registries.ENCHANTMENT);
                woodenSwordStack.enchant(lookup.wrapAsHolder(lookup.getValue(Enchantments.SMITE)), 4);
                woodenSwordStack.enchant(lookup.wrapAsHolder(lookup.getValue(Enchantments.UNBREAKING)), 2);
                return new MerchantOffer(
                        new ItemCost(RDItems.POINT, 29),
                        Optional.of(new ItemCost(RDItems.COPPER_COIN, 31)),
                        woodenSwordStack,
                        7,
                        5,
                        0.13f
                );
            });
            factories.add((entity, random) -> {
              return new MerchantOffer(
                      new ItemCost(RDItems.COPPER_COIN, 29),
                        Optional.empty(),
                        new ItemStack(RDItems.HAKUREI_CANE),
                        7,
                        5,
                        0.13f
                );
            });
        });
    }
}