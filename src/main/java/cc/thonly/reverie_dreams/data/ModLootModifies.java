package cc.thonly.reverie_dreams.data;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.block.ModBlocks;
import cc.thonly.reverie_dreams.component.DanmakuProperties;
import cc.thonly.reverie_dreams.component.ModDataComponentTypes;
import cc.thonly.reverie_dreams.danmaku.DanmakuTemplates;
import cc.thonly.reverie_dreams.entity.GoblinEntity;
import cc.thonly.reverie_dreams.entity.HairballEntity;
import cc.thonly.reverie_dreams.entity.Yousei;
import cc.thonly.reverie_dreams.item.ModItems;
import cc.thonly.reverie_dreams.item.base.AlbumItem;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Stray;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
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

public class ModLootModifies {
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

    public static void register() {
        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
            Optional<ResourceKey<LootTable>> lootTableKey = Blocks.ICE.getLootTable();
            if (lootTableKey.isPresent() && lootTableKey.get().equals(key)) {
                LootPool.Builder poolBuilder = LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.1f));

                poolBuilder.add(LootItem.lootTableItem(ModBlocks.MAGIC_ICE_BLOCK).setWeight(10));
                tableBuilder.withPool(poolBuilder);
            }
        });
        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
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
                tableBuilder.withPool(discPool);

                LootPool.Builder fragmentPool = LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.3f));
                fragmentPool.add(
                        LootItem.lootTableItem(ModItems.UPGRADED_HEALTH_FRAGMENT)
                                .setWeight(10)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0f, 3.0f)))
                );
                fragmentPool.add(
                        LootItem.lootTableItem(ModItems.BOMB_FRAGMENT)
                                .setWeight(10)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0f, 3.0f)))
                );
                tableBuilder.withPool(fragmentPool);

                LootPool.Builder rarePool = LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.2f));
                rarePool.add(LootItem.lootTableItem(ModItems.UPGRADED_HEALTH).setWeight(10));
                rarePool.add(LootItem.lootTableItem(ModItems.BOMB).setWeight(10));
                tableBuilder.withPool(rarePool);
            }
        });
        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
            if (key.equals(OPEN_MINESHAFT_CHEST) || key.equals(VILLAGE_WEAPONSMITH_CHEST) || key.equals(END_CITY_TREASURE_CHEST)) {
                LootPool.Builder poolBuilder = LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.4f));
                for (var entry : DanmakuTemplates.getRegistryItemStackView().entrySet()) {
                    ItemStack itemStack = entry.getValue();
                    DanmakuProperties properties = itemStack.get(ModDataComponentTypes.DANMAKU_PROPERTIES);
                    if (properties==null) continue;
                    poolBuilder.add(LootItem.lootTableItem(itemStack.getItem())
                            .apply(SetComponentsFunction.setComponent(ModDataComponentTypes.DANMAKU_PROPERTIES, properties.clone()))
                            .setWeight(6));
                }
                tableBuilder.withPool(poolBuilder);
            }
        });

        ServerLivingEntityEvents.AFTER_DEATH.register(ModLootModifies::modifyDrops);
    }

    private static void modifyDrops(LivingEntity entity, DamageSource damageSource) {
        dropPointPower(entity, damageSource);
        dropIceScales(entity, damageSource);
        dropCoins(entity, damageSource);
    }

    private static void dropCoins(LivingEntity entity, DamageSource damageSource) {
        ResourceLocation entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        Level world = entity.level();
        if (!(world instanceof ServerLevel serverWorld)) {
            return;
        }
        if (entity instanceof Yousei || entity instanceof GoblinEntity) {
            RandomSource random = RandomSource.create();
            List<Item> itemPool = List.of(
                    ModItems.COPPER_COIN,
                    ModItems.SILVER_COIN,
                    ModItems.SILVER_COIN,
                    ModItems.GOLD_COIN,
                    ModItems.COPPER_COIN,
                    ModItems.COPPER_COIN,
                    ModItems.COPPER_COIN
            );
            int dropChance = 45;
            int maxDropCount = 3;
            if (random.nextInt(100) < dropChance) {
                entity.spawnAtLocation(serverWorld, new ItemStack(itemPool.get(random.nextIntBetweenInclusive(0, itemPool.size() - 1)), random.nextInt(maxDropCount + 1) + 1));
            }
        }
    }

    private static void dropPointPower(LivingEntity entity, DamageSource damageSource) {
        ResourceLocation entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        Level world = entity.level();
        if ((entity instanceof HairballEntity || entity instanceof Monster || entity instanceof Yousei) && world instanceof ServerLevel serverWorld) {
            RandomSource random = RandomSource.create();
            int dropChance = 45;
            int maxDropCount = 3;

            if (random.nextInt(100) < dropChance) {
                entity.spawnAtLocation(serverWorld, new ItemStack(ModItems.POWER, random.nextInt(maxDropCount + 1) + 1));
                entity.spawnAtLocation(serverWorld, new ItemStack(ModItems.POINT, random.nextInt(maxDropCount + 1) + 1));
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
                entity.spawnAtLocation(serverWorld, new ItemStack(ModItems.ICE_SCALES, random.nextInt(maxDropCount + 1) + 1));
            }
        }
    }

    private static ResourceKey<LootTable> vanillaKey(String path) {
        return ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.withDefaultNamespace(path));
    }

    private static ResourceKey<LootTable> key(String path) {
        return ResourceKey.create(Registries.LOOT_TABLE, ReverieDreams.id(path));
    }

}
