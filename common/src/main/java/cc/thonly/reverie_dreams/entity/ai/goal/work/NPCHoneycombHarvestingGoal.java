package cc.thonly.reverie_dreams.entity.ai.goal.work;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.block.BeehiveBlockProxy;
import cc.thonly.reverie_dreams.entity.ai.work.*;
import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.inventory.NPCWorkInventory;
import cc.thonly.reverie_dreams.registry.content.NPCWorkModes;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;

import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

import cc.thonly.reverie_dreams.entity.npc.work.NPCProduction;
import com.mojang.logging.LogUtils;

public class NPCHoneycombHarvestingGoal extends ScheduledWorkGoal {
    private BlockPos pos;
    private BeehiveBlockEntity expected;

    public NPCHoneycombHarvestingGoal(BaseNPCLikeEntity npc) {
        super(npc, NPCWorkModes.HONEYCOMB_HARVESTING);
    }

    @Override
    protected int searchInterval() {
        return 20;
    }

    public static boolean ready(ServerLevel level, BlockPos pos) {
        return level.hasChunkAt(pos) && level.getBlockState(pos).getBlock() instanceof BeehiveBlock
                && level.getBlockState(pos).getValue(BeehiveBlock.HONEY_LEVEL) >= BeehiveBlock.MAX_HONEY_LEVELS;
    }

    @Override
    protected boolean findTask(ServerLevel level) {
        var hives = WorkWorld.blockEntities(level, site).stream().filter(b -> b instanceof BeehiveBlockEntity)
                .map(b -> (BeehiveBlockEntity) b).filter(b -> ready(level, b.getBlockPos()) && work().available(b.getBlockPos(), level.getGameTime()))
                .sorted(Comparator.comparingDouble(b -> b.getBlockPos().distToCenterSqr(npc.position()))).toList();
        expected = hives.stream().filter(b -> !ReverieDreams.config().npcHoneyRequireSmoke || CampfireBlock.isSmokeyPos(level, b.getBlockPos())).findFirst().orElse(null);
        if (expected == null) {
            work().status(hives.isEmpty() ? "waiting_for_honey" : "need_smoke");
            return false;
        }
        pos = expected.getBlockPos().immutable();
        return true;
    }

    private Predicate<ItemStack> wanted() {
        String choice = ReverieDreams.config().npcHoneyProduct;
        boolean shears = choice.equals("honeycomb") || choice.equals("auto") && (npc.getMainHandItem().is(Items.SHEARS)
                || npc.getOffhandItem().is(Items.SHEARS) || NPCWorkInventory.find(npc.getInventory(), s -> s.is(Items.SHEARS)) >= 0);
        return stack -> acceptsTool(choice, shears, stack);
    }

    public static boolean acceptsTool(String choice, boolean shearsAvailable, ItemStack stack) {
        if (choice.equals("honey"))
            return stack.is(Items.GLASS_BOTTLE);
        if (choice.equals("honeycomb") || shearsAvailable)
            return stack.is(Items.SHEARS);
        // With no local tool, automatic supply may use either tool from input storage.
        return stack.is(Items.SHEARS) || stack.is(Items.GLASS_BOTTLE);
    }

    @Override
    protected boolean validTask(ServerLevel level) {
        return pos != null && site.contains(pos) && ready(level, pos) && level.getBlockEntity(pos) == expected && !expected.isRemoved();
    }

    @Override
    protected List<?> resources() {
        return pos == null ? List.of() : List.of(pos);
    }

    @Override
    protected void clearTask() {
        pos = null;
        expected = null;
    }

    @Override
    protected void runTask(ServerLevel level) {
        if (!reach(pos, 2.5))
            return;
        if (ReverieDreams.config().npcHoneyRequireSmoke && !CampfireBlock.isSmokeyPos(level, pos)) {
            finish("need_smoke", 40);
            return;
        }
        var predicate = wanted();
        var hand = work().tools().ensure(npc.getInventory(), predicate);
        if (hand == null) {
            work().demand(predicate, ReverieDreams.config().npcHoneyProduct.equals("honeycomb") ? 1 : 16, "need_harvest_tool");
            finish("need_harvest_tool", 20);
            return;
        }
        if (!validTask(level) || !predicate.test(npc.getItemInHand(hand))) {
            finish("waiting_for_honey", 10);
            return;
        }
        var before = level.getBlockState(pos);
        try {
            NPCProduction.capture(npc, "harvest_honey",
                    () -> ((BeehiveBlockProxy) before.getBlock()).reverie_dreams$onInteractUse(npc, level, before, pos, hand));
            var after = level.getBlockState(pos);
            if (after.hasProperty(BeehiveBlock.HONEY_LEVEL) && after.getValue(BeehiveBlock.HONEY_LEVEL) < before.getValue(BeehiveBlock.HONEY_LEVEL)) {
                npc.swing(hand);
                work().action();
                finish("ready", 20);
            } else
                finish("work_write_refused", 100);
        } catch (RuntimeException failure) {
            LogUtils.getLogger().error("NPC {} failed honey harvesting at {}", npc.getUUID(), pos, failure);
            finish("work_action_failed", 100);
        }
    }
}
