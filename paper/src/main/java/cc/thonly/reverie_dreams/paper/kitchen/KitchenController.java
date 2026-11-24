package cc.thonly.reverie_dreams.paper.kitchen;

import cc.thonly.reverie_dreams.paper.util.Ids;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.UseRemainder;
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Inventory;
import net.momirealms.craftengine.bukkit.world.WorldlyContainerHolder;
import net.momirealms.craftengine.bukkit.world.inventory.BukkitWorldlyStorageContainer;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.world.WorldPosition;
import net.momirealms.craftengine.proxy.bukkit.craftbukkit.inventory.CraftInventoryProxy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.util.Direction;
import org.bukkit.Particle;
import org.slf4j.LoggerFactory;

/** Per-position six-slot kitchen state, persisted by the actual CE 26.8.2 controller ABI. */
public final class KitchenController extends BlockEntityController {
    public static final String DATA_KEY = Ids.MOD_ID + ":kitchen";
    public record Snapshot(String blockId, String recipeType, double speed, double failureProbability,
                           String recipeId, double ticksRemaining, double totalTicks, boolean working,
                           boolean quarantined, List<ItemStack> items) {}
    public record Choice(String recipeId, ItemStack preview, double baseTicks) {}
    private final String blockId;
    private final String recipeType;
    private final double speed;
    private final double failureProbability;
    private final Inventory storage;
    private ItemStack pendingOutput;
    private String recipeId = "";
    private double remaining;
    private double total;
    private boolean loaded;
    private boolean removed;
    private CompoundTag unreadableData;

    KitchenController(BlockEntity entity, String blockId, String type, double speed, double failureProbability) {
        super(entity);
        this.blockId = blockId;
        this.recipeType = type;
        this.speed = speed;
        this.failureProbability = failureProbability;
        WorldlyContainerHolder holder = new WorldlyContainerHolder(player -> {}, () -> new WorldPosition(
                blockEntity.world.world(), blockEntity.pos.x() + .5, blockEntity.pos.y() + .5, blockEntity.pos.z() + .5));
        storage = CraftInventoryProxy.INSTANCE.newInstance(CraftEngine.instance().platform().createContainer(new KitchenContainer(holder, this)));
        holder.setInventory(storage);
    }

    public Snapshot snapshot() {
        requireThread();
        return new Snapshot(blockId, recipeType, speed, failureProbability, recipeId, remaining, total,
                pendingOutput != null, unreadableData != null, Arrays.asList(copies(storage.getContents())));
    }

    public Location location() {
        return new Location((World) blockEntity.world.world().platformWorld(),
                blockEntity.pos.x() + .5, blockEntity.pos.y() + .5, blockEntity.pos.z() + .5);
    }

    public boolean available() { return loaded && !removed && unreadableData == null && KitchenModule.enabled(); }
    public boolean working() { return pendingOutput != null; }
    public boolean protectedFromBreak() { return working() || unreadableData != null; }

    public List<Choice> choices() {
        requireThread();
        if (!available() || working()) return List.of();
        List<Choice> result = new ArrayList<>();
        ItemStack[] items = storage.getContents();
        for (var recipe : KitchenRecipes.matches(recipeType, items)) {
            ItemStack output = KitchenRecipes.preview(recipe, items);
            if (output != null) result.add(new Choice(recipe.id(), output, recipe.seconds() * 20 + 25));
        }
        return List.copyOf(result);
    }

    public ItemStack item(int slot) {
        requireThread();
        checkSlot(slot, 6);
        ItemStack item = storage.getItem(slot);
        return empty(item) ? null : item.clone();
    }

    /** Transfer as many offered items as fit into this source-compatible stackable input slot. */
    public ItemStack offerInput(int slot, ItemStack offered) {
        requireThread();
        checkSlot(slot, 5);
        return offer(slot, offered);
    }

    /** Source GUI uses a normal Slot for output, so it also accepts manual idle-time insertion. */
    public ItemStack offerOutput(ItemStack offered) {
        requireThread();
        return offer(5, offered);
    }

    private ItemStack offer(int slot, ItemStack offered) {
        if (empty(offered)) return null;
        ItemStack remainder = offered.clone();
        if (!available() || working()) return remainder;
        ItemStack previous = storage.getItem(slot);
        if (!empty(previous) && !previous.isSimilar(offered)) return remainder;
        int stored = empty(previous) ? 0 : previous.getAmount();
        int accepted = Math.min(offered.getAmount(), offered.getMaxStackSize() - stored);
        if (accepted <= 0) return remainder;
        ItemStack merged = offered.clone();
        merged.setAmount(stored + accepted);
        storage.setItem(slot, merged);
        remainder.subtract(accepted);
        changed();
        return remainder.isEmpty() ? null : remainder;
    }

    public ItemStack takeInput(int slot, int amount) {
        requireThread();
        checkSlot(slot, 5);
        if (!available() || working()) return null;
        return take(slot, amount);
    }

    public ItemStack extractOutput(int amount) {
        requireThread();
        if (!available()) return null;
        return take(5, amount);
    }

