package cc.thonly.reverie_dreams.datagen;

import cc.thonly.mystias_izakaya.block.MIBlocks;
import cc.thonly.mystias_izakaya.block.kitchenware.AbstractKitchenwareBlock;
import cc.thonly.reverie_dreams.block.CropBlockCreator;
import cc.thonly.reverie_dreams.block.DecorativeBlockCreator;
import cc.thonly.reverie_dreams.block.ModBlocks;
import cc.thonly.reverie_dreams.block.WoodCreator;
import cc.thonly.reverie_dreams.fumo.Fumo;
import cc.thonly.reverie_dreams.fumo.Fumos;
import cc.thonly.reverie_dreams.item.ModItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class ModBlockLootTableProvider extends FabricBlockLootTableProvider {
    private final Function<WoodCreator, Void> woodCreatorLootFunction = (creator) -> {
        creator.stream().forEach((block -> {
            if (block instanceof DoorBlock) {
                LootTable.Builder builder = this.createDoorTable(block);
                this.add(block, builder);
                return;
            }
            if (block instanceof LeavesBlock) {
                this.createLeavesDrops(block, creator.sapling(), 0.2f);
                return;
            }
            if (block instanceof SlabBlock) {
                LootTable.Builder builder = this.createSlabItemTable(block);
                this.add(block, builder);
                return;
            }
            this.dropSelf(block);
        }));
        return null;
    };
    private final Function<DecorativeBlockCreator, Void> decorativeBlockCreatorLootFunction = (creator) -> {
        creator.stream().forEach((block -> {
            if (block instanceof SlabBlock) {
                LootTable.Builder builder = this.createSlabItemTable(block);
                this.add(block, builder);
                return;
            }
            this.dropSelf(block);
        }));
        return null;
    };

    public ModBlockLootTableProvider(FabricDataOutput dataOutput, CompletableFuture<HolderLookup.Provider> registryLookup) {
        super(dataOutput, registryLookup);
    }

    @Override
    public void generate() {
        dropSelf(ModBlocks.DANMAKU_CRAFTING_TABLE);
        dropSelf(ModBlocks.STRENGTH_TABLE);
        dropSelf(ModBlocks.GENSOKYO_ALTAR);
        dropSelf(ModBlocks.MUSIC_BLOCK);

        this.woodCreatorLootFunction.apply(ModBlocks.SPIRITUAL);

        dropSelf(ModBlocks.MAGIC_ICE_BLOCK);
        dropSelf(ModBlocks.MARISA_HAT_BLOCK);
        dropSelf(ModBlocks.ANTI_COLLISION_BARREL);
        dropSelf(ModBlocks.CASH_BOX_BLOCK);
        dropSelf(ModBlocks.WHEEL_CHAIR);

        dropSelf(ModBlocks.POINT_BLOCK);
        dropSelf(ModBlocks.POWER_BLOCK);
        add(ModBlocks.SILVER_ORE, (Block block) -> this.createOreDrop(block, ModItems.RAW_SILVER));
        add(ModBlocks.DEEPSLATE_SILVER_ORE, (Block block) -> this.createOreDrop(block, ModItems.RAW_SILVER));
        Function<Block, LootTable.Builder> orbDropFunction = (Block block) -> {
            LootTable.Builder builder = new LootTable.Builder();

            List<Item> itemList = List.of(
                    ModItems.RED_ORB,
                    ModItems.BLUE_ORB,
                    ModItems.YELLOW_ORB,
                    ModItems.GREEN_ORB,
                    ModItems.PURPLE_ORB
            );

            for (Item item : itemList) {
                LootPool.Builder pool = LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.25f))
                        .add(LootItem.lootTableItem(item));
                builder.withPool(pool);
            }

            LootPool.Builder fallbackPool = LootPool.lootPool()
                    .setRolls(ConstantValue.exactly(1))
                    .add(LootItem.lootTableItem(ModItems.RED_ORB).setWeight(1))
                    .add(LootItem.lootTableItem(ModItems.BLUE_ORB).setWeight(1))
                    .add(LootItem.lootTableItem(ModItems.YELLOW_ORB).setWeight(1))
                    .add(LootItem.lootTableItem(ModItems.GREEN_ORB).setWeight(1))
                    .add(LootItem.lootTableItem(ModItems.PURPLE_ORB).setWeight(1));
            builder.withPool(fallbackPool);

            return builder;
        };
        add(ModBlocks.ORB_ORE, orbDropFunction);
        add(ModBlocks.DEEPSLATE_ORB_ORE, orbDropFunction);
        dropSelf(ModBlocks.SILVER_BLOCK);
        dropSelf(ModBlocks.SILVER_CHEST_BLOCK.chestBlock());
        Function<Block, LootTable.Builder> dreamCrystlDropFunction = (Block block) -> {
            LootTable.Builder builder = new LootTable.Builder();

            List<Item> itemList = List.of(
                    ModItems.DREAM_CRYSTAL_FRAGMENT,
                    ModItems.DREAM_CRYSTAL_FRAGMENT
            );

            for (Item item : itemList) {
                LootPool.Builder pool = LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.25f))
                        .add(LootItem.lootTableItem(item));
                builder.withPool(pool);
            }

            LootPool.Builder fallbackPool = LootPool.lootPool()
                    .setRolls(ConstantValue.exactly(1))
                    .add(LootItem.lootTableItem(ModItems.DREAM_CRYSTAL_FRAGMENT).setWeight(1))
                    ;
            builder.withPool(fallbackPool);

            return builder;
        };
        add(ModBlocks.DREAM_CRYSTAL_ORE, dreamCrystlDropFunction);

        this.decorativeBlockCreatorLootFunction.apply(ModBlocks.ICE_SCALES);
        this.decorativeBlockCreatorLootFunction.apply(ModBlocks.DREAM_STONE);
        this.decorativeBlockCreatorLootFunction.apply(ModBlocks.DREAM_STONE_BRICK);
        this.decorativeBlockCreatorLootFunction.apply(ModBlocks.MOON_STONE);
        this.decorativeBlockCreatorLootFunction.apply(ModBlocks.MOON_STONE_BRICK);

        for (Fumo fumo : Fumos.getView()) {
            dropSelf(fumo.block());
        }

        this.generateMI();
    }

    public void generateMI() {
        for (Block block : AbstractKitchenwareBlock.KITCHENWARE_BLOCKS) {
            dropSelf(block);
        }
        dropSelf(MIBlocks.ITEM_DISPLAY);

        for (Map.Entry<ResourceLocation, CropBlockCreator.Instance> view : CropBlockCreator.getViews()) {
            CropBlockCreator.Instance instance = view.getValue();
            instance.generateLoot(this);
        }

        this.woodCreatorLootFunction.apply(MIBlocks.LEMON);
        dropOther(MIBlocks.LEMON_FRUIT_LEAVES, MIBlocks.LEMON.sapling());

        this.woodCreatorLootFunction.apply(MIBlocks.GINKGO);
        dropOther(MIBlocks.GINKGO_FRUIT_LEAVES, MIBlocks.GINKGO.sapling());

        this.woodCreatorLootFunction.apply(MIBlocks.PEACH);
        dropOther(MIBlocks.PEACH_FRUIT_LEAVES, MIBlocks.PEACH.sapling());

//        addDrop(MIBlocks.COOKTOP);
        dropSelf(MIBlocks.BLACK_SALT_BLOCK);
        dropSelf(MIBlocks.UDUMBARA_FLOWER);
        dropSelf(MIBlocks.TREMELLA);
    }
}
