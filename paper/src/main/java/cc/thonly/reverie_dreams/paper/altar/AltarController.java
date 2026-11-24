package cc.thonly.reverie_dreams.paper.altar;

import cc.thonly.reverie_dreams.paper.util.Ids;

import net.momirealms.craftengine.bukkit.api.CraftEngineBlocks;
import net.momirealms.craftengine.core.block.entity.BlockEntity;
import net.momirealms.craftengine.core.block.entity.BlockEntityController;
import net.momirealms.craftengine.libraries.nbt.CompoundTag;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.slf4j.LoggerFactory;

/** Nine independent persistent slots; crafting is an immediate transaction, with no artificial timer. */
public final class AltarController extends BlockEntityController {
    public enum CraftStatus { SUCCESS, UNAVAILABLE, MISSING_STRUCTURE, UNLOADED_STRUCTURE, MISSING_RECIPE, UNSUPPORTED_OUTPUT }
    public record StructureCheck(boolean valid, boolean unloaded, List<String> missing) {}
    /** Result remains in slot8; only evacuatedCore transfers ownership to the caller. */
    public record CraftResult(CraftStatus status, String recipeId, ItemStack evacuatedCore, String reason) {}
    public record Snapshot(List<ItemStack> items, boolean quarantined) {}
    public static final String DATA_KEY = Ids.MOD_ID + ":altar";
    public static final int[][] OFFSETS = {{0, -4}, {-3, -3}, {3, -3}, {-4, 0}, {4, 0}, {-3, 3}, {3, 3}, {0, 4}};
    private ItemStack[] items = new ItemStack[9];
    private boolean loaded, removed;
    private CompoundTag unreadable;

    AltarController(BlockEntity entity) { super(entity); }
    public boolean available() { return loaded && !removed && unreadable == null && AltarModule.enabled(); }
    public Location location() { return new Location((World) blockEntity.world.world().platformWorld(), blockEntity.pos.x() + .5, blockEntity.pos.y() + .5, blockEntity.pos.z() + .5); }
    public Snapshot snapshot() { thread(); return new Snapshot(Arrays.asList(copies(items)), unreadable != null); }
    public ItemStack item(int slot) { thread(); slot(slot); return empty(items[slot]) ? null : items[slot].clone(); }

    public ItemStack offer(int slot, ItemStack input) {
        thread(); slot(slot);
        if (empty(input)) return null;
        ItemStack remainder = input.clone();
        if (!available() || !empty(items[slot]) && !items[slot].isSimilar(input)) return remainder;
        int previous = empty(items[slot]) ? 0 : items[slot].getAmount();
        int accepted = Math.min(input.getAmount(), input.getMaxStackSize() - previous);
        if (accepted <= 0) return remainder;
        items[slot] = input.clone(); items[slot].setAmount(previous + accepted); remainder.subtract(accepted);
        changed();
        return remainder.isEmpty() ? null : remainder;
    }

    public ItemStack take(int slot, int amount) {
        thread(); slot(slot);
        if (!available() || amount <= 0 || empty(items[slot])) return null;
        ItemStack result = items[slot].clone(); result.setAmount(Math.min(amount, result.getAmount()));
        items[slot].subtract(result.getAmount()); if (items[slot].isEmpty()) items[slot] = null;
        changed(); return result;
    }

    public StructureCheck structure() {
        thread();
        Location center = location(); World world = center.getWorld();
        List<String> missing = new ArrayList<>(); boolean unloaded = false;
        for (int[] offset : OFFSETS) for (int height = 0; height <= 2; height++) {
            int x = center.getBlockX() + offset[0], y = center.getBlockY() + height, z = center.getBlockZ() + offset[1];
            if (!world.isChunkLoaded(x >> 4, z >> 4)) { unloaded = true; continue; }
            String expected = height == 2 ? Ids.MOD_ID + ":blessed_spiritual_log" : Ids.MOD_ID + ":stripped_spiritual_log";
            var state = CraftEngineBlocks.getCustomBlockState(world.getBlockAt(x, y, z));
            if (state == null || !state.owner().value().id().asString().equals(expected)) missing.add(x + "," + y + "," + z);
        }
        return new StructureCheck(!unloaded && missing.isEmpty(), unloaded, List.copyOf(missing));
    }

