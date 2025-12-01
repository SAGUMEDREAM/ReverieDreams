package cc.thonly.reverie_dreams.datagen.tag;

import cc.thonly.reverie_dreams.block.BlockTypeGroup;
import cc.thonly.reverie_dreams.block.creator.WoodCreator;
import cc.thonly.reverie_dreams.data.FumoType;
import cc.thonly.reverie_dreams.registry.content.FumoTypes;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import cc.thonly.reverie_dreams.registry.content.block.RDWoodBlocks;
import cc.thonly.reverie_dreams.registry.tag.RDBlockTags;
import lombok.AccessLevel;
import lombok.Getter;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.tags.TagAppender;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Getter(AccessLevel.PRIVATE)
public class BlockTagProvider extends FabricTagProvider.BlockTagProvider {
    public BlockTagProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider wrapperLookup) {
        TagAppender<Block, Block> fumo = valueLookupBuilder(RDBlockTags.FUMO);
        TagAppender<Block, Block> empty = valueLookupBuilder(RDBlockTags.EMPTY);
        TagAppender<Block, Block> sliver = valueLookupBuilder(RDBlockTags.SILVER);
        TagAppender<Block, Block> minTools = valueLookupBuilder(RDBlockTags.MIN_TOOL);
        TagAppender<Block, Block> axeMineables = valueLookupBuilder(BlockTags.MINEABLE_WITH_AXE);
        TagAppender<Block, Block> hoeMineables = valueLookupBuilder(BlockTags.MINEABLE_WITH_HOE);
        TagAppender<Block, Block> pickaxeMineables = valueLookupBuilder(BlockTags.MINEABLE_WITH_PICKAXE);
        TagAppender<Block, Block> shovelMineables = valueLookupBuilder(BlockTags.MINEABLE_WITH_SHOVEL);
        TagAppender<Block, Block> ores = valueLookupBuilder(ConventionalBlockTags.ORES);
        TagAppender<Block, Block> villagerJobSites = valueLookupBuilder(ConventionalBlockTags.VILLAGER_JOB_SITES);
//        ProvidedTagBuilder<Block, Block> villagerJobSites = valueLookupBuilder(PointOfInterestTypeTags.ACQUIRABLE_JOB_SITE);

        TagAppender<Block, Block> logs = valueLookupBuilder(BlockTags.LOGS);
        TagAppender<Block, Block> planks = valueLookupBuilder(BlockTags.PLANKS);
        for (WoodCreator instance : WoodCreator.INSTANCES) {
            logs.add(instance.log());
            planks.add(instance.planks());
        }
        logs.add(RDWoodBlocks.BLESSED_SPIRITUAL_LOG);

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
                RDBlocks.WOODEN_BOX.chestBlock(),
                RDBlocks.CASH_BOX_BLOCK
        );

        pickaxeMineables.add(RDBlocks.SILVER_BLOCK, RDBlocks.SILVER_ORE, RDBlocks.DEEPSLATE_SILVER_ORE);
        pickaxeMineables.add(RDBlocks.SILVER_CHEST_BLOCK.chestBlock());
        pickaxeMineables.add(RDBlocks.ORB_ORE, RDBlocks.DEEPSLATE_ORB_ORE);
        pickaxeMineables.add(RDBlocks.DREAM_CRYSTAL_ORE);
        pickaxeMineables.add(RDBlocks.GENSOKYO_ALTAR);
        pickaxeMineables.add(RDBlocks.ANTI_COLLISION_BARREL);
        pickaxeMineables.add(RDBlocks.WHEEL_CHAIR);
//        pickaxeMineables.add(MIBlocks.COOKTOP);
        RDWoodBlocks.SPIRITUAL.stream().forEach(axeMineables::add);
        RDWoodBlocks.LEMON.stream().forEach(axeMineables::add);
        RDWoodBlocks.GINKGO.stream().forEach(axeMineables::add);
        RDWoodBlocks.PEACH.stream().forEach(axeMineables::add);
        RDWoodBlocks.SPIRITUAL.stream().forEach(axeMineables::add);
        axeMineables.add(RDBlocks.DANMAKU_CRAFTING_TABLE);
        axeMineables.add(RDBlocks.MUSIC_BLOCK);
        axeMineables.add(RDBlocks.CASH_BOX_BLOCK);
        hoeMineables.add(RDBlocks.POWER_BLOCK);
        hoeMineables.add(RDBlocks.POINT_BLOCK);
        sliver.add(RDBlocks.SILVER_BLOCK, RDBlocks.SILVER_ORE, RDBlocks.DEEPSLATE_SILVER_ORE);
        ores.add(RDBlocks.SILVER_ORE, RDBlocks.DEEPSLATE_SILVER_ORE);
        ores.add(RDBlocks.ORB_ORE, RDBlocks.DEEPSLATE_ORB_ORE);

        pickaxeMineables.add(RDBlocks.ICE_SCALES.block(), RDBlocks.ICE_SCALES.slab(), RDBlocks.ICE_SCALES.stair());
        pickaxeMineables.add(RDBlocks.DREAM_STONE.block(), RDBlocks.DREAM_STONE.slab(), RDBlocks.DREAM_STONE.stair());
        pickaxeMineables.add(RDBlocks.DREAM_STONE_BRICK.block(), RDBlocks.DREAM_STONE_BRICK.slab(), RDBlocks.DREAM_STONE_BRICK.stair());
        pickaxeMineables.add(RDBlocks.MOON_STONE.block(), RDBlocks.MOON_STONE.slab(), RDBlocks.MOON_STONE.stair());
        pickaxeMineables.add(RDBlocks.MOON_STONE_BRICK.block(), RDBlocks.MOON_STONE_BRICK.slab(), RDBlocks.MOON_STONE_BRICK.stair());

        minTools.add(Blocks.BEDROCK);
        minTools.add(Blocks.BARRIER);
        empty.add(Blocks.BEDROCK);
        empty.add(Blocks.BARRIER);
    }
}
