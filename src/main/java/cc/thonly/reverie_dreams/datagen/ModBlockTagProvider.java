package cc.thonly.reverie_dreams.datagen;

import cc.thonly.mystias_izakaya.block.MIBlocks;
import cc.thonly.reverie_dreams.block.BlockTypeGroup;
import cc.thonly.reverie_dreams.block.ModBlocks;
import cc.thonly.reverie_dreams.block.WoodCreator;
import cc.thonly.reverie_dreams.data.ModTags;
import cc.thonly.reverie_dreams.fumo.FumoType;
import cc.thonly.reverie_dreams.fumo.Fumos;
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
public class ModBlockTagProvider extends FabricTagProvider.BlockTagProvider {
    public ModBlockTagProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider wrapperLookup) {
        TagAppender<Block, Block> fumo = valueLookupBuilder(ModTags.BlockTypeTag.FUMO);
        TagAppender<Block, Block> empty = valueLookupBuilder(ModTags.BlockTypeTag.EMPTY);
        TagAppender<Block, Block> sliver = valueLookupBuilder(ModTags.BlockTypeTag.SILVER);
        TagAppender<Block, Block> minTools = valueLookupBuilder(ModTags.BlockTypeTag.MIN_TOOL);
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

        for (FumoType instance : Fumos.getView()) {
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
                ModBlocks.WOODEN_BOX.chestBlock(),
                ModBlocks.CASH_BOX_BLOCK
        );

        pickaxeMineables.add(ModBlocks.SILVER_BLOCK, ModBlocks.SILVER_ORE, ModBlocks.DEEPSLATE_SILVER_ORE);
        pickaxeMineables.add(ModBlocks.SILVER_CHEST_BLOCK.chestBlock());
        pickaxeMineables.add(ModBlocks.ORB_ORE, ModBlocks.DEEPSLATE_ORB_ORE);
        pickaxeMineables.add(ModBlocks.DREAM_CRYSTAL_ORE);
        pickaxeMineables.add(ModBlocks.GENSOKYO_ALTAR);
        pickaxeMineables.add(ModBlocks.ANTI_COLLISION_BARREL);
        pickaxeMineables.add(ModBlocks.WHEEL_CHAIR);
//        pickaxeMineables.add(MIBlocks.COOKTOP);
        ModBlocks.SPIRITUAL.stream().forEach(axeMineables::add);
        MIBlocks.LEMON.stream().forEach(axeMineables::add);
        MIBlocks.GINKGO.stream().forEach(axeMineables::add);
        MIBlocks.PEACH.stream().forEach(axeMineables::add);
        ModBlocks.SPIRITUAL.stream().forEach(axeMineables::add);
        axeMineables.add(ModBlocks.DANMAKU_CRAFTING_TABLE);
        axeMineables.add(ModBlocks.MUSIC_BLOCK);
        axeMineables.add(ModBlocks.CASH_BOX_BLOCK);
        hoeMineables.add(ModBlocks.POWER_BLOCK);
        hoeMineables.add(ModBlocks.POINT_BLOCK);
        sliver.add(ModBlocks.SILVER_BLOCK, ModBlocks.SILVER_ORE, ModBlocks.DEEPSLATE_SILVER_ORE);
        ores.add(ModBlocks.SILVER_ORE, ModBlocks.DEEPSLATE_SILVER_ORE);
        ores.add(ModBlocks.ORB_ORE, ModBlocks.DEEPSLATE_ORB_ORE);

        pickaxeMineables.add(ModBlocks.ICE_SCALES.block(), ModBlocks.ICE_SCALES.slab(), ModBlocks.ICE_SCALES.stair());
        pickaxeMineables.add(ModBlocks.DREAM_STONE.block(), ModBlocks.DREAM_STONE.slab(), ModBlocks.DREAM_STONE.stair());
        pickaxeMineables.add(ModBlocks.DREAM_STONE_BRICK.block(), ModBlocks.DREAM_STONE_BRICK.slab(), ModBlocks.DREAM_STONE_BRICK.stair());
        pickaxeMineables.add(ModBlocks.MOON_STONE.block(), ModBlocks.MOON_STONE.slab(), ModBlocks.MOON_STONE.stair());
        pickaxeMineables.add(ModBlocks.MOON_STONE_BRICK.block(), ModBlocks.MOON_STONE_BRICK.slab(), ModBlocks.MOON_STONE_BRICK.stair());

        minTools.add(Blocks.BEDROCK);
        minTools.add(Blocks.BARRIER);
        empty.add(Blocks.BEDROCK);
        empty.add(Blocks.BARRIER);
    }
}
