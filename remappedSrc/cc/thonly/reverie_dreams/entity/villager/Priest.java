package cc.thonly.reverie_dreams.entity.villager;

import cc.thonly.reverie_dreams.item.ModItems;
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
        TradeOfferHelper.registerVillagerOffers(ModVillagerProfessions.PRIEST, 1, factories -> {
            factories.add((entity, random) -> new MerchantOffer(
                            new ItemCost(ModItems.POWER, 11),
                            Optional.empty(),
                            new ItemStack(ModItems.COPPER_COIN, 3),
                            7,
                            5,
                            0.2f
                    )
            );
            factories.add((entity, random) -> new MerchantOffer(
                            new ItemCost(ModItems.POINT, 12),
                            Optional.empty(),
                            new ItemStack(ModItems.COPPER_COIN, 3),
                            7,
                            5,
                            0.13f
                    )
            );
        });
        TradeOfferHelper.registerVillagerOffers(ModVillagerProfessions.PRIEST, 2, factories -> {
            factories.add((entity, random) -> new MerchantOffer(
                            new ItemCost(ModItems.POWER, 38),
                            Optional.of(new ItemCost(ModItems.COPPER_COIN, 24)),
                            new ItemStack(ModItems.EXORCISM_PAPER, 6),
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
                        new ItemCost(ModItems.POINT, 29),
                        Optional.of(new ItemCost(ModItems.COPPER_COIN, 31)),
                        woodenSwordStack,
                        7,
                        5,
                        0.13f
                );
            });
            factories.add((entity, random) -> {
              return new MerchantOffer(
                      new ItemCost(ModItems.COPPER_COIN, 29),
                        Optional.empty(),
                        new ItemStack(ModItems.HAKUREI_CANE),
                        7,
                        5,
                        0.13f
                );
            });
        });
    }
}