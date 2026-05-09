package cc.thonly.reverie_dreams.loot;

import cc.thonly.keine.api.callback.LootTableCallback;
import cc.thonly.keine.api.loot.KeineLootTableBuilder;
import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.block.bundle.CropBlockBundle;
import cc.thonly.reverie_dreams.component.DanmakuProperties;
import cc.thonly.reverie_dreams.entity.Goblin;
import cc.thonly.reverie_dreams.entity.Hairball;
import cc.thonly.reverie_dreams.entity.interfaces.Yousei;
import cc.thonly.reverie_dreams.item.base.AlbumItem;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import cc.thonly.reverie_dreams.registry.content.block.RDCropBlocks;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponents;
import cc.thonly.reverie_dreams.registry.content.danmaku.DanmakuTemplates;
import cc.thonly.reverie_dreams.registry.content.item.RDIngredientItems;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import cc.thonly.reverie_dreams.util.item.ItemStackTemplateHelper;
import net.blay09.mods.balm.platform.event.callback.LivingEntityCallback;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.skeleton.Stray;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetComponentsFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class RDLootModifies {
    public static final ResourceKey<LootTable> VILLAGE_WEAPONSMITH_CHEST = BuiltInLootTables.VILLAGE_WEAPONSMITH;
    public static final ResourceKey<LootTable> END_CITY_TREASURE_CHEST = BuiltInLootTables.END_CITY_TREASURE;
    public static final ResourceKey<LootTable> OPEN_MINESHAFT_CHEST = BuiltInLootTables.ABANDONED_MINESHAFT;
    public static final ResourceKey<LootTable> NETHER_BRIDGE_CHEST = BuiltInLootTables.NETHER_BRIDGE;
    public static final ResourceKey<LootTable> VILLAGE_PLAINS_CHEST = BuiltInLootTables.VILLAGE_PLAINS_HOUSE;
    public static final ResourceKey<LootTable> VILLAGE_SAVANNA_HOUSE_CHEST = BuiltInLootTables.VILLAGE_SAVANNA_HOUSE;
    public static final ResourceKey<LootTable> PILLAGER_OUTPOST_CHEST = BuiltInLootTables.PILLAGER_OUTPOST;
    public static final ResourceKey<LootTable> SIMPLE_DUNGEON_CHEST = BuiltInLootTables.PILLAGER_OUTPOST;
    public static final ResourceKey<LootTable> SHIPWRECK_TREASURE_CHEST = BuiltInLootTables.SHIPWRECK_TREASURE;
    public static final ResourceKey<LootTable> SHIPWRECK_SUPPLY_CHEST = BuiltInLootTables.SHIPWRECK_SUPPLY;
    public static final ResourceKey<LootTable> DESERT_PYRAMID_CHEST = BuiltInLootTables.DESERT_PYRAMID;
    public static final List<ResourceKey<LootTable>> COMMON_CHESTS = new ArrayList<>(
            List.of(
                    VILLAGE_WEAPONSMITH_CHEST,
                    END_CITY_TREASURE_CHEST,
                    OPEN_MINESHAFT_CHEST,
                    NETHER_BRIDGE_CHEST,
                    VILLAGE_PLAINS_CHEST,
                    VILLAGE_SAVANNA_HOUSE_CHEST,
                    PILLAGER_OUTPOST_CHEST,
                    SIMPLE_DUNGEON_CHEST,
                    SHIPWRECK_SUPPLY_CHEST,
                    SHIPWRECK_TREASURE_CHEST,
                    DESERT_PYRAMID_CHEST
            )
    );
    public static final List<ResourceKey<LootTable>> TRUFFLE_DROPS = createTruffleDrops();
    public static final ResourceKey<LootTable> BLOCKS_SHORT_GRASS = vanillaKey("blocks/short_grass");
    public static final ResourceKey<LootTable> BAMBOO_SAPLING = vanillaKey("blocks/bamboo_sapling");
    public static final ResourceKey<LootTable> FISHING = BuiltInLootTables.FISHING_FISH;

    public static void register() {
        LootTableCallback.MODIFY.register((key, lootTableBuilder, source, registries) -> {
            // 钓鱼修改
            if (source.isBuiltin() && key.equals(FISHING)) {
                KeineLootTableBuilder keineLootTableBuilder = (KeineLootTableBuilder) lootTableBuilder;
                keineLootTableBuilder.modifyPools(tb -> {
                    tb.add(LootItem.lootTableItem(RDIngredientItems.SHRIMP).setWeight(10));
                    tb.add(LootItem.lootTableItem(RDIngredientItems.SHRIMP).setWeight(10));
                    tb.add(LootItem.lootTableItem(RDIngredientItems.CRAB).setWeight(10));
                    tb.add(LootItem.lootTableItem(RDIngredientItems.CRAB).setWeight(10));
                    tb.add(LootItem.lootTableItem(RDIngredientItems.SALMON).setWeight(10));
                    tb.add(LootItem.lootTableItem(RDIngredientItems.TROUT).setWeight(10));
                    tb.add(LootItem.lootTableItem(RDIngredientItems.TROUT).setWeight(10));
                    tb.add(LootItem.lootTableItem(RDIngredientItems.TUNA).setWeight(10));
                    tb.add(LootItem.lootTableItem(RDIngredientItems.TUNA).setWeight(10));
                    tb.add(LootItem.lootTableItem(RDIngredientItems.SUPREME_TUNA).setWeight(1));
                });
            }
            // 松露掉落
            if (TRUFFLE_DROPS.contains(key)) {
                LootPool.Builder poolBuilder = LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.1f));

                poolBuilder.add(LootItem.lootTableItem(RDIngredientItems.TRUFFLE).setWeight(10));
                lootTableBuilder.withPool(poolBuilder);
            }
            // 魔法冰块
            Optional<ResourceKey<LootTable>> lootTableKey = Blocks.ICE.getLootTable();
            if (lootTableKey.isPresent() && lootTableKey.get().equals(key)) {
                LootPool.Builder poolBuilder = LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.1f));

                poolBuilder.add(LootItem.lootTableItem(RDBlocks.MAGIC_ICE_BLOCK).setWeight(10));
                lootTableBuilder.withPool(poolBuilder);
            }
            // 奖励箱掉落
            if (
                    COMMON_CHESTS.contains(key)
            ) {
                // disc pool
                LootPool.Builder discPool = LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.2f));
                for (var item : AlbumItem.ITEMS) {
                    discPool.add(LootItem.lootTableItem(item).setWeight(8));
                }
                lootTableBuilder.withPool(discPool);

                LootPool.Builder fragmentPool = LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.3f));
                fragmentPool.add(
                        LootItem.lootTableItem(RDItems.UPGRADED_HEALTH_FRAGMENT)
                                .setWeight(10)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0f, 3.0f)))
                );
                fragmentPool.add(
                        LootItem.lootTableItem(RDItems.BOMB_FRAGMENT)
                                .setWeight(10)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0f, 3.0f)))
                );
                lootTableBuilder.withPool(fragmentPool);

                LootPool.Builder rarePool = LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.2f));
                rarePool.add(LootItem.lootTableItem(RDItems.UPGRADED_HEALTH).setWeight(10));
                rarePool.add(LootItem.lootTableItem(RDItems.BOMB).setWeight(10));
                lootTableBuilder.withPool(rarePool);
            }
            // 种子掉落 I
            if (key.equals(BLOCKS_SHORT_GRASS)) {
                LootPool.Builder poolBuilder = LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.05f));

                for (CropBlockBundle.Entry instance : RDCropBlocks.GRASS_DROPS) {
                    poolBuilder = poolBuilder.add(LootItem.lootTableItem(instance.getSeed()).setWeight(10));
                }

                lootTableBuilder.withPool(poolBuilder);
            }
            // 种子掉落 II
            if (RDLootModifies.COMMON_CHESTS.contains(key)) {
                List<CropBlockBundle.Entry> chestDrops = RDCropBlocks.CHEST_DROPS;
                LootPool.Builder poolBuilder = LootPool.lootPool()
                        .setRolls(UniformGenerator.between(1, 2))
                        .when(LootItemRandomChanceCondition.randomChance(0.24f));
                for (CropBlockBundle.Entry instance : chestDrops) {
                    Item seed = instance.getSeed().asItem();
                    poolBuilder.add(LootItem.lootTableItem(seed).setWeight(8));
                }
                lootTableBuilder.withPool(poolBuilder);
            }
            if (key.equals(OPEN_MINESHAFT_CHEST) || key.equals(VILLAGE_WEAPONSMITH_CHEST) || key.equals(END_CITY_TREASURE_CHEST)) {
                LootPool.Builder poolBuilder = LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.4f));
                for (var entry : DanmakuTemplates.getRegistryItemStackView().entrySet()) {
                    ItemStackTemplate template = entry.getValue();
                    DanmakuProperties properties = ItemStackTemplateHelper.get(template, RDDataComponents.DANMAKU_PROPERTIES.value());
                    if (properties==null) continue;
                    poolBuilder.add(LootItem.lootTableItem(template.item().value()).setWeight(6))
                            .apply(
                                    SetComponentsFunction.setComponent(RDDataComponents.DANMAKU_PROPERTIES.value(), properties.clone())
                            );
                }
                lootTableBuilder.withPool(poolBuilder);
            }
        });
        LootTableCallback.REPLACE.register((key, table, source, registries) -> {
            // 竹笋掉落
            if (source.isBuiltin() && key.equals(BAMBOO_SAPLING)) {
                LootTable.Builder builder = new LootTable.Builder();
                LootPool.Builder poolBuilder = LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(1f))
                        .add(LootItem.lootTableItem(RDIngredientItems.BAMBOO_SHOOTS).setWeight(10));
                builder.withPool(poolBuilder);
                return builder.build();
            }
            return table;
        });
        LivingEntityCallback.Death.Before.EVENT.register((entity, damageSource) -> {
            RDLootModifies.modifyDrops(entity, damageSource);
            return true;
        });
    }

    public static List<ResourceKey<LootTable>> createTruffleDrops() {
        ArrayList<ResourceKey<LootTable>> keys = new ArrayList<>();
        keys.add(vanillaKey("blocks/oak_log"));
        keys.add(vanillaKey("blocks/birch_log"));
        keys.add(vanillaKey("blocks/dark_oak_log"));
        keys.add(vanillaKey("blocks/spruce_log"));
        return keys;
    }

    private static void modifyDrops(LivingEntity entity, DamageSource damageSource) {
        dropPointPower(entity, damageSource);
        dropIceScales(entity, damageSource);
        dropCoins(entity, damageSource);
    }

    private static void dropCoins(LivingEntity entity, DamageSource damageSource) {
        Identifier entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        Level world = entity.level();
        if (!(world instanceof ServerLevel serverWorld)) {
            return;
        }
        if (entity instanceof Yousei || entity instanceof Goblin) {
            RandomSource random = RandomSource.create();
            List<Item> itemPool = List.of(
                    RDItems.COPPER_COIN.asItem(),
                    RDItems.SILVER_COIN.asItem(),
                    RDItems.SILVER_COIN.asItem(),
                    RDItems.COPPER_COIN.asItem(),
                    RDItems.COPPER_COIN.asItem(),
                    RDItems.COPPER_COIN.asItem()
            );
            int dropChance = 45;
            int maxDropCount = 5;
            if (random.nextInt(100) < dropChance) {
                entity.spawnAtLocation(serverWorld, new ItemStack(itemPool.get(random.nextIntBetweenInclusive(0, itemPool.size() - 1)), random.nextInt(maxDropCount + 1) + 1));
            }
        }
    }

    private static void dropPointPower(LivingEntity entity, DamageSource damageSource) {
        Identifier entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        Level world = entity.level();
        if ((entity instanceof Hairball || entity instanceof Monster || entity instanceof Yousei) && world instanceof ServerLevel serverWorld) {
            RandomSource random = RandomSource.create();
            int dropChance = 45;
            int maxDropCount = 3;

            if (random.nextInt(100) < dropChance) {
                entity.spawnAtLocation(serverWorld, new ItemStack(RDItems.POWER.asItem(), random.nextInt(maxDropCount + 1) + 1));
                entity.spawnAtLocation(serverWorld, new ItemStack(RDItems.POINT.asItem(), random.nextInt(maxDropCount + 1) + 1));
            }
        }

    }

    private static void dropIceScales(LivingEntity entity, DamageSource damageSource) {
        Level world = entity.level();
        if (world instanceof ServerLevel serverWorld && entity instanceof Stray) {
            RandomSource random = RandomSource.create();
            int dropChance = 45;
            int maxDropCount = 3;
            if (random.nextInt(100) < dropChance) {
                entity.spawnAtLocation(serverWorld, new ItemStack(RDItems.ICE_SCALES.asItem(), random.nextInt(maxDropCount + 1) + 1));
            }
        }
    }

    private static ResourceKey<LootTable> vanillaKey(String path) {
        return ResourceKey.create(Registries.LOOT_TABLE, Identifier.withDefaultNamespace(path));
    }

    private static ResourceKey<LootTable> key(String path) {
        return ResourceKey.create(Registries.LOOT_TABLE, ReverieDreams.id(path));
    }

    public record WeightedEntry(int weight, Function<RandomSource, ItemStack> factory) {
        public ItemStack create(RandomSource random) {
            return factory.apply(random);
        }
    }

}
