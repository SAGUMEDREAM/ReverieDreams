package cc.thonly.reverie_dreams.paper.world.forest;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.random.RandomGenerator;
/** Pure source planner/validator, independent of Bukkit, NMS, CE and all lifecycle hooks. */
public final class GensokyoForestPlanner {
 public record Point(int x,int y,int z){}
 public record Write(Point point,boolean log,GensokyoTreeShape.Axis axis,int leafDistance){}
 public record Plan(GensokyoTreeProfile profile,long shapeSeed,int height,List<Write> writes){public Plan{writes=List.copyOf(writes);}}
 public interface Region {
  int minY();int maxY();boolean canWrite(int x,int y,int z);boolean substrate(int x,int y,int z);
  boolean validTreePosition(int x,int y,int z);boolean fluid(int x,int y,int z);boolean blockEntity(int x,int y,int z);
  void write(GensokyoTreeProfile profile,Write write);
 }
 public static GensokyoTreeProfile select(GensokyoForestType type,RandomGenerator random){float remaining=1;for(int i=0;i<type.mix().size()-1;i++){var entry=type.mix().get(i);float chance=entry.weight()/remaining;remaining-=entry.weight();if(random.nextFloat()<chance)return entry.profile();}return type.mix().getLast().profile();}
 public static Plan create(GensokyoTreeProfile profile,long shapeSeed,int x,int y,int z){var tree=GensokyoTreeShape.create(profile.settings(),new Random(shapeSeed));var writes=new ArrayList<Write>(tree.voxels().size());for(var voxel:tree.voxels())writes.add(new Write(new Point(x+voxel.point().x(),y+voxel.point().y(),z+voxel.point().z()),voxel.log(),voxel.axis(),voxel.leafDistance()));return new Plan(profile,shapeSeed,tree.height(),writes);}
 public static boolean place(Region region,GensokyoTreeProfile profile,long seed,int x,int y,int z){
  var plan=create(profile,seed,x,y,z);for(int dx=0;dx<profile.settings().trunkWidth();dx++)for(int dz=0;dz<profile.settings().trunkWidth();dz++)if(!inside(region,x+dx,y-1,z+dz)||!region.substrate(x+dx,y-1,z+dz))return false;
  for(var write:plan.writes()){var p=write.point();if(!inside(region,p.x,p.y,p.z)||!region.validTreePosition(p.x,p.y,p.z)||region.fluid(p.x,p.y,p.z)||region.blockEntity(p.x,p.y,p.z))return false;}
  for(var write:plan.writes())region.write(profile,write);return true;
 }
 private static boolean inside(Region r,int x,int y,int z){return y>=r.minY()&&y<r.maxY()&&r.canWrite(x,y,z);}
 private GensokyoForestPlanner(){}
}
