package cc.thonly.reverie_dreams.entity.ai.goal.work;

import cc.thonly.reverie_dreams.entity.ai.work.ScheduledWorkGoal;
import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.registry.content.NPCWorkModes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.level.gameevent.GameEvent;

import java.util.Comparator;
import java.util.List;

import cc.thonly.reverie_dreams.entity.npc.work.NPCProduction;

public class NPCSheepShearGoal extends ScheduledWorkGoal {
    private Sheep sheep;

    public NPCSheepShearGoal(BaseNPCLikeEntity npc) {
        super(npc, NPCWorkModes.SHEEP_SHEARING);
    }

    @Override
    protected int searchInterval() {
        return 20;
    }

    private static boolean tool(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof ShearsItem;
    }

    @Override
    protected boolean findTask(ServerLevel level) {
        sheep = level.getEntitiesOfClass(Sheep.class, site.box(), s -> s.isAlive() && s.readyForShearing() && !s.hasCustomName()
                        && site.contains(s.blockPosition()) && work().available(s.getUUID(), level.getGameTime()))
                .stream().min(Comparator.comparingDouble(npc::distanceToSqr)).orElse(null);
        if (sheep == null) {
            work().status("waiting_for_sheep");
            return false;
        }
        return true;
    }

    @Override
    protected boolean validTask(ServerLevel level) {
        return sheep != null && sheep.level() == level && sheep.isAlive() && sheep.readyForShearing() && site.contains(sheep.blockPosition());
    }

    @Override
    protected List<?> resources() {
        return sheep == null ? List.of() : List.of(sheep.getUUID());
    }

    @Override
    protected void clearTask() {
        sheep = null;
    }

    @Override
    protected void runTask(ServerLevel level) {
        if (!reach(sheep, 1.8))
            return;
        var hand = work().tools().ensure(npc.getInventory(), NPCSheepShearGoal::tool);
        if (hand == null) {
            work().demand(NPCSheepShearGoal::tool, 1, "need_shears_in_hand");
            finish("need_shears_in_hand", 20);
            return;
        }
        var stack = npc.getItemInHand(hand);
        if (!validTask(level) || !tool(stack) || npc.distanceToSqr(sheep) > 4)
            return;
        NPCProduction.capture(npc, "shear_sheep", () -> sheep.shear(level, SoundSource.PLAYERS, stack));
        if (!sheep.readyForShearing()) {
            stack.hurtAndBreak(1, npc, hand.asEquipmentSlot());
            sheep.gameEvent(GameEvent.SHEAR, npc);
            npc.swing(hand);
            work().action();
        }
        finish("ready", 10);
    }
}
