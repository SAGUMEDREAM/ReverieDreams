package cc.thonly.reverie_dreams.datagen;

import cc.thonly.mystias_izakaya.block.MIBlocks;
import cc.thonly.reverie_dreams.fumo.Fumo;
import cc.thonly.reverie_dreams.fumo.Fumos;
import cc.thonly.reverie_dreams.block.ModBlocks;
import cc.thonly.reverie_dreams.data.ModTags;
import lombok.AccessLevel;
import lombok.Getter;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.data.tag.ProvidedTagBuilder;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.BlockTags;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@Getter(AccessLevel.PRIVATE)
public class ModBlockTagProvider extends FabricTagProvider.BlockTagProvider {
    public static final Set<Block> FENCES = new HashSet<>();
    public static final Set<Block> FENCE_GATES = new HashSet<>();
    public static final Set<Block> WALLS = new HashSet<>();
    public static final Set<Block> LEAVES = new HashSet<>();
    public static final Set<Block> SAPLINGS = new HashSet<>();
    public static final Set<Block> STAIRS = new HashSet<>();
    public static final Set<Block> SLABS = new HashSet<>();
    public static final Set<Block> BUTTONS = new HashSet<>();
    public static final Set<Block> PRESSURE_PLATES = new HashSet<>();
    public static final Set<Block> TRAPDOORS = new HashSet<>();
    public static final Set<Block> DOORS = new HashSet<>();


    public ModBlockTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {
        ProvidedTagBuilder<Block, Block> fumo = valueLookupBuilder(ModTags.BlockTypeTag.FUMO);
        ProvidedTagBuilder<Block, Block> empty = valueLookupBuilder(ModTags.BlockTypeTag.EMPTY);
        ProvidedTagBuilder<Block, Block> fences = valueLookupBuilder(BlockTags.FENCES);
        ProvidedTagBuilder<Block, Block> walls = valueLookupBuilder(BlockTags.WALLS);
        ProvidedTagBuilder<Block, Block> woodenFences = valueLookupBuilder(BlockTags.WOODEN_FENCES);
        ProvidedTagBuilder<Block, Block> fenceGates = valueLookupBuilder(BlockTags.FENCE_GATES);
        ProvidedTagBuilder<Block, Block> stairs = valueLookupBuilder(BlockTags.STAIRS);
        ProvidedTagBuilder<Block, Block> slabs = valueLookupBuilder(BlockTags.SLABS);
        ProvidedTagBuilder<Block, Block> saplings = valueLookupBuilder(BlockTags.SAPLINGS);
        ProvidedTagBuilder<Block, Block> leaves = valueLookupBuilder(BlockTags.LEAVES);
        ProvidedTagBuilder<Block, Block> buttons = valueLookupBuilder(BlockTags.BUTTONS);
        ProvidedTagBuilder<Block, Block> pressurePlates = valueLookupBuilder(BlockTags.PRESSURE_PLATES);
        ProvidedTagBuilder<Block, Block> trapdoors = valueLookupBuilder(BlockTags.TRAPDOORS);
        ProvidedTagBuilder<Block, Block> doors = valueLookupBuilder(BlockTags.DOORS);
        ProvidedTagBuilder<Block, Block> sliver = valueLookupBuilder(ModTags.BlockTypeTag.SILVER);
        ProvidedTagBuilder<Block, Block> minTools = valueLookupBuilder(ModTags.BlockTypeTag.MIN_TOOL);
        ProvidedTagBuilder<Block, Block> axeMineables = valueLookupBuilder(BlockTags.AXE_MINEABLE);
        ProvidedTagBuilder<Block, Block> hoeMineables = valueLookupBuilder(BlockTags.HOE_MINEABLE);
        ProvidedTagBuilder<Block, Block> pickaxeMineables = valueLookupBuilder(BlockTags.PICKAXE_MINEABLE);
        ProvidedTagBuilder<Block, Block> shovelMineables = valueLookupBuilder(BlockTags.SHOVEL_MINEABLE);
        ProvidedTagBuilder<Block, Block> ores = valueLookupBuilder(ConventionalBlockTags.ORES);

        for (Fumo instance : Fumos.getView()) {
            fumo.add(instance.block());
        }

        LEAVES.forEach(leaves::add);
        SAPLINGS.forEach(saplings::add);
        FENCES.forEach(woodenFences::add);
        FENCES.forEach(fences::add);
        WALLS.forEach(walls::add);
        FENCE_GATES.forEach(fenceGates::add);
        STAIRS.forEach(stairs::add);
        SLABS.forEach(slabs::add);
        BUTTONS.forEach(buttons::add);
        PRESSURE_PLATES.forEach(pressurePlates::add);
        TRAPDOORS.forEach(trapdoors::add);
        DOORS.forEach(doors::add);

        pickaxeMineables.add(ModBlocks.SILVER_BLOCK, ModBlocks.SILVER_ORE, ModBlocks.DEEPSLATE_SILVER_ORE);
        pickaxeMineables.add(ModBlocks.ORB_ORE, ModBlocks.DEEPSLATE_ORB_ORE);
        pickaxeMineables.add(ModBlocks.DREAM_CRYSTAL_ORE);
        pickaxeMineables.add(ModBlocks.GENSOKYO_ALTAR);
//        pickaxeMineables.add(MIBlocks.COOKTOP);
        ModBlocks.SPIRITUAL.stream().forEach(axeMineables::add);
        MIBlocks.LEMON.stream().forEach(axeMineables::add);
        MIBlocks.GINKGO.stream().forEach(axeMineables::add);
        MIBlocks.PEACH.stream().forEach(axeMineables::add);
        ModBlocks.SPIRITUAL.stream().forEach(axeMineables::add);
        axeMineables.add(ModBlocks.DANMAKU_CRAFTING_TABLE);
        axeMineables.add(ModBlocks.MUSIC_BLOCK);
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
