package cc.thonly.reverie_dreams.block.entity;

import cc.thonly.reverie_dreams.block.decor.workshop.*;
import cc.thonly.reverie_dreams.recipe.BaseRecipeType;
import cc.thonly.reverie_dreams.recipe.RecipeManager;
import cc.thonly.reverie_dreams.recipe.entry.WorkshopProcessRecipe;
import cc.thonly.reverie_dreams.registry.content.block.entity.RDBlockEntityTypes;
import cc.thonly.reverie_dreams.workshop.*;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.*;

import java.util.*;

import net.minecraft.network.chat.Component;

/**
 * Small stations hold real items. Only the drying tray has a server ticker.
 */
public class WorkshopStationBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer, WorkshopDisplayHost {
    public static final int DRYING_CAPACITY = 4;

    public enum DryingConditions { SUNNY, RAIN, COVERED, LOW_LIGHT }

    private NonNullList<ItemStack> items = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
    private final int[] progress = new int[4];
    private final String[] activeRecipes = {"", "", "", ""};
    private long lastGrind = Long.MIN_VALUE;
    private Runnable displayUpdate;
    private List<ItemStack> published = List.of();
    private int grindAngle;

    public WorkshopStationBlockEntity(BlockPos pos, BlockState state) {
        this(RDBlockEntityTypes.WORKSHOP_STATION.get(), pos, state);
    }

    protected WorkshopStationBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public WorkshopKind workshopKind() {
        return ((WorkshopStationBlock) getBlockState().getBlock()).kind();
    }

    @Override
    public int getContainerSize() {
        return getBlockState().getBlock() instanceof WorkshopRackBlock ? 6 : 5;
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> value) {
        items = value;
    }

    @Override
    protected Component getDefaultName() {
        return getBlockState().getBlock().getName();
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return new HopperMenu(id, inventory, this);
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        return new int[0];
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction direction) {
        return false;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) {
        return false;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return false;
    }

    private int capacity() {
        return switch (workshopKind()) {
            case HERB_DRYING_TRAY -> DRYING_CAPACITY;
            case EMA_RACK, OFUDA_RACK -> 6;
            case BOOK_STACK -> 3;
            case SUZURI -> 4;
            default -> 1;
        };
    }

    private boolean handMill() {
        return workshopKind() == WorkshopKind.YAGEN || workshopKind() == WorkshopKind.STONE_MILL;
    }

    private BaseRecipeType<WorkshopProcessRecipe> recipeType() {
        return switch (workshopKind()) {
            case YAGEN -> RecipeManager.GRINDING;
            case STONE_MILL -> RecipeManager.MILLING;
            default -> RecipeManager.HERB_DRYING;
        };
    }

    public Optional<WorkshopProcessRecipe> recipe(ItemStack stack) {
        return recipeType().values().stream().filter(r -> r.matches(stack)).sorted(Comparator.comparing((WorkshopProcessRecipe r) -> r.ingredient().startsWith("#")).thenComparing(r -> r.getId().toString())).findFirst();
    }

    public boolean accepts(ItemStack stack) {
        return switch (workshopKind()) {
            case YAGEN, STONE_MILL, HERB_DRYING_TRAY -> recipe(stack).isPresent();
            default -> WorkshopItems.displayable(workshopKind(), stack);
        };
    }

    public boolean insert(ItemStack held, Player player) {
        if (!accepts(held) || handMill() && !getItem(4).isEmpty())
            return false;
        for (int i = 0; i < capacity(); i++)
            if (getItem(i).isEmpty()) {
                setItem(i, held.copyWithCount(1));
                held.consume(1, player);
                FurnitureSounds.transfer(this, workshopKind(), false);
                return true;
            }
        return false;
    }

    public boolean take(Player player) {
        int last = handMill() && !getItem(4).isEmpty() ? 4 : capacity() - 1;
        for (int i = last; i >= 0; i--)
            if (!getItem(i).isEmpty()) {
                var stack = removeItemNoUpdate(i);
                reset(i);
                setChanged();
                WorkshopItems.give(player, stack);
                FurnitureSounds.transfer(this, workshopKind(), true);
                return true;
            }
        return false;
    }

    public int progress(int slot) {
        return progress[slot];
    }

    private void reset(int slot) {
        if (slot < 4) {
            progress[slot] = 0;
            activeRecipes[slot] = "";
        }
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        reset(slot);
        super.setItem(slot, stack);
    }

    public boolean grind(long now) {
        if (!handMill() || !getItem(4).isEmpty() || lastGrind != Long.MIN_VALUE && now - lastGrind < 4)
            return false;
        var recipe = recipe(getItem(0)).orElse(null);
        if (recipe == null)
            return false;
        String signature = signature(recipe);
        if (!signature.equals(activeRecipes[0])) {
            progress[0] = 0;
            activeRecipes[0] = signature;
        }
        lastGrind = now;
        grindAngle = (grindAngle + 1) % 16;
        progress[0]++;
        if (progress[0] >= recipe.work()) {
            getItem(0).shrink(1);
            items.set(4, recipe.getOutput().build());
            reset(0);
        }
        setChanged();
        boolean mill = workshopKind() == WorkshopKind.STONE_MILL;
        FurnitureSounds.play(level, worldPosition, SoundEvents.GRINDSTONE_USE, mill ? .65f : .55f, mill ? .65f : .9f);
        return true;
    }

