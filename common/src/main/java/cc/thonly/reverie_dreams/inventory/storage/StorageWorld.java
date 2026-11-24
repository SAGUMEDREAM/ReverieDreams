package cc.thonly.reverie_dreams.inventory.storage;

import cc.thonly.reverie_dreams.block.entity.CustomChestBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.phys.AABB;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import cc.thonly.reverie_dreams.entity.ai.work.WorkArea;

/** Narrow storage allowlist. Never generate chunks or unpack untouched loot while inspecting storage. */
public final class StorageWorld {
    public static final int RADIUS = 8;
    private StorageWorld() {}

    public static BlockPos attachedTo(ItemFrame frame) {
        return frame.getPos().relative(frame.getDirection().getOpposite());
    }

    public static boolean supported(BlockEntity entity) {
        return entity instanceof ChestBlockEntity || entity instanceof BarrelBlockEntity || entity instanceof CustomChestBlockEntity;
    }

    private static boolean usable(BlockEntity entity) {
        return entity != null && supported(entity) && !entity.isRemoved()
                && !(entity instanceof BaseContainerBlockEntity base && base.isLocked())
                && !(entity instanceof RandomizableContainerBlockEntity random && random.getLootTable() != null);
    }

    public static StoragePlanner.Store resolve(ServerLevel level, BlockPos pos) {
        if (!level.hasChunkAt(pos)) return null;
        return resolve(level, pos, level.getEntitiesOfClass(ItemFrame.class, new AABB(pos).inflate(2), f -> !f.isRemoved()));
    }

    private static StoragePlanner.Store resolve(ServerLevel level, BlockPos pos, List<ItemFrame> frames) {
        if (!level.hasChunkAt(pos)) return null;
        var entity = level.getBlockEntity(pos);
        if (!usable(entity)) return null;
        List<BlockPos> parts = new ArrayList<>();
        parts.add(pos.immutable());
        Container container = (Container) entity;
        var state = level.getBlockState(pos);
        if (entity instanceof ChestBlockEntity && state.getBlock() instanceof ChestBlock block) {
            if (state.getValue(ChestBlock.TYPE) != ChestType.SINGLE) {
                var partner = ChestBlock.getConnectedBlockPos(pos, state);
                if (!level.hasChunkAt(partner) || !usable(level.getBlockEntity(partner))) return null;
                parts.add(partner.immutable());
            }
            container = ChestBlock.getContainer(block, state, level, pos, false);
            if (container == null || container.getContainerSize() != parts.size() * 27) return null;
        }
        parts.sort(Comparator.comparingLong(BlockPos::asLong));
        // Resolve from the canonical half, so combined slot order and sourceSlot never flip.
        if (!parts.getFirst().equals(pos)) return resolve(level, parts.getFirst(), frames);
        List<Object> identities = new ArrayList<>();
        for (var part : parts) identities.add(level.getBlockEntity(part));
        var samples = frames.stream().filter(f -> parts.contains(attachedTo(f)))
                .map(f -> new StorageRule.Sample(f.getItem(), f.getRotation())).toList();
        return new StoragePlanner.Store(parts.getFirst(), parts, container, StorageRule.of(samples), identities);
    }

    public static List<StoragePlanner.Store> scan(ServerLevel level, BlockPos center) {
        return scan(level, new WorkArea(center, RADIUS, RADIUS));
    }

    public static List<StoragePlanner.Store> scan(ServerLevel level, WorkArea area) {
        var center = area.center();
        var min = center.offset(-area.radius(), -area.height(), -area.radius());
        var max = center.offset(area.radius(), area.height(), area.radius());
        var frames = level.getEntitiesOfClass(ItemFrame.class, new AABB(min.getX(), min.getY(), min.getZ(),
                max.getX() + 1.0, max.getY() + 1.0, max.getZ() + 1.0).inflate(2), f -> !f.isRemoved());
        var stores = new ArrayList<StoragePlanner.Store>();
        var seen = new HashSet<BlockPos>();
        for (int x = SectionPos.blockToSectionCoord(min.getX()); x <= SectionPos.blockToSectionCoord(max.getX()); x++) {
            for (int z = SectionPos.blockToSectionCoord(min.getZ()); z <= SectionPos.blockToSectionCoord(max.getZ()); z++) {
                var chunk = level.getChunkSource().getChunkNow(x, z);
                if (chunk == null) continue;
                for (var pos : List.copyOf(chunk.getBlockEntities().keySet())) {
                    if (!area.contains(pos) || seen.contains(pos)) continue;
                    var store = resolve(level, pos, frames);
                    if (store == null || store.parts().stream().anyMatch(p -> !area.contains(p))) continue;
                    seen.addAll(store.parts());
                    stores.add(store);
                }
            }
        }
        stores.sort(Comparator.comparingLong(s -> s.pos().asLong()));
        return stores;
    }

    public static boolean inside(BlockPos pos, BlockPos center) {
        return Math.abs((long) pos.getX() - center.getX()) <= RADIUS
                && Math.abs((long) pos.getY() - center.getY()) <= RADIUS
                && Math.abs((long) pos.getZ() - center.getZ()) <= RADIUS;
    }
}
