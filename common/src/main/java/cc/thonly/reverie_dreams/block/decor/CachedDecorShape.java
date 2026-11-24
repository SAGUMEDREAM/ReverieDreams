package cc.thonly.reverie_dreams.block.decor;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.ArrayVoxelShape;
import net.minecraft.world.phys.shapes.BitSetDiscreteVoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Immutable decoration geometry with precomputed picking boxes and outline edges.
 */
final class CachedDecorShape extends ArrayVoxelShape {
    private record Edge(double x1, double y1, double z1, double x2, double y2, double z2) {
    }

    private final List<AABB> boxes;
    private final List<Edge> edges;

    static VoxelShape of(VoxelShape source) {
        if (source.isEmpty() || source instanceof CachedDecorShape)
            return source;
        return new CachedDecorShape(source, List.copyOf(source.toAabbs()),
                source.getCoords(Direction.Axis.X).toDoubleArray(),
                source.getCoords(Direction.Axis.Y).toDoubleArray(),
                source.getCoords(Direction.Axis.Z).toDoubleArray());
    }

    private CachedDecorShape(VoxelShape source, List<AABB> boxes, double[] xs, double[] ys, double[] zs) {
        super(copyMesh(boxes, xs, ys, zs), xs, ys, zs);
        this.boxes = boxes;
        List<Edge> edges = new ArrayList<>();
        source.forAllEdges((x1, y1, z1, x2, y2, z2) -> edges.add(new Edge(x1, y1, z1, x2, y2, z2)));
        this.edges = List.copyOf(edges);
    }

    private static BitSetDiscreteVoxelShape copyMesh(List<AABB> boxes, double[] xs, double[] ys, double[] zs) {
        BitSetDiscreteVoxelShape mesh = new BitSetDiscreteVoxelShape(xs.length - 1, ys.length - 1, zs.length - 1);
        for (AABB box : boxes) {
            int x1 = Arrays.binarySearch(xs, box.minX), x2 = Arrays.binarySearch(xs, box.maxX);
            int y1 = Arrays.binarySearch(ys, box.minY), y2 = Arrays.binarySearch(ys, box.maxY);
            int z1 = Arrays.binarySearch(zs, box.minZ), z2 = Arrays.binarySearch(zs, box.maxZ);
            for (int x = x1; x < x2; x++)
                for (int y = y1; y < y2; y++)
                    for (int z = z1; z < z2; z++)
                        mesh.fill(x, y, z);
        }
        return mesh;
    }

    @Override
    public void forAllEdges(Shapes.DoubleLineConsumer consumer) {
        for (int i = 0; i < edges.size(); i++) {
            Edge edge = edges.get(i);
            consumer.consume(edge.x1, edge.y1, edge.z1, edge.x2, edge.y2, edge.z2);
        }
    }

    @Override
    public void forAllBoxes(Shapes.DoubleLineConsumer consumer) {
        for (int i = 0; i < boxes.size(); i++) {
            AABB box = boxes.get(i);
            consumer.consume(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ);
        }
    }

    @Override
    public List<AABB> toAabbs() {
        // Vanilla exports a mutable list; keep callers from modifying the cached boxes.
        return new ArrayList<>(boxes);
    }
}
