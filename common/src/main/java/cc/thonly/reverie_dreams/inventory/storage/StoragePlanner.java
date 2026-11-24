package cc.thonly.reverie_dreams.inventory.storage;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import java.util.List;
import java.util.Comparator;
import java.util.function.Predicate;

public final class StoragePlanner {
    public static final int CARGO_SLOTS = 18;
    private StoragePlanner() {}

    public record Store(BlockPos pos, List<BlockPos> parts, Container container, StorageRule rule, List<Object> identities) {
        public Store { pos = pos.immutable(); parts = parts.stream().map(BlockPos::immutable).toList(); identities = List.copyOf(identities); }
        public boolean sameIncarnation(Store other) {
            if (other == null || !parts.equals(other.parts) || identities.size() != other.identities.size()) return false;
            for (int i = 0; i < identities.size(); i++) if (identities.get(i) != other.identities.get(i)) return false;
            return true;
        }
    }
    public record Job(Store source, Store destination, int sourceSlot, ItemStack sample, int amount, boolean routed) {
        public Job { sample = sample.copyWithCount(1); }
        public boolean pickup() { return source != null; }
        public Store visiting() { return pickup() ? source : destination; }
    }
    public record Result(Job job, String code) {}

    public static int exportable(Container inventory, int slot, StorageSettings settings) {
        if (slot < 0 || slot >= Math.min(CARGO_SLOTS, inventory.getContainerSize())) return 0;
        ItemStack stack = inventory.getItem(slot);
        if (stack.isEmpty()) return 0;
        int reserve = stack.has(DataComponents.FOOD) ? settings.reserveFood()
                : stack.has(DataComponents.MAX_DAMAGE) ? settings.reserveTools() : 0;
        for (int i = 0; i < slot && reserve > 0; i++) {
            if (inventory.getItem(i).is(stack.getItem())) reserve -= inventory.getItem(i).getCount();
        }
        return Math.max(0, stack.getCount() - Math.max(0, reserve));
    }

    public static Store destination(List<Store> stores, ItemStack stack, StorageSettings settings, Vec3 position,
                                    Predicate<Store> available) {
        Store best = null;
        int bestPriority = Integer.MAX_VALUE;
        double bestDistance = Double.MAX_VALUE;
        for (var store : stores) {
            if (!available.test(store) || !store.rule.accepts(stack, store.container, settings)
                    || StorageTransfer.capacity(store.container, stack, store.container.getContainerSize()) == 0) continue;
            int priority = store.rule.priority(store.container, stack);
            double distance = store.pos.distToCenterSqr(position);
            if (priority < bestPriority || priority == bestPriority && distance < bestDistance) {
                best = store;
                bestPriority = priority;
                bestDistance = distance;
            }
        }
        return best;
    }

    public static Result plan(Container cargo, List<Store> stores, StorageSettings settings, Vec3 position,
                              Predicate<Store> available) {
        if (stores.isEmpty()) return new Result(null, "no_chest");
        boolean export = false, matching = false, busy = false;
        for (int i = 0; i < Math.min(CARGO_SLOTS, cargo.getContainerSize()); i++) {
            int amount = exportable(cargo, i, settings);
            if (amount <= 0) continue;
            export = true;
            var stack = cargo.getItem(i);
            var destination = destination(stores, stack, settings, position, available);
            if (destination != null) return new Result(new Job(null, destination, i, stack, amount, false), "ready");
            for (var store : stores) {
                if (store.rule.accepts(stack, store.container, settings)) {
                    matching = true;
                    busy |= !available.test(store) && StorageTransfer.capacity(store.container, stack, store.container.getContainerSize()) > 0;
                }
            }
        }
        // Only explicitly marked input containers can be sources. Outputs are never drained.
        boolean input = false, needsCargoSpace = false;
        for (var source : stores.stream().sorted(Comparator.comparingDouble(s -> s.pos.distToCenterSqr(position))).toList()) {
            if (source.rule.mode() != StorageRule.Mode.INPUT) continue;
            if (!available.test(source)) { busy |= !source.container.isEmpty(); continue; }
            for (int slot = 0; slot < source.container.getContainerSize(); slot++) {
                var stack = source.container.getItem(slot);
                if (stack.isEmpty()) continue;
                input = true;
                var destination = destination(stores, stack, settings, position, available);
                int capacity = StorageTransfer.capacity(cargo, stack, CARGO_SLOTS);
                for (var store : stores) {
                    if (store.rule.accepts(stack, store.container, settings)) {
                        matching = true;
                        busy |= !available.test(store) && StorageTransfer.capacity(store.container, stack, store.container.getContainerSize()) > 0;
                    }
                }
                needsCargoSpace |= destination != null && capacity <= 0;
                if (destination == null || capacity <= 0) continue;
                int amount = Math.min(stack.getCount(), Math.min(capacity,
                        StorageTransfer.capacity(destination.container, stack, destination.container.getContainerSize())));
                return new Result(new Job(source, destination, slot, stack, amount, true), "ready");
            }
        }
        if (busy) return new Result(null, "storage_busy");
        if (!export && !input) return new Result(null, "sorting_finished");
        if (stores.stream().anyMatch(s -> s.rule.mode() == StorageRule.Mode.CONFLICT)) return new Result(null, "storage_rule_conflict");
        if (export) return new Result(null, matching ? "storage_full" : "no_matching_storage");
        return new Result(null, needsCargoSpace ? "inventory_full" : matching ? "storage_full" : "no_matching_storage");
    }
}
