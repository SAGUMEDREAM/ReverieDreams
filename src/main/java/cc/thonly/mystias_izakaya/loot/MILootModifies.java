package cc.thonly.mystias_izakaya.loot;

import cc.thonly.mystias_izakaya.block.MIBlocks;
import cc.thonly.mystias_izakaya.item.MIItems;
import cc.thonly.reverie_dreams.Touhou;
import cc.thonly.reverie_dreams.block.CropBlockCreator;
import cc.thonly.reverie_dreams.data.ModLootModifies;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.item.Item;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.condition.RandomChanceLootCondition;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.loot.provider.number.UniformLootNumberProvider;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class MILootModifies {
    public static final List<RegistryKey<LootTable>> TRUFFLE_DROPS = createTruffleDrops();

    public static List<RegistryKey<LootTable>> createTruffleDrops() {
        ArrayList<RegistryKey<LootTable>> keys = new ArrayList<>();
        keys.add(vanillaKey("blocks/oak_log"));
        keys.add(vanillaKey("blocks/birch_log"));
        keys.add(vanillaKey("blocks/dark_oak_log"));
        keys.add(vanillaKey("blocks/spruce_log"));
        return keys;
    }

    public static final RegistryKey<LootTable> OPEN_MINESHAFT_CHEST = LootTables.ABANDONED_MINESHAFT_CHEST;
    public static final RegistryKey<LootTable> BLOCKS_SHORT_GRASS = vanillaKey("blocks/short_grass");
    public static final RegistryKey<LootTable> BAMBOO_SAPLING = vanillaKey("blocks/bamboo_sapling");
    public static final RegistryKey<LootTable> FISHING = LootTables.FISHING_FISH_GAMEPLAY;

    public static void bootstrap() {
        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
            if (key.equals(BLOCKS_SHORT_GRASS)) {
                LootPool.Builder poolBuilder = LootPool.builder()
                        .rolls(ConstantLootNumberProvider.create(1))
                        .conditionally(RandomChanceLootCondition.builder(0.05f));

                for (CropBlockCreator.Instance instance : MIBlocks.GRASS_DROPS) {
                    poolBuilder = poolBuilder.with(ItemEntry.builder(instance.getSeed()).weight(10));
                }

                tableBuilder.pool(poolBuilder);
            }
        });
        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
            if (source.isBuiltin() && key.equals(FISHING)) {
                tableBuilder.modifyPools(tb -> {
                    tb.with(ItemEntry.builder(MIItems.SHRIMP).weight(10));
                    tb.with(ItemEntry.builder(MIItems.SHRIMP).weight(10));
                    tb.with(ItemEntry.builder(MIItems.CRAB).weight(10));
                    tb.with(ItemEntry.builder(MIItems.CRAB).weight(10));
                    tb.with(ItemEntry.builder(MIItems.SALMON).weight(10));
                    tb.with(ItemEntry.builder(MIItems.TROUT).weight(10));
                    tb.with(ItemEntry.builder(MIItems.TROUT).weight(10));
                    tb.with(ItemEntry.builder(MIItems.TUNA).weight(10));
                    tb.with(ItemEntry.builder(MIItems.TUNA).weight(10));
                    tb.with(ItemEntry.builder(MIItems.SUPREME_TUNA).weight(1));
                });
            }
        });
        LootTableEvents.REPLACE.register((key, table, source, registries) -> {
            if (source.isBuiltin() && key.equals(BAMBOO_SAPLING)) {
                LootTable.Builder builder = new LootTable.Builder();
                LootPool.Builder poolBuilder = LootPool.builder()
                        .rolls(ConstantLootNumberProvider.create(1))
                        .conditionally(RandomChanceLootCondition.builder(1f))
                        .with(ItemEntry.builder(MIItems.BAMBOO_SHOOTS).weight(10));
                builder.pool(poolBuilder);
                return builder.build();
            }
            return table;
        });
        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
            if (TRUFFLE_DROPS.contains(key)) {
                LootPool.Builder poolBuilder = LootPool.builder()
                        .rolls(ConstantLootNumberProvider.create(1))
                        .conditionally(RandomChanceLootCondition.builder(0.1f));

                poolBuilder.with(ItemEntry.builder(MIItems.TRUFFLE).weight(10));
                tableBuilder.pool(poolBuilder);
            }
        });
        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
            if (ModLootModifies.COMMON_CHESTS.contains(key)) {
                List<CropBlockCreator.Instance> chestDrops = MIBlocks.CHEST_DROPS;
                LootPool.Builder poolBuilder = LootPool.builder()
                        .rolls(UniformLootNumberProvider.create(1, 2))
                        .conditionally(RandomChanceLootCondition.builder(0.24f));
                for (CropBlockCreator.Instance instance : chestDrops) {
                    Item seed = instance.getSeed();
                    poolBuilder.with(ItemEntry.builder(seed).weight(8));
                }
                tableBuilder.pool(poolBuilder);
            }
        });
    }

    private static RegistryKey<LootTable> vanillaKey(String path) {
        return RegistryKey.of(RegistryKeys.LOOT_TABLE, Identifier.ofVanilla(path));
    }

    private static RegistryKey<LootTable> key(String path) {
        return RegistryKey.of(RegistryKeys.LOOT_TABLE, Touhou.id(path));
    }
}
