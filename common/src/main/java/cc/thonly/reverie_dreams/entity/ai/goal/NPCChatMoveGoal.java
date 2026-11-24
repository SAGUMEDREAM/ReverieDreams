package cc.thonly.reverie_dreams.entity.ai.goal;

import cc.thonly.reverie_dreams.entity.npc.NPCSimpleEntity;
import cc.thonly.reverie_dreams.registry.content.NPCStates;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class NPCChatMoveGoal extends Goal {
    private final NPCSimpleEntity npc;

    public NPCChatMoveGoal(NPCSimpleEntity npc) {
        this.npc = npc;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return npc.getChatMoveTarget() != null && npc.getNPCState() == NPCStates.NORMAL
                && npc.level().getGameTime() < npc.getChatMoveDeadline()
                && npc.distanceToSqr(npc.getChatMoveTarget()) > 1;
    }

    @Override
    public void tick() {
        var target = npc.getChatMoveTarget();
        if (target != null && npc.tickCount % 10 == 0) {
            npc.getNavigation().moveTo(target.x, target.y, target.z, 1.0);
        }
    }

    @Override
    public void stop() {
        npc.setChatMoveTarget(null);
        npc.getNavigation().stop();
        if (npc.getNPCState() == NPCStates.NORMAL) {
            npc.setNPCState(NPCStates.NO_WALK);
        }
    }
}
