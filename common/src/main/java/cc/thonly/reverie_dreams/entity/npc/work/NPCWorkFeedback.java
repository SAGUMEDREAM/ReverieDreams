package cc.thonly.reverie_dreams.entity.npc.work;

import cc.thonly.reverie_dreams.entity.ai.work.WorkArea;
import cc.thonly.reverie_dreams.entity.ai.work.WorkWorld;
import cc.thonly.reverie_dreams.entity.ai.goal.work.NPCFarmGoal;
import cc.thonly.reverie_dreams.entity.ai.goal.work.NPCFishingGoal;
import cc.thonly.reverie_dreams.entity.ai.goal.work.NPCSmeltGoal;
import cc.thonly.reverie_dreams.entity.npc.NPCSimpleEntity;
import cc.thonly.reverie_dreams.inventory.storage.StorageRule;
import cc.thonly.reverie_dreams.inventory.storage.StorageWorld;
import cc.thonly.reverie_dreams.inventory.storage.StoragePlanner;
import cc.thonly.reverie_dreams.inventory.storage.StorageSettings;
import cc.thonly.reverie_dreams.ReverieDreams;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;

import java.util.Set;
import java.util.function.Predicate;

import cc.thonly.reverie_dreams.block.base.AbstractCropBlock;
import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.registry.content.NPCWorkModes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.item.ItemEntity;

/**
 * Diagnostics share live execution status; preparation/borrowing belongs to work goals for every entry point.
 */
public final class NPCWorkFeedback {
    private NPCWorkFeedback() {
    }

    public record Report(String code, boolean blocked) {
    }

    private static final Set<String> BLOCKED = Set.of("unreachable", "work_write_refused", "work_action_failed",
            "inventory_full", "no_workstation", "no_furnace", "no_nearby_water", "no_farmland", "no_beehive",
            "no_chest", "no_matching_storage", "need_fishing_rod", "need_shears_in_hand", "need_harvest_tool",
            "need_fuel", "need_smelt_input", "need_seeds", "need_matching_seeds", "need_smoke", "need_animals_and_food");

    public static Report classify(String code) {
        return new Report(code, BLOCKED.contains(code));
    }

    public static void recordAction(BaseNPCLikeEntity npc) {
        if (npc instanceof NPCSimpleEntity simple)
            simple.getSkillTask().recordAction();
    }

    public static int count(NPCSimpleEntity npc, Item item) {
        int count = 0;
        for (int i = 0; i < npc.getInventory().getContainerSize(); i++) {
            var stack = npc.getInventory().getItem(i);
            if (stack.is(item))
                count += stack.getCount();
        }
        return count;
    }

    public static void prepare(NPCSimpleEntity npc, String skill) { /* Shared work goals own tool leases. */ }

    private static boolean has(NPCSimpleEntity npc, Predicate<ItemStack> wanted) {
        for (int i = 0; i < npc.getInventory().getContainerSize(); i++) {
            if (i >= 18 && i < 22)
                continue;
            if (wanted.test(npc.getInventory().getItem(i)))
                return true;
        }
        return false;
    }

