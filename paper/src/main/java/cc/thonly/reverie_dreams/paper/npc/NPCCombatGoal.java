package cc.thonly.reverie_dreams.paper.npc;

import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;
import java.util.EnumSet;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Wolf;
import org.bukkit.Location;

/** Source ordinary melee speed1.5, attack interval20, path retry4..10 + failure/distance penalties. */
final class NPCCombatGoal implements Goal<Wolf> {
    private final NPCAiService ai;private final Wolf wolf;private int nextCanUse,nextPath,nextAttack;private Location lastPath;
    NPCCombatGoal(NPCAiService ai,Wolf wolf){this.ai=ai;this.wolf=wolf;}
    private LivingEntity target(){var target=wolf.getTarget();return target==null?null:ai.canonical(target);}
    @Override public boolean shouldActivate(){
        ai.acquireGuard(wolf);var target=target();int now=Bukkit.getCurrentTick();
        if(target==null||!ai.mayAttack(wolf,target)||now<nextCanUse)return false;nextCanUse=now+20;
        if(ai.rangedPending(wolf)){ai.state(wolf).status="ranged_ai_pending";return false;}
        if(ai.service.mode(wolf)==NPCMode.STAY)return ai.meleeReach(wolf,target);
        return ai.meleeReach(wolf,target)||wolf.getPathfinder().findPath(target)!=null;
    }
    @Override public boolean shouldStayActive(){var target=target();return target!=null&&ai.mayAttack(wolf,target)&&!ai.rangedPending(wolf)&&(ai.meleeReach(wolf,target)||wolf.getPathfinder().hasPath());}
    @Override public void start(){nextPath=0;nextAttack=0;lastPath=null;}
    @Override public void tick(){
        var target=target();if(target==null||!ai.mayAttack(wolf,target)){wolf.setTarget(null);stop();return;}
        if(wolf.getTarget()!=target)wolf.setTarget(target);wolf.lookAt(target,30,30);int now=Bukkit.getCurrentTick();
        if(ai.service.mode(wolf)==NPCMode.STAY)wolf.getPathfinder().stopPathfinding();
        else if(now>=nextPath&&(lastPath==null||lastPath.distanceSquared(target.getLocation())>=1||ThreadLocalRandom.current().nextFloat()<.05f)){
            lastPath=target.getLocation();int delay=4+ThreadLocalRandom.current().nextInt(7);double distance=wolf.getLocation().distanceSquared(lastPath);
            if(distance>1024)delay+=10;else if(distance>256)delay+=5;if(!wolf.getPathfinder().moveTo(target,1.5))delay+=15;nextPath=now+delay;
        }
        if(now>=nextAttack&&ai.meleeReach(wolf,target)&&ai.lineOfSight(wolf,target)){nextAttack=now+20;ai.attackOnce(wolf.getUniqueId(),target);}
    }
    @Override public void stop(){wolf.getPathfinder().stopPathfinding();}
    @Override public GoalKey<Wolf> getKey(){return ai.combatKey;}
    @Override public EnumSet<GoalType> getTypes(){return EnumSet.of(GoalType.MOVE,GoalType.LOOK);}
}
