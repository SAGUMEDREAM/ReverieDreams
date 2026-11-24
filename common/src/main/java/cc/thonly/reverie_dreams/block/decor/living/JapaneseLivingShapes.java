package cc.thonly.reverie_dreams.block.decor.living;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Generated from the exact integer cuboids in tools/japanese-living-batch1/build.mjs.
 */
public final class JapaneseLivingShapes {
    private static final Map<String, VoxelShape> NORTH = Map.ofEntries(
            Map.entry("bell_end", Shapes.or(Block.box(6, 14, 6, 10, 16, 10), Block.box(5, 12, 5, 11, 14, 11), Block.box(4, 10, 4, 12, 12, 12), Block.box(3, 5, 3, 13, 10, 5), Block.box(3, 5, 11, 13, 10, 13), Block.box(3, 5, 5, 5, 10, 11), Block.box(11, 5, 5, 13, 10, 11), Block.box(2, 4, 2, 14, 5, 5), Block.box(2, 4, 11, 14, 5, 14), Block.box(2, 4, 5, 5, 5, 11), Block.box(11, 4, 5, 14, 5, 11), Block.box(7, 0, 7, 9, 3, 9), Block.box(7, 3, 7, 9, 10, 9), Block.box(6, 2, 6, 10, 4, 10), Block.box(5, 6, 2, 11, 7, 3), Block.box(5, 6, 13, 11, 7, 14))),
            Map.entry("bell_middle", Shapes.or(Block.box(6, 14, 6, 10, 16, 10), Block.box(5, 12, 5, 11, 14, 11), Block.box(4, 10, 4, 12, 12, 12), Block.box(3, 5, 3, 13, 10, 5), Block.box(3, 5, 11, 13, 10, 13), Block.box(3, 5, 5, 5, 10, 11), Block.box(11, 5, 5, 13, 10, 11), Block.box(2, 4, 2, 14, 5, 5), Block.box(2, 4, 11, 14, 5, 14), Block.box(2, 4, 5, 5, 5, 11), Block.box(11, 4, 5, 14, 5, 11), Block.box(7, 0, 7, 9, 3, 9), Block.box(7, 3, 7, 9, 10, 9), Block.box(6, 2, 6, 10, 4, 10), Block.box(5, 6, 2, 11, 7, 3), Block.box(5, 6, 13, 11, 7, 14))),
            Map.entry("rope_end", Shapes.or(Block.box(7, 6, 7, 9, 16, 9), Block.box(6, 5, 6, 10, 7, 10), Block.box(5, 3, 5, 11, 5, 11), Block.box(5, 0, 5, 7, 3, 7), Block.box(9, 0, 5, 11, 3, 7), Block.box(7, 0, 7, 9, 3, 9), Block.box(5, 0, 9, 7, 3, 11), Block.box(9, 0, 9, 11, 3, 11))),
            Map.entry("rope_middle", Shapes.or(Block.box(7, 0, 7, 9, 16, 9))),
            Map.entry("chime_end", Shapes.or(Block.box(7, 13, 7, 8, 16, 8), Block.box(6, 12, 6, 10, 13, 10), Block.box(5, 10, 5, 11, 12, 11), Block.box(4, 7, 4, 12, 10, 6), Block.box(4, 7, 10, 12, 10, 12), Block.box(4, 7, 6, 6, 10, 10), Block.box(10, 7, 6, 12, 10, 10), Block.box(7, 5, 7, 9, 9, 9), Block.box(7, 4, 7, 8, 7, 8), Block.box(6, 0, 7, 10, 4, 8), Block.box(7, 0, 6, 8, 3, 7))),
            Map.entry("chime_middle", Shapes.or(Block.box(7, 13, 7, 8, 16, 8), Block.box(6, 12, 6, 10, 13, 10), Block.box(5, 10, 5, 11, 12, 11), Block.box(4, 7, 4, 12, 10, 6), Block.box(4, 7, 10, 12, 10, 12), Block.box(4, 7, 6, 6, 10, 10), Block.box(10, 7, 6, 12, 10, 10), Block.box(7, 5, 7, 9, 9, 9), Block.box(7, 4, 7, 8, 7, 8), Block.box(6, 0, 7, 10, 4, 8), Block.box(7, 0, 6, 8, 3, 7))),
            Map.entry("hook_end", Shapes.or(Block.box(7, 9, 7, 8, 16, 8), Block.box(8, 11, 7, 12, 12, 8), Block.box(10, 8, 7, 11, 14, 8), Block.box(6, 8, 7, 10, 9, 8), Block.box(5, 7, 7, 6, 9, 8), Block.box(10, 7, 7, 11, 9, 8), Block.box(4, 2, 4, 12, 5, 12), Block.box(3, 3, 4, 4, 6, 12), Block.box(12, 3, 4, 13, 6, 12), Block.box(4, 3, 3, 12, 6, 4), Block.box(4, 3, 12, 12, 6, 13), Block.box(5, 6, 5, 11, 7, 11), Block.box(7, 7, 7, 9, 8, 9))),
            Map.entry("hook_middle", Shapes.or(Block.box(7, 0, 7, 8, 16, 8), Block.box(8, 9, 7, 12, 10, 8), Block.box(10, 6, 7, 11, 12, 8))),
            Map.entry("futon_head", Shapes.or(Block.box(1, 0, 0, 15, 1, 16), Block.box(1, 1, 0, 15, 2, 16), Block.box(1, 2, 8, 15, 3, 16), Block.box(2, 3, 9, 14, 4, 16), Block.box(2, 2, 1, 14, 4, 7), Block.box(3, 4, 2, 13, 5, 6), Block.box(2, 2, 7, 14, 3, 8))),
            Map.entry("futon_foot", Shapes.or(Block.box(1, 0, 0, 15, 1, 16), Block.box(1, 1, 0, 15, 2, 16), Block.box(1, 2, 0, 15, 3, 16), Block.box(2, 3, 0, 14, 4, 16))),
            Map.entry("folded_futon", Shapes.or(Block.box(2, 0, 2, 14, 1, 14), Block.box(2, 1, 2, 14, 2, 14), Block.box(2, 2, 2, 14, 3, 14), Block.box(2, 3, 2, 14, 4, 14), Block.box(2, 4, 2, 14, 5, 14), Block.box(3, 5, 3, 13, 6, 13), Block.box(3, 6, 4, 13, 7, 10), Block.box(4, 7, 5, 12, 8, 9))),
            Map.entry("irori", Shapes.or(Block.box(0, 0, 0, 16, 6, 16), Block.box(0, 6, 0, 3, 16, 16), Block.box(13, 6, 0, 16, 16, 16), Block.box(3, 6, 0, 13, 16, 3), Block.box(3, 6, 13, 13, 16, 16), Block.box(3, 6, 3, 13, 9, 13), Block.box(4, 9, 5, 12, 11, 7), Block.box(5, 9, 8, 11, 11, 10), Block.box(6, 11, 5, 8, 12, 11)))
    );
    private static final Map<String, Map<Direction, VoxelShape>> ROTATED = NORTH.entrySet().stream()
            .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, e -> Shapes.rotateHorizontal(e.getValue())));

    public static VoxelShape north(String key) {
        return NORTH.get(key);
    }

    public static VoxelShape shape(String key, Direction facing) {
        return ROTATED.get(key).get(facing);
    }

    private JapaneseLivingShapes() {
    }
}