    public static Report inspect(NPCSimpleEntity npc, String skill, BlockPos site) {
        if (!(npc.level() instanceof ServerLevel level))
            return new Report("no_workstation", true);
        var definition = NPCSkills.find(skill);
        if (definition == null || !definition.action().equals("work"))
            return classify("no_workstation");
        var config = ReverieDreams.config();
        var area = new WorkArea(site, config.npcWorkRange, config.npcWorkVerticalRange);
        if (skill.equals(npc.getSkillTask().skill()) && npc.getSkillTask().waitingForProducedItems()) {
            var id = Identifier.parse(npc.getSkillTask().objectiveItem());
            var item = BuiltInRegistries.ITEM.getValue(id);
            boolean onGround = !level.getEntitiesOfClass(ItemEntity.class, area.box(),
                    entity -> !entity.isRemoved() && entity.getItem().is(item)).isEmpty();
            boolean inFurnace = WorkWorld.blockEntities(level, area).stream().anyMatch(block ->
                    block instanceof AbstractFurnaceBlockEntity furnace && furnace.getItem(2).is(item));
            return new Report(onGround || inFurnace ? "collecting_outputs" : "produced_items_unavailable", !onGround && !inFurnace);
        }
        if (skill.equals("sort_chests")) {
            String code = npc.getChestClassificationGoal().inspect(site);
            return new Report(code, !code.equals("ready") && !code.equals("sorting_finished") && !code.equals("storage_busy"));
        }
        // Inspect the requested site even while a blocked task has stopped its work goals.
        if (skill.equals("fish")) {
            boolean water = false;
            for (var pos : area.positions())
                if (NPCFishingGoal.isWater(level, pos)) {
                    water = true;
                    break;
                }
            if (!water)
                return classify("no_nearby_water");
        }
        if (skill.equals("farm")) {
            boolean farmland = false, crops = false, mature = false;
            for (var pos : area.positions()) {
                if (!level.hasChunkAt(pos))
                    continue;
                var block = level.getBlockState(pos).getBlock();
                farmland |= block instanceof FarmlandBlock;
                crops |= block instanceof CropBlock || block instanceof AbstractCropBlock;
                if (NPCFarmGoal.isCrop(pos, level))
                    mature = true;
            }
            if (!farmland && !crops)
                return classify("no_farmland");
            boolean selected = npc.getMainHandItem().getItem() instanceof BlockItem && npc.getMainHandItem().is(ItemTags.VILLAGER_PLANTABLE_SEEDS)
                    || npc.getOffhandItem().getItem() instanceof BlockItem && npc.getOffhandItem().is(ItemTags.VILLAGER_PLANTABLE_SEEDS);
            if (!mature && !crops && !selected)
                return classify("need_seeds");
        }
        if (skill.equals("smelt")) {
            var furnaces = WorkWorld.blockEntities(level, area).stream().filter(b -> b instanceof AbstractFurnaceBlockEntity)
                    .map(b -> (AbstractFurnaceBlockEntity) b).toList();
            if (furnaces.isEmpty())
                return classify("no_furnace");
            boolean output = furnaces.stream().anyMatch(f -> !f.getItem(2).isEmpty());
            if (!output) {
                Predicate<ItemStack> input = stack -> furnaces.stream().anyMatch(f -> NPCSmeltGoal.matches(level, f, stack));
                boolean loaded = furnaces.stream().anyMatch(f -> NPCSmeltGoal.matches(level, f, f.getItem(0)));
                if (!loaded && !has(npc, input))
                    return supplyReport(npc, area, input, "need_smelt_input");
                if (furnaces.stream().noneMatch(f -> level.fuelValues().isFuel(f.getItem(1))
                        || f.getBlockState().getValue(AbstractFurnaceBlock.LIT)) && !has(npc, level.fuelValues()::isFuel))
                    return supplyReport(npc, area, level.fuelValues()::isFuel, "need_fuel");
            }
        }
        if (skill.equals("harvest_honey")) {
            var hives = WorkWorld.blockEntities(level, area).stream().filter(b -> b instanceof BeehiveBlockEntity).toList();
            if (hives.isEmpty())
                return classify("no_beehive");
            boolean ready = hives.stream().anyMatch(h -> level.getBlockState(h.getBlockPos()).getBlock() instanceof BeehiveBlock
                    && level.getBlockState(h.getBlockPos()).getValue(BeehiveBlock.HONEY_LEVEL) >= 5);
            if (ready && config.npcHoneyRequireSmoke && hives.stream().noneMatch(h ->
                    level.getBlockState(h.getBlockPos()).getBlock() instanceof BeehiveBlock
                            && level.getBlockState(h.getBlockPos()).getValue(BeehiveBlock.HONEY_LEVEL) >= 5
                            && CampfireBlock.isSmokeyPos(level, h.getBlockPos())))
                return classify("need_smoke");
        }
        Predicate<ItemStack> wanted = switch (skill) {
            case "fish" -> s -> s.getItem() instanceof FishingRodItem;
            case "shear_sheep" -> s -> s.getItem() instanceof ShearsItem;
            case "harvest_honey" ->
                    s -> ReverieDreams.config().npcHoneyProduct.equals("honey") ? s.is(Items.GLASS_BOTTLE)
                            : ReverieDreams.config().npcHoneyProduct.equals("honeycomb") ? s.getItem() instanceof ShearsItem
                            : s.is(Items.GLASS_BOTTLE) || s.getItem() instanceof ShearsItem;
            default -> null;
        };
        if (wanted != null && !has(npc, wanted)) {
            return supplyReport(npc, area, wanted, skill.equals("fish") ? "need_fishing_rod" : skill.equals("shear_sheep") ? "need_shears_in_hand" : "need_harvest_tool");
        }
        boolean current = npc.getWorkController().working() && site.equals(npc.getWorkingPos())
                && npc.getWorkMode() == NPCWorkModes.get(ReverieDreams.id(definition.mode()));
        String code = current ? npc.getWorkController().status() : "work_enabled_unverified";
        var demand = npc.getWorkController().demand();
        if (current && demand != null && code.equals(demand.reason()))
            return supplyReport(npc, area, demand.accepts(), code);
        // Allow ordinary logistics to clear a full inventory rather than stopping the worker that unloads it.
        if (code.equals("inventory_full") && config.npcWorkAutoLogistics) {
            var stores = StorageWorld.scan(level, area);
            var settings = StorageSettings.from(config);
            for (int slot = 0; slot < Math.min(18, npc.getInventory().getContainerSize()); slot++) {
                var stack = npc.getInventory().getItem(slot);
                if (!npc.getSkillTask().protectsCargo(stack) && StoragePlanner.exportable(npc.getInventory(), slot, settings) > 0
                        && StoragePlanner.destination(stores, stack, settings, npc.position(), store -> true) != null)
                    return new Report("unloading_outputs", false);
            }
        }
        return classify(code);
    }

    private static Report supplyReport(NPCSimpleEntity npc, WorkArea area, Predicate<ItemStack> wanted, String missing) {
        boolean supply = ReverieDreams.config().npcWorkAutoLogistics && StorageWorld.scan((ServerLevel) npc.level(), area).stream()
                .filter(s -> s.rule().mode() == StorageRule.Mode.INPUT).anyMatch(s -> {
                    for (int i = 0; i < s.container().getContainerSize(); i++)
                        if (wanted.test(s.container().getItem(i)))
                            return true;
                    return false;
                });
        return supply ? new Report("collecting_supplies", false) : classify(missing);
    }
}