    private ItemStack take(int slot, int amount) {
        ItemStack stored = storage.getItem(slot);
        if (amount <= 0 || empty(stored)) return null;
        int moved = Math.min(amount, stored.getAmount());
        ItemStack result = stored.clone();
        result.setAmount(moved);
        ItemStack remainder = stored.clone();
        remainder.subtract(moved);
        storage.setItem(slot, remainder.isEmpty() ? null : remainder);
        changed();
        return result;
    }

    /** Explicit recipe selection, revalidated against current inventory when the click is processed. */
    public boolean start(String selectedRecipeId) {
        requireThread();
        if (!available() || working()) return false;
        ItemStack[] items = copies(storage.getContents());
        var recipe = KitchenRecipes.byId(selectedRecipeId);
        if (recipe == null || !KitchenRecipes.matches(recipeType, items).contains(recipe)) return false;
        ItemStack output = KitchenRecipes.preview(recipe, items);
        if (output == null || (failureProbability > 0 && KitchenRecipes.darkCuisine() == null)) return false;
        return begin(output, recipe.id(), recipe.seconds() * 20 + 25);
    }

    /** Source FastRecipeBook uses its persisted recipe snapshot, plain output and ingredient-count surcharge. */
    public boolean startRememberedRecipe(String selectedRecipeId, String selectedType, List<ItemStack> ingredients,
                                        ItemStack output, double costSeconds) {
        requireThread();
        if (!available() || working() || !recipeType.equals(selectedType) || selectedRecipeId == null
                || ingredients.isEmpty() || ingredients.size() > 5 || empty(output)
                || !Double.isFinite(costSeconds) || costSeconds < 0
                || failureProbability > 0 && KitchenRecipes.darkCuisine() == null) return false;
        ItemStack[] inputs = storage.getContents();
        for (ItemStack required : ingredients) {
            if (empty(required)) return false;
            boolean found = false;
            for (int slot = 0; slot < 5; slot++) {
                if (!empty(inputs[slot]) && KitchenRecipes.id(inputs[slot]).equals(KitchenRecipes.id(required))
                        && inputs[slot].getAmount() >= required.getAmount()) { found = true; break; }
            }
            if (!found) return false;
        }
        return begin(output, selectedRecipeId, costSeconds * 20 + 5 * ingredients.size());
    }

    private boolean begin(ItemStack output, String selectedRecipeId, double ticks) {
        ItemStack[] items = copies(storage.getContents());
        List<ItemStack> remainders = new ArrayList<>();
        for (int slot = 0; slot < 5; slot++) {
            if (empty(items[slot])) continue;
            UseRemainder remainder = items[slot].getData(DataComponentTypes.USE_REMAINDER);
            if (remainder != null && !empty(remainder.transformInto())) remainders.add(remainder.transformInto().clone());
        }
        // Prepare everything before taking one from EACH occupied input slot, including extra ingredients.
        pendingOutput = output.clone();
        recipeId = selectedRecipeId;
        remaining = total = ticks;
        for (int slot = 0; slot < 5; slot++) {
            if (empty(items[slot])) continue;
            items[slot].subtract(1);
            if (items[slot].isEmpty()) items[slot] = null;
        }
        storage.setContents(items);
        KitchenModule.closeViews(this);
        changed();
        for (ItemStack remainder : remainders) location().getWorld().dropItemNaturally(location(), remainder);
        return true;
    }

    private void tick() {
        if (!Bukkit.getServer().getServerTickManager().isRunningNormally()) return;
        if (!available() || !working()) return;
        remaining = Math.max(0, remaining - speed);
        location().getWorld().spawnParticle(Particle.SNOWFLAKE,
                location().clone().add(0, -.5, 0), 1, 0, .5, 0, .1);
        if (remaining == 0) finish();
        dirty();
    }

    private void finish() {
        if (failureProbability > 0 && KitchenRecipes.darkCuisine() == null) return;
        ItemStack result = pendingOutput.clone();
        ItemStack previous = storage.getItem(5);
        List<ItemStack> drops = new ArrayList<>();
        if (!empty(previous) && previous.isSimilar(result)) {
            // Preserve the source rule that stacking the same dish does not roll the Nuke failure.
            int combined = previous.getAmount() + result.getAmount();
            int maximum = previous.getMaxStackSize();
            ItemStack stacked = previous.clone();
            stacked.setAmount(Math.min(combined, maximum));
            storage.setItem(5, stacked);
            if (combined > maximum) {
                ItemStack overflow = result.clone();
                overflow.setAmount(combined - maximum);
                drops.add(overflow);
            }
        } else {
            if (!empty(previous)) drops.add(previous.clone());
            if (ThreadLocalRandom.current().nextDouble() < failureProbability) result = KitchenRecipes.darkCuisine();
            storage.setItem(5, result);
        }
        pendingOutput = null;
        recipeId = "";
        remaining = 0;
        total = 1;
        changed();
        for (ItemStack drop : drops) location().getWorld().dropItemNaturally(location(), drop);
        location().getWorld().playSound(location(), Sound.BLOCK_NOTE_BLOCK_PLING, SoundCategory.BLOCKS, 1f, 1f);
    }

