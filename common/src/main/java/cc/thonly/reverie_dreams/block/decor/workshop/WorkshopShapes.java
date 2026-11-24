package cc.thonly.reverie_dreams.block.decor.workshop;

import com.google.gson.JsonParser;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.io.IOException;

/**
 * Cached voxel geometry generated from the same integer cuboids as the Java models.
 */
public final class WorkshopShapes {
    private static final Map<String, Map<Direction, VoxelShape>> SHAPES = load();

    private static Map<String, Map<Direction, VoxelShape>> load() {
        Map<String, Map<Direction, VoxelShape>> output = new HashMap<>();
        try (var reader = new InputStreamReader(Objects.requireNonNull(WorkshopShapes.class.getResourceAsStream(
                "/data/reverie_dreams/japanese_set2_shapes.json")), StandardCharsets.UTF_8)) {
            for (var entry : JsonParser.parseReader(reader).getAsJsonObject().entrySet()) {
                VoxelShape shape = Shapes.empty();
                for (var part : entry.getValue().getAsJsonArray()) {
                    var p = part.getAsJsonArray();
                    shape = Shapes.or(shape, Block.box(p.get(0).getAsDouble(), p.get(1).getAsDouble(), p.get(2).getAsDouble(),
                            p.get(3).getAsDouble(), p.get(4).getAsDouble(), p.get(5).getAsDouble()));
                }
                output.put(entry.getKey(), Shapes.rotateHorizontal(shape.optimize()));
            }
        } catch (IOException error) {
            throw new IllegalStateException("Workshop geometry", error);
        }
        return Map.copyOf(output);
    }

    public static VoxelShape shape(String key, Direction facing) {
        var shapes = SHAPES.get(key);
        if (shapes == null)
            throw new IllegalArgumentException("Unknown workshop geometry " + key);
        return shapes.get(facing);
    }

    private WorkshopShapes() {
    }
}
