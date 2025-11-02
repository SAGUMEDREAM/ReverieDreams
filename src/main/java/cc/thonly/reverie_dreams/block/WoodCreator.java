package cc.thonly.reverie_dreams.block;

import cc.thonly.reverie_dreams.ReverieDreams;
import lombok.Getter;
import net.fabricmc.fabric.api.registry.StrippableBlockRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.BlockFamilies;
import net.minecraft.data.BlockFamily;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TintedParticleLeavesBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class WoodCreator extends AbstractBlockCreator {
    public static final List<WoodCreator> INSTANCES = new ArrayList<>();
    public static final List<Item> BLOCK_ITEMS = new ArrayList<>();
    @Getter
    private final TreeGrower saplingGenerator;
    @Getter
    private BlockFamily blockFamily;
    private Block log;
    private Block wood;
    private Block strippedLog;
    private Block strippedWood;
    private Block leaves;
    private Block sapling;
    private Block planks;
    private Block stairs;
    private Block slab;
    private Block door;
    private Block trapdoor;
    private Block fence;
    private Block fenceGate;
    private Block button;

    private WoodCreator(ResourceLocation id, TreeGrower saplingGenerator) {
        super(id.getPath(), id);
        this.saplingGenerator = saplingGenerator;
        INSTANCES.add(this);
    }

    private WoodCreator(String name, TreeGrower saplingGenerator) {
        this(ReverieDreams.id(name), saplingGenerator);
    }

    public WoodCreator build() {
        this.log = ModBlocks.registerSimpleBlock(suffix("log"),
                RotatedPillarBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LOG).noOcclusion());

        this.wood = ModBlocks.registerSimpleBlock(suffix("wood"),
                RotatedPillarBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LOG).noOcclusion());

        this.strippedLog = ModBlocks.registerSimpleBlock(prefix(suffix("log"), "stripped"),
                RotatedPillarBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LOG).noOcclusion());

        this.strippedWood = ModBlocks.registerSimpleBlock(prefix(suffix("wood"), "stripped"),
                RotatedPillarBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_WOOD).noOcclusion());

        this.leaves = ModBlocks.registerSimpleBlock(suffix("leaves"),
                (settings) -> new TintedParticleLeavesBlock(0.01f, settings), BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LEAVES));

        this.sapling = ModBlocks.registerSimpleBlock(suffix("sapling"),
                (settings) -> new SaplingBlock(this.saplingGenerator, settings), BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SAPLING));

        this.planks = ModBlocks.registerSimpleBlock(suffix("planks"),
                Block::new, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS));

        this.stairs = ModBlocks.registerSimpleBlock(suffix("stairs"),
                (settings) -> new StairBlock(this.planks.defaultBlockState(), settings), BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_STAIRS));

        this.slab = ModBlocks.registerSimpleBlock(suffix("slab"),
                SlabBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SLAB));

        this.door = ModBlocks.registerSimpleBlock(suffix("door"),
                (settings) -> new DoorBlock(BlockSetType.OAK, settings), BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_DOOR));

        this.trapdoor = ModBlocks.registerSimpleBlock(suffix("trapdoor"),
                (settings) -> new TrapDoorBlock(BlockSetType.OAK, settings), BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_TRAPDOOR));

        this.fence = ModBlocks.registerSimpleBlock(suffix("fence"),
                FenceBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_FENCE));

        this.fenceGate = ModBlocks.registerSimpleBlock(suffix("fence_gate"),
                (settings) -> new FenceGateBlock(WoodType.OAK, settings), BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_FENCE_GATE));

        this.button = ModBlocks.registerSimpleBlock(suffix("button"),
                (settings) -> new ButtonBlock(BlockSetType.OAK, 30, settings), BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_BUTTON));

        BuiltInRegistries.ITEM.addAlias(suffix("stair"), suffix("stairs"));

        this.stream().forEach((block) -> {
            BLOCK_ITEMS.add(block.asItem());
        });
        StrippableBlockRegistry.register(this.log, this.strippedLog);

        this.blockFamily = BlockFamilies.familyBuilder(this.planks())
                .slab(this.slab())
                .stairs(this.stairs())
                .fence(this.fence())
                .fenceGate(this.fenceGate())
                .button(this.button())
                .door(this.door())
                .trapdoor(this.trapdoor())
                .recipeGroupPrefix("wooden").recipeUnlockedBy("has_planks")
                .getFamily();
        return this;
    }

    public Block log() {
        return this.log;
    }

    public Block wood() {
        return this.wood;
    }

    public Block strippedLog() {
        return this.strippedLog;
    }

    public Block strippedWood() {
        return this.strippedWood;
    }

    public Block leaves() {
        return this.leaves;
    }

    public Block sapling() {
        return this.sapling;
    }

    public Block planks() {
        return this.planks;
    }

    public Block stairs() {
        return this.stairs;
    }

    public Block slab() {
        return this.slab;
    }

    public Block door() {
        return this.door;
    }

    public Block trapdoor() {
        return this.trapdoor;
    }

    public Block fence() {
        return this.fence;
    }

    public Block fenceGate() {
        return this.fenceGate;
    }

    public Block button() {
        return this.button;
    }

    public Stream<Block> stream() {
        return Stream.of(
                log,
                wood,
                strippedLog,
                strippedWood,
                leaves,
                sapling,
                planks,
                stairs,
                slab,
                door,
                trapdoor,
                fence,
                fenceGate,
                button
        ).filter(Objects::nonNull);
    }


    public static WoodCreator create(String name, TreeGrower saplingGenerator) {
        return new WoodCreator(name, saplingGenerator);
    }

    public static WoodCreator create(ResourceLocation id, TreeGrower saplingGenerator) {
        return new WoodCreator(id, saplingGenerator);
    }
}
