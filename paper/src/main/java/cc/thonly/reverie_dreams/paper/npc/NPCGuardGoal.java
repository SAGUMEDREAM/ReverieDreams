package cc.thonly.reverie_dreams.paper.npc;

import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;
import java.util.EnumSet;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Wolf;

/** Original guard returns to work-center, then patrols inside it; owner may be offline. */
final class NPCGuardGoal implements Goal<Wolf>{
    private final NPCAiService ai;private final Wolf wolf;private final NPCWorkNavigation navigation;private Location target;private int next;private boolean returning;
    NPCGuardGoal(NPCAiService ai,Wolf wolf){this.ai=ai;this.wolf=wolf;navigation=new NPCWorkNavigation(wolf);}
    @Override public boolean shouldActivate(){
        if(!ai.working(wolf,NPCWorkMode.COMBAT)||Bukkit.getCurrentTick()<next)return false;ai.acquireGuard(wolf);if(wolf.getTarget()!=null)return false;
        var site=ai.state(wolf).site;var center=new Location(wolf.getWorld(),site.x()+.5,site.y()+.5,site.z()+.5);
        returning=!site.contains(wolf.getLocation())||center.distanceSquared(wolf.getLocation())>9;target=center;
        if(!returning){int radius=Math.max(1,site.radius()-1);target=target.clone().add(ThreadLocalRandom.current().nextInt(-radius,radius+1),0,ThreadLocalRandom.current().nextInt(-radius,radius+1));}
        return target.getWorld().isChunkLoaded(target.getBlockX()>>4,target.getBlockZ()>>4);
    }
    @Override public boolean shouldStayActive(){return target!=null&&ai.working(wolf,NPCWorkMode.COMBAT)&&wolf.getTarget()==null;}
    @Override public void tick(){
        if(!shouldStayActive()){stop();return;}var result=navigation.move(target,1.5);
        if(result==NPCWorkNavigation.Result.REACHED){stop();ai.state(wolf).status="guarding";next=Bukkit.getCurrentTick()+(returning?20:80);}
        else if(result==NPCWorkNavigation.Result.FAILED){stop();ai.state(wolf).status="unreachable";next=Bukkit.getCurrentTick()+10;}
        else ai.state(wolf).status="walking";
    }
    @Override public void stop(){navigation.stop();target=null;next=Bukkit.getCurrentTick()+10;}
    @Override public GoalKey<Wolf> getKey(){return ai.guardKey;}
    @Override public EnumSet<GoalType> getTypes(){return EnumSet.of(GoalType.MOVE,GoalType.LOOK);}
}
