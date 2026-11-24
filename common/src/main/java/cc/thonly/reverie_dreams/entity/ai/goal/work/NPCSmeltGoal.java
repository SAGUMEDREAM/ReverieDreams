package cc.thonly.reverie_dreams.entity.ai.goal.work;

import cc.thonly.reverie_dreams.entity.ai.work.*;
import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.inventory.NPCWorkInventory;
import cc.thonly.reverie_dreams.inventory.storage.StorageTransfer;
import cc.thonly.reverie_dreams.registry.content.NPCWorkModes;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.entity.*;

import java.util.Comparator;
import java.util.List;

import cc.thonly.reverie_dreams.entity.npc.NPCSimpleEntity;
import cc.thonly.reverie_dreams.entity.npc.work.NPCProduction;
import net.minecraft.core.registries.BuiltInRegistries;

public class NPCSmeltGoal extends ScheduledWorkGoal {
    private AbstractFurnaceBlockEntity expected;
    private BlockPos pos;
    private int sourceSlot;
    private int furnaceSlot;
    private boolean collecting;
    private ItemStack sample = ItemStack.EMPTY;

    public NPCSmeltGoal(BaseNPCLikeEntity npc) {
        super(npc, NPCWorkModes.SMELT);
    }

    @Override
    protected int searchInterval() {
        return 20;
    }

    public static boolean matches(ServerLevel level, AbstractFurnaceBlockEntity furnace, ItemStack stack) {
        if (stack.isEmpty())
            return false;
        var input = new SingleRecipeInput(stack);
        if (furnace instanceof SmokerBlockEntity)
            return level.recipeAccess().getRecipeFor(RecipeType.SMOKING, input, level).isPresent();
        if (furnace instanceof BlastFurnaceBlockEntity)
            return level.recipeAccess().getRecipeFor(RecipeType.BLASTING, input, level).isPresent();
        return furnace instanceof FurnaceBlockEntity && level.recipeAccess().getRecipeFor(RecipeType.SMELTING, input, level).isPresent();
    }

    private boolean requested(ServerLevel level, AbstractFurnaceBlockEntity furnace, ItemStack stack) {
        if (!matches(level, furnace, stack))
            return false;
        if (!(npc instanceof NPCSimpleEntity simple) || simple.getSkillTask().objectiveItem() == null)
            return true;
        var type = furnace instanceof SmokerBlockEntity ? RecipeType.SMOKING : furnace instanceof BlastFurnaceBlockEntity ? RecipeType.BLASTING : RecipeType.SMELTING;
        return level.recipeAccess().getRecipeFor(type, new SingleRecipeInput(stack), level)
                .map(recipe -> BuiltInRegistries.ITEM.getKey(recipe.value().assemble(new SingleRecipeInput(stack)).getItem()).toString()
                        .equals(simple.getSkillTask().objectiveItem())).orElse(false);
    }

    private boolean available(AbstractFurnaceBlockEntity furnace) {
        if (!(furnace instanceof NPCProduction.FurnaceSource tracked))
            return true;
        if (npc instanceof NPCSimpleEntity simple)
            tracked.reverie_dreams$productionBatch().flush(simple);
        return tracked.reverie_dreams$productionBatch().available(furnace, npc);
    }

