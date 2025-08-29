package cc.thonly.reverie_dreams.entity.ai.goal.work;

import cc.thonly.reverie_dreams.entity.ai.goal.util.EntityTargetUtil;
import cc.thonly.reverie_dreams.entity.npc.NPCEntityImpl;
import cc.thonly.reverie_dreams.entity.npc.NPCWorkModes;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.TemptGoal;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.EnumSet;

public class NPCCloseToCropGoal extends Goal {

    protected final NPCEntityImpl maid;
    private final double speed;
    private int cooldown;
    private int moveTime = 0;
    private static final int WORKING_RANGE = 15;
//    private static final Direction SEARCH_DIR = Direction.EAST;
    private static final int MAX_SEARCH_COUNT = 10;
    //1tick搜索的方块数量
    private BlockPos goalPos = null;
    private BlockPos bufferPos = null;

    private boolean hasSeeds;


    private int currRange = 1;
    private int currCount = 0;

    public NPCCloseToCropGoal(NPCEntityImpl maid, double speed) {
        this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
        this.maid = maid;
        this.speed = speed;
    }

    @Override
    public boolean canStart() {
//        if (this.cooldown > 0) {
//            --this.cooldown;
//            return false;
//        }
        if (!EntityTargetUtil.isThisWorkMode(maid, NPCWorkModes.FARM)) {
            return false;
        }
        return true;
    }


    @Override
    public boolean shouldContinue() {
        return this.canStart();
    }


    @Override
    public void start() {
    }

    @Override
    public void stop() {
        this.maid.getNavigation().stop();
        this.cooldown = TemptGoal.toGoalTicks(100);
    }

    @Override
    public void tick() {
        hasSeeds = NPCFarmGoal.isMaidHasSeeds(maid);
//        System.out.println("Seed "+ hasSeeds);
        moveTime++;
        if (goalPos == null){
            goalPos = bufferPos;
            bufferPos = getNextGoal();
        }


        if (goalPos ==null
                ||moveTime>500
                || goalPos.toCenterPos().squaredDistanceTo(Vec3d.of(this.maid.getWorkingPos()))>770
                ||this.maid.squaredDistanceTo(Vec3d.of(goalPos)) < 2

        ) {
//            System.out.println("Goal "+ (goalPos ==null) +" Time:"+ moveTime );
            this.maid.getNavigation().stop();
            goalPos = null;
            moveTime = 0;
        } else {
            Vec3d look = goalPos.toCenterPos().offset(Direction.DOWN, 0.5);
            this.maid.getLookControl().lookAt(look);
            this.maid.getNavigation().startMovingTo(look.x,look.y,look.z,this.speed);
        }
    }



    //寻路 农田按照平面考虑 应该不会有人在啥阴间的地方让女仆工作吧


    private BlockPos getNextGoal(){
        BlockPos pos = nextPos();

        for (int searchCount = 0;searchCount<MAX_SEARCH_COUNT;searchCount++){
            if (isTargetPos(pos)) {
                return pos;
            }
            pos = nextPos();
        }
        return null;
    }

    private BlockPos nextPos(){
        currCount++;
        if (currCount >= (currRange - 1) * 8) {
            currCount = 0;
            currRange++;
        }
        if (currRange>WORKING_RANGE)currRange = 1;
        return getPos(maid.getWorkingPos(),currRange,currCount);
    }
    private static final  Direction[] facList = new Direction[]{Direction.EAST,Direction.NORTH,Direction.WEST,Direction.SOUTH};



    private static BlockPos getPos(BlockPos origin,int r,int c){
        if (r == 1) return origin;
        int sideLength = (r-1)*2;
        int side =  c/sideLength;
        int sidePos = c%sideLength;
        Direction offDir = facList[side];
        BlockPos offOrigin = origin.offset(facList[(side+2)%4],r).offset(facList[(side+3)%4],r);
        BlockPos p = offOrigin.offset(offDir, sidePos);
        return p;
    }
    protected boolean isTargetPos(BlockPos pos) {
        ServerWorld world = getServerWorld(maid);
        boolean b = (NPCFarmGoal.isCrop(pos, world)
                ||  (hasSeeds&& NPCFarmGoal.isFarmLandTop(pos, world)));
//        System.out.println(pos+"  aaaaa "+b +"  cr "+ NPCFarmGoal.isCrop(pos, world)
//        +" farm "+NPCFarmGoal.isFarmLandTop(pos, world));
        return b;
    }
//    private BlockPos offset(BlockPos pos,Direction dir){
//        BlockPos offset3 = pos.offset(dir, 3);
//        if (isInRange(offset3)){
//            return offset3;
//        }
//        BlockPos offset2 = pos.offset(dir, 2);
//        return isInRange(offset2)?offset2:null;
//    }
//    private boolean isInRange(BlockPos pos){
//        BlockPos workingPos = maid.getWorkingPos();
//        return pos.getX()>=workingPos.getX()-15&&pos.getX()<=workingPos.getX()+15
//                &&pos.getZ()>=workingPos.getZ()-15&&pos.getZ()<=workingPos.getZ()+15;
//    }
//    //重置位置
//    public void resetPos(){
//        offDir = SEARCH_DIR;
//        searchPos = maid.getWorkingPos().offset(offDir.rotateCounterclockwise(Direction.Axis.Y),WORKING_RANGE)
//                .offset(offDir.getOpposite(),15);
//
//    }


}

