//package cc.thonly.reverie_dreams.entity.ai.goal.work;
//
//import cc.thonly.reverie_dreams.entity.ai.goal.util.EntityTargetUtil;
//import cc.thonly.reverie_dreams.entity.npc.NPCEntityImpl;
//import cc.thonly.reverie_dreams.entity.npc.NPCWorkModes;
//import net.minecraft.entity.ai.goal.Goal;
//import net.minecraft.entity.ai.goal.MoveToTargetPosGoal;
//import net.minecraft.entity.mob.PathAwareEntity;
//import net.minecraft.server.world.ServerWorld;
//import net.minecraft.util.math.BlockPos;
//import net.minecraft.world.WorldView;
//
//import java.util.EnumSet;
//
//public class NPCMoveToCropGoal extends MoveToTargetPosGoal {
//    public NPCMoveToCropGoal(NPCEntityImpl maid, double speed, int range) {
//        super(maid, speed, range);
//        this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
//    }
//
//    @Override
//    public boolean canStart() {
//        NPCEntityImpl maid = (NPCEntityImpl) mob;
//        if (!EntityTargetUtil.isThisWorkMode(maid, NPCWorkModes.FARM)) {
//            return false;
//        }
//        return super.canStart();
//    }
////    @Override
////    public boolean shouldResetPath() {
////        return this.tryingTime % 100 == 0||!isInRange(targetPos);
////    }
//
//    @Override
//    protected boolean isTargetPos(WorldView ignore, BlockPos pos) {
//        NPCEntityImpl maid = (NPCEntityImpl) mob;
//
//        ServerWorld world = getServerWorld(maid);
//        boolean b = (NPCFarmGoal.isCrop(pos, world)
//                || (NPCFarmGoal.isMaidHasSeeds(maid) && NPCFarmGoal.isFarmLandTop(pos, world)))
//                &&isInRange(pos);
//        System.out.println(pos);
//
//        return b;
//    }
//
//
//
//    private boolean isInRange(BlockPos pos) {
//        BlockPos workingPos = ((NPCEntityImpl) mob).getWorkingPos();
//        return pos.getX() >= workingPos.getX() - 15 && pos.getX() <= workingPos.getX() + 15
//                && pos.getZ() >= workingPos.getZ() - 15 && pos.getZ() <= workingPos.getZ() + 15;
//    }
//}
