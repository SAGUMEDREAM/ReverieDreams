package cc.thonly.reverie_dreams.block.cooking;

import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Generated with the model meshes by tools/cookware-remodel-v1/models.mjs.
 */
public final class KitchenwareShapes {
    private static final Map<String, Map<Direction, VoxelShape>> OUTLINES = create();

    private KitchenwareShapes() {
    }

    public static VoxelShape outline(String model, Direction facing) {
        Map<Direction, VoxelShape> shapes = OUTLINES.get(model);
        if (shapes == null || !facing.getAxis().isHorizontal())
            throw new IllegalArgumentException(model + ": " + facing);
        return shapes.get(facing);
    }

    public static VoxelShape outline(BlockState state, String baseModel) {
        var id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        String model = id != null && id.getNamespace().equals("reverie_dreams") && OUTLINES.containsKey(id.getPath()) ? id.getPath() : baseModel;
        return outline(model, state.getValue(AbstractKitchenwareBlock.FACING));
    }

    private static Map<String, Map<Direction, VoxelShape>> create() {
        Map<String, Map<Direction, VoxelShape>> shapes = new HashMap<>();
        add(shapes, "cooking_pot",
                6, 0, 5, 10, 10, 11,
                5, 0, 6, 6, 10, 10,
                10, 0, 6, 11, 10, 10,
                5, 1, 4, 11, 9, 5,
                4, 1, 5, 6, 9, 6,
                10, 1, 5, 12, 9, 6,
                4, 1, 6, 5, 9, 11,
                11, 1, 6, 12, 9, 11,
                5, 1, 10, 6, 9, 12,
                10, 1, 10, 11, 9, 12,
                6, 1, 11, 10, 9, 12,
                5, 3, 3, 11, 6, 4,
                4, 3, 4, 5, 6, 5,
                11, 3, 4, 12, 6, 5,
                3, 3, 5, 4, 6, 11,
                12, 3, 5, 13, 6, 11,
                4, 3, 11, 5, 6, 12,
                11, 3, 11, 12, 6, 12,
                5, 3, 12, 11, 6, 13,
                1, 5, 6, 3, 6, 10,
                13, 5, 6, 15, 6, 10,
                1, 6, 6, 3, 8, 7,
                13, 6, 6, 15, 8, 7,
                1, 6, 9, 3, 8, 10,
                13, 6, 9, 15, 8, 10,
                5, 7, 3, 11, 8, 4,
                4, 7, 4, 5, 8, 5,
                11, 7, 4, 12, 8, 5,
                3, 7, 5, 4, 8, 11,
                12, 7, 5, 13, 8, 11,
                1, 7, 7, 3, 8, 9,
                13, 7, 7, 15, 8, 9,
                4, 7, 11, 5, 8, 12,
                11, 7, 11, 12, 8, 12,
                5, 7, 12, 11, 8, 13,
                7, 10, 7, 9, 11, 9);
        add(shapes, "nuke_cooking_pot",
                6, 0, 5, 10, 10, 11,
                5, 0, 6, 6, 10, 10,
                10, 0, 6, 11, 10, 10,
                5, 1, 4, 11, 9, 5,
                4, 1, 5, 6, 9, 6,
                10, 1, 5, 12, 9, 6,
                4, 1, 6, 5, 9, 11,
                11, 1, 6, 12, 9, 11,
                5, 1, 10, 6, 9, 12,
                10, 1, 10, 11, 9, 12,
                6, 1, 11, 10, 9, 12,
                6, 3, 2, 10, 5, 4,
                5, 3, 3, 6, 6, 4,
                10, 3, 3, 11, 6, 4,
                4, 3, 4, 5, 6, 5,
                11, 3, 4, 12, 6, 5,
                3, 3, 5, 4, 6, 11,
                12, 3, 5, 13, 6, 11,
                4, 3, 11, 5, 6, 12,
                11, 3, 11, 12, 6, 12,
                5, 3, 12, 11, 6, 13,
                7, 5, 2, 9, 6, 4,
                6, 5, 3, 7, 6, 4,
                9, 5, 3, 10, 6, 4,
                1, 5, 6, 3, 6, 10,
                13, 5, 6, 15, 6, 10,
                1, 6, 6, 3, 8, 7,
                13, 6, 6, 15, 8, 7,
                1, 6, 9, 3, 8, 10,
                13, 6, 9, 15, 8, 10,
                5, 7, 3, 11, 8, 4,
                4, 7, 4, 5, 8, 5,
                11, 7, 4, 12, 8, 5,
                3, 7, 5, 4, 8, 11,
                12, 7, 5, 13, 8, 11,
                1, 7, 7, 3, 8, 9,
                13, 7, 7, 15, 8, 9,
                4, 7, 11, 5, 8, 12,
                11, 7, 11, 12, 8, 12,
                5, 7, 12, 11, 8, 13,
                7, 10, 7, 9, 11, 9);
        add(shapes, "grill",
                2, 0, 3, 14, 1, 13,
                2, 1, 3, 4, 2, 13,
                6, 1, 3, 7, 2, 13,
                9, 1, 3, 10, 2, 13,
                12, 1, 3, 14, 2, 13,
                4, 1, 4, 6, 2, 13,
                7, 1, 4, 9, 2, 13,
                10, 1, 4, 12, 2, 13,
                2, 2, 3, 14, 5, 4,
                2, 2, 4, 3, 5, 13,
                13, 2, 4, 14, 5, 13,
                3, 2, 12, 13, 5, 13,
                4, 4, 4, 5, 5, 12,
                6, 4, 4, 7, 5, 12,
                8, 4, 4, 9, 5, 12,
                10, 4, 4, 11, 5, 12,
                12, 4, 4, 13, 5, 12);
        add(shapes, "nuke_grill",
                2, 0, 3, 14, 1, 13,
                2, 1, 3, 4, 2, 13,
                6, 1, 3, 7, 2, 13,
                9, 1, 3, 10, 2, 13,
                12, 1, 3, 14, 2, 13,
                4, 1, 4, 6, 2, 13,
                7, 1, 4, 9, 2, 13,
                10, 1, 4, 12, 2, 13,
                7, 2, 2, 9, 3, 4,
                2, 2, 3, 7, 5, 4,
                9, 2, 3, 14, 5, 4,
                2, 2, 4, 3, 5, 13,
                13, 2, 4, 14, 5, 13,
                1, 2, 5, 2, 3, 10,
                14, 2, 5, 15, 3, 10,
                3, 2, 12, 13, 5, 13,
                7, 3, 3, 9, 5, 4,
                4, 4, 4, 5, 5, 12,
                6, 4, 4, 7, 5, 12,
                8, 4, 4, 9, 5, 12,
                10, 4, 4, 11, 5, 12,
                12, 4, 4, 13, 5, 12);
        add(shapes, "frying_pan",
                4, 0, 0, 12, 1, 10,
                3, 0, 1, 4, 1, 9,
                12, 0, 1, 13, 1, 9,
                2, 0, 2, 3, 3, 8,
                13, 0, 2, 14, 3, 8,
                4, 1, 0, 12, 3, 1,
                3, 1, 1, 5, 3, 2,
                11, 1, 1, 13, 3, 2,
                3, 1, 2, 4, 3, 3,
                12, 1, 2, 13, 3, 3,
                3, 1, 7, 4, 3, 9,
                12, 1, 7, 13, 3, 9,
                4, 1, 8, 5, 3, 10,
                11, 1, 8, 12, 3, 10,
                5, 1, 9, 11, 3, 10,
                7, 1, 10, 9, 2, 16,
                7, 2, 12, 9, 3, 16);
        add(shapes, "nuke_frying_pan",
                4, 0, 0, 12, 1, 10,
                3, 0, 1, 4, 1, 9,
                12, 0, 1, 13, 1, 9,
                2, 0, 2, 3, 3, 8,
                13, 0, 2, 14, 3, 8,
                4, 1, 0, 12, 3, 1,
                3, 1, 1, 5, 3, 2,
                11, 1, 1, 13, 3, 2,
                3, 1, 2, 4, 3, 3,
                12, 1, 2, 13, 3, 3,
                3, 1, 7, 4, 3, 9,
                12, 1, 7, 13, 3, 9,
                4, 1, 8, 5, 3, 10,
                11, 1, 8, 12, 3, 10,
                5, 1, 9, 11, 3, 10,
                7, 1, 10, 9, 2, 16,
                7, 2, 10, 9, 3, 11,
                7, 2, 12, 9, 3, 16);
        add(shapes, "steamer",
                5, 0, 4, 11, 10, 12,
                4, 0, 5, 5, 10, 11,
                11, 0, 5, 12, 10, 11,
                5, 1, 3, 11, 10, 4,
                4, 1, 4, 5, 10, 5,
                11, 1, 4, 12, 10, 5,
                3, 1, 5, 4, 10, 11,
                12, 1, 5, 13, 10, 11,
                4, 1, 11, 5, 10, 12,
                11, 1, 11, 12, 10, 12,
                5, 1, 12, 11, 10, 13,
                1, 6, 7, 3, 7, 9,
                13, 6, 7, 15, 7, 9,
                4, 8, 2, 12, 9, 3,
                3, 8, 3, 5, 9, 4,
                11, 8, 3, 13, 9, 4,
                2, 8, 4, 4, 9, 5,
                12, 8, 4, 14, 9, 5,
                2, 8, 5, 3, 9, 12,
                13, 8, 5, 14, 9, 12,
                3, 8, 11, 4, 9, 13,
                12, 8, 11, 13, 9, 13,
                4, 8, 12, 5, 9, 14,
                11, 8, 12, 12, 9, 14,
                5, 8, 13, 11, 9, 14,
                6, 10, 5, 10, 11, 11,
                5, 10, 6, 6, 11, 10,
                10, 10, 6, 11, 11, 10,
                7, 11, 7, 9, 12, 9);
        add(shapes, "nuke_steamer",
                5, 0, 4, 11, 10, 12,
                4, 0, 5, 5, 10, 11,
                11, 0, 5, 12, 10, 11,
                5, 1, 3, 11, 10, 4,
                4, 1, 4, 5, 10, 5,
                11, 1, 4, 12, 10, 5,
                3, 1, 5, 4, 10, 11,
                12, 1, 5, 13, 10, 11,
                4, 1, 11, 5, 10, 12,
                11, 1, 11, 12, 10, 12,
                5, 1, 12, 11, 10, 13,
                6, 2, 2, 10, 4, 3,
                7, 6, 2, 9, 7, 3,
                1, 6, 7, 3, 7, 9,
                13, 6, 7, 15, 7, 9,
                4, 8, 2, 12, 9, 3,
                3, 8, 3, 5, 9, 4,
                11, 8, 3, 13, 9, 4,
                2, 8, 4, 4, 9, 5,
                12, 8, 4, 14, 9, 5,
                2, 8, 5, 3, 9, 12,
                13, 8, 5, 14, 9, 12,
                3, 8, 11, 4, 9, 13,
                12, 8, 11, 13, 9, 13,
                4, 8, 12, 5, 9, 14,
                11, 8, 12, 12, 9, 14,
                5, 8, 13, 11, 9, 14,
                6, 10, 5, 10, 11, 11,
                5, 10, 6, 6, 11, 10,
                10, 10, 6, 11, 11, 10,
                7, 11, 7, 9, 12, 9);
        add(shapes, "cutting_board",
                1, 0, 1, 15, 2, 15);
        add(shapes, "nuke_cutting_board",
                1, 0, 1, 15, 2, 15,
                7, 2, 12, 9, 3, 15,
                4, 2, 13, 7, 3, 15,
                9, 2, 13, 12, 3, 15);
        shapes.put("mystia_cooking_pot", shapes.get("cooking_pot"));
        shapes.put("super_cooking_pot", shapes.get("cooking_pot"));
        shapes.put("extreme_cooking_pot", shapes.get("cooking_pot"));
        shapes.put("mystia_grill", shapes.get("grill"));
        shapes.put("super_grill", shapes.get("grill"));
        shapes.put("extreme_grill", shapes.get("grill"));
        shapes.put("mystia_frying_pan", shapes.get("frying_pan"));
        shapes.put("super_frying_pan", shapes.get("frying_pan"));
        shapes.put("extreme_frying_pan", shapes.get("frying_pan"));
        shapes.put("mystia_steamer", shapes.get("steamer"));
        shapes.put("super_steamer", shapes.get("steamer"));
        shapes.put("extreme_steamer", shapes.get("steamer"));
        shapes.put("mystia_cutting_board", shapes.get("cutting_board"));
        shapes.put("super_cutting_board", shapes.get("cutting_board"));
        shapes.put("extreme_cutting_board", shapes.get("cutting_board"));
        return Map.copyOf(shapes);
    }

    private static void add(Map<String, Map<Direction, VoxelShape>> shapes, String model, int... boxes) {
        Map<Direction, VoxelShape> rotations = new EnumMap<>(Direction.class);
        for (Direction facing : new Direction[]{Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST}) {
            VoxelShape shape = Shapes.empty();
            int turns = switch (facing) {
                case EAST -> 1;
                case SOUTH -> 2;
                case WEST -> 3;
                default -> 0;
            };
            for (int i = 0; i < boxes.length; i += 6) {
                double x1 = boxes[i], y1 = boxes[i + 1], z1 = boxes[i + 2], x2 = boxes[i + 3], y2 = boxes[i + 4], z2 = boxes[i + 5];
                for (int turn = 0; turn < turns; turn++) {
                    double oldX1 = x1, oldX2 = x2;
                    x1 = 16 - z2;
                    x2 = 16 - z1;
                    z1 = oldX1;
                    z2 = oldX2;
                }
                shape = Shapes.or(shape, Block.box(x1, y1, z1, x2, y2, z2));
            }
            rotations.put(facing, shape.optimize());
        }
        shapes.put(model, Map.copyOf(rotations));
    }
}
