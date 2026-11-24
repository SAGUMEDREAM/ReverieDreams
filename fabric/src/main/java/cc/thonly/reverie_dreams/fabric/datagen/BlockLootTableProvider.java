package cc.thonly.reverie_dreams.fabric.datagen;

import cc.thonly.reverie_dreams.block.base.AbstractCropBlock;
import cc.thonly.reverie_dreams.block.bundle.CropBlockBundle;
import cc.thonly.reverie_dreams.block.bundle.DecorativeBlockBundle;
import cc.thonly.reverie_dreams.block.bundle.WoodBundle;
import cc.thonly.reverie_dreams.block.cooking.AbstractKitchenwareBlock;
import cc.thonly.reverie_dreams.block.decor.ChabudaiBlock;
import cc.thonly.reverie_dreams.data.FumoType;
import cc.thonly.reverie_dreams.registry.content.FumoTypes;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import cc.thonly.reverie_dreams.registry.content.block.RDWoodBlocks;
import cc.thonly.reverie_dreams.registry.content.block.RDWoolBlocks;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootSubProvider;
import net.minecraft.advancements.criterion.StatePropertiesPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Stream;
import cc.thonly.reverie_dreams.block.decor.living.FutonBlock;
import cc.thonly.reverie_dreams.block.decor.workshop.WorkshopDeskBlock;
import cc.thonly.reverie_dreams.block.decor.workshop.WorkshopRackBlock;
import cc.thonly.reverie_dreams.registry.content.block.RDDeadwoodBlocks;
import cc.thonly.reverie_dreams.registry.content.block.RDJapaneseLivingBlocks;
import cc.thonly.reverie_dreams.registry.content.block.RDJapaneseSet2Blocks;
import cc.thonly.reverie_dreams.registry.content.block.RDJapaneseHomeBlocks;
import cc.thonly.reverie_dreams.registry.content.block.RDJapaneseShrineBlocks;
import cc.thonly.reverie_dreams.registry.content.block.RDLandmarkBlocks;
import cc.thonly.reverie_dreams.registry.content.block.RDMagicForestBlocks;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

public class BlockLootTableProvider extends FabricBlockLootSubProvider {
    protected static final float[] NORMAL_LEAVES_SAPLING_CHANCES = new float[]{0.05F, 0.0625F, 0.083333336F, 0.1F};
    private final Function<WoodBundle, Void> woodBundleLootFunction = (creator) -> {
        creator.stream().forEach((deferredBlock -> {
            Block block = deferredBlock.asBlock();
            if (block instanceof DoorBlock) {
                LootTable.Builder builder = this.createDoorTable(block);
                this.add(block, builder);
                return;
            } else if (block instanceof LeavesBlock) {
                LootTable.Builder builder = this.createLeavesDrops(block, creator.sapling().asBlock(), NORMAL_LEAVES_SAPLING_CHANCES);
                this.add(block, builder);
                return;
            } else if (block instanceof SlabBlock) {
                LootTable.Builder builder = this.createSlabItemTable(block);
                this.add(block, builder);
                return;
            }
            this.dropSelf(block);
        }));
        return null;
    };
    private final Function<DecorativeBlockBundle, Void> decorativeBlockBundleLootFunction = (creator) -> {
        creator.stream().forEach((block -> {
            if (block.asBlock() instanceof SlabBlock) {
                LootTable.Builder builder = this.createSlabItemTable(block.asBlock());
                this.add(block.asBlock(), builder);
                return;
            }
            this.dropSelf(block.asBlock());
        }));
        return null;
    };

    public BlockLootTableProvider(FabricPackOutput dataOutput, CompletableFuture<HolderLookup.Provider> registryLookup) {
        super(dataOutput, registryLookup);
    }

