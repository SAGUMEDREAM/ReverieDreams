package cc.thonly.reverie_dreams.inventory.storage;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import java.util.function.Function;
import cc.thonly.reverie_dreams.entity.ai.work.WorkArea;

/** Re-resolve the live world at commit, rather than writing a container retained during navigation. */
public final class StorageExecution {
    private StorageExecution() {}
    public record Outcome(int moved, StoragePlanner.Job next, String code) {}

    public static StoragePlanner.Store live(StoragePlanner.Store planned, BlockPos center,
                                            Function<BlockPos, StoragePlanner.Store> resolver) {
        return live(planned, new WorkArea(center, 8, 8), resolver);
    }

    public static StoragePlanner.Store live(StoragePlanner.Store planned, WorkArea area,
                                            Function<BlockPos, StoragePlanner.Store> resolver) {
        var live = resolver.apply(planned.pos());
        return planned.sameIncarnation(live) && live.parts().stream().allMatch(area::contains) ? live : null;
    }

    public static Outcome execute(StoragePlanner.Job job, Container cargo, StorageSettings settings, BlockPos center,
                                   Function<BlockPos, StoragePlanner.Store> resolver) {
        return execute(job, cargo, settings, new WorkArea(center, 8, 8), resolver);
    }

    public static Outcome execute(StoragePlanner.Job job, Container cargo, StorageSettings settings, WorkArea area,
                                   Function<BlockPos, StoragePlanner.Store> resolver) {
        var destination = live(job.destination(), area, resolver);
        var source = job.pickup() ? live(job.source(), area, resolver) : null;
        if (destination == null || job.pickup() && (source == null || source.rule().mode() != StorageRule.Mode.INPUT)
                || !destination.rule().accepts(job.sample(), destination.container(), settings)) {
            return new Outcome(0, null, "no_matching_storage");
        }
        if (job.pickup()) {
            int slot = StorageTransfer.cargoSlot(cargo, job.sample());
            if (slot < 0) return new Outcome(0, null, "inventory_full");
            int amount = Math.min(job.amount(), StorageTransfer.capacity(destination.container(), job.sample(), destination.container().getContainerSize()));
            int moved = StorageTransfer.move(source.container(), job.sourceSlot(), job.sample(), cargo, i -> i == slot, amount);
            if (moved <= 0) return new Outcome(0, null, "no_matching_storage");
            return new Outcome(moved, new StoragePlanner.Job(null, destination, slot, job.sample(), moved, true), "ready");
        }
        if (job.sourceSlot() < 0 || job.sourceSlot() >= StoragePlanner.CARGO_SLOTS) return new Outcome(0, null, "sorting_finished");
        int amount = job.routed() ? job.amount() : Math.min(job.amount(), StoragePlanner.exportable(cargo, job.sourceSlot(), settings));
        int moved = StorageTransfer.move(cargo, job.sourceSlot(), job.sample(), destination.container(), destination.container().getContainerSize(), amount);
        return new Outcome(moved, null, moved > 0 ? "ready" : "storage_full");
    }
}
