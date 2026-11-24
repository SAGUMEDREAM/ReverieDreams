package cc.thonly.reverie_dreams.loot;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.api.entity.type.YouseiType;
import cc.thonly.reverie_dreams.api.loot.LootTableModificationContextExtension;
import cc.thonly.reverie_dreams.block.bundle.CropBlockBundle;
import cc.thonly.reverie_dreams.component.DanmakuProperties;
import cc.thonly.reverie_dreams.data.danmaku.DanmakuTrajectoryConfig;
import cc.thonly.reverie_dreams.data.danmaku.trajectory.PlayerTrajectoryType;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuDefaults;
import cc.thonly.reverie_dreams.entity.Hairball;
import cc.thonly.reverie_dreams.item.base.AlbumItem;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import cc.thonly.reverie_dreams.registry.content.block.RDCropBlocks;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponentTypes;
import cc.thonly.reverie_dreams.registry.content.danmaku.DanmakuTemplates;
import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell;
import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpells;
import cc.thonly.reverie_dreams.registry.content.item.RDIngredientItems;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import cc.thonly.reverie_dreams.util.item.ItemStackTemplateHelper;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.EntityEvent;
import dev.architectury.event.events.common.LootEvent;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.skeleton.Stray;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetComponentsFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

@SuppressWarnings({"resource", "removal"})
public class RDLootModifies {
    public static final ResourceKey<LootTable> VILLAGE_WEAPONSMITH_CHEST = BuiltInLootTables.VILLAGE_WEAPONSMITH;
    public static final ResourceKey<LootTable> END_CITY_TREASURE_CHEST = BuiltInLootTables.END_CITY_TREASURE;
    public static final ResourceKey<LootTable> OPEN_MINESHAFT_CHEST = BuiltInLootTables.ABANDONED_MINESHAFT;
    public static final ResourceKey<LootTable> NETHER_BRIDGE_CHEST = BuiltInLootTables.NETHER_BRIDGE;
    public static final ResourceKey<LootTable> VILLAGE_PLAINS_CHEST = BuiltInLootTables.VILLAGE_PLAINS_HOUSE;
    public static final ResourceKey<LootTable> VILLAGE_SAVANNA_HOUSE_CHEST = BuiltInLootTables.VILLAGE_SAVANNA_HOUSE;
    public static final ResourceKey<LootTable> PILLAGER_OUTPOST_CHEST = BuiltInLootTables.PILLAGER_OUTPOST;
    public static final ResourceKey<LootTable> SIMPLE_DUNGEON_CHEST = BuiltInLootTables.SIMPLE_DUNGEON;
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
    /**
     * 仅主世界（Overworld）维度的结构箱子，不含下界/末地。
     */
    public static final List<ResourceKey<LootTable>> OVERWORLD_CHESTS = new ArrayList<>(
            List.of(
                    BuiltInLootTables.VILLAGE_WEAPONSMITH,
                    BuiltInLootTables.VILLAGE_PLAINS_HOUSE,
                    BuiltInLootTables.VILLAGE_SAVANNA_HOUSE,
                    BuiltInLootTables.VILLAGE_TEMPLE,
                    BuiltInLootTables.VILLAGE_TOOLSMITH,
                    BuiltInLootTables.ABANDONED_MINESHAFT,
                    BuiltInLootTables.SIMPLE_DUNGEON,
                    BuiltInLootTables.DESERT_PYRAMID,
                    BuiltInLootTables.JUNGLE_TEMPLE,
                    BuiltInLootTables.PILLAGER_OUTPOST,
                    BuiltInLootTables.SHIPWRECK_TREASURE,
                    BuiltInLootTables.SHIPWRECK_SUPPLY,
                    BuiltInLootTables.BURIED_TREASURE,
                    BuiltInLootTables.UNDERWATER_RUIN_BIG,
                    BuiltInLootTables.UNDERWATER_RUIN_SMALL,
                    BuiltInLootTables.RUINED_PORTAL,
                    BuiltInLootTables.STRONGHOLD_CROSSING,
                    BuiltInLootTables.STRONGHOLD_LIBRARY,
                    BuiltInLootTables.WOODLAND_MANSION,
                    BuiltInLootTables.IGLOO_CHEST,
                    BuiltInLootTables.ANCIENT_CITY,
                    BuiltInLootTables.TRIAL_CHAMBERS_SUPPLY,
                    BuiltInLootTables.TRIAL_CHAMBERS_CORRIDOR
            )
    );
    public static final List<ResourceKey<LootTable>> TRUFFLE_DROPS = createTruffleDrops();
    public static final ResourceKey<LootTable> BLOCKS_SHORT_GRASS = vanillaKey("blocks/short_grass");
    public static final ResourceKey<LootTable> FISHING = BuiltInLootTables.FISHING_FISH;