    public CraftResult craft() {
        thread();
        if (!available()) return new CraftResult(CraftStatus.UNAVAILABLE, "", null, "unavailable");
        StructureCheck check = structure();
        if (!check.valid) return new CraftResult(check.unloaded ? CraftStatus.UNLOADED_STRUCTURE : CraftStatus.MISSING_STRUCTURE, "", null, "structure");
        AltarRecipes.Plan plan = AltarRecipes.match(copies(items));
        if (plan == null) return new CraftResult(CraftStatus.MISSING_RECIPE, "", null, "recipe");
        if (plan.unavailable() != null) return new CraftResult(CraftStatus.UNSUPPORTED_OUTPUT, plan.id(), null, plan.unavailable());
        ItemStack[] next = copies(items);
        for (int slot = 0; slot < 8; slot++) if (plan.counts()[slot] > 0) {
            next[slot].subtract(plan.counts()[slot]); if (next[slot].isEmpty()) next[slot] = null;
        }
        ItemStack coreRemainder = next[8];
        if (!empty(coreRemainder)) { coreRemainder.subtract(plan.counts()[8]); if (coreRemainder.isEmpty()) coreRemainder = null; }
        next[8] = plan.output().clone();
        // Output was fully built first, including the NPC factory's durable group metadata.
        items = next; changed();
        return new CraftResult(CraftStatus.SUCCESS, plan.id(), coreRemainder, null);
    }

    @Override public void onLoad() { loaded = true; removed = false; AltarModule.loaded(this); }
    @Override public void onUnload() { loaded = false; AltarModule.closeViews(this); AltarModule.unloaded(this); }
    @Override public void onRemove() {
        if (removed) return;
        removed = true; loaded = false; AltarModule.closeViews(this); AltarModule.unloaded(this);
        ItemStack[] drops = items; items = new ItemStack[9];
        for (ItemStack item : drops) if (!empty(item)) location().getWorld().dropItemNaturally(location(), item);
    }
    @Override public void saveCustomData(CompoundTag outer) {
        if (unreadable != null) { outer.put(DATA_KEY, unreadable.copy()); return; }
        CompoundTag data = new CompoundTag(); data.putInt("schema", 1);
        for (int i = 0; i < 9; i++) if (!empty(items[i])) data.putByteArray("slot" + i, items[i].serializeAsBytes());
        outer.put(DATA_KEY, data);
    }
    @Override public void loadCustomData(CompoundTag outer) {
        CompoundTag data = outer.getCompound(DATA_KEY); if (data == null || data.isEmpty()) return;
        try {
            if (data.getInt("schema", -1) != 1) throw new IllegalArgumentException("Unknown altar schema");
            ItemStack[] decoded = new ItemStack[9];
            for (int i = 0; i < 9; i++) { byte[] bytes = data.getByteArray("slot" + i); if (bytes != null && bytes.length > 0) decoded[i] = ItemStack.deserializeBytes(bytes); }
            if (Arrays.stream(decoded).anyMatch(item -> !empty(item) && item.getAmount() > item.getMaxStackSize())) throw new IllegalArgumentException("Oversized altar stack");
            items = decoded; unreadable = null;
        } catch (RuntimeException failure) {
            unreadable = data.copy(); LoggerFactory.getLogger(AltarController.class).error("Altar data at {} quarantined and preserved", blockEntity.pos, failure);
        }
    }
    private void changed() { dirty(); AltarModule.refreshViews(this); }
    void dirty() { if (blockEntity.world != null) blockEntity.world.blockEntityChanged(blockEntity.pos); }
    static boolean empty(ItemStack item) { return item == null || item.isEmpty(); }
    static void thread() { if (!Bukkit.isPrimaryThread()) throw new IllegalStateException("Altar state requires Paper main thread"); }
    private static void slot(int slot) { if (slot < 0 || slot >= 9) throw new IndexOutOfBoundsException("Altar slot " + slot); }
    private static ItemStack[] copies(ItemStack[] values) { return Arrays.stream(values).map(item -> empty(item) ? null : item.clone()).toArray(ItemStack[]::new); }
}
