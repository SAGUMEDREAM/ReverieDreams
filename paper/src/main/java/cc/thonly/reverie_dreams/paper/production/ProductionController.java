package cc.thonly.reverie_dreams.paper.production;

import cc.thonly.reverie_dreams.paper.util.Ids;

import net.kyori.adventure.text.Component;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.entity.BlockEntity;
import net.momirealms.craftengine.core.block.entity.BlockEntityController;
import net.momirealms.craftengine.core.block.entity.tick.BlockEntityTicker;
import net.momirealms.craftengine.core.world.CEWorld;
import net.momirealms.craftengine.libraries.nbt.CompoundTag;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.Objects;
import net.momirealms.craftengine.bukkit.util.BlockStateUtils;
import net.momirealms.craftengine.bukkit.util.LocationUtils;
import org.slf4j.LoggerFactory;
import net.momirealms.craftengine.proxy.minecraft.world.level.LevelProxy;

/**
 * One instance per CE block entity. The CE chunk owns persistence and loaded-only ticking.
 * API methods transfer ownership of cloned stacks; callers must commit their remainder to
 * their source inventory in the same main-thread callback. They never expose a live stack.
 * Pinned to the shaded controller ABI of the distributed CE 26.8.2 plugin.
 */
public final class ProductionController extends BlockEntityController implements InventoryHolder {
    public enum Kind { ICE, BARREL }
    public record Snapshot(Kind kind, String recipeId, int ticksRemaining, int totalTicks,
                           int servings, int iceOutputs, boolean quarantined, List<ItemStack> inputs) {}

    public static final String DATA_KEY = Ids.MOD_ID + ":production";
    private static final int SCHEMA = 1;
    private final Kind kind;
    private final Inventory inventory;
    private final List<ItemStack> iceOutputs = new ArrayList<>();
    // Escrow supports a conservative refund if another plugin forcibly removes a working block.
    private ItemStack[] consumedInputs = new ItemStack[9];
    private ItemStack pendingOutput;
    private String recipeId = "";
    private int ticksRemaining;
    private int totalTicks;
    private int servings;
    private boolean loaded;
    private boolean removed;
    private int lastComparatorSignal = -1;
    private CompoundTag unreadableData;

    ProductionController(BlockEntity entity, Kind kind) {
        super(entity);
        this.kind = kind;
        inventory = Bukkit.createInventory(this, 9, Component.translatable("block.reverie_dreams.brewing_barrel"));
    }

    public Kind kind() { return kind; }

    public int comparatorSignal() {
        return kind == Kind.ICE && ticksRemaining == 0 ? Math.min(15, (iceOutputs.size() * 15 + 7) / 8) : 0;
    }

    /** The GUI is read-only to vanilla transfer mechanics; ProductionModule performs mutations. */
    @Override
    public Inventory getInventory() { return inventory; }

    public Location location() {
        return new Location((World) blockEntity.world.world().platformWorld(),
                blockEntity.pos.x() + 0.5, blockEntity.pos.y() + 0.5, blockEntity.pos.z() + 0.5);
    }

    public boolean available() { return loaded && !removed && unreadableData == null && ProductionModule.enabled(); }

    public Snapshot snapshot() {
        requireThread();
        return new Snapshot(kind, recipeId, ticksRemaining, totalTicks, servings, iceOutputs.size(),
                unreadableData != null, Arrays.asList(cloneItems(inventory.getContents())));
    }

    /** Refuse ordinary destruction while a process/output exists. Unload is always allowed. */
    public boolean requiresProtection() {
        return unreadableData != null || ticksRemaining > 0 || servings > 0 || !iceOutputs.isEmpty();
    }

    public boolean canEditInputs() {
        return available() && kind == Kind.BARREL && ticksRemaining == 0 && servings == 0;
    }

    /** Accept at most one item in a barrel slot; return the cloned unaccepted remainder. */
    public ItemStack offerInput(int slot, ItemStack offered) {
        requireThread();
        checkSlot(slot);
        if (empty(offered)) return null;
        ItemStack remainder = offered.clone();
        if (!canEditInputs() || !empty(inventory.getItem(slot))) return remainder;
        ItemStack accepted = offered.clone();
        accepted.setAmount(1);
        inventory.setItem(slot, accepted);
        remainder.subtract(1);
        dirty();
        return remainder.isEmpty() ? null : remainder;
    }

    public ItemStack takeInput(int slot) {
        requireThread();
        checkSlot(slot);
        if (!canEditInputs()) return null;
        ItemStack stored = inventory.getItem(slot);
        if (empty(stored)) return null;
        ItemStack result = stored.clone();
        inventory.setItem(slot, null);
        dirty();
        return result;
    }

