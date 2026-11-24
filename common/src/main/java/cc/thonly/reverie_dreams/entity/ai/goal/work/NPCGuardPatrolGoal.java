package cc.thonly.reverie_dreams.entity.ai.goal.work;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.entity.ai.goal.util.EntityTargetUtil;
import cc.thonly.reverie_dreams.entity.ai.work.ScheduledWorkGoal;
import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.registry.content.NPCWorkModes;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

public final class NPCGuardPatrolGoal extends ScheduledWorkGoal {
    private BlockPos target;
    private boolean returning;

    public NPCGuardPatrolGoal(BaseNPCLikeEntity npc) {
        super(npc, NPCWorkModes.COMBAT);
    }

    private boolean peaceful() {
        if (npc.getTarget() != null && !EntityTargetUtil.canAttack(npc.getTarget(), npc)) {
            npc.setTarget(null);
            npc.getNavigation().stop();
        }
        return npc.getTarget() == null;
    }

    @Override
    protected boolean findTask(ServerLevel level) {
        if (!peaceful())
            return false;
        returning = !site.contains(npc.blockPosition()) || site.center().distToCenterSqr(npc.position()) > 9;
        target = site.center();
        if (!returning && !ReverieDreams.config().npcCombatPatrol)
            return false;
        if (!returning) {
            int r = Math.max(1, site.radius() - 1);
            target = site.center().offset(npc.getRandom().nextInt(r * 2 + 1) - r, 0, npc.getRandom().nextInt(r * 2 + 1) - r);
        }
        return level.hasChunkAt(target);
    }

    @Override
    protected boolean validTask(ServerLevel level) {
        return peaceful() && target != null && level.hasChunkAt(target);
    }

    @Override
    protected void runTask(ServerLevel level) {
        if (reach(target, 1.5))
            finish("guarding", returning ? 20 : 80);
    }

    @Override
    protected void clearTask() {
        target = null;
    }
}
