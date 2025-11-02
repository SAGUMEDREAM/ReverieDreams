package cc.thonly.reverie_dreams.datagen;

import cc.thonly.mystias_izakaya.block.MIBlocks;
import cc.thonly.mystias_izakaya.item.MIItems;
import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.block.ModBlocks;
import cc.thonly.reverie_dreams.item.ModItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;

import net.fabricmc.fabric.api.datagen.v1.provider.SimpleFabricLootTableProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public class ModChestLootTableProvider extends SimpleFabricLootTableProvider {

    public ModChestLootTableProvider(FabricDataOutput dataOutput, CompletableFuture<HolderLookup.Provider> registryLookup) {
        super(dataOutput, registryLookup, LootContextParamSets.CHEST);
    }

    public static final ResourceKey<LootTable> DREAM_CHEST = ResourceKey.create(Registries.LOOT_TABLE, ReverieDreams.id("dream_chest"));
    public static final ResourceKey<LootTable> OUTER_SHRINE_CHEST = ResourceKey.create(Registries.LOOT_TABLE, ReverieDreams.id("outer_shrine_chest"));
    public static final ResourceKey<LootTable> ABANDONED_ALTAR_CHEST = ResourceKey.create(Registries.LOOT_TABLE, ReverieDreams.id("abandoned_altar_chest"));
    public static final ResourceKey<LootTable> MINI_BAR_CHEST = ResourceKey.create(Registries.LOOT_TABLE, ReverieDreams.id("mini_bar_chest"));
    public static final ResourceKey<LootTable> BAMBOO_FOREST_BBQ_CHEST = ResourceKey.create(Registries.LOOT_TABLE, ReverieDreams.id("bamboo_forest_bbq_chest"));
    public static final ResourceKey<LootTable> SAKURAZUKA_CHEST = ResourceKey.create(Registries.LOOT_TABLE, ReverieDreams.id("sakurazuka_chest"));

    @Override
    public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> lootTableBiConsumer) {
        lootTableBiConsumer.accept(DREAM_CHEST, LootTable
                .lootTable()
                .withPool(
                        LootPool.lootPool().setRolls(ConstantValue.exactly(3))
                                .add(LootItem.lootTableItem(Items.DIAMOND).setWeight(1))
                                .add(LootItem.lootTableItem(Items.IRON_INGOT).setWeight(5))
                                .add(LootItem.lootTableItem(ModItems.DREAM_CRYSTAL_FRAGMENT).setWeight(5))
                )
                .withPool(
                        LootPool.lootPool().setRolls(ConstantValue.exactly(2))
                                .add(LootItem.lootTableItem(Items.APPLE).setWeight(1))
                                .add(LootItem.lootTableItem(Items.BREAD).setWeight(1))
                                .add(LootItem.lootTableItem(ModItems.POINT).setWeight(1))
                                .add(LootItem.lootTableItem(ModItems.POWER).setWeight(1))
                )
                .withPool(
                        LootPool.lootPool().setRolls(ConstantValue.exactly(2))
                                .add(LootItem.lootTableItem(MIBlocks.LEMON.sapling().asItem()).setWeight(1))
                                .add(LootItem.lootTableItem(ModItems.UPGRADED_HEALTH).setWeight(1))
                                .add(LootItem.lootTableItem(ModItems.UPGRADED_HEALTH_FRAGMENT).setWeight(1))
                                .add(LootItem.lootTableItem(ModItems.BOMB).setWeight(1))
                                .add(LootItem.lootTableItem(ModItems.BOMB_FRAGMENT).setWeight(1))
                )
        );

        lootTableBiConsumer.accept(OUTER_SHRINE_CHEST,
                LootTable.lootTable()
                        .withPool(
                                LootPool.lootPool().setRolls(UniformGenerator.between(1, 4))
                                        .add(LootItem.lootTableItem(Items.APPLE))
                                        .add(LootItem.lootTableItem(ModItems.UPGRADED_HEALTH_FRAGMENT).apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0f, 2.0f))))
                                        .add(LootItem.lootTableItem(ModItems.BOMB_FRAGMENT))
                                        .add(LootItem.lootTableItem(ModItems.COOKIE).apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0f, 2.0f))))
                        )
                        .withPool(
                                LootPool.lootPool().setRolls(UniformGenerator.between(1, 3))
                                        .add(LootItem.lootTableItem(Items.GOLD_INGOT))
                                        .add(LootItem.lootTableItem(Items.IRON_INGOT))
                                        .add(LootItem.lootTableItem(ModItems.POWER).apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0f, 3.0f))))
                                        .add(LootItem.lootTableItem(ModItems.POINT).apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0f, 3.0f))))
                        )
                        .withPool(
                                LootPool.lootPool().setRolls(UniformGenerator.between(1, 3))
                                        .add(LootItem.lootTableItem(Items.ROTTEN_FLESH))
                                        .add(LootItem.lootTableItem(Items.BONE))
                                        .add(LootItem.lootTableItem(Items.EMERALD))
                        )
        );
        lootTableBiConsumer.accept(ABANDONED_ALTAR_CHEST,
                LootTable.lootTable()
                        .withPool(
                                LootPool.lootPool().setRolls(UniformGenerator.between(1, 3))
                                        .add(LootItem.lootTableItem(Items.BONE))
                                        .add(LootItem.lootTableItem(Items.ROTTEN_FLESH))
                                        .add(LootItem.lootTableItem(Items.STRING))
                                        .add(LootItem.lootTableItem(ModItems.EXORCISM_PAPER))
                        )
                        .withPool(
                                LootPool.lootPool().setRolls(UniformGenerator.between(1, 3))
                                        .add(LootItem.lootTableItem(Items.DIAMOND))
                                        .add(LootItem.lootTableItem(Items.GOLD_INGOT))
                        )
                        .withPool(
                                LootPool.lootPool().setRolls(UniformGenerator.between(1, 4))
                                        .add(LootItem.lootTableItem(ModItems.COPPER_COIN))
                                        .add(LootItem.lootTableItem(ModItems.SILVER_COIN))
                                        .add(LootItem.lootTableItem(ModItems.SILVER_COIN))
                        )
        );
        var miniBarPool = LootPool.lootPool().setRolls(UniformGenerator.between(4, 7));
        for (Item drinkItem : MIItems.DRINK_ITEMS) {
            miniBarPool.add(LootItem.lootTableItem(drinkItem));
        }
        lootTableBiConsumer.accept(MINI_BAR_CHEST,
                LootTable.lootTable()
                        .withPool(
                                miniBarPool
                        )
        );
        var bambooForestBbqPool0 = LootPool.lootPool().setRolls(UniformGenerator.between(1, 4));
        for (Item drinkItem : MIItems.FOOD_ITEMS) {
            bambooForestBbqPool0.add(LootItem.lootTableItem(drinkItem));
        }
        var bambooForestBbqPool1 = LootPool.lootPool().setRolls(UniformGenerator.between(1, 3));
        for (Item drinkItem : MIItems.INGREDIENTS) {
            bambooForestBbqPool1.add(LootItem.lootTableItem(drinkItem));
        }
        lootTableBiConsumer.accept(BAMBOO_FOREST_BBQ_CHEST,
                LootTable.lootTable()
                        .withPool(
                                bambooForestBbqPool0
                        )
                        .withPool(
                                bambooForestBbqPool1
                        )
        );
        lootTableBiConsumer.accept(SAKURAZUKA_CHEST,
                LootTable.lootTable()
                        .withPool(LootPool.lootPool()
                                .setRolls(UniformGenerator.between(8, 9))
                                .add(LootItem.lootTableItem(Items.GOLD_INGOT).setWeight(10))
                                .add(LootItem.lootTableItem(Items.GOLD_NUGGET).setWeight(8))
                                .add(LootItem.lootTableItem(Items.DIAMOND).setWeight(2))
                                .add(LootItem.lootTableItem(Items.BONE).setWeight(7))
                                .add(LootItem.lootTableItem(Items.ROTTEN_FLESH).setWeight(6))
                                .add(LootItem.lootTableItem(Items.PINK_PETALS).setWeight(4))
                                .add(LootItem.lootTableItem(Items.CHERRY_SAPLING).setWeight(3))
                                .add(LootItem.lootTableItem(Items.IRON_INGOT).setWeight(9))
                                .add(LootItem.lootTableItem(Items.LEATHER_CHESTPLATE).setWeight(5))
                                .add(LootItem.lootTableItem(ModItems.POINT).setWeight(1))
                                .add(LootItem.lootTableItem(ModItems.POWER).setWeight(1))
                                .add(LootItem.lootTableItem(ModBlocks.POINT_BLOCK).setWeight(1))
                                .add(LootItem.lootTableItem(ModBlocks.POWER_BLOCK).setWeight(1))
                                .add(LootItem.lootTableItem(Items.IRON_SWORD).setWeight(6))
                                .add(LootItem.lootTableItem(ModItems.COPPER_COIN).setWeight(4))
                                .add(LootItem.lootTableItem(ModItems.GOLD_COIN).setWeight(2))
                        )
        );

    }
}
