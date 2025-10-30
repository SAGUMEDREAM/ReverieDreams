package cc.thonly.reverie_dreams.block;

import cc.thonly.mystias_izakaya.block.kitchenware.AbstractKitchenwareBlock;
import lombok.Getter;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.VegetationBlock;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

@Getter
public enum BlockTypeGroup {
    BLOCK(),
    PLANT(),
    SAPLING(),
    FENCE(),
    FENCE_GATE(),
    WALL(),
    LEAVES(),
    STAIR(),
    SLAB(),
    BUTTON(),
    PRESSURE_PLATE(),
    TRAPDOOR(),
    DOOR(),
    FRUIT_LEAVES(),
    PICKAXE_MINEABLE(),
    AXE_MINEABLE(),
    SHOVEL_MINEABLE(),
    HOE_MINEABLE(),
    SWORD_MINEABLE(),
    KITCHENWARE(),
    ;
    private final Set<Block> entries = new LinkedHashSet<>();

    BlockTypeGroup() {
    }

    public static void join(Block block) {
        if (block instanceof FenceBlock) {
            FENCE.add(block);
        }
        if (block instanceof FenceGateBlock) {
            FENCE_GATE.add(block);
        }
        if (block instanceof StairBlock) {
            STAIR.add(block);
        }
        if (block instanceof SlabBlock) {
            SLAB.add(block);
        }
        if (block instanceof ButtonBlock) {
            BUTTON.add(block);
        }
        if (block instanceof PressurePlateBlock) {
            PRESSURE_PLATE.add(block);
        }
        if (block instanceof TrapDoorBlock) {
            TRAPDOOR.add(block);
        }
        if (block instanceof DoorBlock) {
            DOOR.add(block);
        }
        if (block instanceof LeavesBlock) {
            LEAVES.add(block);
        }
        if (block instanceof AbstractKitchenwareBlock) {
            KITCHENWARE.add(block);
        }
        if (block instanceof VegetationBlock) {
            PLANT.add(block);
        }
        BLOCK.add(block);
    }

    public void add(Block block) {
        this.entries.add(block);
    }

    public Stream<Block> stream() {
        return this.entries.stream();
    }

    public Collection<Item> items() {
        return Set.copyOf(this.entries.stream().map(Block::asItem).filter(Objects::nonNull).toList());
    }

    public Collection<Block> blocks() {
        return Set.copyOf(this.entries);
    }
}
