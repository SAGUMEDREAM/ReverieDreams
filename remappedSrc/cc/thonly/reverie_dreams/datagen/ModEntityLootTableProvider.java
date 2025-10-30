package cc.thonly.reverie_dreams.datagen;

import cc.thonly.mystias_izakaya.entity.MIEntities;
import cc.thonly.mystias_izakaya.item.MIItems;
import cc.thonly.reverie_dreams.entity.ModEntities;
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

public class ModEntityLootTableProvider extends FabricEntityLootTableProvider {
    public ModEntityLootTableProvider(FabricDataOutput dataOutput, CompletableFuture<HolderLookup.Provider> registryLookup) {
        super(dataOutput, registryLookup);
    }

    @Override
    public void generate() {
        this.add(MIEntities.WILD_PIG,
                new LootTable.Builder()
                        .withPool(new LootPool.Builder()
                                .setRolls(UniformGenerator.between(1.0f, 3.0f))
                                .add(LootItem.lootTableItem(MIItems.WILD_BOAR_MEAT))
                        )
        );
        this.add(ModEntities.MOON_RABBIT_ENTITY_TYPE,
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
                                .add(LootItem.lootTableItem(MIItems.MOONLIGHT_DUMPLINGS)
                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1f, 3f)))
                                )
                        )
        );
    }
}
