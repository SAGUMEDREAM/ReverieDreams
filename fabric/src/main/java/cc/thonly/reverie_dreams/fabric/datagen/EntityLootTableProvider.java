package cc.thonly.reverie_dreams.fabric.datagen;

import cc.thonly.reverie_dreams.registry.content.entity.RDEntityTypes;
import cc.thonly.reverie_dreams.registry.content.entity.RDAquaticEntities;
import cc.thonly.reverie_dreams.entity.aquatic.AquaticSpecies;
import cc.thonly.reverie_dreams.registry.content.item.RDCuisineItems;
import cc.thonly.reverie_dreams.registry.content.item.RDIngredientItems;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricEntityLootSubProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.EnchantedCountIncreaseFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import java.util.concurrent.CompletableFuture;
import net.minecraft.world.level.ItemLike;

public class EntityLootTableProvider extends FabricEntityLootSubProvider {
    public EntityLootTableProvider(FabricPackOutput dataOutput, CompletableFuture<HolderLookup.Provider> registryLookup) {
        super(dataOutput, registryLookup);
    }

    @Override
    public void generate() {
        this.add(RDEntityTypes.YOUKAI_CAT.value(), LootTable.lootTable());
        this.add(RDEntityTypes.YOUKAI_TANUKI.value(), LootTable.lootTable());
        this.add(RDEntityTypes.YOUKAI_FOX.value(), LootTable.lootTable());
        for (var species : AquaticSpecies.values()) {
            this.add(RDAquaticEntities.type(species).value(),
                    LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1))
                            .add(LootItem.lootTableItem(RDAquaticEntities.ingredient(species)))));
        }
        this.add(RDEntityTypes.WILD_PIG.value(),
                new LootTable.Builder()
                        .withPool(new LootPool.Builder()
                                .setRolls(UniformGenerator.between(1.0f, 3.0f))
                                .add(LootItem.lootTableItem(RDIngredientItems.WILD_BOAR_MEAT))
                        )
        );
        this.add(RDEntityTypes.WAGYU.value(), LootTable.lootTable()
                .withPool(wagyuDrop(Items.LEATHER, 0, 2))
                .withPool(wagyuDrop(RDIngredientItems.WAGYU_BEEF, 1, 3)));
        this.add(RDEntityTypes.YOUKAI_RABBIT.value(),
                LootTable.lootTable()
                        .withPool(LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1))
                                .add(LootItem.lootTableItem(Items.BAMBOO)
                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(0f, 2f)))))
                        .withPool(LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1))
                                .when(LootItemRandomChanceCondition.randomChance(.35f))
                                .add(LootItem.lootTableItem(Items.GOLD_NUGGET)
                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1f, 2f)))))
        );
        this.add(RDEntityTypes.MUSHROOM_MONSTER.value(),
                LootTable.lootTable()
                        .withPool(
                                LootPool.lootPool()
                                        .setRolls(ConstantValue.exactly(2))
                                        .add(
                                                LootItem.lootTableItem(Items.RED_MUSHROOM)
                                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 3)))
                                                        .setWeight(1)
                                        )
                                        .add(
                                                LootItem.lootTableItem(Items.BROWN_MUSHROOM)
                                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 3)))
                                                        .setWeight(1)
                                        )
                        )
        );
        this.add(RDEntityTypes.UFO.value(),
                LootTable.lootTable()
                        .withPool(
                                LootPool.lootPool()
                                        .setRolls(ConstantValue.exactly(2))
                                        .add(
                                                LootItem.lootTableItem(Items.GUNPOWDER)
                                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 8)))
                                                        .setWeight(1)
                                        )
                        )
                        .withPool(
                                LootPool.lootPool()
                                        .setRolls(ConstantValue.exactly(2))
                                        .add(
                                                LootItem.lootTableItem(RDItems.POINT)
                                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(3, 15)))
                                                        .setWeight(1)
                                        )
                                        .add(
                                                LootItem.lootTableItem(RDItems.POWER)
                                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(10, 20)))
                                                        .setWeight(1)
                                        )
                        )
        );
        this.add(RDEntityTypes.HAIRBALL.value(),
                LootTable.lootTable()
                        .withPool(
                                LootPool.lootPool()
                                        .setRolls(ConstantValue.exactly(2))
                                        .add(
                                                LootItem.lootTableItem(Items.LEATHER)
                                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 2)))
                                                        .setWeight(1)
                                        )
                        )
                        .withPool(
                                LootPool.lootPool()
                                        .setRolls(ConstantValue.exactly(2))
                                        .add(
                                                LootItem.lootTableItem(Items.FEATHER)
                                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 2)))
                                                        .setWeight(1)
                                        )
                        )
        );
        this.add(RDEntityTypes.RABBIT_UNIT.value(),
                LootTable.lootTable()
                        .withPool(
                                LootPool.lootPool()
                                        .setRolls(ConstantValue.exactly(1))
                                        .add(
                                                LootItem.lootTableItem(RDItems.COPPER_COIN)
                                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(9, 20)))
                                                        .setWeight(1)
                                        )
                                        .add(
                                                LootItem.lootTableItem(RDItems.SILVER_COIN)
                                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(2, 7)))
                                                        .setWeight(1)
                                        )
                        )
                        .withPool(
                                LootPool.lootPool()
                                        .setRolls(ConstantValue.exactly(1))
                                        .add(
                                                LootItem.lootTableItem(Items.LEATHER)
                                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(9, 20)))
                                                        .setWeight(1)
                                        )
                        )
        );

        this.add(RDEntityTypes.KILLER_BEE.value(),
                LootTable.lootTable()
                        .withPool(item(Items.HONEYCOMB, 1, 2))
                        .withPool(chance(0.25f, Items.HONEY_BOTTLE))
                        .withPool(chance(0.10f, RDItems.POWER, 1, 2))
        );

        this.add(RDEntityTypes.GHOST.value(),
                LootTable.lootTable()
                        .withPool(item(RDItems.GHOST_VEIL, 1, 2))
                        .withPool(LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1))
                                .add(LootItem.lootTableItem(RDItems.POWER)
                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 3)))
                                        .setWeight(1))
                                .add(LootItem.lootTableItem(RDItems.POINT)
                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 3)))
                                        .setWeight(1)))
                        .withPool(chance(0.15f, RDItems.EXORCISM_PAPER, 1, 1))
        );

        this.add(RDEntityTypes.YOUSEI.value(),
                LootTable.lootTable()
                        .withPool(item(RDItems.FAIRY_DUST, 1, 2))
                        .withPool(LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1))
                                .add(LootItem.lootTableItem(RDItems.POWER)
                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 2)))
                                        .setWeight(2))
                                .add(LootItem.lootTableItem(RDItems.POINT)
                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 2)))
                                        .setWeight(1)))
                        .withPool(chance(0.02f, RDItems.SPEED_FEATHER)
                        )
        );

        this.add(RDEntityTypes.MAID_YOUSEI.value(),
                LootTable.lootTable()
                        .withPool(item(RDItems.FAIRY_DUST, 1, 2))
                        .withPool(item(Items.STRING, 1, 3))
                        .withPool(chance(0.35f, RDItems.POWER, 1, 3))
        );

        this.add(RDEntityTypes.SUNFLOWER_YOUSEI.value(),
                LootTable.lootTable()
                        .withPool(item(RDItems.FAIRY_DUST, 1, 2))
                        .withPool(item(Items.SUNFLOWER, 1, 2))
                        .withPool(chance(0.35f, RDItems.POWER, 1, 3))
        );

        this.add(RDEntityTypes.ICE_FAIRY.value(),
                LootTable.lootTable()
                        .withPool(item(RDItems.FAIRY_DUST, 1, 2))
                        .withPool(item(RDItems.ICE_SCALES, 1, 2))
                        .withPool(chance(0.20f, RDBlocks.MAGIC_ICE_BLOCK, 1, 1))
                        .withPool(chance(0.35f, RDItems.POWER, 1, 3))
        );

        this.add(RDEntityTypes.GOBLIN.value(),
                LootTable.lootTable()
                        .withPool(item(RDItems.COPPER_COIN, 5, 12))
                        .withPool(chance(0.35f, Items.IRON_INGOT, 1, 2))
                        .withPool(chance(0.20f, RDItems.SILVER_NUGGET, 1, 2))
        );

        this.add(RDEntityTypes.ONI.value(),
                LootTable.lootTable()
                        .withPool(item(RDItems.ONI_HORN, 1, 1))
                        .withPool(item(RDItems.SILVER_COIN, 2, 6))
                        .withPool(chance(0.50f, Items.GOLD_INGOT, 1, 2))
                        .withPool(chance(0.70f, RDItems.POWER, 2, 5))
        );

        this.add(RDEntityTypes.WATER_ELEMENTAL.value(),
                LootTable.lootTable()
                        .withPool(item(Items.PRISMARINE_SHARD, 1, 3))
                        .withPool(chance(0.70f, RDItems.POWER, 1, 3))
                        .withPool(chance(0.10f, Items.PRISMARINE_CRYSTALS, 1, 1))
        );

        this.add(RDEntityTypes.FIRE_ELEMENTAL.value(),
                LootTable.lootTable()
                        .withPool(item(Items.BLAZE_POWDER, 1, 3))
                        .withPool(chance(0.70f, RDItems.POWER, 1, 3))
                        .withPool(chance(0.10f, Items.MAGMA_CREAM, 1, 1))
        );

        this.add(RDEntityTypes.ICE_ELEMENTAL.value(),
                LootTable.lootTable()
                        .withPool(item(RDItems.ICE_SCALES, 1, 3))
                        .withPool(item(RDBlocks.MAGIC_ICE_BLOCK, 1, 2))
                        .withPool(chance(0.50f, RDItems.POWER, 1, 3))
        );
    }

    private LootPool.Builder wagyuDrop(ItemLike item, int min, int max) {
        return LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(item)
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(min, max)))
                        .apply(EnchantedCountIncreaseFunction.lootingMultiplier(this.registries, UniformGenerator.between(0, 1))));
    }

    private static LootPool.Builder item(ItemLike item, int min, int max) {
        return LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(item)
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(min, max))));
    }

    private static LootPool.Builder chance(float chance, ItemLike item) {
        return chance(chance, item, 1, 1);
    }

    private static LootPool.Builder chance(float chance, ItemLike item, int min, int max) {
        return LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1))
                .when(LootItemRandomChanceCondition.randomChance(chance))
                .add(LootItem.lootTableItem(item)
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(min, max))));
    }
}
