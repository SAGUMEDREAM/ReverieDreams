package cc.thonly.reverie_dreams.entity.ai.goal.work;

import cc.thonly.reverie_dreams.entity.ai.goal.util.EntityTargetUtil;
import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.entity.npc.NPCState;
import cc.thonly.reverie_dreams.entity.npc.NPCStates;
import cc.thonly.reverie_dreams.entity.npc.NPCWorkModes;
import lombok.Getter;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.TrackTargetGoal;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.util.EnumSet;
import java.util.List;

@Getter
public class NPCCleanMonsterGoal extends TrackTargetGoal {

    public NPCCleanMonsterGoal(BaseNPCLikeEntity maid) {
        super(maid, false);
        this.maid = maid;
        this.setControls(EnumSet.of(Goal.Control.TARGET));
    }


    private final BaseNPCLikeEntity maid;


    TargetPredicate targetPredicate = TargetPredicate.createAttackable().setBaseMaxDistance(16).setPredicate((e,w)->{return  !e.hasCustomName();});
    LivingEntity targetEntity;

    @Override
    public boolean canStart() {
//        System.out.println("invoke");
        if (!this.maid.isTamed() || this.maid.isSitting()) {
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
        ServerWorld serverWorld = getServerWorld(maid);

        //serverWorld.getClosestEntity(HostileEntity.class,)
        List<HostileEntity> targets = this.mob.getWorld().getEntitiesByClass(HostileEntity.class, new Box(workPos).expand(16, 8, 16), (e)->{
            return e.isAlive()&& EntityTargetUtil.canAttack(e,maid);
        });
//        System.out.println(targets.size());
        //处于工作原点水平方向拓展16格内的所有怪物
        targetEntity = serverWorld.getClosestEntity(targets, targetPredicate, this.maid, this.maid.getX(), this.maid.getEyeY(), this.maid.getZ());
        //挑选离女仆最近的怪物
        return targetEntity!=null;
    }
    @Override
    public void start() {
        this.maid.setTarget(targetEntity);
    }
}