    /** Build the complete effect-bearing result first; missing pack content never consumes input. */
    public boolean startBrewing() {
        requireThread();
        if (!canEditInputs()) return false;
        ItemStack[] inputs = cloneItems(inventory.getContents());
        ProductionRecipes.BarrelRecipe recipe = ProductionRecipes.match(inputs);
        if (recipe == null) return false;
        ItemStack output = ProductionRecipes.prepareOutput(recipe, inputs);
        if (output == null) return false;
        pendingOutput = output;
        consumedInputs = inputs;
        recipeId = recipe.id();
        ticksRemaining = totalTicks = recipe.ticks();
        inventory.clear(); // Original consumes one from every occupied one-item slot, including extras.
        closeViewers();
        dirty();
        return true;
    }

    /** Caller performs exactly one water-bucket/empty-bucket exchange after this succeeds. */
    public boolean startIce() {
        requireThread();
        if (!available() || kind != Kind.ICE || requiresProtection()) return false;
        List<ItemStack> generated = new ArrayList<>(8);
        List<String> ids = ProductionRecipes.iceOutputs();
        if (ids.isEmpty()) return false;
        // Validate every tag member before accepting water. Missing magic_ice must not silently alter odds.
        List<ItemStack> templates = ids.stream().map(id -> ProductionRecipes.build(
                new ProductionRecipes.Ingredient(id, 1))).toList();
        if (templates.stream().anyMatch(Objects::isNull)) return false;
        for (int i = 0; i < 8; i++) generated.add(templates.get(ThreadLocalRandom.current().nextInt(templates.size())).clone());
        // Roll once when starting and persist the reserved outputs, preventing rerolls on reload.
        iceOutputs.addAll(generated);
        ticksRemaining = totalTicks = 160;
        recipeId = Ids.MOD_ID + ":ice_making_machine";
        dirty();
        return true;
    }

    /**
     * Transfer one ready ice output or one barrel serving. Gameplay callers exchange a bottle
     * atomically for barrel results; this low-level method also supports integration probes.
     */
    public ItemStack extractOutput() {
        requireThread();
        if (!available() || ticksRemaining > 0) return null;
        ItemStack result;
        if (kind == Kind.ICE) {
            if (iceOutputs.isEmpty()) return null;
            result = iceOutputs.removeFirst();
        } else {
            if (servings <= 0 || pendingOutput == null) return null;
            result = pendingOutput.clone();
            if (--servings == 0) {
                pendingOutput = null;
                consumedInputs = new ItemStack[9];
            }
        }
        if (!requiresProtection()) {
            recipeId = "";
            totalTicks = 0;
        }
        dirty();
        return result.clone();
    }

    private void tick() {
        if (!Bukkit.getServer().getServerTickManager().isRunningNormally()) return;
        if (!available() || ticksRemaining <= 0) return;
        --ticksRemaining;
        if (ticksRemaining == 0) {
            if (kind == Kind.BARREL) {
                servings = 9;
                consumedInputs = new ItemStack[9];
            }
            location().getWorld().playSound(location(), kind == Kind.ICE ? Sound.BLOCK_GLASS_BREAK
                    : Sound.ENTITY_PLAYER_LEVELUP, SoundCategory.BLOCKS, 0.5f, 1f);
        }
        dirty();
    }

    @Override
    public <C extends BlockEntityController> BlockEntityTicker<C> createBlockEntityTicker(CEWorld world, ImmutableBlockState state) {
        // A sleeping ticker cannot be reliably awakened by this CE extension API. Idle ticks are O(1).
        return createTickerHelper((BlockEntityTicker<ProductionController>) (w, p, s, controller) -> controller.tick());
    }

    @Override
    public void onLoad() {
        loaded = true;
        removed = false;
        ProductionModule.loaded(this);
    }

    @Override
    public void onUnload() {
        loaded = false;
        closeViewers();
        ProductionModule.unloaded(this);
    }

    @Override
    public void onRemove() {
        if (removed) return;
        closeViewers();
        removed = true;
        loaded = false;
        ProductionModule.unloaded(this);
        List<ItemStack> recovery = new ArrayList<>();
        for (ItemStack item : inventory.getContents()) if (!empty(item)) recovery.add(item.clone());
        inventory.clear();
        if (kind == Kind.ICE) {
            // Water already returned an empty bucket: returning a water bucket here would duplicate it.
            recovery.addAll(iceOutputs.stream().map(ItemStack::clone).toList());
            iceOutputs.clear();
        } else if (ticksRemaining > 0) {
            for (ItemStack item : consumedInputs) if (!empty(item)) recovery.add(item.clone());
        } else if (pendingOutput != null) {
            // Only forced API replacement reaches this; ordinary breaks/explosions are protected.
            for (int i = 0; i < servings; i++) recovery.add(pendingOutput.clone());
        }
        consumedInputs = new ItemStack[9];
        pendingOutput = null;
        ticksRemaining = totalTicks = servings = 0;
        for (ItemStack item : recovery) location().getWorld().dropItemNaturally(location(), item);
    }