    @Override
    protected boolean findTask(ServerLevel level) {
        var furnaces = WorkWorld.blockEntities(level, site).stream().filter(b -> b instanceof AbstractFurnaceBlockEntity)
                .map(b -> (AbstractFurnaceBlockEntity) b).filter(b -> work().available(b.getBlockPos(), level.getGameTime()))
                .filter(this::available)
                .sorted(Comparator.comparingDouble(b -> b.getBlockPos().distToCenterSqr(npc.position()))).toList();
        if (furnaces.isEmpty()) {
            work().status("no_furnace");
            return false;
        }
        boolean outputBlocked = false;
        for (var furnace : furnaces)
            for (int slot = 2; slot >= 1; slot--) {
                var stack = furnace.getItem(slot);
                if (stack.isEmpty() || slot == 1 && level.fuelValues().isFuel(stack))
                    continue;
                if (StorageTransfer.capacity(npc.getInventory(), stack, NPCWorkInventory.CARGO_SIZE) == 0) {
                    outputBlocked = true;
                    continue;
                }
                select(furnace, -1, slot, true, stack);
                return true;
            }
        for (var furnace : furnaces) {
            if (matches(level, furnace, furnace.getItem(0)) && furnace.getItem(1).getCount() < 8) {
                int slot = NPCWorkInventory.find(npc.getInventory(), stack -> level.fuelValues().isFuel(stack) && FurnaceTransfer.space(furnace, 1, stack) > 0);
                if (slot >= 0) {
                    select(furnace, slot, 1, false, npc.getInventory().getItem(slot));
                    return true;
                }
            }
        }
        for (var furnace : furnaces) {
            // A production batch starts with an empty input slot. Topping up would lose input provenance.
            if (npc instanceof NPCSimpleEntity simple && simple.getSkillTask().objectiveItem() != null
                    && (simple.getSkillTask().quantityRemaining() == 0 || !furnace.getItem(0).isEmpty()
                    || furnaces.stream().anyMatch(f -> f instanceof NPCProduction.FurnaceSource tracked
                    && tracked.reverie_dreams$productionBatch().issuedTo(simple))))
                continue;
            int slot = NPCWorkInventory.find(npc.getInventory(), stack -> requested(level, furnace, stack) && FurnaceTransfer.space(furnace, 0, stack) > 0);
            if (slot >= 0) {
                select(furnace, slot, 0, false, npc.getInventory().getItem(slot));
                return true;
            }
        }
        boolean fuelNeeded = furnaces.stream().anyMatch(f -> matches(level, f, f.getItem(0)) && f.getItem(1).isEmpty());
        if (outputBlocked)
            work().status("inventory_full");
        else if (fuelNeeded)
            work().demand(level.fuelValues()::isFuel, 32, "need_fuel");
        else if (furnaces.stream().allMatch(f -> f.getItem(0).isEmpty()))
            work().demand(stack -> furnaces.stream().anyMatch(f -> requested(level, f, stack)), 32, "need_smelt_input");
        else
            work().status("waiting_for_smelting");
        return false;
    }

    private void select(AbstractFurnaceBlockEntity furnace, int source, int slot, boolean output, ItemStack stack) {
        expected = furnace;
        pos = furnace.getBlockPos().immutable();
        sourceSlot = source;
        furnaceSlot = slot;
        collecting = output;
        sample = stack.copy();
    }

    @Override
    protected boolean validTask(ServerLevel level) {
        return pos != null && site.contains(pos) && level.hasChunkAt(pos) && level.getBlockEntity(pos) == expected && !expected.isRemoved();
    }

    @Override
    protected List<?> resources() {
        return pos == null ? List.of() : List.of(pos);
    }

    @Override
    protected void clearTask() {
        expected = null;
        pos = null;
        sample = ItemStack.EMPTY;
    }

    @Override
    protected void runTask(ServerLevel level) {
        if (!reach(pos, 2.5))
            return;
        // Resolve again after navigation. Never write the instance kept in the plan.
        if (!(level.getBlockEntity(pos) instanceof AbstractFurnaceBlockEntity live) || live != expected || live.isRemoved()) {
            finish("no_furnace", 10);
            return;
        }
        int moved;
        if (collecting) {
            if (furnaceSlot == 1 && level.fuelValues().isFuel(live.getItem(1))) {
                finish("ready", 10);
                return;
            }
            moved = FurnaceTransfer.collect(live, furnaceSlot, sample, npc.getInventory());
        } else {
            int maximum = Integer.MAX_VALUE;
            if (furnaceSlot == 0 && npc instanceof NPCSimpleEntity simple && simple.getSkillTask().objectiveItem() != null) {
                if (!live.getItem(0).isEmpty()) {
                    finish("waiting_for_smelting", 10);
                    return;
                }
                var type = live instanceof SmokerBlockEntity ? RecipeType.SMOKING : live instanceof BlastFurnaceBlockEntity ? RecipeType.BLASTING : RecipeType.SMELTING;
                int outputCount = level.recipeAccess().getRecipeFor(type, new SingleRecipeInput(sample), level)
                        .map(recipe -> recipe.value().assemble(new SingleRecipeInput(sample)).getCount()).orElse(1);
                maximum = (simple.getSkillTask().quantityRemaining() + Math.max(1, outputCount) - 1) / Math.max(1, outputCount);
            }
            moved = FurnaceTransfer.insert(npc.getInventory(), sourceSlot, sample, live, furnaceSlot,
                    stack -> furnaceSlot == 0 ? requested(level, live, stack) : level.fuelValues().isFuel(stack) && matches(level, live, live.getItem(0)), maximum);
        }
        if (moved > 0 && !collecting && furnaceSlot == 0 && npc instanceof NPCSimpleEntity simple
                && simple.getSkillTask().objectiveItem() != null && live instanceof NPCProduction.FurnaceSource tracked)
            tracked.reverie_dreams$productionBatch().issued(simple, sample, moved);
        if (moved > 0) {
            npc.swing(InteractionHand.MAIN_HAND);
            work().action();
        }
        finish(moved > 0 ? "ready" : collecting ? "inventory_full" : "waiting_for_smelting", 10);
    }
}
