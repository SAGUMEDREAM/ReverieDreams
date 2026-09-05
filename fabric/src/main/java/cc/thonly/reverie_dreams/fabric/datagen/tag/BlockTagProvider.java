package cc.thonly.reverie_dreams.fabric.datagen.tag;

import cc.thonly.reverie_dreams.block.BlockTypeGroup;
import cc.thonly.reverie_dreams.block.bundle.WoodBundle;
import cc.thonly.reverie_dreams.data.FumoType;
import cc.thonly.reverie_dreams.registry.content.FumoTypes;
import cc.thonly.reverie_dreams.registry.content.block.RDKitchenBlocks;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import cc.thonly.reverie_dreams.registry.content.block.RDWoodBlocks;
import cc.thonly.reverie_dreams.registry.delegate.BlockDelegate;
import cc.thonly.reverie_dreams.registry.tag.RDBlockTags;
import lombok.AccessLevel;
import lombok.Getter;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.tags.TagAppender;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@Getter(AccessLevel.PRIVATE)
public class BlockTagProvider extends FabricTagProvider.BlockTagProvider {
    public BlockTagProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider wrapperLookup) {
        TagAppender<Block, Block> fumo = valueLookupBuilder(RDBlockTags.FUMO);
        TagAppender<Block, Block> empty = valueLookupBuilder(RDBlockTags.EMPTY).add(Blocks.BEDROCK).add(Blocks.BARRIER);
        TagAppender<Block, Block> sliver = valueLookupBuilder(RDBlockTags.SILVER);
        TagAppender<Block, Block> minTools = valueLookupBuilder(RDBlockTags.MIN_TOOL).add(Blocks.BEDROCK).add(Blocks.BARRIER);
        TagAppender<Block, Block> axeMineables = valueLookupBuilder(BlockTags.MINEABLE_WITH_AXE);
        TagAppender<Block, Block> hoeMineables = valueLookupBuilder(BlockTags.MINEABLE_WITH_HOE);
        TagAppender<Block, Block> pickaxeMineables = valueLookupBuilder(BlockTags.MINEABLE_WITH_PICKAXE);
        TagAppender<Block, Block> shovelMineables = valueLookupBuilder(BlockTags.MINEABLE_WITH_SHOVEL);
        TagAppender<Block, Block> ores = valueLookupBuilder(ConventionalBlockTags.ORES);
        TagAppender<Block, Block> needsStoneTool = valueLookupBuilder(BlockTags.NEEDS_STONE_TOOL);
        TagAppender<Block, Block> needsIronTool = valueLookupBuilder(BlockTags.NEEDS_IRON_TOOL);
        TagAppender<Block, Block> needsDiamondTool = valueLookupBuilder(BlockTags.NEEDS_DIAMOND_TOOL);
        TagAppender<Block, Block> villagerJobSites = valueLookupBuilder(ConventionalBlockTags.VILLAGER_JOB_SITES);
//        ProvidedTagBuilder<Block, Block> villagerJobSites = valueLookupBuilder(PointOfInterestTypeTags.ACQUIRABLE_JOB_SITE);

        TagAppender<Block, Block> logs = valueLookupBuilder(BlockTags.LOGS);
        TagAppender<Block, Block> strippedLogs = valueLookupBuilder(ConventionalBlockTags.STRIPPED_LOGS);
        TagAppender<Block, Block> strippedWoods = valueLookupBuilder(ConventionalBlockTags.STRIPPED_WOODS);
        TagAppender<Block, Block> planks = valueLookupBuilder(BlockTags.PLANKS);
        TagAppender<Block, Block> buttons = valueLookupBuilder(BlockTags.BUTTONS);
        TagAppender<Block, Block> fences = valueLookupBuilder(BlockTags.FENCES);
        TagAppender<Block, Block> fenceGates = valueLookupBuilder(BlockTags.FENCE_GATES);
        TagAppender<Block, Block> slabs = valueLookupBuilder(BlockTags.SLABS);
        TagAppender<Block, Block> doors = valueLookupBuilder(BlockTags.DOORS);
        TagAppender<Block, Block> trapdoors = valueLookupBuilder(BlockTags.TRAPDOORS);
        Consumer<WoodBundle> woodBundleFunction = (bundle) -> {
            logs.add(bundle.log().asBlock());
            strippedLogs.add(bundle.strippedLog().asBlock());
            strippedWoods.add(bundle.strippedWood().asBlock());
            planks.add(bundle.log().asBlock());
            buttons.add(bundle.button().asBlock());
            fences.add(bundle.fence().asBlock());
            fenceGates.add(bundle.fenceGate().asBlock());
            slabs.add(bundle.slab().asBlock());
            doors.add(bundle.door().asBlock());
            trapdoors.add(bundle.trapdoor().asBlock());
        };
        for (WoodBundle instance : WoodBundle.INSTANCES) {
            woodBundleFunction.accept(instance);
        }
        logs.add(RDWoodBlocks.BLESSED_SPIRITUAL_LOG.asBlock());

        for (FumoType instance : FumoTypes.getView()) {
            fumo.add(instance.block());
        }

        Map<TagKey<Block>, Collection<? extends ItemLike>> blockItemGroups = Map.of(
                BlockTags.FENCES, BlockTypeGroup.FENCE.blocks(),
                BlockTags.FENCE_GATES, BlockTypeGroup.FENCE_GATE.blocks(),
                BlockTags.WALLS, BlockTypeGroup.WALL.blocks(),
                BlockTags.STAIRS, BlockTypeGroup.STAIR.blocks(),
                BlockTags.SLABS, BlockTypeGroup.SLAB.blocks(),
                BlockTags.BUTTONS, BlockTypeGroup.BUTTON.blocks(),
                BlockTags.TRAPDOORS, BlockTypeGroup.TRAPDOOR.blocks(),
                BlockTags.DOORS, BlockTypeGroup.DOOR.blocks(),
                BlockTags.LEAVES, BlockTypeGroup.LEAVES.blocks()
        );
        blockItemGroups.forEach((tag, list) -> {
            TagAppender<Block, Block> builder = valueLookupBuilder(tag);
            for (ItemLike itemConvertible : list) {
                if (itemConvertible instanceof Block block) {
                    builder.add(block);
                }
            }
        });
        villagerJobSites.add(
                RDBlocks.WOODEN_BOX.chestBlock().asBlock(),
                RDBlocks.CASH_BOX_BLOCK.asBlock()
        );

        pickaxeMineables.add(
                RDBlocks.SILVER_BLOCK.asBlock(),
                RDBlocks.SILVER_ORE.asBlock(),
                RDBlocks.DEEPSLATE_SILVER_ORE.asBlock()
        );
        pickaxeMineables.add(
                RDBlocks.MOON_QUARTZ_ORE.asBlock(),
                RDBlocks.MOON_GOLD_ORE.asBlock(),
                RDBlocks.MOON_IRON_ORE.asBlock(),
                RDBlocks.MOON_DIAMOND_ORE.asBlock()
        );
        pickaxeMineables.add(RDBlocks.SILVER_CHEST_BLOCK.chestBlock().asBlock());
        pickaxeMineables.add(RDBlocks.ORB_ORE.asBlock(), RDBlocks.DEEPSLATE_ORB_ORE.asBlock());
        pickaxeMineables.add(RDBlocks.DREAM_CRYSTAL_ORE.asBlock());
        pickaxeMineables.add(RDBlocks.GENSOKYO_ALTAR.asBlock());
        pickaxeMineables.add(RDBlocks.ANTI_COLLISION_BARREL.asBlock());
        pickaxeMineables.add(RDBlocks.WHEEL_CHAIR.asBlock());
        pickaxeMineables.add(RDBlocks.ICE_MAKING_MACHINE.asBlock());
        needsStoneTool.add(
                RDBlocks.SILVER_BLOCK.asBlock(),
                RDBlocks.SILVER_ORE.asBlock(),
                RDBlocks.SILVER_CHEST_BLOCK.chestBlock().asBlock(),
                RDBlocks.DEEPSLATE_SILVER_ORE.asBlock()
        );
        needsIronTool.add(
                RDBlocks.ORB_ORE.asBlock(),
                RDBlocks.RED_ORB_BLOCK.asBlock(),
                RDBlocks.YELLOW_ORB_BLOCK.asBlock(),
                RDBlocks.BLUE_ORB_BLOCK.asBlock(),
                RDBlocks.GREEN_ORB_BLOCK.asBlock(),
                RDBlocks.PURPLE_ORB_BLOCK.asBlock(),
                RDBlocks.MOON_DIAMOND_ORE.asBlock(),
                RDBlocks.MOON_GOLD_ORE.asBlock()
        );

        RDWoodBlocks.SPIRITUAL_BUNDLE.stream().stream().map(BlockDelegate::asBlock).forEach(axeMineables::add);
        RDWoodBlocks.LEMON_BUNDLE.stream().stream().map(BlockDelegate::asBlock).forEach(axeMineables::add);
        RDWoodBlocks.GINKGO_BUNDLE.stream().stream().map(BlockDelegate::asBlock).forEach(axeMineables::add);
        RDWoodBlocks.PEACH_BUNDLE.stream().stream().map(BlockDelegate::asBlock).forEach(axeMineables::add);
        RDWoodBlocks.SPIRITUAL_BUNDLE.stream().stream().map(BlockDelegate::asBlock).forEach(axeMineables::add);
        axeMineables.add(RDBlocks.DANMAKU_CRAFTING_TABLE.asBlock());
        axeMineables.add(RDBlocks.MUSIC_BLOCK.asBlock());
        axeMineables.add(RDBlocks.CASH_BOX_BLOCK.asBlock());
        axeMineables.add(RDBlocks.CHAIR.asBlock());
        axeMineables.add(RDBlocks.TABLE.asBlock());
        axeMineables.add(RDBlocks.CUPBOARD.asBlock());
        axeMineables.add(RDBlocks.BREWING_BARREL.asBlock());
        hoeMineables.add(RDBlocks.POWER_BLOCK.asBlock());
        hoeMineables.add(RDBlocks.POINT_BLOCK.asBlock());
        sliver.add(RDBlocks.SILVER_BLOCK.asBlock(), RDBlocks.SILVER_ORE.asBlock(), RDBlocks.DEEPSLATE_SILVER_ORE.asBlock());
        ores.add(RDBlocks.SILVER_ORE.asBlock(), RDBlocks.DEEPSLATE_SILVER_ORE.asBlock());
        ores.add(RDBlocks.ORB_ORE.asBlock(), RDBlocks.DEEPSLATE_ORB_ORE.asBlock());
        ores.add(RDBlocks.MOON_IRON_ORE.asBlock(),
                RDBlocks.MOON_GOLD_ORE.asBlock(),
                RDBlocks.MOON_QUARTZ_ORE.asBlock(),
                RDBlocks.MOON_DIAMOND_ORE.asBlock()
        );

        pickaxeMineables.add(RDBlocks.ICE_SCALES.block().asBlock(), RDBlocks.ICE_SCALES.slab().asBlock(), RDBlocks.ICE_SCALES.stair().asBlock());
        pickaxeMineables.add(RDBlocks.DREAM_STONE.block().asBlock(), RDBlocks.DREAM_STONE.slab().asBlock(), RDBlocks.DREAM_STONE.stair().asBlock());
        pickaxeMineables.add(RDBlocks.DREAM_STONE_BRICK.block().asBlock(), RDBlocks.DREAM_STONE_BRICK.slab().asBlock(), RDBlocks.DREAM_STONE_BRICK.stair().asBlock());
        pickaxeMineables.add(RDBlocks.MOON_STONE.block().asBlock(), RDBlocks.MOON_STONE.slab().asBlock(), RDBlocks.MOON_STONE.stair().asBlock());
        pickaxeMineables.add(RDBlocks.MOON_STONE_BRICK.block().asBlock(), RDBlocks.MOON_STONE_BRICK.slab().asBlock(), RDBlocks.MOON_STONE_BRICK.stair().asBlock());

        TagAppender<Block, Block> cookingTop = valueLookupBuilder(RDBlockTags.COOKING_TOP).add(
                RDKitchenBlocks.COOKING_POT.asBlock(),
                RDKitchenBlocks.MYSTIA_COOKING_POT.asBlock(),
                RDKitchenBlocks.SUPER_COOKING_POT.asBlock(),
                RDKitchenBlocks.EXTREME_COOKING_POT.asBlock(),
                RDKitchenBlocks.NUKE_COOKING_POT.asBlock()
        );
        TagAppender<Block, Block> cuttingBoard = valueLookupBuilder(RDBlockTags.CUTTING_BOARD).add(
                RDKitchenBlocks.CUTTING_BOARD.asBlock(),
                RDKitchenBlocks.MYSTIA_CUTTING_BOARD.asBlock(),
                RDKitchenBlocks.SUPER_CUTTING_BOARD.asBlock(),
                RDKitchenBlocks.EXTREME_CUTTING_BOARD.asBlock(),
                RDKitchenBlocks.NUKE_CUTTING_BOARD.asBlock()
        );
        TagAppender<Block, Block> fryingPan = valueLookupBuilder(RDBlockTags.FRYING_PAN).add(
                RDKitchenBlocks.FRYING_PAN.asBlock(),
                RDKitchenBlocks.MYSTIA_FRYING_PAN.asBlock(),
                RDKitchenBlocks.SUPER_FRYING_PAN.asBlock(),
                RDKitchenBlocks.EXTREME_FRYING_PAN.asBlock(),
                RDKitchenBlocks.NUKE_FRYING_PAN.asBlock()
        );
        TagAppender<Block, Block> grill = valueLookupBuilder(RDBlockTags.GRILL).add(
                RDKitchenBlocks.GRILL.asBlock(),
                RDKitchenBlocks.MYSTIA_GRILL.asBlock(),
                RDKitchenBlocks.SUPER_GRILL.asBlock(),
                RDKitchenBlocks.EXTREME_GRILL.asBlock(),
                RDKitchenBlocks.NUKE_GRILL.asBlock()
        );
        TagAppender<Block, Block> steamer = valueLookupBuilder(RDBlockTags.STEAMER).add(
                RDKitchenBlocks.STEAMER.asBlock(),
                RDKitchenBlocks.MYSTIA_STEAMER.asBlock(),
                RDKitchenBlocks.SUPER_STEAMER.asBlock(),
                RDKitchenBlocks.EXTREME_STEAMER.asBlock(),
                RDKitchenBlocks.NUKE_STEAMER.asBlock()
        );
        TagAppender<Block, Block> kitchenware = valueLookupBuilder(RDBlockTags.KITCHENWARE)
                .addOptionalTag(RDBlockTags.COOKING_TOP)
                .addOptionalTag(RDBlockTags.CUTTING_BOARD)
                .addOptionalTag(RDBlockTags.FRYING_PAN)
                .addOptionalTag(RDBlockTags.GRILL)
                .addOptionalTag(RDBlockTags.STEAMER);

        valueLookupBuilder(RDBlockTags.TRUFFLE_DROPABLE).add(Blocks.OAK_LOG, Blocks.BIRCH_LOG, Blocks.DARK_OAK_BUTTON, Blocks.SPRUCE_LOG);
        valueLookupBuilder(BlockTags.RAILS)
                .add(RDBlocks.RAIL_CONTROLLER_BLOCK.asBlock())
                .add(RDBlocks.SIGNAL_RAIL_BLOCK.asBlock());
        valueLookupBuilder(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(RDBlocks.RAIL_CONTROLLER_BLOCK.asBlock())
                .add(RDBlocks.SIGNAL_RAIL_BLOCK.asBlock())
                .add(RDBlocks.SIGNAL_DELAYER_BLOCK.asBlock())
                .add(RDBlocks.REMOTE_CLIENT.asBlock())
                .add(RDBlocks.REMOTE_SERVER.asBlock());
        valueLookupBuilder(BlockTags.MINEABLE_WITH_AXE)
                .add(RDBlocks.SPEAKER.asBlock());
    }
}
