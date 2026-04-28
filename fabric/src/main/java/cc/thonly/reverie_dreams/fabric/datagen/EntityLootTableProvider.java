package cc.thonly.reverie_dreams.fabric.datagen;

import cc.thonly.reverie_dreams.registry.content.entity.RDEntityTypes;
import cc.thonly.reverie_dreams.registry.content.item.RDFoodItems;
import cc.thonly.reverie_dreams.registry.content.item.RDIngredientItems;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricEntityLootTableProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import java.util.concurrent.CompletableFuture;

public class EntityLootTableProvider extends FabricEntityLootTableProvider {
    public EntityLootTableProvider(FabricDataOutput dataOutput, CompletableFuture<HolderLookup.Provider> registryLookup) {
        super(dataOutput, registryLookup);
    }

    @Override
    public void generate() {
        this.add(RDEntityTypes.WILD_PIG.asHolder().value(),
                new LootTable.Builder()
                        .withPool(new LootPool.Builder()
                                .setRolls(UniformGenerator.between(1.0f, 3.0f))
                                .add(LootItem.lootTableItem(RDIngredientItems.WILD_BOAR_MEAT))
                        )
        );
        this.add(RDEntityTypes.MOON_RABBIT.asHolder().value(),
                new LootTable.Builder()
                        .withPool(new LootPool.Builder()
                                .setRolls(ConstantValue.exactly(1.0f))
                                .add(LootItem.lootTableItem(Items.RABBIT))
                        )
                        .withPool(new LootPool.Builder()
                                .setRolls(ConstantValue.exactly(0.8f))
                                .add(LootItem.lootTableItem(Items.RABBIT_FOOT))
                        )
                        .withPool(LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1))
                                .when(LootItemRandomChanceCondition.randomChance(0.4f))
                                .add(LootItem.lootTableItem(Items.GOLD_NUGGET)
                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1f, 3f)))
                                )
                        ).withPool(LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1))
                                .when(LootItemRandomChanceCondition.randomChance(0.1f))
                                .add(LootItem.lootTableItem(RDFoodItems.MOONLIGHT_DUMPLINGS)
                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1f, 3f)))
                                )
                        )
        );
        this.add(RDEntityTypes.MUSHROOM_MONSTER.asHolder().value(),
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
        this.add(RDEntityTypes.UFO.asHolder().value(),
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
        this.add(RDEntityTypes.HAIRBALL.asHolder().value(),
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
        );
        this.add(RDEntityTypes.RABBIT_UNIT.asHolder().value(),
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
    }
}
