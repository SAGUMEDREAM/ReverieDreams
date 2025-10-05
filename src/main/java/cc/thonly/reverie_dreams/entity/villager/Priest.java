package cc.thonly.reverie_dreams.entity.villager;

import cc.thonly.reverie_dreams.item.ModItems;
import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradedItem;

import java.util.Optional;

public class Priest {
    public static void registers() {
        TradeOfferHelper.registerVillagerOffers(ModVillagerProfessions.PRIEST, 1, factories -> {
            factories.add((entity, random) -> new TradeOffer(
                            new TradedItem(ModItems.POWER, 11),
                            Optional.empty(),
                            new ItemStack(ModItems.COPPER_COIN, 3),
                            7,
                            5,
                            0.2f
                    )
            );
            factories.add((entity, random) -> new TradeOffer(
                            new TradedItem(ModItems.POINT, 12),
                            Optional.empty(),
                            new ItemStack(ModItems.COPPER_COIN, 3),
                            7,
                            5,
                            0.13f
                    )
            );
        });
        TradeOfferHelper.registerVillagerOffers(ModVillagerProfessions.PRIEST, 2, factories -> {
            factories.add((entity, random) -> new TradeOffer(
                            new TradedItem(ModItems.POWER, 38),
                            Optional.of(new TradedItem(ModItems.COPPER_COIN, 24)),
                            new ItemStack(ModItems.EXORCISM_PAPER, 6),
                            4,
                            5,
                            0.2f
                    )
            );
            factories.add((entity, random) -> {
                DynamicRegistryManager registryManager = entity.getRegistryManager();
                ItemStack woodenSwordStack = Items.WOODEN_SWORD.getDefaultStack();
                Registry<Enchantment> lookup = registryManager.getOrThrow(RegistryKeys.ENCHANTMENT);
                woodenSwordStack.addEnchantment(lookup.getEntry(lookup.get(Enchantments.SMITE)), 4);
                woodenSwordStack.addEnchantment(lookup.getEntry(lookup.get(Enchantments.UNBREAKING)), 2);
                return new TradeOffer(
                        new TradedItem(ModItems.POINT, 29),
                        Optional.of(new TradedItem(ModItems.COPPER_COIN, 31)),
                        woodenSwordStack,
                        7,
                        5,
                        0.13f
                );
            });
            factories.add((entity, random) -> {
              return new TradeOffer(
                      new TradedItem(ModItems.COPPER_COIN, 29),
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