    @Override
    public <C extends BlockEntityController> BlockEntityTicker<C> createBlockEntityTicker(CEWorld world, ImmutableBlockState state) {
        return createTickerHelper((BlockEntityTicker<KitchenController>) (w, p, s, controller) -> controller.tick());
    }

    @Override public void onLoad() { loaded = true; removed = false; KitchenModule.loaded(this); }
    @Override public void onUnload() { loaded = false; KitchenModule.closeViews(this); KitchenModule.unloaded(this); }

    @Override
    public void onRemove() {
        if (removed) return;
        removed = true;
        loaded = false;
        KitchenModule.closeViews(this);
        KitchenModule.unloaded(this);
        List<ItemStack> drops = new ArrayList<>();
        ItemStack[] items = storage.getContents();
        for (ItemStack item : items) if (!empty(item)) drops.add(item.clone());
        if (pendingOutput != null) drops.add(pendingOutput.clone());
        storage.clear();
        pendingOutput = null;
        remaining = total = 0;
        for (ItemStack drop : drops) location().getWorld().dropItemNaturally(location(), drop);
    }

    @Override
    public void saveCustomData(CompoundTag outer) {
        if (unreadableData != null) { outer.put(DATA_KEY, unreadableData.copy()); return; }
        CompoundTag data = new CompoundTag();
        data.putInt("schema", 1);
        data.putString("block", blockId);
        data.putString("recipe", recipeId);
        data.putDouble("remaining", remaining);
        data.putDouble("total", total);
        ItemStack[] items = storage.getContents();
        for (int slot = 0; slot < 6; slot++) if (!empty(items[slot])) data.putByteArray("slot" + slot, items[slot].serializeAsBytes());
        if (pendingOutput != null) data.putByteArray("pending", pendingOutput.serializeAsBytes());
        outer.put(DATA_KEY, data);
    }

    @Override
    public void loadCustomData(CompoundTag outer) {
        CompoundTag data = outer.getCompound(DATA_KEY);
        if (data == null || data.isEmpty()) return;
        try {
            if (data.getInt("schema", -1) != 1 || !blockId.equals(data.getString("block"))) throw new IllegalArgumentException("Kitchen schema/block mismatch");
            ItemStack[] restored = new ItemStack[6];
            for (int slot = 0; slot < 6; slot++) {
                byte[] bytes = data.getByteArray("slot" + slot);
                if (bytes != null && bytes.length > 0) restored[slot] = ItemStack.deserializeBytes(bytes);
            }
            byte[] bytes = data.getByteArray("pending");
            ItemStack pending = bytes == null || bytes.length == 0 ? null : ItemStack.deserializeBytes(bytes);
            double left = data.getDouble("remaining", 0), max = data.getDouble("total", 0);
            if (!Double.isFinite(left) || !Double.isFinite(max) || left < 0 || max < left || left > 0 && empty(pending)
                    || Arrays.stream(restored).anyMatch(item -> !empty(item) && item.getAmount() > item.getMaxStackSize())) {
                throw new IllegalArgumentException("Invalid kitchen items/progress");
            }
            storage.setContents(restored);
            pendingOutput = pending;
            remaining = left;
            total = max;
            recipeId = data.getString("recipe", "");
            unreadableData = null;
        } catch (RuntimeException failure) {
            unreadableData = data.copy();
            LoggerFactory.getLogger(KitchenController.class).error("Kitchen data at {} quarantined and preserved", blockEntity.pos, failure);
        }
    }

    private void changed() { dirty(); KitchenModule.refreshViews(this); }
    Inventory storageInventory() { return storage; }
    void dirty() { if (blockEntity.world != null) blockEntity.world.blockEntityChanged(blockEntity.pos); }
    static boolean empty(ItemStack item) { return item == null || item.isEmpty(); }
    private static ItemStack[] copies(ItemStack[] stacks) { return Arrays.stream(stacks).map(item -> empty(item) ? null : item.clone()).toArray(ItemStack[]::new); }
    static void requireThread() { if (!Bukkit.isPrimaryThread()) throw new IllegalStateException("Kitchen state requires Paper main thread"); }
    private static void checkSlot(int slot, int bound) { if (slot < 0 || slot >= bound) throw new IndexOutOfBoundsException("Kitchen slot " + slot); }

    /** Same native CE container bridge as SimpleStorage, narrowed to the original kitchen face rules. */
    private static final class KitchenContainer extends BukkitWorldlyStorageContainer {
        private final KitchenController controller;
        KitchenContainer(WorldlyContainerHolder holder, KitchenController controller) {
            super(holder, 6, true, true);
            this.controller = controller;
            setMaxStackSize(64);
        }
        @Override public int[] getSlotsForFace(Direction side) { return new int[]{5}; }
        @Override public boolean canPlaceItemThroughFace(int slot, Item item,
                                                       Direction side) {
            return slot == 5 && controller.available();
        }
        @Override public boolean canTakeItemThroughFace(int slot, Item item,
                                                      Direction side) {
            return slot == 5 && side == Direction.DOWN && controller.available();
        }
        @Override public void setChanged() { controller.changed(); }
    }
}
