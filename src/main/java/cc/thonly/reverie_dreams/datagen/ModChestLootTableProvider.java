package cc.thonly.reverie_dreams.datagen;

import cc.thonly.mystias_izakaya.block.MIBlocks;
import cc.thonly.mystias_izakaya.item.MIItems;
import cc.thonly.reverie_dreams.Touhou;
import cc.thonly.reverie_dreams.block.ModBlocks;
import cc.thonly.reverie_dreams.item.ModItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;

import net.fabricmc.fabric.api.datagen.v1.provider.SimpleFabricLootTableProvider;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.function.SetCountLootFunction;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.loot.provider.number.UniformLootNumberProvider;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public class ModChestLootTableProvider extends SimpleFabricLootTableProvider {

    public ModChestLootTableProvider(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        super(dataOutput, registryLookup, LootContextTypes.CHEST);
    }

    public static final RegistryKey<LootTable> DREAM_CHEST = RegistryKey.of(RegistryKeys.LOOT_TABLE, Touhou.id("dream_chest"));
    public static final RegistryKey<LootTable> OUTER_SHRINE_CHEST = RegistryKey.of(RegistryKeys.LOOT_TABLE, Touhou.id("outer_shrine_chest"));
    public static final RegistryKey<LootTable> ABANDONED_ALTAR_CHEST = RegistryKey.of(RegistryKeys.LOOT_TABLE, Touhou.id("abandoned_altar_chest"));
    public static final RegistryKey<LootTable> MINI_BAR_CHEST = RegistryKey.of(RegistryKeys.LOOT_TABLE, Touhou.id("mini_bar_chest"));
    public static final RegistryKey<LootTable> BAMBOO_FOREST_BBQ_CHEST = RegistryKey.of(RegistryKeys.LOOT_TABLE, Touhou.id("bamboo_forest_bbq_chest"));
    public static final RegistryKey<LootTable> SAKURAZUKA_CHEST = RegistryKey.of(RegistryKeys.LOOT_TABLE, Touhou.id("sakurazuka_chest"));

    @Override
    public void accept(BiConsumer<RegistryKey<LootTable>, LootTable.Builder> lootTableBiConsumer) {
        lootTableBiConsumer.accept(DREAM_CHEST, LootTable
                .builder()
                .pool(
                        LootPool.builder().rolls(ConstantLootNumberProvider.create(3))
                                .with(ItemEntry.builder(Items.DIAMOND).weight(1))
                                .with(ItemEntry.builder(Items.IRON_INGOT).weight(5))
                                .with(ItemEntry.builder(ModItems.DREAM_CRYSTAL_FRAGMENT).weight(5))
                )
                .pool(
                        LootPool.builder().rolls(ConstantLootNumberProvider.create(2))
                                .with(ItemEntry.builder(Items.APPLE).weight(1))
                                .with(ItemEntry.builder(Items.BREAD).weight(1))
                                .with(ItemEntry.builder(ModItems.POINT).weight(1))
                                .with(ItemEntry.builder(ModItems.POWER).weight(1))
                )
                .pool(
                        LootPool.builder().rolls(ConstantLootNumberProvider.create(2))
                                .with(ItemEntry.builder(MIBlocks.LEMON.sapling().asItem()).weight(1))
                                .with(ItemEntry.builder(ModItems.UPGRADED_HEALTH).weight(1))
                                .with(ItemEntry.builder(ModItems.UPGRADED_HEALTH_FRAGMENT).weight(1))
                                .with(ItemEntry.builder(ModItems.BOMB).weight(1))
                                .with(ItemEntry.builder(ModItems.BOMB_FRAGMENT).weight(1))
                )
        );

        lootTableBiConsumer.accept(OUTER_SHRINE_CHEST,
                LootTable.builder()
                        .pool(
                                LootPool.builder().rolls(UniformLootNumberProvider.create(1, 4))
                                        .with(ItemEntry.builder(Items.APPLE))
                                        .with(ItemEntry.builder(ModItems.UPGRADED_HEALTH_FRAGMENT).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(1.0f, 2.0f))))
                                        .with(ItemEntry.builder(ModItems.BOMB_FRAGMENT))
                                        .with(ItemEntry.builder(ModItems.COOKIE).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(1.0f, 2.0f))))
                        )
                        .pool(
                                LootPool.builder().rolls(UniformLootNumberProvider.create(1, 3))
                                        .with(ItemEntry.builder(Items.GOLD_INGOT))
                                        .with(ItemEntry.builder(Items.IRON_INGOT))
                                        .with(ItemEntry.builder(ModItems.POWER).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(1.0f, 3.0f))))
                                        .with(ItemEntry.builder(ModItems.POINT).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(1.0f, 3.0f))))
                        )
                        .pool(
                                LootPool.builder().rolls(UniformLootNumberProvider.create(1, 3))
                                        .with(ItemEntry.builder(Items.ROTTEN_FLESH))
                                        .with(ItemEntry.builder(Items.BONE))
                                        .with(ItemEntry.builder(Items.EMERALD))
                        )
        );
        lootTableBiConsumer.accept(ABANDONED_ALTAR_CHEST,
                LootTable.builder()
                        .pool(
                                LootPool.builder().rolls(UniformLootNumberProvider.create(1, 3))
                                        .with(ItemEntry.builder(Items.BONE))
                                        .with(ItemEntry.builder(Items.ROTTEN_FLESH))
                                        .with(ItemEntry.builder(Items.STRING))
                                        .with(ItemEntry.builder(ModItems.EXORCISM_PAPER))
                        )
                        .pool(
                                LootPool.builder().rolls(UniformLootNumberProvider.create(1, 3))
                                        .with(ItemEntry.builder(Items.DIAMOND))
                                        .with(ItemEntry.builder(Items.GOLD_INGOT))
                        )
                        .pool(
                                LootPool.builder().rolls(UniformLootNumberProvider.create(1, 4))
                                        .with(ItemEntry.builder(ModItems.COPPER_COIN))
                                        .with(ItemEntry.builder(ModItems.SILVER_COIN))
                                        .with(ItemEntry.builder(ModItems.SILVER_COIN))
                        )
        );
        var miniBarPool = LootPool.builder().rolls(UniformLootNumberProvider.create(4, 7));
        for (Item drinkItem : MIItems.DRINK_ITEMS) {
            miniBarPool.with(ItemEntry.builder(drinkItem));
        }
        lootTableBiConsumer.accept(MINI_BAR_CHEST,
                LootTable.builder()
                        .pool(
                                miniBarPool
                        )
        );
        var bambooForestBbqPool0 = LootPool.builder().rolls(UniformLootNumberProvider.create(1, 4));
        for (Item drinkItem : MIItems.FOOD_ITEMS) {
            bambooForestBbqPool0.with(ItemEntry.builder(drinkItem));
        }
        var bambooForestBbqPool1 = LootPool.builder().rolls(UniformLootNumberProvider.create(1, 3));
        for (Item drinkItem : MIItems.INGREDIENTS) {
            bambooForestBbqPool1.with(ItemEntry.builder(drinkItem));
        }
        lootTableBiConsumer.accept(BAMBOO_FOREST_BBQ_CHEST,
                LootTable.builder()
                        .pool(
                                bambooForestBbqPool0
                        )
                        .pool(
                                bambooForestBbqPool1
                        )
        );
        lootTableBiConsumer.accept(SAKURAZUKA_CHEST,
                LootTable.builder()
                        .pool(LootPool.builder()
                                .rolls(UniformLootNumberProvider.create(8, 9))
                                .with(ItemEntry.builder(Items.GOLD_INGOT).weight(10))
                                .with(ItemEntry.builder(Items.GOLD_NUGGET).weight(8))
                                .with(ItemEntry.builder(Items.DIAMOND).weight(2))
                                .with(ItemEntry.builder(Items.BONE).weight(7))
                                .with(ItemEntry.builder(Items.ROTTEN_FLESH).weight(6))
                                .with(ItemEntry.builder(Items.PINK_PETALS).weight(4))
                                .with(ItemEntry.builder(Items.CHERRY_SAPLING).weight(3))
                                .with(ItemEntry.builder(Items.IRON_INGOT).weight(9))
                                .with(ItemEntry.builder(Items.LEATHER_CHESTPLATE).weight(5))
                                .with(ItemEntry.builder(ModItems.POINT).weight(1))
                                .with(ItemEntry.builder(ModItems.POWER).weight(1))
                                .with(ItemEntry.builder(ModBlocks.POINT_BLOCK).weight(1))
                                .with(ItemEntry.builder(ModBlocks.POWER_BLOCK).weight(1))
                                .with(ItemEntry.builder(Items.IRON_SWORD).weight(6))
                                .with(ItemEntry.builder(ModItems.COPPER_COIN).weight(4))
                                .with(ItemEntry.builder(ModItems.GOLD_COIN).weight(2))
                        )
        );

    }
}