    @Override
    public void saveCustomData(CompoundTag outer) {
        if (unreadableData != null) {
            outer.put(DATA_KEY, unreadableData.copy());
            return;
        }
        CompoundTag data = new CompoundTag();
        data.putInt("schema", SCHEMA);
        data.putString("machine", kind.name());
        data.putString("recipe", recipeId);
        data.putInt("remaining", ticksRemaining);
        data.putInt("total", totalTicks);
        data.putInt("servings", servings);
        saveItems(data, "input", inventory.getContents());
        saveItems(data, "escrow", consumedInputs);
        saveItems(data, "ice", iceOutputs.toArray(ItemStack[]::new));
        if (pendingOutput != null) data.putByteArray("output", pendingOutput.serializeAsBytes());
        outer.put(DATA_KEY, data);
    }

    @Override
    public void loadCustomData(CompoundTag outer) {
        CompoundTag data = outer.getCompound(DATA_KEY);
        if (data == null || data.isEmpty()) return;
        try {
            if (data.getInt("schema", -1) != SCHEMA || !kind.name().equals(data.getString("machine"))) {
                throw new IllegalArgumentException("Unknown production schema or wrong machine kind");
            }
            ItemStack[] inputs = loadItems(data, "input", 9);
            ItemStack[] escrow = loadItems(data, "escrow", 9);
            ItemStack[] ice = loadItems(data, "ice", 8);
            int remaining = data.getInt("remaining", 0);
            int total = data.getInt("total", 0);
            int count = data.getInt("servings", 0);
            byte[] bytes = data.getByteArray("output");
            ItemStack output = bytes == null || bytes.length == 0 ? null : ItemStack.deserializeBytes(bytes);
            if (remaining < 0 || total < remaining || count < 0 || count > 9
                    || (kind == Kind.BARREL && (remaining > 0 || count > 0) && empty(output))
                    || (kind == Kind.ICE && remaining > 0 && Arrays.stream(ice).filter(i -> !empty(i)).count() != 8)
                    || Arrays.stream(inputs).anyMatch(i -> !empty(i) && i.getAmount() != 1)) {
                throw new IllegalArgumentException("Invalid production inventory/progress");
            }
            // Commit only after all item payloads and invariants have been checked.
            inventory.setContents(inputs);
            consumedInputs = escrow;
            iceOutputs.clear();
            for (ItemStack item : ice) if (!empty(item)) iceOutputs.add(item);
            pendingOutput = output;
            ticksRemaining = remaining;
            totalTicks = total;
            servings = count;
            recipeId = data.getString("recipe", "");
            unreadableData = null;
        } catch (RuntimeException exception) {
            // Keep the entire opaque tag on future saves; never turn a failed decode into empty storage.
            unreadableData = data.copy();
            LoggerFactory.getLogger(ProductionController.class).error(
                    "Production data at {} was quarantined and preserved", blockEntity.pos, exception);
        }
    }

    private static void saveItems(CompoundTag data, String prefix, ItemStack[] items) {
        data.putInt(prefix + "_size", items.length);
        for (int i = 0; i < items.length; i++) {
            if (!empty(items[i])) data.putByteArray(prefix + i, items[i].serializeAsBytes());
        }
    }

    private static ItemStack[] loadItems(CompoundTag data, String prefix, int maximum) {
        int size = data.getInt(prefix + "_size", 0);
        if (size < 0 || size > maximum) throw new IllegalArgumentException("Oversized " + prefix);
        ItemStack[] items = new ItemStack[maximum];
        for (int i = 0; i < size; i++) {
            byte[] bytes = data.getByteArray(prefix + i);
            if (bytes != null && bytes.length > 0) items[i] = ItemStack.deserializeBytes(bytes);
        }
        return items;
    }

    void closeViewers() {
        for (HumanEntity viewer : List.copyOf(inventory.getViewers())) viewer.closeInventory();
    }

    void dirty() {
        if (blockEntity.world != null) {
            blockEntity.world.blockEntityChanged(blockEntity.pos);
            int signal = comparatorSignal();
            if (kind == Kind.ICE && !removed && signal != lastComparatorSignal) {
                lastComparatorSignal = signal;
                LevelProxy.INSTANCE.updateNeighbourForOutputSignal(
                        blockEntity.world.world().minecraftWorld(),
                        LocationUtils.toBlockPos(blockEntity.pos),
                        BlockStateUtils.getBlockOwner(
                                blockEntity.blockState().customBlockState().minecraftState()));
            }
        }
    }

    static boolean empty(ItemStack stack) { return stack == null || stack.isEmpty(); }
    private static ItemStack[] cloneItems(ItemStack[] items) {
        return Arrays.stream(items).map(i -> empty(i) ? null : i.clone()).toArray(ItemStack[]::new);
    }
    private static void checkSlot(int slot) {
        if (slot < 0 || slot >= 9) throw new IndexOutOfBoundsException("Barrel slot " + slot);
    }
    static void requireThread() {
        if (!Bukkit.isPrimaryThread()) throw new IllegalStateException("Production state requires the Paper main thread");
    }
}
