package cc.thonly.reverie_dreams.paper.npc;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Wolf;
import com.destroystokyo.paper.entity.Pathfinder;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

/** Source work retry=10, three failed paths, progress timeout100, no teleport/chunk loading. */
final class NPCWorkNavigation {
    enum Result { MOVING, REACHED, FAILED }
    private final Wolf wolf;
    private Location target;
    private UUID entityTarget;
    private int nextPath, progressAt, failures;
    private double best;
    NPCWorkNavigation(Wolf wolf) { this.wolf=wolf; }
    Result move(Block block,double reach) { return move(block.getLocation().add(.5,.5,.5),reach); }
    Result move(Location point,double reach) {
        if(point.getWorld()!=wolf.getWorld()||!point.getWorld().isChunkLoaded(point.getBlockX()>>4,point.getBlockZ()>>4))return Result.FAILED;
        int now=Bukkit.getCurrentTick();double distance=wolf.getLocation().distanceSquared(point);
        if(target==null||target.distanceSquared(point)>1.0e-6){target=point.clone();nextPath=now;progressAt=now;best=distance;failures=0;}
        if(distance<=reach*reach){
            var direction=point.toVector().subtract(wolf.getEyeLocation().toVector());
            var hit=direction.lengthSquared()<1.0e-9?null:wolf.getWorld().rayTraceBlocks(wolf.getEyeLocation(),direction,direction.length(),FluidCollisionMode.NEVER,true);
            if(hit==null||hit.getHitBlock()!=null&&hit.getHitBlock().equals(point.getBlock())){wolf.getPathfinder().stopPathfinding();return Result.REACHED;}
        }
        if(distance<best-.25){best=distance;progressAt=now;}if(now-progressAt>=100)return Result.FAILED;
        if(now>=nextPath){nextPath=now+10;Pathfinder.PathResult bestPath=null;double bestDistance=Double.MAX_VALUE;
            for(int[] offset:new int[][]{{1,0},{-1,0},{0,1},{0,-1}})for(int dy=-1;dy<=1;dy++){
                var stand=point.clone().add(offset[0],dy,offset[1]);var world=stand.getWorld();int x=stand.getBlockX(),y=stand.getBlockY(),z=stand.getBlockZ();
                if(!world.isChunkLoaded(x>>4,z>>4)||y<=world.getMinHeight()||y+1>=world.getMaxHeight())continue;
                if(!world.getBlockAt(x,y,z).getCollisionShape().getBoundingBoxes().isEmpty()||!world.getBlockAt(x,y+1,z).getCollisionShape().getBoundingBoxes().isEmpty()||world.getBlockAt(x,y-1,z).getCollisionShape().getBoundingBoxes().isEmpty())continue;
                var path=wolf.getPathfinder().findPath(new Location(world,x+.5,y,z+.5));
                if(path!=null&&path.canReachFinalPoint()){double d=wolf.getLocation().distanceSquared(stand);if(d<bestDistance){bestDistance=d;bestPath=path;}}
            }
            boolean started=bestPath!=null&&wolf.getPathfinder().moveTo(bestPath,1.0);failures=started?0:failures+1;if(failures>=3)return Result.FAILED;
        }return Result.MOVING;
    }
    Result move(Entity entity,double reach) {
        if(!entity.isValid()||entity.getWorld()!=wolf.getWorld()||!entity.getWorld().isChunkLoaded(entity.getLocation().getBlockX()>>4,entity.getLocation().getBlockZ()>>4))return Result.FAILED;
        int now=Bukkit.getCurrentTick();var point=entity.getLocation();double distance=wolf.getLocation().distanceSquared(point);
        if(!entity.getUniqueId().equals(entityTarget)){entityTarget=entity.getUniqueId();target=null;nextPath=now;progressAt=now;best=distance;failures=0;}
        if(distance<=reach*reach&&wolf.hasLineOfSight(point.clone().add(0,entity.getHeight()/2,0))){wolf.getPathfinder().stopPathfinding();return Result.REACHED;}
        if(distance<best-.25){best=distance;progressAt=now;}if(now-progressAt>=100)return Result.FAILED;
        if(now>=nextPath){nextPath=now+10;var path=entity instanceof LivingEntity living?wolf.getPathfinder().findPath(living):wolf.getPathfinder().findPath(point);boolean started=path!=null&&path.canReachFinalPoint()&&wolf.getPathfinder().moveTo(path,1.0);failures=started?0:failures+1;if(failures>=3)return Result.FAILED;}
        return Result.MOVING;
    }
    Result moveParts(List<Block> blocks,double reach){
        if(blocks.isEmpty()||blocks.stream().anyMatch(b->!NPCContainerWorld.loaded(b)||b.getWorld()!=wolf.getWorld()))return Result.FAILED;
        int now=Bukkit.getCurrentTick();var point=blocks.getFirst().getLocation().add(.5,.5,.5);double distance=blocks.stream().mapToDouble(b->b.getLocation().add(.5,.5,.5).distanceSquared(wolf.getLocation())).min().orElseThrow();
        if(target==null||target.distanceSquared(point)>1.0e-6){target=point;entityTarget=null;nextPath=now;progressAt=now;best=distance;failures=0;}
        for(var block:blocks){var center=block.getLocation().add(.5,.5,.5);if(wolf.getLocation().distanceSquared(center)>reach*reach)continue;var direction=center.toVector().subtract(wolf.getEyeLocation().toVector());var hit=direction.lengthSquared()<1.0e-9?null:wolf.getWorld().rayTraceBlocks(wolf.getEyeLocation(),direction,direction.length(),FluidCollisionMode.NEVER,true);if(hit==null||hit.getHitBlock()!=null&&blocks.contains(hit.getHitBlock())){wolf.getPathfinder().stopPathfinding();return Result.REACHED;}}
        if(distance<best-.25){best=distance;progressAt=now;}if(now-progressAt>=100)return Result.FAILED;
        if(now>=nextPath){nextPath=now+10;Pathfinder.PathResult pathBest=null;double cost=Double.MAX_VALUE;
            for(var block:blocks)for(int[] offset:new int[][]{{1,0},{-1,0},{0,1},{0,-1}})for(int dy=-1;dy<=1;dy++){
                var stand=block.getLocation().add(offset[0]+.5,dy,offset[1]+.5);var world=stand.getWorld();int x=stand.getBlockX(),y=stand.getBlockY(),z=stand.getBlockZ();if(!world.isChunkLoaded(x>>4,z>>4)||y<=world.getMinHeight()||y+1>=world.getMaxHeight())continue;
                if(!world.getBlockAt(x,y,z).getCollisionShape().getBoundingBoxes().isEmpty()||!world.getBlockAt(x,y+1,z).getCollisionShape().getBoundingBoxes().isEmpty()||world.getBlockAt(x,y-1,z).getCollisionShape().getBoundingBoxes().isEmpty())continue;
                var path=wolf.getPathfinder().findPath(stand);if(path!=null&&path.canReachFinalPoint()){double d=stand.distanceSquared(wolf.getLocation());if(d<cost){cost=d;pathBest=path;}}
            }boolean started=pathBest!=null&&wolf.getPathfinder().moveTo(pathBest,1.0);failures=started?0:failures+1;if(failures>=3)return Result.FAILED;
        }return Result.MOVING;
    }
    void stop(){target=null;entityTarget=null;wolf.getPathfinder().stopPathfinding();}
}
