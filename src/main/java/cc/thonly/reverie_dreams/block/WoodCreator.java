package cc.thonly.reverie_dreams.block;

import cc.thonly.reverie_dreams.Touhou;
import lombok.Getter;
import net.fabricmc.fabric.api.registry.StrippableBlockRegistry;
import net.minecraft.block.*;
import net.minecraft.data.family.BlockFamilies;
import net.minecraft.data.family.BlockFamily;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class WoodCreator extends AbstractBlockCreator {
    public static final List<WoodCreator> INSTANCES = new ArrayList<>();
    public static final List<Item> BLOCK_ITEMS = new ArrayList<>();
    @Getter
    private final SaplingGenerator saplingGenerator;
    @Getter
    private BlockFamily blockFamily;
    private Block log;
    private Block wood;
    private Block strippedLog;
    private Block strippedWood;
    private Block leaves;
    private Block sapling;
    private Block planks;
    private Block stair;
    private Block slab;
    private Block door;
    private Block trapdoor;
    private Block fence;
    private Block fenceGate;
    private Block button;

    private WoodCreator(Identifier id, SaplingGenerator saplingGenerator) {
        super(id.getPath(), id);
        this.saplingGenerator = saplingGenerator;
        INSTANCES.add(this);
    }

    private WoodCreator(String name, SaplingGenerator saplingGenerator) {
        this(Touhou.id(name), saplingGenerator);
    }

    public WoodCreator build() {
        this.log = ModBlocks.registerSimpleBlock(suffix("log"),
                PillarBlock::new, AbstractBlock.Settings.copy(Blocks.OAK_LOG).nonOpaque());

        this.wood = ModBlocks.registerSimpleBlock(suffix("wood"),
                PillarBlock::new, AbstractBlock.Settings.copy(Blocks.OAK_LOG).nonOpaque());

        this.strippedLog = ModBlocks.registerSimpleBlock(prefix(suffix("log"), "stripped"),
                PillarBlock::new, AbstractBlock.Settings.copy(Blocks.OAK_LOG).nonOpaque());

        this.strippedWood = ModBlocks.registerSimpleBlock(prefix(suffix("wood"), "stripped"),
                PillarBlock::new, AbstractBlock.Settings.copy(Blocks.OAK_WOOD).nonOpaque());

        this.leaves = ModBlocks.registerSimpleBlock(suffix("leaves"),
                (settings) -> new TintedParticleLeavesBlock(0.01f, settings), AbstractBlock.Settings.copy(Blocks.OAK_LEAVES));

        this.sapling = ModBlocks.registerSimpleBlock(suffix("sapling"),
                (settings) -> new SaplingBlock(this.saplingGenerator, settings), AbstractBlock.Settings.copy(Blocks.OAK_SAPLING));

        this.planks = ModBlocks.registerSimpleBlock(suffix("planks"),
                Block::new, AbstractBlock.Settings.copy(Blocks.OAK_PLANKS));

        this.stair = ModBlocks.registerSimpleBlock(suffix("stairs"),
                (settings) -> new StairsBlock(this.planks.getDefaultState(), settings), AbstractBlock.Settings.copy(Blocks.OAK_STAIRS));

        this.slab = ModBlocks.registerSimpleBlock(suffix("slab"),
                SlabBlock::new, AbstractBlock.Settings.copy(Blocks.OAK_SLAB));

        this.door = ModBlocks.registerSimpleBlock(suffix("door"),
                (settings) -> new DoorBlock(BlockSetType.OAK, settings), AbstractBlock.Settings.copy(Blocks.OAK_DOOR));

        this.trapdoor = ModBlocks.registerSimpleBlock(suffix("trapdoor"),
                (settings) -> new TrapdoorBlock(BlockSetType.OAK, settings), AbstractBlock.Settings.copy(Blocks.OAK_TRAPDOOR));

        this.fence = ModBlocks.registerSimpleBlock(suffix("fence"),
                FenceBlock::new, AbstractBlock.Settings.copy(Blocks.OAK_FENCE));

        this.fenceGate = ModBlocks.registerSimpleBlock(suffix("fence_gate"),
                (settings) -> new FenceGateBlock(WoodType.OAK, settings), AbstractBlock.Settings.copy(Blocks.OAK_FENCE_GATE));

        this.button = ModBlocks.registerSimpleBlock(suffix("button"),
                (settings) -> new ButtonBlock(BlockSetType.OAK, 30, settings), AbstractBlock.Settings.copy(Blocks.OAK_BUTTON));

        Registries.ITEM.addAlias(suffix("stair"), suffix("stairs"));

        this.stream().forEach((block) -> {
            BLOCK_ITEMS.add(block.asItem());
        });
        StrippableBlockRegistry.register(this.log, this.strippedLog);

        this.blockFamily = BlockFamilies.register(this.planks())
                .slab(this.slab())
                .stairs(this.stair())
                .fence(this.fence())
                .fenceGate(this.fenceGate())
                .button(this.button())
                .door(this.door())
                .trapdoor(this.trapdoor())
                .group("wooden").unlockCriterionName("has_planks")
                .build();
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

    public Block stair() {
        return this.stair;
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
                stair,
                slab,
                door,
                trapdoor,
                fence,
                fenceGate,
                button
        ).filter(Objects::nonNull);
    }


    public static WoodCreator create(String name, SaplingGenerator saplingGenerator) {
        return new WoodCreator(name, saplingGenerator);
    }

    public static WoodCreator create(Identifier id, SaplingGenerator saplingGenerator) {
        return new WoodCreator(id, saplingGenerator);
    }
}