    private static String signature(WorkshopProcessRecipe recipe) {
        return recipe.getId() + "/" + recipe.work() + "/" + recipe.getOutput().build();
    }

    public Optional<WorkshopProcessRecipe> dryingRecipe(int slot) {
        ItemStack stack = getItem(slot);
        return stack.getCount() == 1 ? recipe(stack) : Optional.empty();
    }

    public int dryingProgress(int slot, WorkshopProcessRecipe recipe) {
        return signature(recipe).equals(activeRecipes[slot]) ? progress[slot] : 0;
    }

    public void dryStep(int activeTicks) {
        if (workshopKind() != WorkshopKind.HERB_DRYING_TRAY)
            return;
        boolean changed = false;
        for (int slot = 0; slot < DRYING_CAPACITY; slot++) {
            var recipe = dryingRecipe(slot).orElse(null);
            if (recipe == null) {
                reset(slot);
                continue;
            }
            String signature = signature(recipe);
            if (!signature.equals(activeRecipes[slot])) {
                progress[slot] = 0;
                activeRecipes[slot] = signature;
            }
            progress[slot] = Math.min(recipe.work(), progress[slot] + activeTicks);
            changed = true;
            if (progress[slot] >= recipe.work()) {
                items.set(slot, recipe.getOutput().build());
                reset(slot);
            }
        }
        // Progress is saved, but no render packet is needed until an actual item changes.
        if (changed)
            setChanged();
    }

    public static void tick(ServerLevel level, BlockPos pos, BlockState state, WorkshopStationBlockEntity host) {
        if (level.getGameTime() % 20 == 0 && dryingConditions(level, pos) == DryingConditions.SUNNY)
            host.dryStep(20);
    }

    public static DryingConditions dryingConditions(ServerLevel level, BlockPos pos) {
        if (!level.dimensionType().hasSkyLight())
            return DryingConditions.LOW_LIGHT;
        if (level.isRaining())
            return DryingConditions.RAIN;
        if (!level.canSeeSky(pos.above()))
            return DryingConditions.COVERED;
        return level.isBrightOutside() ? DryingConditions.SUNNY : DryingConditions.LOW_LIGHT;
    }

    public float grindRotation(float partialTick) {
        if (level == null || lastGrind == Long.MIN_VALUE)
            return grindAngle * 22.5f;
        return grindAngle * 22.5f - 22.5f * (1 - Math.clamp((level.getGameTime() + partialTick - lastGrind) / 4f, 0, 1));
    }

    @Override
    public List<ItemStack> displayItems() {
        return items.stream().map(s -> s.copyWithCount(s.isEmpty() ? 0 : 1)).toList();
    }

    @Override
    public void setDisplayUpdate(Runnable callback) {
        displayUpdate = callback;
        if (callback != null)
            callback.run();
    }

    @Override
    public void clearDisplayUpdate(Runnable callback) {
        if (displayUpdate == callback)
            displayUpdate = null;
    }

    @Override
    public void setChanged() {
        super.setChanged();
        boolean changed = published.size() != items.size();
        if (!changed)
            for (int i = 0; i < items.size(); i++)
                if (!ItemStack.isSameItemSameComponents(items.get(i), published.get(i)) || items.get(i).getCount() != published.get(i).getCount()) {
                    changed = true;
                    break;
                }
        if (changed || handMill())
            if (level instanceof ServerLevel server) {
                published = items.stream().map(ItemStack::copy).toList();
                server.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
                if (displayUpdate != null)
                    displayUpdate.run();
            }
    }

    @Override
    protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        ContainerHelper.saveAllItems(out, items);
        out.putInt("GrindAngle", grindAngle);
        out.putLong("LastGrind", lastGrind);
        for (int i = 0; i < 4; i++) {
            out.putInt("Progress" + i, progress[i]);
            out.putString("ActiveRecipe" + i, activeRecipes[i]);
        }
    }

    @Override
    protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        items = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(in, items);
        grindAngle = Math.floorMod(in.getIntOr("GrindAngle", 0), 16);
        lastGrind = in.getLongOr("LastGrind", Long.MIN_VALUE);
        for (int i = 0; i < 4; i++) {
            progress[i] = Math.clamp(in.getIntOr("Progress" + i, 0), 0, 72000);
            activeRecipes[i] = in.getStringOr("ActiveRecipe" + i, "");
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void setRemoved() {
        displayUpdate = null;
        super.setRemoved();
    }
}
