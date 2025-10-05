package cc.thonly.reverie_dreams.datagen;

import cc.thonly.mystias_izakaya.block.MIBlocks;
import cc.thonly.reverie_dreams.block.BlockTypeGroup;
import cc.thonly.reverie_dreams.block.ModBlocks;
import cc.thonly.reverie_dreams.block.WoodCreator;
import cc.thonly.reverie_dreams.data.ModTags;
import cc.thonly.reverie_dreams.fumo.Fumo;
import cc.thonly.reverie_dreams.fumo.Fumos;
import lombok.AccessLevel;
import lombok.Getter;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.data.tag.ProvidedTagBuilder;
import net.minecraft.item.ItemConvertible;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.PointOfInterestTypeTags;
import net.minecraft.registry.tag.TagKey;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Getter(AccessLevel.PRIVATE)
public class ModBlockTagProvider extends FabricTagProvider.BlockTagProvider {
    public ModBlockTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {
        ProvidedTagBuilder<Block, Block> fumo = valueLookupBuilder(ModTags.BlockTypeTag.FUMO);
        ProvidedTagBuilder<Block, Block> empty = valueLookupBuilder(ModTags.BlockTypeTag.EMPTY);
        ProvidedTagBuilder<Block, Block> sliver = valueLookupBuilder(ModTags.BlockTypeTag.SILVER);
        ProvidedTagBuilder<Block, Block> minTools = valueLookupBuilder(ModTags.BlockTypeTag.MIN_TOOL);
        ProvidedTagBuilder<Block, Block> axeMineables = valueLookupBuilder(BlockTags.AXE_MINEABLE);
        ProvidedTagBuilder<Block, Block> hoeMineables = valueLookupBuilder(BlockTags.HOE_MINEABLE);
        ProvidedTagBuilder<Block, Block> pickaxeMineables = valueLookupBuilder(BlockTags.PICKAXE_MINEABLE);
        ProvidedTagBuilder<Block, Block> shovelMineables = valueLookupBuilder(BlockTags.SHOVEL_MINEABLE);
        ProvidedTagBuilder<Block, Block> ores = valueLookupBuilder(ConventionalBlockTags.ORES);
        ProvidedTagBuilder<Block, Block> villagerJobSites = valueLookupBuilder(ConventionalBlockTags.VILLAGER_JOB_SITES);
//        ProvidedTagBuilder<Block, Block> villagerJobSites = valueLookupBuilder(PointOfInterestTypeTags.ACQUIRABLE_JOB_SITE);

        ProvidedTagBuilder<Block, Block> logs = valueLookupBuilder(BlockTags.LOGS);
        ProvidedTagBuilder<Block, Block> planks = valueLookupBuilder(BlockTags.PLANKS);
        for (WoodCreator instance : WoodCreator.INSTANCES) {
            logs.add(instance.log());
            planks.add(instance.planks());
        }

        for (Fumo instance : Fumos.getView()) {
            fumo.add(instance.block());
        }

        Map<TagKey<Block>, Collection<? extends ItemConvertible>> blockItemGroups = Map.of(
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
            ProvidedTagBuilder<Block, Block> builder = valueLookupBuilder(tag);
            for (ItemConvertible itemConvertible : list) {
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