    public static void register() {
        LootEvent.MODIFY_LOOT_TABLE.register((key, context, builtin) -> {
            // 钓鱼
            if (key.equals(FISHING)) {
                LootTableModificationContextExtension contextExtension = LootTableModificationContextExtension.get(context);
                contextExtension.modifyPool(pool -> {
                    pool.add(LootItem.lootTableItem(RDIngredientItems.SHRIMP).setWeight(8));
                    pool.add(LootItem.lootTableItem(RDIngredientItems.CRAB).setWeight(8));
                    pool.add(LootItem.lootTableItem(RDIngredientItems.SALMON).setWeight(8));
                    pool.add(LootItem.lootTableItem(RDIngredientItems.TROUT).setWeight(8));
                    pool.add(LootItem.lootTableItem(RDIngredientItems.TUNA).setWeight(8));
                    pool.add(LootItem.lootTableItem(RDIngredientItems.SUPREME_TUNA).setWeight(8));
                });
            }
            // 松露掉落
            if (TRUFFLE_DROPS.contains(key)) {
                registerTruffleDrops(key, context, builtin);
            }
            // 魔法冰块
            Optional<ResourceKey<LootTable>> lootTableKey = Blocks.ICE.getLootTable();
            if (lootTableKey.isPresent() && lootTableKey.get().equals(key)) {
                registerMagicIceDrops(key, context, builtin);
            }
            // 通用奖励箱掉落
            if (COMMON_CHESTS.contains(key)) {
                registerDiscChestDrops(key, context, builtin);
                registerCommonChestDrops(key, context, builtin);
            }
            // 种子掉落 I
            if (key.equals(BLOCKS_SHORT_GRASS)) {
                registerSeedDrops_1(key, context, builtin);
            }
            // 锄头破坏藤蔓 -> 小概率葡萄
            if (key.equals(vanillaKey("blocks/vine"))) {
                registerVineDrops(key, context, builtin);
            }
            // 香椿种子：橡树/白桦树叶低概率掉落（香椿芽采自树头）
            if (key.equals(vanillaKey("blocks/oak_leaves")) || key.equals(vanillaKey("blocks/birch_leaves"))) {
                registerToonSeedDrops(key, context, builtin);
            }
            // 种子掉落 II
            if (RDLootModifies.COMMON_CHESTS.contains(key)) {
                registerSeedDrops_2(key, context, builtin);
            }
            registerTemplateDrops(key, context, builtin);
            // 主世界箱子中的符卡（稀有：多数箱子没有，出现 0-2 张且互不相同）
            if (OVERWORLD_CHESTS.contains(key)) {
                registerSpellcardChestDrops(key, context, builtin);
            }
        });
        // Preserve vanilla/datapack bamboo sapling loot; custom shoot drops belong on a separate plant.
        EntityEvent.LIVING_DEATH.register((entity, damageSource) -> {
            RDLootModifies.modifyDrops(entity, damageSource);
            return EventResult.pass();
        });
    }

