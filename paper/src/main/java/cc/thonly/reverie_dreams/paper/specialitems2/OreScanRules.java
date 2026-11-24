package cc.thonly.reverie_dreams.paper.specialitems2;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;

/** Source six-neighbour grouping, stable nearest ordering and marker selection without Minecraft imports. */
public final class OreScanRules {
    public record Position(int x,int y,int z){
        Position offset(int dx,int dy,int dz){return new Position(x+dx,y+dy,z+dz);}
        double distanceSquared(double x,double y,double z){double a=this.x+.5-x,b=this.y+.5-y,c=this.z+.5-z;return a*a+b*b+c*c;}
    }
    public record Ore(Position position,String id,int color){}
    public record Distribution(String id,int count){}
    public record Scan(List<Ore> ores,List<List<Ore>> veins,List<Ore> targets,List<Distribution> distribution,int nearestDistance){}
    private static final int[][] NEIGHBOURS={{0,-1,0},{0,1,0},{0,0,-1},{0,0,1},{-1,0,0},{1,0,0}};
    private OreScanRules(){}
    public static Scan build(List<Ore> input,double x,double y,double z){
        var ores=new ArrayList<>(input);Comparator<Ore> near=Comparator.comparingDouble(o->o.position.distanceSquared(x,y,z));ores.sort(near);
        var byPosition=new LinkedHashMap<Position,Ore>();for(var ore:ores)byPosition.put(ore.position,ore);
        var remaining=new HashSet<>(byPosition.keySet());var veins=new ArrayList<List<Ore>>();
        for(var start:ores){if(!remaining.remove(start.position))continue;var vein=new ArrayList<Ore>();var queue=new ArrayDeque<Position>();queue.add(start.position);
            while(!queue.isEmpty()){var current=queue.removeFirst();vein.add(byPosition.get(current));for(var direction:NEIGHBOURS){var next=current.offset(direction[0],direction[1],direction[2]);if(remaining.remove(next))queue.add(next);}}
            vein.sort(near);veins.add(List.copyOf(vein));
        }
        veins.sort(Comparator.comparingDouble(v->v.getFirst().position.distanceSquared(x,y,z)));
        var targets=new ArrayList<Ore>();if(!veins.isEmpty()){for(var ore:veins.getFirst()){if(targets.size()>=12)break;targets.add(ore);}for(int i=1;i<veins.size()&&targets.size()<20;i++)targets.add(veins.get(i).getFirst());}
        var counts=new LinkedHashMap<String,Integer>();for(var ore:ores)counts.merge(ore.id,1,Integer::sum);var distribution=new ArrayList<Distribution>();counts.forEach((id,count)->distribution.add(new Distribution(id,count)));distribution.sort(Comparator.comparingInt(Distribution::count).reversed());
        int distance=ores.isEmpty()?0:(int)Math.round(Math.sqrt(ores.getFirst().position.distanceSquared(x,y,z)));
        return new Scan(List.copyOf(ores),List.copyOf(veins),List.copyOf(targets),List.copyOf(distribution),distance);
    }
}
