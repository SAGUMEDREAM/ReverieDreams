package cc.thonly.reverie_dreams.block.bundle;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.api.registry.FlammableBlockRegistry;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import cc.thonly.reverie_dreams.registry.delegate.BlockDelegate;
import dev.architectury.hooks.item.tool.AxeItemHooks;
import dev.architectury.registry.fuel.FuelRegistry;
import lombok.Getter;
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
    private BlockDelegate log;
    private BlockDelegate wood;
    private BlockDelegate strippedLog;
    private BlockDelegate strippedWood;
    private BlockDelegate leaves;
    private BlockDelegate sapling;
    private BlockDelegate planks;
    private BlockDelegate stairs;
    private BlockDelegate slab;
    private BlockDelegate door;
    private BlockDelegate trapdoor;
    private BlockDelegate fence;
    private BlockDelegate fenceGate;
    private BlockDelegate button;
    @Nullable
    private BlockDelegate fruitLeaves;

    private WoodBundle(Identifier id, TreeGrower saplingGenerator) {
        super(id.getPath(), id);
        this.saplingGenerator = saplingGenerator;
        INSTANCES.add(this);
    }

    private WoodBundle(String name, TreeGrower saplingGenerator) {
        this(ReverieDreams.id(name), saplingGenerator);
    }

    public WoodBundle build() {
        this.log = RDBlocks.registerSimpleBlock(suffix("log"),
                RotatedPillarBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LOG).noOcclusion());

        this.wood = RDBlocks.registerSimpleBlock(suffix("wood"),
                RotatedPillarBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LOG).noOcclusion());

        this.strippedLog = RDBlocks.registerSimpleBlock(prefix(suffix("log"), "stripped"),
                RotatedPillarBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LOG).noOcclusion());

        this.strippedWood = RDBlocks.registerSimpleBlock(prefix(suffix("wood"), "stripped"),
                RotatedPillarBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_WOOD).noOcclusion());

        this.leaves = RDBlocks.registerSimpleBlock(suffix("leaves"),
                (settings) -> new TintedParticleLeavesBlock(0.01f, settings), BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LEAVES));

        this.sapling = RDBlocks.registerSimpleBlock(suffix("sapling"),
                (settings) -> new SaplingBlock(this.saplingGenerator, settings), BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SAPLING));

        this.planks = RDBlocks.registerSimpleBlock(suffix("planks"),
                Block::new, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS));

        this.stairs = RDBlocks.registerSimpleBlock(suffix("stairs"),
                (settings) -> new StairBlock(this.planks.asBlock().defaultBlockState(), settings), BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_STAIRS));

        this.slab = RDBlocks.registerSimpleBlock(suffix("slab"),
                SlabBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SLAB));

        this.door = RDBlocks.registerSimpleBlock(suffix("door"),
                (settings) -> new DoorBlock(BlockSetType.OAK, settings), BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_DOOR));

        this.trapdoor = RDBlocks.registerSimpleBlock(suffix("trapdoor"),
                (settings) -> new TrapDoorBlock(BlockSetType.OAK, settings), BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_TRAPDOOR));

        this.fence = RDBlocks.registerSimpleBlock(suffix("fence"),
                FenceBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_FENCE));

        this.fenceGate = RDBlocks.registerSimpleBlock(suffix("fence_gate"),
                (settings) -> new FenceGateBlock(WoodType.OAK, settings), BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_FENCE_GATE));

        this.button = RDBlocks.registerSimpleBlock(suffix("button"),
                (settings) -> new ButtonBlock(BlockSetType.OAK, 30, settings), BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_BUTTON));

        ReverieDreams.COMMON_LATE_INIT.add(() -> {
            // 剥皮：log -> stripped log，wood -> stripped wood
            AxeItemHooks.addStrippable(this.log().get(), this.strippedLog().get());
            AxeItemHooks.addStrippable(this.wood().get(), this.strippedWood().get());

            // 燃料
            FuelRegistry.register(300, this.log());
            FuelRegistry.register(300, this.strippedLog());
            FuelRegistry.register(300, this.wood());
            FuelRegistry.register(300, this.strippedWood());
            FuelRegistry.register(300, this.planks());
            FuelRegistry.register(300, this.stairs());
            FuelRegistry.register(300, this.slab());
            FuelRegistry.register(300, this.fence());
            FuelRegistry.register(300, this.fenceGate());
            FuelRegistry.register(200, this.door());
            FuelRegistry.register(300, this.trapdoor());
            FuelRegistry.register(100, this.button());
            FuelRegistry.register(100, this.sapling());

            // 可燃：igniteOdds, burnOdds
            FlammableBlockRegistry.register(this.log(), 5, 20);
            FlammableBlockRegistry.register(this.strippedLog(), 5, 20);
            FlammableBlockRegistry.register(this.wood(), 5, 20);
            FlammableBlockRegistry.register(this.strippedWood(), 5, 20);
            FlammableBlockRegistry.register(this.planks(), 5, 20);
            FlammableBlockRegistry.register(this.stairs(), 5, 20);
            FlammableBlockRegistry.register(this.slab(), 5, 20);
            FlammableBlockRegistry.register(this.fence(), 5, 20);
            FlammableBlockRegistry.register(this.fenceGate(), 5, 20);
            FlammableBlockRegistry.register(this.door(), 5, 20);
            FlammableBlockRegistry.register(this.trapdoor(), 5, 20);
            FlammableBlockRegistry.register(this.button(), 5, 20);
            FlammableBlockRegistry.register(this.leaves(), 30, 60);
            if (this.fruitLeaves != null) {
                FlammableBlockRegistry.register(this.fruitLeaves(), 30, 60);
            }
        });

        return this;
    }

    public BlockDelegate log() {
        return this.log;
    }

    public BlockDelegate wood() {
        return this.wood;
    }

    public BlockDelegate strippedLog() {
        return this.strippedLog;
    }

    public BlockDelegate strippedWood() {
        return this.strippedWood;
    }

    public BlockDelegate leaves() {
        return this.leaves;
    }

    public BlockDelegate sapling() {
        return this.sapling;
    }

    public BlockDelegate planks() {
        return this.planks;
    }

    public BlockDelegate stairs() {
        return this.stairs;
    }

    public BlockDelegate slab() {
        return this.slab;
    }

    public BlockDelegate door() {
        return this.door;
    }

    public BlockDelegate trapdoor() {
        return this.trapdoor;
    }

    public BlockDelegate fence() {
        return this.fence;
    }

    public BlockDelegate fenceGate() {
        return this.fenceGate;
    }

    public BlockDelegate button() {
        return this.button;
    }

    public BlockDelegate fruitLeaves() {
        return this.fruitLeaves;
    }

    public void setFruitLeaves(@NotNull BlockDelegate fruitLeaves) {
        this.fruitLeaves = fruitLeaves;
    }

    public Collection<BlockDelegate> stream() {
        ArrayList<BlockDelegate> result = new ArrayList<>(List.of(this.log,
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
