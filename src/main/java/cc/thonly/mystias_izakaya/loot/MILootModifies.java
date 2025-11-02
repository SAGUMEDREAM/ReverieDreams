package cc.thonly.mystias_izakaya.loot;

import cc.thonly.mystias_izakaya.block.MIBlocks;
import cc.thonly.mystias_izakaya.item.MIItems;
import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.block.CropBlockCreator;
import cc.thonly.reverie_dreams.data.ModLootModifies;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import java.util.ArrayList;
import java.util.List;

public class MILootModifies {
    public static final List<ResourceKey<LootTable>> TRUFFLE_DROPS = createTruffleDrops();

    public static List<ResourceKey<LootTable>> createTruffleDrops() {
        ArrayList<ResourceKey<LootTable>> keys = new ArrayList<>();
        keys.add(vanillaKey("blocks/oak_log"));
        keys.add(vanillaKey("blocks/birch_log"));
        keys.add(vanillaKey("blocks/dark_oak_log"));
        keys.add(vanillaKey("blocks/spruce_log"));
        return keys;
    }

    public static final ResourceKey<LootTable> OPEN_MINESHAFT_CHEST = BuiltInLootTables.ABANDONED_MINESHAFT;
    public static final ResourceKey<LootTable> BLOCKS_SHORT_GRASS = vanillaKey("blocks/short_grass");
    public static final ResourceKey<LootTable> BAMBOO_SAPLING = vanillaKey("blocks/bamboo_sapling");
    public static final ResourceKey<LootTable> FISHING = BuiltInLootTables.FISHING_FISH;

    public static void bootstrap() {
        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
            if (key.equals(BLOCKS_SHORT_GRASS)) {
                LootPool.Builder poolBuilder = LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.05f));

                for (CropBlockCreator.Instance instance : MIBlocks.GRASS_DROPS) {
                    poolBuilder = poolBuilder.add(LootItem.lootTableItem(instance.getSeed()).setWeight(10));
                }

                tableBuilder.withPool(poolBuilder);
            }
        });
        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
            if (source.isBuiltin() && key.equals(FISHING)) {
                tableBuilder.modifyPools(tb -> {
                    tb.add(LootItem.lootTableItem(MIItems.SHRIMP).setWeight(10));
                    tb.add(LootItem.lootTableItem(MIItems.SHRIMP).setWeight(10));
                    tb.add(LootItem.lootTableItem(MIItems.CRAB).setWeight(10));
                    tb.add(LootItem.lootTableItem(MIItems.CRAB).setWeight(10));
                    tb.add(LootItem.lootTableItem(MIItems.SALMON).setWeight(10));
                    tb.add(LootItem.lootTableItem(MIItems.TROUT).setWeight(10));
                    tb.add(LootItem.lootTableItem(MIItems.TROUT).setWeight(10));
                    tb.add(LootItem.lootTableItem(MIItems.TUNA).setWeight(10));
                    tb.add(LootItem.lootTableItem(MIItems.TUNA).setWeight(10));
                    tb.add(LootItem.lootTableItem(MIItems.SUPREME_TUNA).setWeight(1));
                });
            }
        });
        LootTableEvents.REPLACE.register((key, table, source, registries) -> {
            if (source.isBuiltin() && key.equals(BAMBOO_SAPLING)) {
                LootTable.Builder builder = new LootTable.Builder();
                LootPool.Builder poolBuilder = LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(1f))
                        .add(LootItem.lootTableItem(MIItems.BAMBOO_SHOOTS).setWeight(10));
                builder.withPool(poolBuilder);
                return builder.build();
            }
            return table;
        });
        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
            if (TRUFFLE_DROPS.contains(key)) {
                LootPool.Builder poolBuilder = LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.1f));

                poolBuilder.add(LootItem.lootTableItem(MIItems.TRUFFLE).setWeight(10));
                tableBuilder.withPool(poolBuilder);
            }
        });
        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
            if (ModLootModifies.COMMON_CHESTS.contains(key)) {
                List<CropBlockCreator.Instance> chestDrops = MIBlocks.CHEST_DROPS;
                LootPool.Builder poolBuilder = LootPool.lootPool()
                        .setRolls(UniformGenerator.between(1, 2))
                        .when(LootItemRandomChanceCondition.randomChance(0.24f));
                for (CropBlockCreator.Instance instance : chestDrops) {
                    Item seed = instance.getSeed();
                    poolBuilder.add(LootItem.lootTableItem(seed).setWeight(8));
                }
                tableBuilder.withPool(poolBuilder);
            }
        });
    }

    private static ResourceKey<LootTable> vanillaKey(String path) {
        return ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.withDefaultNamespace(path));
    }

    private static ResourceKey<LootTable> key(String path) {
        return ResourceKey.create(Registries.LOOT_TABLE, ReverieDreams.id(path));
    }
}
