package cc.thonly.reverie_dreams.entity.ai.goal.work;

import cc.thonly.reverie_dreams.entity.ai.goal.util.EntityTargetUtil;
import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.data.npc.NPCState;
import cc.thonly.reverie_dreams.registry.content.NPCStates;
import cc.thonly.reverie_dreams.registry.content.NPCWorkModes;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.phys.AABB;
import java.util.EnumSet;
import java.util.List;

@Getter
public class NPCCleanMonsterGoal extends TargetGoal {

    public NPCCleanMonsterGoal(BaseNPCLikeEntity maid) {
        super(maid, false);
        this.maid = maid;
        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
    }


    private final BaseNPCLikeEntity maid;


    TargetingConditions targetPredicate = TargetingConditions.forCombat().range(16).selector((e,w)->{return  !e.hasCustomName();});
    LivingEntity targetEntity;

    @Override
    public boolean canUse() {
//        System.out.println("invoke");
        if (!this.maid.isTame() || this.maid.isOrderedToSit()) {
//            System.out.println("ret1"+this.maid.isTamed());
            return false;
        }
        NPCState state = maid.getNpcState();
        LivingEntity owner = this.maid.getOwner();
//        boolean isMaidHasWeapon = this.maid.getMainHandStack().isIn(ItemTags.SWORDS);
        if (owner == null||state!= NPCStates.WORKING|| maid.getWorkMode()!= NPCWorkModes.COMBAT) {
//            System.out.println("ret2"+" own"+owner);
            return false;
        }
        BlockPos workPos = maid.getWorkingPos();
        ServerLevel serverWorld = getServerLevel(maid);

        //serverWorld.getClosestEntity(HostileEntity.class,)
        List<Monster> targets = this.mob.level().getEntitiesOfClass(Monster.class, new AABB(workPos).inflate(16, 8, 16), (e)->{
            return e.isAlive()&& EntityTargetUtil.canAttack(e,maid);
        });
//        System.out.println(targets.size());
        //处于工作原点水平方向拓展16格内的所有怪物
        targetEntity = serverWorld.getNearestEntity(targets, targetPredicate, this.maid, this.maid.getX(), this.maid.getEyeY(), this.maid.getZ());
        //挑选离女仆最近的怪物
        return targetEntity!=null;
    }
    @Override
    public void start() {
        this.maid.setTarget(targetEntity);
    }
}
