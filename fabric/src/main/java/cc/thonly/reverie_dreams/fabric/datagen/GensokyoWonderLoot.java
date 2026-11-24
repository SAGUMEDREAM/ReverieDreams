package cc.thonly.reverie_dreams.fabric.datagen;

import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.EnchantWithLevelsFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

/** Common value floor; each landmark still authors its own themed rewards. */
final class GensokyoWonderLoot {
    enum Tier {
        SUPPLY(12, 20, .4F, .08F, 1),
        CACHE(20, 30, .75F, .2F, 2),
        TREASURY(30, 40, 1F, .45F, 3);

        final int minLevel;
        final int maxLevel;
        final float enchantChance;
        final float rareChance;
        final int materialRolls;

        Tier(int minLevel, int maxLevel, float enchantChance, float rareChance, int materialRolls) {
            this.minLevel = minLevel;
            this.maxLevel = maxLevel;
            this.enchantChance = enchantChance;
            this.rareChance = rareChance;
            this.materialRolls = materialRolls;
        }
    }

    static LootTable.Builder cache(HolderLookup.Provider registries, Tier tier, ItemLike signature) {
        var equipment = once()
                .add(enchanted(registries, tier, Items.BOOK).setWeight(5))
                .add(enchanted(registries, tier, Items.IRON_PICKAXE).setWeight(2))
                .add(enchanted(registries, tier, Items.IRON_CHESTPLATE).setWeight(2))
                .add(enchanted(registries, tier, Items.BOW).setWeight(2));
        if (tier != Tier.SUPPLY) {
            equipment.add(enchanted(registries, tier, Items.DIAMOND_SWORD).setWeight(2))
                    .add(enchanted(registries, tier, Items.DIAMOND_BOOTS).setWeight(2));
        }
        return LootTable.lootTable()
                .withPool(ChestLootTableProvider.supplies(signature, 1, 1))
                .withPool(ChestLootTableProvider.supplies(RDItems.COPPER_COIN, 12, 24))
                .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(tier.materialRolls))
                        .add(stack(RDItems.POWER, 2, 5).setWeight(5))
                        .add(stack(RDItems.FAIRY_DUST, 3, 7).setWeight(4))
                        .add(stack(RDItems.BOMB_FRAGMENT, 1, 3).setWeight(3))
                        .add(stack(RDItems.UPGRADED_HEALTH_FRAGMENT, 1, 2).setWeight(2))
                        .add(stack(RDItems.SILVER_INGOT, 2, 4).setWeight(3))
                        .add(stack(Items.GOLD_INGOT, 2, 5).setWeight(3))
                        .add(stack(Items.DIAMOND, 1, 2).setWeight(1)))
                .withPool(equipment.when(LootItemRandomChanceCondition.randomChance(tier.enchantChance)))
                .withPool(once().when(LootItemRandomChanceCondition.randomChance(tier.rareChance))
                        .add(stack(Items.GOLDEN_APPLE, 1, 2).setWeight(5))
                        .add(stack(Items.DIAMOND, 2, 4).setWeight(4))
                        .add(stack(RDItems.BOMB, 1, 1).setWeight(4))
                        .add(stack(RDItems.UPGRADED_HEALTH, 1, 1).setWeight(2))
                        .add(stack(Items.ENCHANTED_GOLDEN_APPLE, 1, 1).setWeight(1)));
    }

    static LootPool.Builder book(HolderLookup.Provider registries, int min, int max) {
        return once().add(LootItem.lootTableItem(Items.BOOK)
                .apply(EnchantWithLevelsFunction.enchantWithLevels(registries, UniformGenerator.between(min, max))));
    }

    static LootPool.Builder bonus(ItemLike item, float chance) {
        return ChestLootTableProvider.supplies(item, 1, 1)
                .when(LootItemRandomChanceCondition.randomChance(chance));
    }

    private static LootPoolSingletonContainer.Builder<?> enchanted(HolderLookup.Provider registries, Tier tier, ItemLike item) {
        return LootItem.lootTableItem(item)
                .apply(EnchantWithLevelsFunction.enchantWithLevels(registries, UniformGenerator.between(tier.minLevel, tier.maxLevel)));
    }

    private static LootPoolSingletonContainer.Builder<?> stack(ItemLike item, int min, int max) {
        return LootItem.lootTableItem(item).apply(SetItemCountFunction.setCount(UniformGenerator.between(min, max)));
    }

    private static LootPool.Builder once() {
        return LootPool.lootPool().setRolls(ConstantValue.exactly(1));
    }

    private GensokyoWonderLoot() {
    }
}
