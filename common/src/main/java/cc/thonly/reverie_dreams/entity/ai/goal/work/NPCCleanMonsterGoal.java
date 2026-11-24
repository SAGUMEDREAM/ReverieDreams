package cc.thonly.reverie_dreams.entity.ai.goal.work;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.entity.ai.TargetReachability;
import cc.thonly.reverie_dreams.entity.ai.goal.util.EntityTargetUtil;
import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.registry.content.NPCWorkModes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.monster.Monster;

import java.util.Comparator;
import java.util.EnumSet;

import net.minecraft.world.entity.ai.targeting.TargetingConditions;

/**
 * A bounded guard target, independent of an online owner.
 */
public class NPCCleanMonsterGoal extends TargetGoal {
    private final BaseNPCLikeEntity npc;
    private final TargetReachability reachability;
    private final TargetingConditions conditions = TargetingConditions.forCombat();
    private LivingEntity candidate;
    private long nextSearch;

    public NPCCleanMonsterGoal(BaseNPCLikeEntity npc) {
        super(npc, false);
        this.npc = npc;
        reachability = new TargetReachability(npc);
        setFlags(EnumSet.of(Flag.TARGET));
    }

    private boolean valid(LivingEntity target) {
        return npc.getWorkController().working(NPCWorkModes.COMBAT) && target != null
                && !target.hasCustomName() && EntityTargetUtil.canAttack(target, npc)
                && conditions.test(getServerLevel(npc), npc, target);
    }

    private int priority(LivingEntity target) {
        String policy = ReverieDreams.config().npcCombatTargetPriority;
        if (policy.equals("nearest"))
            return 1;
        LivingEntity threat = policy.equals("npc_threat") ? npc : npc.getOwner();
        return threat != null && (target instanceof Mob mob && mob.getTarget() == threat || threat.getLastHurtByMob() == target) ? 0 : 1;
    }

    @Override
    public boolean canUse() {
        if (!npc.getWorkController().working(NPCWorkModes.COMBAT) || npc.level().getGameTime() < nextSearch)
            return false;
        nextSearch = npc.level().getGameTime() + 20;
        candidate = npc.level().getEntitiesOfClass(Monster.class, npc.getWorkController().area().box(), this::valid).stream()
                .sorted(Comparator.<Monster>comparingInt(this::priority).thenComparingDouble(npc::distanceToSqr))
                .filter(reachability::canEngage).findFirst().orElse(null);
        return candidate != null;
    }

    @Override
    public void start() {
        targetMob = candidate;
        npc.setTarget(candidate);
        super.start();
    }

    @Override
    public boolean canContinueToUse() {
        return valid(candidate) && npc.getTarget() == candidate && reachability.canEngage(candidate);
    }

    @Override
    public void stop() {
        if (npc.getTarget() == candidate)
            npc.setTarget(null);
        candidate = null;
        targetMob = null;
    }
}
