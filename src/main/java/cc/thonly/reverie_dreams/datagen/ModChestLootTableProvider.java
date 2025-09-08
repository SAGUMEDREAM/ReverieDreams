package cc.thonly.reverie_dreams.datagen;

import cc.thonly.mystias_izakaya.block.MIBlocks;
import cc.thonly.mystias_izakaya.item.MIItems;
import cc.thonly.reverie_dreams.Touhou;
import cc.thonly.reverie_dreams.item.ModItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;

import net.fabricmc.fabric.api.datagen.v1.provider.SimpleFabricLootTableProvider;
import net.minecraft.item.Items;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
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

    @Override
    public void accept(BiConsumer<RegistryKey<LootTable>, LootTable.Builder> lootTableBiConsumer) {
        lootTableBiConsumer.accept(DREAM_CHEST, LootTable
                .builder()
                .pool(
                        LootPool.builder()
                                .rolls(ConstantLootNumberProvider.create(3))
                                .with(ItemEntry.builder(Items.DIAMOND).weight(1))
                                .with(ItemEntry.builder(Items.IRON_INGOT).weight(5))
                                .with(ItemEntry.builder(ModItems.DREAM_CRYSTAL_FRAGMENT).weight(5))
                )
                .pool(
                        LootPool.builder()
                                .rolls(ConstantLootNumberProvider.create(2))
                                .with(ItemEntry.builder(Items.APPLE).weight(1))
                                .with(ItemEntry.builder(Items.BREAD).weight(1))
                                .with(ItemEntry.builder(ModItems.POINT).weight(1))
                                .with(ItemEntry.builder(ModItems.POWER).weight(1))
                )
                .pool(
                        LootPool.builder()
                                .rolls(ConstantLootNumberProvider.create(2))
                                .with(ItemEntry.builder(MIBlocks.LEMON.sapling().asItem()).weight(1))
                                .with(ItemEntry.builder(ModItems.UPGRADED_HEALTH).weight(1))
                                .with(ItemEntry.builder(ModItems.UPGRADED_HEALTH_FRAGMENT).weight(1))
                                .with(ItemEntry.builder(ModItems.BOMB).weight(1))
                                .with(ItemEntry.builder(ModItems.BOMB_FRAGMENT).weight(1))
                )
        );
    }
}
