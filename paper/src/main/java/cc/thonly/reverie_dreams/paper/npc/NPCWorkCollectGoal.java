package cc.thonly.reverie_dreams.paper.npc;

import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.entity.Wolf;
import org.bukkit.event.entity.EntityPickupItemEvent;
import java.util.UUID;

/** Source production output collection: only real nearby Item entities, only cargo0..17. */
final class NPCWorkCollectGoal implements Goal<Wolf> {
    private final NPCAiService ai;private final Wolf wolf;private final NPCWorkNavigation navigation;
    private Item item;private NPCWorkMode mode;private NPCWorkSite site;private int nextSearch;private final Map<UUID,Integer> rejected=new HashMap<>();
    NPCWorkCollectGoal(NPCAiService ai,Wolf wolf){this.ai=ai;this.wolf=wolf;navigation=new NPCWorkNavigation(wolf);}
    private boolean valid(Item item){return item!=null&&item.isValid()&&!item.isDead()&&!item.getItemStack().isEmpty()&&item.getPickupDelay()<=0&&item.getWorld()==wolf.getWorld()&&site.contains(item.getLocation());}
    private boolean capacity(Item item){var stack=item.getItemStack().clone();int count=stack.getAmount();NPCAiService.insertCargo(ai.inventory(wolf),stack);return stack.getAmount()<count;}
    @Override public boolean shouldActivate(){
        var state=ai.state(wolf);if(!ai.working(wolf,state.mode)||!state.mode.production()||Bukkit.getCurrentTick()<nextSearch)return false;
        nextSearch=Bukkit.getCurrentTick()+20;site=state.site;mode=state.mode;var center=new Location(wolf.getWorld(),site.x()+.5,site.y()+.5,site.z()+.5);rejected.values().removeIf(until->until<=Bukkit.getCurrentTick());
        item=wolf.getWorld().getNearbyEntities(center,site.radius()+.5,site.height()+.5,site.radius()+.5).stream().filter(e->e instanceof Item).map(e->(Item)e).filter(i->valid(i)&&capacity(i)&&!rejected.containsKey(i.getUniqueId())&&ai.available(wolf,i.getUniqueId())).min(Comparator.comparingDouble(i->wolf.getLocation().distanceSquared(i.getLocation()))).orElse(null);
        return item!=null&&ai.claim(wolf,List.of(item.getUniqueId()));
    }
    @Override public boolean shouldStayActive(){return valid(item)&&ai.working(wolf,mode)&&site.equals(ai.state(wolf).site)&&capacity(item);}
    @Override public void tick(){
        if(!shouldStayActive()||!ai.claim(wolf,List.of(item.getUniqueId()))){stop();return;}
        var move=navigation.move(item,1.5);if(move==NPCWorkNavigation.Result.FAILED){rejected.put(item.getUniqueId(),Bukkit.getCurrentTick()+200);stop();return;}if(move!=NPCWorkNavigation.Result.REACHED){ai.state(wolf).status="collecting_outputs";return;}
        var before=ai.inventory(wolf);var expected=NPCSnapshot.copy(before);var original=item.getItemStack();var remainder=original.clone();NPCAiService.insertCargo(before,remainder);int moved=original.getAmount()-remainder.getAmount();
        if(moved<=0){stop();return;}var event=new EntityPickupItemEvent(wolf,item,remainder.getAmount());Bukkit.getPluginManager().callEvent(event);
        if(event.isCancelled()||!valid(item)||!item.getItemStack().equals(original)||!Arrays.equals(expected,ai.inventory(wolf))){rejected.put(item.getUniqueId(),Bukkit.getCurrentTick()+200);stop();return;}
        ai.inventory(wolf,before);wolf.playPickupItemAnimation(item,moved);if(remainder.isEmpty())item.remove();else item.setItemStack(remainder);stop();
    }
    @Override public void stop(){item=null;navigation.stop();ai.release(wolf);nextSearch=Bukkit.getCurrentTick()+10;}
    @Override public GoalKey<Wolf> getKey(){return ai.collectKey;}
    @Override public EnumSet<GoalType> getTypes(){return EnumSet.of(GoalType.MOVE,GoalType.LOOK);}
}
