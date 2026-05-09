package cc.thonly.reverie_dreams.block.bundle;

import cc.thonly.keine.api.KeineRegistries;
import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import lombok.Getter;
import net.blay09.mods.balm.world.level.block.BalmBlockRegistrar;
import net.blay09.mods.balm.world.level.block.DeferredBlock;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class WoodBundle extends AbstractBlockBundle {
    public static final List<WoodBundle> INSTANCES = new ArrayList<>();
    @Getter
    private final TreeGrower saplingGenerator;
    private DeferredBlock log;
    private DeferredBlock wood;
    private DeferredBlock strippedLog;
    private DeferredBlock strippedWood;
    private DeferredBlock leaves;
    private DeferredBlock sapling;
    private DeferredBlock planks;
    private DeferredBlock stairs;
    private DeferredBlock slab;
    private DeferredBlock door;
    private DeferredBlock trapdoor;
    private DeferredBlock fence;
    private DeferredBlock fenceGate;
    private DeferredBlock button;
    @Nullable
    private DeferredBlock fruitLeaves;

    private WoodBundle(Identifier id, TreeGrower saplingGenerator) {
        super(id.getPath(), id);
        this.saplingGenerator = saplingGenerator;
        INSTANCES.add(this);
    }

    private WoodBundle(String name, TreeGrower saplingGenerator) {
        this(ReverieDreams.id(name), saplingGenerator);
    }

    public WoodBundle build(BalmBlockRegistrar registrar) {
        this.log = RDBlocks.registerSimpleBlock(registrar, suffix("log"),
                RotatedPillarBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LOG).noOcclusion());

        this.wood = RDBlocks.registerSimpleBlock(registrar, suffix("wood"),
                RotatedPillarBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LOG).noOcclusion());

        this.strippedLog = RDBlocks.registerSimpleBlock(registrar, prefix(suffix("log"), "stripped"),
                RotatedPillarBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LOG).noOcclusion());

        this.strippedWood = RDBlocks.registerSimpleBlock(registrar, prefix(suffix("wood"), "stripped"),
                RotatedPillarBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_WOOD).noOcclusion());

        this.leaves = RDBlocks.registerSimpleBlock(registrar, suffix("leaves"),
                (settings) -> new TintedParticleLeavesBlock(0.01f, settings), BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LEAVES));

        this.sapling = RDBlocks.registerSimpleBlock(registrar, suffix("sapling"),
                (settings) -> new SaplingBlock(this.saplingGenerator, settings), BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SAPLING));

        this.planks = RDBlocks.registerSimpleBlock(registrar, suffix("planks"),
                Block::new, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS));

        this.stairs = RDBlocks.registerSimpleBlock(registrar, suffix("stairs"),
                (settings) -> new StairBlock(this.planks.defaultBlockState(), settings), BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_STAIRS));

        this.slab = RDBlocks.registerSimpleBlock(registrar, suffix("slab"),
                SlabBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SLAB));

        this.door = RDBlocks.registerSimpleBlock(registrar, suffix("door"),
                (settings) -> new DoorBlock(BlockSetType.OAK, settings), BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_DOOR));

        this.trapdoor = RDBlocks.registerSimpleBlock(registrar, suffix("trapdoor"),
                (settings) -> new TrapDoorBlock(BlockSetType.OAK, settings), BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_TRAPDOOR));

        this.fence = RDBlocks.registerSimpleBlock(registrar, suffix("fence"),
                FenceBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_FENCE));

        this.fenceGate = RDBlocks.registerSimpleBlock(registrar, suffix("fence_gate"),
                (settings) -> new FenceGateBlock(WoodType.OAK, settings), BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_FENCE_GATE));

        this.button = RDBlocks.registerSimpleBlock(registrar, suffix("button"),
                (settings) -> new ButtonBlock(BlockSetType.OAK, 30, settings), BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_BUTTON));

        KeineRegistries registries = ReverieDreams.getKeineRegistries();
        registries.strippableBlockRegistry().register(context -> {
            context.add(this.log(), this.strippedLog());
        });
        registries.flammableBlockRegistry().register(context -> {
            context.addBlock(this.log(), 5, 20);
            context.addBlock(this.strippedLog(), 5, 20);
            context.addBlock(this.wood(), 5, 20);
            context.addBlock(this.strippedWood(), 5, 20);
            context.addBlock(this.planks(), 5, 20);
            context.addBlock(this.stairs(), 5, 20);
            context.addBlock(this.slab(), 5, 20);
            context.addBlock(this.fence(), 5, 20);
            context.addBlock(this.fenceGate(), 5, 20);
        });
        registries.fuelRegistry().register(context -> {
            context.addBlock(this.log(), 300);
            context.addBlock(this.strippedLog(), 300);
            context.addBlock(this.wood(), 300);
            context.addBlock(this.strippedWood(), 300);
            context.addBlock(this.planks(), 300);
            context.addBlock(this.stairs(), 300);
            context.addBlock(this.slab(), 300);
            context.addBlock(this.fence(), 300);
            context.addBlock(this.fenceGate(), 300);
            context.addBlock(this.fence(), 300);
        });

        return this;
    }

    public DeferredBlock log() {
        return this.log;
    }

    public DeferredBlock wood() {
        return this.wood;
    }

    public DeferredBlock strippedLog() {
        return this.strippedLog;
    }

    public DeferredBlock strippedWood() {
        return this.strippedWood;
    }

    public DeferredBlock leaves() {
        return this.leaves;
    }

    public DeferredBlock sapling() {
        return this.sapling;
    }

    public DeferredBlock planks() {
        return this.planks;
    }

    public DeferredBlock stairs() {
        return this.stairs;
    }

    public DeferredBlock slab() {
        return this.slab;
    }

    public DeferredBlock door() {
        return this.door;
    }

    public DeferredBlock trapdoor() {
        return this.trapdoor;
    }

    public DeferredBlock fence() {
        return this.fence;
    }

    public DeferredBlock fenceGate() {
        return this.fenceGate;
    }

    public DeferredBlock button() {
        return this.button;
    }

    public DeferredBlock fruitLeaves() {
        return this.fruitLeaves;
    }

    public void setFruitLeaves(@NotNull DeferredBlock fruitLeaves) {
        this.fruitLeaves = fruitLeaves;
    }

    public Collection<DeferredBlock> stream() {
        ArrayList<DeferredBlock> result = new ArrayList<>(List.of(this.log,
                this.wood,
                this.strippedLog,
                this.strippedWood,
                this.leaves,
                this.sapling,
                this.planks,
                this.stairs,
                this.slab,
                this.door,
                this.trapdoor,
                this.fence,
                this.fenceGate,
                this.button)
        );
        if (this.fruitLeaves != null) {
            result.add(this.fruitLeaves);
        }
        return result;
    }

    public static WoodBundle create(String name, TreeGrower saplingGenerator) {
        return new WoodBundle(name, saplingGenerator);
    }

    public static WoodBundle create(Identifier id, TreeGrower saplingGenerator) {
        return new WoodBundle(id, saplingGenerator);
    }
}