    public static void registerTruffleDrops(ResourceKey<LootTable> key, LootEvent.LootTableModificationContext context, boolean builtin) {
        LootPool.Builder pool = LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1))
                .when(LootItemRandomChanceCondition.randomChance(0.07f));

        pool.add(LootItem.lootTableItem(RDIngredientItems.TRUFFLE).setWeight(10));
        context.addPool(pool);
    }

    public static void registerMagicIceDrops(ResourceKey<LootTable> key, LootEvent.LootTableModificationContext context, boolean builtin) {
        LootPool.Builder poolBuilder = LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1))
                .when(LootItemRandomChanceCondition.randomChance(0.1f));

        poolBuilder.add(LootItem.lootTableItem(RDBlocks.MAGIC_ICE_BLOCK).setWeight(10));
        context.addPool(poolBuilder);
    }

    public static void registerDiscChestDrops(ResourceKey<LootTable> key, LootEvent.LootTableModificationContext context, boolean builtin) {
        LootPool.Builder discPool = LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1))
                .when(LootItemRandomChanceCondition.randomChance(0.2f));
        for (var item : AlbumItem.ITEMS) {
            discPool.add(LootItem.lootTableItem(item).setWeight(8));
        }
        context.addPool(discPool);
    }

    public static void registerCommonChestDrops(ResourceKey<LootTable> key, LootEvent.LootTableModificationContext context, boolean builtin) {
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
        context.addPool(fragmentPool);

        LootPool.Builder rarePool = LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1))
                .when(LootItemRandomChanceCondition.randomChance(0.2f));
        rarePool.add(LootItem.lootTableItem(RDItems.UPGRADED_HEALTH).setWeight(10));
        rarePool.add(LootItem.lootTableItem(RDItems.BOMB).setWeight(10));
        context.addPool(rarePool);
    }

    public static void registerSeedDrops_1(ResourceKey<LootTable> key, LootEvent.LootTableModificationContext context, boolean builtin) {
        LootPool.Builder poolBuilder = LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1))
                .when(LootItemRandomChanceCondition.randomChance(0.05f));

        for (CropBlockBundle.Entry instance : RDCropBlocks.GRASS_DROPS) {
            poolBuilder = poolBuilder.add(LootItem.lootTableItem(instance.getSeed()).setWeight(10));
        }

        context.addPool(poolBuilder);

        // 稀有种子独立池：红薯/白萝卜/香椿/葡萄，1% 概率、4 种均分（约 0.25%/株），
        // 避免把这 4 种直接塞进常规池稀释现有 8 种种子的掉落
        LootPool.Builder rarePoolBuilder = LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1))
                .when(LootItemRandomChanceCondition.randomChance(0.01f));

        for (CropBlockBundle.Entry instance : RDCropBlocks.RARE_GRASS_DROPS) {
            rarePoolBuilder = rarePoolBuilder.add(LootItem.lootTableItem(instance.getSeed()).setWeight(10));
        }

        context.addPool(rarePoolBuilder);
    }

    public static void registerSeedDrops_2(ResourceKey<LootTable> key, LootEvent.LootTableModificationContext context, boolean builtin) {
        List<CropBlockBundle.Entry> chestDrops = RDCropBlocks.CHEST_DROPS;
        LootPool.Builder poolBuilder = LootPool.lootPool()
                .setRolls(UniformGenerator.between(1, 2))
                .when(LootItemRandomChanceCondition.randomChance(0.24f));
        for (CropBlockBundle.Entry instance : chestDrops) {
            Item seed = instance.getSeed().asItem();
            poolBuilder.add(LootItem.lootTableItem(seed).setWeight(8));
        }
        context.addPool(poolBuilder);
    }

    /**
     * 用锄头破坏藤蔓（minecraft:vine）时有 10% 概率掉落葡萄。
     *
     * <p>这里不能用 {@code minecraft:hoes} 标签：战利品表是在“加载期”构造 {@link ItemPredicate} 的，
     * 此时 {@code HolderGetter} 尚未绑定物品标签，{@code getOrThrow(TagKey)} 会抛
     * {@code Missing tag ... minecraft:hoes}（带标签的 lookup 只在运行时才有）。
     * 故按类型从物品注册表直接筛选：只要继承 {@link HoeItem}，即涵盖原版、本模组与其它模组。
     */
    public static void registerVineDrops(ResourceKey<LootTable> key, LootEvent.LootTableModificationContext context, boolean builtin) {
        Stream<Item> itemStream = BuiltInRegistries.ITEM.stream().filter(item -> item instanceof HoeItem);

        LootPool.Builder pool = LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1))
                .when(LootItemRandomChanceCondition.randomChance(0.1f))
                .when(MatchTool.toolMatches(ItemPredicate.Builder.item().of(BuiltInRegistries.ITEM, itemStream.toArray(Item[]::new))));

        pool.add(LootItem.lootTableItem(RDIngredientItems.GRAPE).setWeight(10));
        // 野葡萄藤也带种子：锄藤小概率掉葡萄种子
        pool.add(LootItem.lootTableItem(RDCropBlocks.GRAPE.getSeed()).setWeight(3));
        context.addPool(pool);
    }

    /**
     * 橡树/白桦树叶 0.5% 掉香椿种子；不加 survives_explosion 条件，与原版树苗一样自然枯萎时也可掉落。
     */
    public static void registerToonSeedDrops(ResourceKey<LootTable> key, LootEvent.LootTableModificationContext context, boolean builtin) {
        LootPool.Builder pool = LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1))
                .when(LootItemRandomChanceCondition.randomChance(0.005f));

        pool.add(LootItem.lootTableItem(RDCropBlocks.TOON.getSeed()));
        context.addPool(pool);
    }

    public static void registerTemplateDrops(ResourceKey<LootTable> key, LootEvent.LootTableModificationContext context, boolean builtin) {
        for (PlayerTrajectoryType type : PlayerTrajectoryType.values()) {
            if (!key.equals(type.lootTable())) {
                continue;
            }
            ItemStackTemplate template = DanmakuTemplates.createItemStackTemplate(type.id());
            DanmakuProperties properties = ItemStackTemplateHelper.get(template, RDDataComponentTypes.DANMAKU_PROPERTIES.value());
            if (properties == null)
                continue;
            var templateEntry = LootItem.lootTableItem(template.item().value())
                    .setWeight(6)
                    .apply(SetComponentsFunction.setComponent(RDDataComponentTypes.DANMAKU_PROPERTIES.value(), properties.clone()));
            DanmakuTrajectoryConfig config = ItemStackTemplateHelper.get(template, RDDataComponentTypes.DANMAKU_TRAJECTORY_CONFIG.value());
            if (config != null) {
                templateEntry = templateEntry.apply(SetComponentsFunction.setComponent(RDDataComponentTypes.DANMAKU_TRAJECTORY_CONFIG.value(), config));
            }
            context.addPool(LootPool.lootPool()
                    .setRolls(ConstantValue.exactly(1))
                    .when(LootItemRandomChanceCondition.randomChance(0.30f))
                    .add(templateEntry));
        }
    }

    /**
     * 主世界结构箱子的符卡战利品：每个箱子两道独立奖池（rolls 各 1），分别有
     * 25% / 12% 的概率出卡——多数箱子一张也没有，最多 2 张，期望约 0.37 张/箱。
     * 卡面 = 全部已实现（implemented）的 TouhouSpell，按组件写入图案与难度染色；
     * 两池重复抽到同一张卡的概率约为 1/N²（N 为符卡总数，数百），可视为互不相同。
     */
    public static void registerSpellcardChestDrops(ResourceKey<LootTable> key, LootEvent.LootTableModificationContext context, boolean builtin) {
        List<TouhouSpell> cards2 = new ArrayList<>();
        for (TouhouSpell spell : TouhouSpells.catalog()) {
            if (spell.patternFactory() != null) {
                cards2.add(spell);
            }
        }
        if (cards2.isEmpty()) {
            return;
        }
        List<LootItem.Builder> cards = new ArrayList<>();
        for (TouhouSpell spell : cards2) {
            cards.add(LootItem.lootTableItem(RDItems.SPELLCARD.asItem())
                    .setWeight(1)
                    .apply(SetComponentsFunction.setComponent(RDDataComponentTypes.DANMAKU_PATTERN.value(), spell.id()))
                    .apply(SetComponentsFunction.setComponent(DataComponents.DYED_COLOR,
                            new DyedItemColor(DanmakuDefaults.difficultyColor(spell.id())))));
        }
        if (cards.isEmpty()) {
            return;
        }

        LootPool.Builder firstPool = LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1))
                .when(LootItemRandomChanceCondition.randomChance(0.25f));
        for (LootItem.Builder card : cards) {
            firstPool.add(card);
        }
        context.addPool(firstPool);

        LootPool.Builder secondPool = LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1))
                .when(LootItemRandomChanceCondition.randomChance(0.12f));
        for (LootItem.Builder card : cards) {
            secondPool.add(card);
        }
        context.addPool(secondPool);
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
        dropWildSeeds(entity, damageSource);
    }

    private static void dropPointPower(LivingEntity entity, DamageSource damageSource) {
        Identifier entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        RegistryAccess registryAccess = entity.registryAccess();
        Level world = entity.level();
        if ((entity instanceof Hairball || entity instanceof Monster || entity instanceof YouseiType) && world instanceof ServerLevel serverWorld) {
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
        RegistryAccess registryAccess = entity.registryAccess();
        if (world instanceof ServerLevel serverWorld && (entity instanceof Stray)) {
            RandomSource random = RandomSource.create();
            int dropChance = 45;
            int maxDropCount = 3;
            if (random.nextInt(100) < dropChance) {
                entity.spawnAtLocation(serverWorld, new ItemStack(RDItems.ICE_SCALES.asItem(), random.nextInt(maxDropCount + 1) + 1));
            }
        }
    }

    /**
     * 野外种子的主题化获取：僵尸系小概率掉红薯种子（呼应原版土豆），兔子小概率掉白萝卜种子（呼应兔子拱萝卜）。
     */
    private static void dropWildSeeds(LivingEntity entity, DamageSource damageSource) {
        Level world = entity.level();
        if (!(world instanceof ServerLevel serverWorld)) {
            return;
        }
        EntityType<?> type = entity.getType();
        Item seed = null;
        if (type == EntityType.ZOMBIE || type == EntityType.HUSK || type == EntityType.ZOMBIE_VILLAGER) {
            seed = RDCropBlocks.SWEET_POTATO.getSeed().asItem();
        } else if (type == EntityType.RABBIT) {
            seed = RDCropBlocks.WHITE_RADISH.getSeed().asItem();
        }
        if (seed == null) {
            return;
        }
        RandomSource random = RandomSource.create();
        if (random.nextInt(100) < 3) {
            entity.spawnAtLocation(serverWorld, new ItemStack(seed, 1));
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
