package cc.thonly.reverie_dreams.entity.ai.goal.work;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.data.npc.NPCWorkMode;
import cc.thonly.reverie_dreams.entity.ai.NPCWorkController;
import cc.thonly.reverie_dreams.entity.ai.work.*;
import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.inventory.NPCWorkInventory;
import cc.thonly.reverie_dreams.inventory.storage.*;
import cc.thonly.reverie_dreams.registry.content.NPCWorkModes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;

import java.util.EnumSet;
import java.util.List;

import cc.thonly.reverie_dreams.block.base.AbstractCropBlock;
import cc.thonly.reverie_dreams.entity.npc.NPCSimpleEntity;

/**
 * A storage subtask never changes the selected work mode or its saved work origin.
 */
public final class NPCWorkLogisticsGoal extends Goal {
    private final BaseNPCLikeEntity npc;
    private final WorkNavigation navigation;
    private StoragePlanner.Store planned;
    private NPCWorkMode originalMode;
    private WorkArea area;
    private ItemStack sample = ItemStack.EMPTY;
    private int sourceSlot;
    private int amount;
    private boolean withdrawing;
    private long nextSearch;
    private NPCWorkController.Demand supply;

    public NPCWorkLogisticsGoal(BaseNPCLikeEntity npc) {
        this.npc = npc;
        navigation = new WorkNavigation(npc);
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    public static boolean production(NPCWorkMode mode) {
        return mode == NPCWorkModes.FARM || mode == NPCWorkModes.SMELT || mode == NPCWorkModes.BREED
                || mode == NPCWorkModes.SHEEP_SHEARING || mode == NPCWorkModes.HONEYCOMB_HARVESTING || mode == NPCWorkModes.FISHING;
    }

    private boolean available(StoragePlanner.Store store, ServerLevel level) {
        return npc.getWorkController().available(store.pos(), level.getGameTime())
                && StorageClaims.forWorld(level).available(store.pos(), npc.getUUID(), level.getGameTime());
    }

    @Override
    public boolean canUse() {
        var work = npc.getWorkController();
        if (!(npc.level() instanceof ServerLevel level) || !work.working() || !production(npc.getWorkMode())
                || !ReverieDreams.config().npcWorkAutoLogistics || level.getGameTime() < nextSearch)
            return false;
        nextSearch = level.getGameTime() + 20;
        area = work.area();
        originalMode = npc.getWorkMode();
        supply = work.demand();
        var stores = StorageWorld.scan(level, area);
        if (supply != null) {
            int missing = supply.amount() - AnimalFeeding.foodCount(npc.getInventory(), supply.accepts());
            if (missing <= 0)
                work.clearDemand();
            else
                for (var source : stores) {
                    if (source.rule().mode() != StorageRule.Mode.INPUT || !available(source, level))
                        continue;
                    for (int slot = 0; slot < source.container().getContainerSize(); slot++) {
                        var stack = source.container().getItem(slot);
                        if (stack.isEmpty() || !supply.accepts().test(stack))
                            continue;
                        int capacity = StorageTransfer.capacity(npc.getInventory(), stack, NPCWorkInventory.CARGO_SIZE);
                        if (capacity <= 0)
                            continue;
                        planned = source;
                        sourceSlot = slot;
                        sample = stack.copy();
                        amount = Math.min(missing, Math.min(capacity, stack.getCount()));
                        withdrawing = true;
                        return StorageClaims.forWorld(level).acquire(List.of(planned.pos()), npc.getUUID(), level.getGameTime());
                    }
                }
        }
        if (NPCWorkInventory.occupied(npc.getInventory()) < ReverieDreams.config().npcWorkUnloadThreshold
                && work.idleFor(level.getGameTime()) < 100 && !work.status().equals("inventory_full"))
            return false;
        var furnaces = WorkWorld.blockEntities(level, area).stream().filter(b -> b instanceof AbstractFurnaceBlockEntity).map(b -> (AbstractFurnaceBlockEntity) b).toList();
        var animals = originalMode == NPCWorkModes.BREED ? level.getEntitiesOfClass(Animal.class, area.box(), Animal::isAlive) : List.<Animal>of();
        var settings = StorageSettings.from(ReverieDreams.config());
        for (int slot = 0; slot < NPCWorkInventory.CARGO_SIZE; slot++) {
            var stack = npc.getInventory().getItem(slot);
            if (stack.isEmpty() || protectedObjective(stack))
                continue;
            int export = exportable(level, slot, furnaces, animals, settings);
            if (export <= 0)
                continue;
            var target = StoragePlanner.destination(stores, stack, settings, npc.position(), store -> available(store, level));
            if (target == null)
                continue;
            planned = target;
            sourceSlot = slot;
            sample = stack.copy();
            amount = export;
            withdrawing = false;
            return StorageClaims.forWorld(level).acquire(List.of(planned.pos()), npc.getUUID(), level.getGameTime());
        }
        return false;
    }

    private boolean protectedObjective(ItemStack stack) {
        return npc instanceof NPCSimpleEntity simple && simple.getSkillTask().protectsCargo(stack);
    }

    private int exportable(ServerLevel level, int slot, List<AbstractFurnaceBlockEntity> furnaces, List<Animal> animals, StorageSettings settings) {
        var stack = npc.getInventory().getItem(slot);
        int reserve = 0;
        if (originalMode == NPCWorkModes.FARM && stack.getItem() instanceof BlockItem block
                && (block.getBlock() instanceof CropBlock || block.getBlock() instanceof AbstractCropBlock))
            reserve = 16;
        if (originalMode == NPCWorkModes.SMELT) {
            boolean fuel = level.fuelValues().isFuel(stack);
            boolean input = furnaces.stream().anyMatch(f -> NPCSmeltGoal.matches(level, f, stack));
            boolean earlierFuel = false, earlierInput = false;
            for (int i = 0; i < slot; i++) {
                var prior = npc.getInventory().getItem(i);
                if (prior.isEmpty() || prior.is(stack.getItem()))
                    continue;
                earlierFuel |= level.fuelValues().isFuel(prior);
                earlierInput |= furnaces.stream().anyMatch(f -> NPCSmeltGoal.matches(level, f, prior));
            }
            // Keep one type of fuel and input ready, instead of filling every slot with reserves.
            if (fuel && !earlierFuel || input && !earlierInput)
                reserve = 64;
        }
        if (originalMode == NPCWorkModes.BREED && animals.stream().anyMatch(a -> a.isFood(stack)))
            reserve = ReverieDreams.config().npcBreedFoodReserve + 2;
        for (int i = 0; i < slot && reserve > 0; i++)
            if (npc.getInventory().getItem(i).is(stack.getItem()))
                reserve -= npc.getInventory().getItem(i).getCount();
        return Math.min(StoragePlanner.exportable(npc.getInventory(), slot, settings), Math.max(0, stack.getCount() - Math.max(0, reserve)));
    }

    @Override
    public void start() {
        npc.getWorkController().logistics(true);
        npc.getWorkController().status(withdrawing ? "collecting_supplies" : "unloading_outputs");
    }

    @Override
    public boolean canContinueToUse() {
        return planned != null && npc.getWorkController().working(originalMode) && area.equals(npc.getWorkController().area());
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (!canContinueToUse() || !(npc.level() instanceof ServerLevel level)) {
            stop();
            return;
        }
        if (!StorageClaims.forWorld(level).acquire(List.of(planned.pos()), npc.getUUID(), level.getGameTime())) {
            stop();
            return;
        }
        var live = StorageExecution.live(planned, area, pos -> StorageWorld.resolve(level, pos));
        if (live == null || (withdrawing ? live.rule().mode() != StorageRule.Mode.INPUT
                : !live.rule().accepts(sample, live.container(), StorageSettings.from(ReverieDreams.config())))) {
            stop();
            return;
        }
        var motion = navigation.move(live.pos(), 2.5);
        if (motion == WorkNavigation.Result.FAILED) {
            npc.getWorkController().reject(live.pos(), level.getGameTime());
            stop();
            return;
        }
        if (motion != WorkNavigation.Result.REACHED)
            return;
        int moved;
        if (withdrawing) {
            if (supply == null || !supply.accepts().test(live.container().getItem(sourceSlot))) {
                stop();
                return;
            }
            amount = Math.min(amount, Math.max(0, supply.amount() - AnimalFeeding.foodCount(npc.getInventory(), supply.accepts())));
            moved = StorageTransfer.move(live.container(), sourceSlot, sample, npc.getInventory(), NPCWorkInventory.CARGO_SIZE, amount);
        } else {
            if (protectedObjective(npc.getInventory().getItem(sourceSlot))) {
                stop();
                return;
            }
            var furnaces = WorkWorld.blockEntities(level, area).stream().filter(b -> b instanceof AbstractFurnaceBlockEntity).map(b -> (AbstractFurnaceBlockEntity) b).toList();
            var animals = originalMode == NPCWorkModes.BREED ? level.getEntitiesOfClass(Animal.class, area.box(), Animal::isAlive) : List.<Animal>of();
            amount = Math.min(amount, exportable(level, sourceSlot, furnaces, animals, StorageSettings.from(ReverieDreams.config())));
            moved = StorageTransfer.move(npc.getInventory(), sourceSlot, sample, live.container(), live.container().getContainerSize(), amount);
        }
        if (moved > 0) {
            npc.swing(InteractionHand.MAIN_HAND);
            npc.getWorkController().action();
        }
        stop();
    }

    @Override
    public void stop() {
        planned = null;
        supply = null;
        navigation.stop();
        StorageClaims.forWorld(npc.level()).release(npc.getUUID());
        npc.getWorkController().logistics(false);
        nextSearch = npc.level().getGameTime() + 20;
    }
}