    @Override
    public void generate() {
        for (RDWoolBlocks.Family wool : RDWoolBlocks.FAMILIES) {
            Block slab = wool.slab().asBlock();
            add(slab, createSlabItemTable(slab));
            dropSelf(wool.stairs().asBlock());
        }
        cc.thonly.reverie_dreams.registry.content.block.RDGensokyoSaplings.SAPLINGS.values()
                .forEach(sapling -> dropSelf(sapling.asBlock()));
        LargeBambooData.loot(this::add);
        BambooForestData.loot(this::add);
        for (var entry : RDJapaneseSet2Blocks.ALL) {
            Block block = entry.asBlock();
            if (block instanceof WorkshopDeskBlock) {
                var table = createSingleItemTable(block);
                for (var accessory : new WorkshopDeskBlock.Accessory[]{
                        WorkshopDeskBlock.Accessory.INKSTONE,
                        WorkshopDeskBlock.Accessory.BOOKS}) {
                    var item = RDJapaneseSet2Blocks.get(accessory.kind());
                    table.withPool(applyExplosionCondition(item, LootPool.lootPool().setRolls(ConstantValue.exactly(1))
                            .add(LootItem.lootTableItem(item)).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                                    .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(
                                            WorkshopDeskBlock.ACCESSORY, accessory)))));
                }
                add(block, table);
            } else if (block instanceof WorkshopRackBlock) {
                add(block, createSinglePropConditionTable(block, WorkshopRackBlock.HALF, DoubleBlockHalf.LOWER));
            } else dropSelf(block);
        }

        for (var entry : RDJapaneseLivingBlocks.ALL) {
            Block block = entry.asBlock();
            if (block instanceof FutonBlock)
                add(block, createSinglePropConditionTable(block, BedBlock.PART, BedPart.HEAD));
            else dropSelf(block);
        }
        for (var entry : RDJapaneseShrineBlocks.ALL) {
            dropSelf(entry.asBlock());
        }
        add(RDJapaneseHomeBlocks.SHOJI_DOOR.asBlock(), this.createDoorTable(RDJapaneseHomeBlocks.SHOJI_DOOR.asBlock()));
        for (var entry : List.of(RDJapaneseHomeBlocks.KOTATSU, RDJapaneseHomeBlocks.HIBACHI,
                RDJapaneseHomeBlocks.KAMIDANA, RDJapaneseHomeBlocks.BUTSUDAN)) {
            dropSelf(entry.asBlock());
        }
        for (var entry : RDDeadwoodBlocks.SPLINTERED_LOGS.values())
            dropSelf(entry.asBlock());
        dropSelf(RDBlocks.BLUE_MUSHROOM.asBlock());
        for (var entry : RDLandmarkBlocks.ALL) {
            if (entry == RDLandmarkBlocks.PURPLE_CHERRY_LEAVES)
                add(entry.asBlock(),createLeavesDrops(entry.asBlock(),RDLandmarkBlocks.PURPLE_CHERRY_SAPLING.asBlock(),NORMAL_LEAVES_SAPLING_CHANCES));
            else if (entry == RDLandmarkBlocks.GARDEN_SUNFLOWER)
                add(entry.asBlock(),createSinglePropConditionTable(entry.asBlock(),DoublePlantBlock.HALF,DoubleBlockHalf.LOWER));
            else if (entry == RDLandmarkBlocks.PURPLE_CHERRY_PETALS)
                add(entry.asBlock(),createSegmentedBlockDrops(entry.asBlock()));
            else dropSelf(entry.asBlock());
        }
        dropSelf(RDBlocks.PURPLE_MUSHROOM.asBlock());
        add(RDBlocks.BLUE_MUSHROOM_BLOCK.asBlock(), createMushroomBlockDrop(RDBlocks.BLUE_MUSHROOM_BLOCK.asBlock(), RDBlocks.BLUE_MUSHROOM));
        add(RDBlocks.PURPLE_MUSHROOM_BLOCK.asBlock(), createMushroomBlockDrop(RDBlocks.PURPLE_MUSHROOM_BLOCK.asBlock(), RDBlocks.PURPLE_MUSHROOM));
        dropWhenSilkTouch(RDBlocks.MAGIC_MUSHROOM_STEM.asBlock());
        dropSelf(RDBlocks.DANMAKU_CRAFTING_TABLE.asBlock());
        dropSelf(RDBlocks.STRENGTH_TABLE.asBlock());
        dropSelf(RDBlocks.GENSOKYO_ALTAR.asBlock());
        dropSelf(RDBlocks.MUSIC_BLOCK.asBlock());
        dropSelf(RDBlocks.RECORD_PLAYER.asBlock());

        dropSelf(RDBlocks.MAGIC_ICE_BLOCK.asBlock());
        dropSelf(RDBlocks.MARISA_HAT_BLOCK.asBlock());
        dropSelf(RDBlocks.ANTI_COLLISION_BARREL.asBlock());
        dropSelf(RDBlocks.CASH_BOX_BLOCK.asBlock());
        dropSelf(RDBlocks.WHEEL_CHAIR.asBlock());

        dropSelf(RDBlocks.POINT_BLOCK.asBlock());
        dropSelf(RDBlocks.POWER_BLOCK.asBlock());
        dropSelf(RDBlocks.RED_ORB_BLOCK.asBlock());
        dropSelf(RDBlocks.YELLOW_ORB_BLOCK.asBlock());
        dropSelf(RDBlocks.BLUE_ORB_BLOCK.asBlock());
        dropSelf(RDBlocks.GREEN_ORB_BLOCK.asBlock());
        dropSelf(RDBlocks.PURPLE_ORB_BLOCK.asBlock());
        dropSelf(RDBlocks.DREAM_RED_BLOCK.asBlock());
        dropSelf(RDBlocks.FOOTSTEP_MOSS.asBlock());
        dropSelf(RDBlocks.DREAM_BLUE_BLOCK.asBlock());
        dropSelf(RDBlocks.WOODEN_BOX.chestBlock().asBlock());
        dropSelf(RDBlocks.CUPBOARD.asBlock());
        dropSelf(RDBlocks.TATAMI.asBlock());
        dropSelf(RDBlocks.TATAMI_BLOCK.asBlock());
        dropSelf(RDBlocks.TATAMI_CORNER.asBlock());
        dropSelf(RDBlocks.SHOJI_PANE.asBlock());
        dropSelf(RDBlocks.RANMA.asBlock());
        dropSelf(RDBlocks.SUDARE.asBlock());
        dropSelf(RDBlocks.ANDON.asBlock());
        dropSelf(RDBlocks.CHOCHIN.asBlock());
        dropSelf(RDBlocks.ZABUTON.asBlock());
        RDBlocks.ZABUTONS.values().forEach(block -> dropSelf(block.asBlock()));
        add(RDBlocks.CHABUDAI.asBlock(), createSingleItemTable(RDBlocks.CHABUDAI.asBlock())
                .withPool(applyExplosionCondition(RDBlocks.TEA_SET, LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1)).add(LootItem.lootTableItem(RDBlocks.TEA_SET))
                        .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(RDBlocks.CHABUDAI.asBlock())
                                .setProperties(StatePropertiesPredicate.Builder.properties()
                                        .hasProperty(ChabudaiBlock.HAS_TEA_SET, true))))));
        dropSelf(RDBlocks.TEA_SET.asBlock());
        dropSelf(RDBlocks.XIANGQI_BOARD.asBlock());
        dropSelf(RDBlocks.CHESS_BOARD.asBlock());
        dropSelf(RDBlocks.GO_BOARD.asBlock());
        dropSelf(RDBlocks.BYOBU.asBlock());
        dropSelf(RDBlocks.KAKEJIKU.asBlock());
        dropSelf(RDBlocks.NOREN_INDIGO.asBlock());
        dropSelf(RDBlocks.NOREN_WHITE.asBlock());
        dropSelf(RDBlocks.NOREN_RED.asBlock());
        dropSelf(RDBlocks.NOREN_BLACK.asBlock());
        dropSelf(RDBlocks.NOREN_BROWN.asBlock());
        dropSelf(RDBlocks.NOREN_GREEN.asBlock());

        // Japanese exterior batch 1.
        dropSelf(RDBlocks.ENGAWA.asBlock());
        dropSelf(RDBlocks.YOTSUME_GAKI.asBlock());
        dropSelf(RDBlocks.BAMBOO_FENCE.asBlock());
        dropSelf(RDBlocks.MUSHIKO_MADO.asBlock());
        dropSelf(RDBlocks.SHIKKUI_BLOCK.asBlock());
        dropSelf(RDBlocks.SHIKKUI_SLAB.asBlock());
        dropSelf(RDBlocks.SHIKKUI_STAIRS.asBlock());
        dropSelf(RDBlocks.SHIKKUI_WALL.asBlock());
        dropSelf(RDBlocks.KAWARA_TILES.asBlock());
        dropSelf(RDBlocks.KAWARA_TILES_SLAB.asBlock());
        dropSelf(RDBlocks.KAWARA_TILES_STAIRS.asBlock());
        dropSelf(RDBlocks.STONE_LANTERN.asBlock());
        dropSelf(RDBlocks.TSUKUBAI.asBlock());
        dropSelf(RDBlocks.STEPPING_STONE.asBlock());
        dropSelf(RDBlocks.RAKED_GRAVEL.asBlock());

        add(RDBlocks.FUSUMA_DOOR.asBlock(), this.createDoorTable(RDBlocks.FUSUMA_DOOR.asBlock()));
        add(RDBlocks.SILVER_ORE.asBlock(), (Block block) -> this.createOreDrop(block, RDItems.RAW_SILVER.asItem()));
        add(RDBlocks.MOON_IRON_ORE.asBlock(), (Block block) -> this.createOreDrop(block, Items.RAW_IRON.asItem()));
        add(RDBlocks.MOON_GOLD_ORE.asBlock(), (Block block) -> this.createOreDrop(block, Items.RAW_GOLD.asItem()));
        add(RDBlocks.MOON_DIAMOND_ORE.asBlock(), (Block block) -> this.createOreDrop(block, Items.DIAMOND.asItem()));
        add(RDBlocks.MOON_QUARTZ_ORE.asBlock(), (Block block) -> this.createOreDrop(block, Items.QUARTZ.asItem()));
        add(RDBlocks.DEEPSLATE_SILVER_ORE.asBlock(), (Block block) -> this.createOreDrop(block, RDItems.RAW_SILVER.asItem()));
        Function<Block, LootTable.Builder> orbDropFunction = (Block block) -> {
            LootTable.Builder builder = new LootTable.Builder();

            List<Item> itemList = Stream.of(
                    RDItems.RED_ORB,
                    RDItems.BLUE_ORB,
                    RDItems.YELLOW_ORB,
                    RDItems.GREEN_ORB,
                    RDItems.PURPLE_ORB
            ).map(ItemLike::asItem).toList();

            for (Item item : itemList) {
                LootPool.Builder pool = LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.25f))
                        .add(LootItem.lootTableItem(item));
                builder.withPool(pool);
            }

            LootPool.Builder fallbackPool = LootPool.lootPool()
                    .setRolls(ConstantValue.exactly(1))
                    .add(LootItem.lootTableItem(RDItems.RED_ORB).setWeight(1))
                    .add(LootItem.lootTableItem(RDItems.BLUE_ORB).setWeight(1))
                    .add(LootItem.lootTableItem(RDItems.YELLOW_ORB).setWeight(1))
                    .add(LootItem.lootTableItem(RDItems.GREEN_ORB).setWeight(1))
                    .add(LootItem.lootTableItem(RDItems.PURPLE_ORB).setWeight(1));
            builder.withPool(fallbackPool);

            return builder;
        };
        add(RDBlocks.ORB_ORE.asBlock(), orbDropFunction);
        add(RDBlocks.DEEPSLATE_ORB_ORE.asBlock(), orbDropFunction);
        dropSelf(RDBlocks.SILVER_BLOCK.asBlock());
        dropSelf(RDBlocks.SILVER_CHEST_BLOCK.chestBlock().asBlock());
        Function<Block, LootTable.Builder> dreamCrystlDropFunction = (Block block) -> {
            LootTable.Builder builder = new LootTable.Builder();

            List<Item> itemList = Stream.of(
                    RDItems.DREAM_CRYSTAL_FRAGMENT,
                    RDItems.DREAM_CRYSTAL_FRAGMENT
            ).map(ItemLike::asItem).toList();

            for (Item item : itemList) {
                LootPool.Builder pool = LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.25f))
                        .add(LootItem.lootTableItem(item));
                builder.withPool(pool);
            }

            LootPool.Builder fallbackPool = LootPool.lootPool()
                    .setRolls(ConstantValue.exactly(1))
                    .add(LootItem.lootTableItem(RDItems.DREAM_CRYSTAL_FRAGMENT).setWeight(1));
            builder.withPool(fallbackPool);

            return builder;
        };
        add(RDBlocks.DREAM_CRYSTAL_ORE.asBlock(), dreamCrystlDropFunction);

        this.decorativeBlockBundleLootFunction.apply(RDBlocks.ICE_SCALES);
        this.decorativeBlockBundleLootFunction.apply(RDBlocks.DREAM_STONE);
        this.decorativeBlockBundleLootFunction.apply(RDBlocks.DREAM_STONE_BRICK);
        this.decorativeBlockBundleLootFunction.apply(RDBlocks.MOON_STONE);
        this.decorativeBlockBundleLootFunction.apply(RDBlocks.MOON_STONE_BRICK);

        for (FumoType fumo : FumoTypes.getView()) {
            dropSelf(fumo.block());
        }

        for (Block block : AbstractKitchenwareBlock.KITCHENWARE_BLOCKS) {
            dropSelf(block);
        }
        dropSelf(RDBlocks.PLATE.asBlock());
        dropSelf(RDBlocks.CHAIR.asBlock());
        dropSelf(RDBlocks.TABLE.asBlock());
        dropSelf(RDBlocks.BREWING_BARREL.asBlock());
        dropSelf(RDBlocks.ICE_MAKING_MACHINE.asBlock());

        for (Map.Entry<Identifier, CropBlockBundle.Entry> view : CropBlockBundle.getViews()) {
            CropBlockBundle.Entry entry = view.getValue();
            generateCropLoot(entry);
        }

        this.woodBundleLootFunction.apply(RDWoodBlocks.SPIRITUAL_BUNDLE);
        dropSelf(RDWoodBlocks.BLESSED_SPIRITUAL_LOG.asBlock());

        this.woodBundleLootFunction.apply(RDWoodBlocks.LEMON_BUNDLE);
        dropOther(RDWoodBlocks.LEMON_FRUIT_LEAVES.asBlock(), RDWoodBlocks.LEMON_BUNDLE.sapling());

        this.woodBundleLootFunction.apply(RDWoodBlocks.GINKGO_BUNDLE);
        dropOther(RDWoodBlocks.GINKGO_FRUIT_LEAVES.asBlock(), RDWoodBlocks.GINKGO_BUNDLE.sapling());

        this.woodBundleLootFunction.apply(RDWoodBlocks.PEACH_BUNDLE);
        dropOther(RDWoodBlocks.PEACH_FRUIT_LEAVES.asBlock(), RDWoodBlocks.PEACH_BUNDLE.sapling());

        dropOther(RDWoodBlocks.PLUM_FRUIT_LEAVES.asBlock(), RDWoodBlocks.PLUM_SAPLING);
        dropSelf(RDWoodBlocks.PLUM_SAPLING.asBlock());
        dropOther(RDWoodBlocks.PINE_FRUIT_LEAVES.asBlock(), RDWoodBlocks.PINE_SAPLING);
        dropSelf(RDWoodBlocks.PINE_SAPLING.asBlock());
        dropOther(RDWoodBlocks.CHESTNUT_FRUIT_LEAVES.asBlock(), RDWoodBlocks.CHESTNUT_SAPLING);
        dropSelf(RDWoodBlocks.CHESTNUT_SAPLING.asBlock());

        this.woodBundleLootFunction.apply(RDWoodBlocks.IROHA_MAPLE_BUNDLE);
        this.woodBundleLootFunction.apply(RDWoodBlocks.ITAYA_MAPLE_BUNDLE);
        this.woodBundleLootFunction.apply(RDWoodBlocks.HAUCHIWA_MAPLE_BUNDLE);

        dropSelf(RDBlocks.BLACK_SALT_BLOCK.asBlock());
        dropOther(RDBlocks.PADDY_FIELD.asBlock(), Blocks.DIRT);
        dropSelf(RDWoodBlocks.UDUMBARA_FLOWER.asBlock());
        dropSelf(RDWoodBlocks.TREMELLA.asBlock());
        for (var block : RDMagicForestBlocks.ALL) {
            dropSelf(block.asBlock());
        }

        dropSelf(RDBlocks.RAIL_CONTROLLER_BLOCK.asBlock());
        dropSelf(RDBlocks.SIGNAL_RAIL_BLOCK.asBlock());
        dropSelf(RDBlocks.SIGNAL_DELAYER_BLOCK.asBlock());
        dropSelf(RDBlocks.REMOTE_CLIENT.asBlock());
        dropSelf(RDBlocks.REMOTE_SERVER.asBlock());
        dropSelf(RDBlocks.SPEAKER.asBlock());
        dropSelf(RDBlocks.MYSTIA_BELL.asBlock());
        dropSelf(RDBlocks.MENU_BOOK.asBlock());
    }

    void generateCropLoot(CropBlockBundle.Entry entry) {
        if (entry.getCropBlock() != null && entry.getProduct() != null) {
            LootItemBlockStatePropertyCondition.Builder condition = LootItemBlockStatePropertyCondition
                    .hasBlockStateProperties(entry.getCropBlock().asBlock())
                    .setProperties(
                            StatePropertiesPredicate.Builder
                                    .properties()
                                    .hasProperty(((AbstractCropBlock) entry.getCropBlock().asBlock()).getAgeProperty(), ((AbstractCropBlock) entry.getCropBlock().asBlock()).getMaxAge())
                    );
//                LootTable.Builder lootTableBuilder = provider.cropDrops(this.cropBlock, this.product, this.seed, condition);
            LootTable.Builder lootTableBuilder = LootTable.lootTable();
            LootPoolSingletonContainer.Builder<?> productEntry = LootItem.lootTableItem(entry.getProduct())
                    .apply(SetItemCountFunction.setCount(
                            UniformGenerator.between(1.0f, 3.0f)
                    ));
            LootPoolSingletonContainer.Builder<?> seedEntry = LootItem.lootTableItem(entry.getSeed())
                    .apply(SetItemCountFunction.setCount(
                            UniformGenerator.between(1.0f, 2.0f)
                    ));
            LootPoolSingletonContainer.Builder<?> baseSeedEntry = LootItem.lootTableItem(entry.getSeed())
                    .apply(SetItemCountFunction.setCount(
                            ConstantValue.exactly(1)
                    ));
            lootTableBuilder.withPool(
                    LootPool.lootPool()
                            .when(condition.build())
                            .setRolls(ConstantValue.exactly(1))
                            .add(baseSeedEntry)
            );
            lootTableBuilder.withPool(
                    LootPool.lootPool()
                            .when(condition.build())
                            .setRolls(ConstantValue.exactly(1))
                            .add(productEntry)
            );
            lootTableBuilder.withPool(
                    LootPool.lootPool()
                            .when(condition.build())
                            .setRolls(ConstantValue.exactly(1))
                            .add(seedEntry)
            );
            this.add(entry.getCropBlock().asBlock(), lootTableBuilder);
        }
    }

}
