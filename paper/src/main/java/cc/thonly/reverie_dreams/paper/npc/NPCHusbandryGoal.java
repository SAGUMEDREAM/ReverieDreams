package cc.thonly.reverie_dreams.paper.npc;

import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Wolf;
import org.slf4j.LoggerFactory;

/** Original BREED/SHEEP_SHEARING/HONEYCOMB_HARVESTING state machines, shared bounded navigation. */
final class NPCHusbandryGoal implements Goal<Wolf> {
    private final NPCAiService ai;private final NPCHusbandry work;private final Wolf wolf;private final NPCWorkNavigation navigation;
    private NPCWorkMode mode;private NPCWorkSite site;private Animals first,second;private Block hive;private int phase,nextSearch,cursor;
    private List<?> resources=List.of();private final Map<Object,Integer> rejected=new HashMap<>();
    NPCHusbandryGoal(NPCAiService ai,Wolf wolf){this.ai=ai;work=ai.husbandry;this.wolf=wolf;navigation=new NPCWorkNavigation(wolf);}
    private boolean animalMode(NPCWorkMode mode){return mode==NPCWorkMode.BREED||mode==NPCWorkMode.SHEEP_SHEARING||mode==NPCWorkMode.HONEYCOMB_HARVESTING;}
    @Override public boolean shouldActivate(){
        var state=ai.state(wolf);if(!animalMode(state.mode)||!ai.working(wolf,state.mode)||Bukkit.getCurrentTick()<nextSearch)return false;
        if(!state.site.equals(site)||mode!=state.mode){site=state.site;mode=state.mode;cursor=0;rejected.clear();}int now=Bukkit.getCurrentTick();nextSearch=now+20;rejected.values().removeIf(until->until<=now);
        if(mode==NPCWorkMode.BREED){
            var animals=work.animals(wolf);var candidates=animals.stream().filter(a->work.ready(wolf,a)&&available(a.getUniqueId())).sorted(Comparator.comparingDouble(a->a.getLocation().distanceSquared(wolf.getLocation()))).toList();
            for(var animal:candidates){
                int count=(int)animals.stream().filter(a->a.getType()==animal.getType()).count(),lovers=(int)animals.stream().filter(a->a.getType()==animal.getType()&&a.isLoveMode()).count();if(!NPCHusbandry.withinLimit(count,lovers))continue;
                var partner=animals.stream().filter(a->a!=animal&&a.getType()==animal.getType()&&work.valid(wolf,a)&&a.customName()==null&&(a.isLoveMode()||work.ready(wolf,a))&&available(a.getUniqueId())).min(Comparator.comparingDouble(a->a.getLocation().distanceSquared(animal.getLocation()))).orElse(null);if(partner==null)continue;
                if(work.countFood(wolf,animal)<(partner.isLoveMode()?1:2)+NPCHusbandry.FOOD_RESERVE){state.status="need_animals_and_food";continue;}
                var keys=work.breedKeys(wolf,animal,partner);if(ai.claim(wolf,keys)){first=animal;second=partner;phase=0;resources=keys;return true;}
            }if(!state.status.equals("need_animals_and_food"))state.status=candidates.isEmpty()?"waiting_for_animals":"breed_limit_or_pair";return false;
        }
        if(mode==NPCWorkMode.SHEEP_SHEARING){
            var target=work.animals(wolf).stream().filter(a->a instanceof Sheep sheep&&work.validSheep(wolf,sheep)&&available(a.getUniqueId())).min(Comparator.comparingDouble(a->a.getLocation().distanceSquared(wolf.getLocation()))).orElse(null);
            if(target!=null&&ai.claim(wolf,List.of(target.getUniqueId()))){first=target;resources=List.of(target.getUniqueId());return true;}state.status="waiting_for_sheep";return false;
        }
        var hives=work.hives(wolf).stream().filter(block->available(new NPCWorkSite.Pos(block.getX(),block.getY(),block.getZ()))&&ai.available(wolf,block)&&work.fullHive(wolf,block)).toList();
        Block best=hives.stream().filter(block->work.readyHive(wolf,block)).min(Comparator.comparingDouble(block->wolf.getLocation().distanceSquared(block.getLocation().add(.5,.5,.5)))).orElse(null);
        if(best!=null&&ai.claim(wolf,best)){hive=best;resources=List.of(ai.resource(best));return true;}state.status=hives.isEmpty()?"waiting_for_honey":"need_smoke";return false;
    }
    private boolean available(Object key){return rejected.getOrDefault(key,Integer.MIN_VALUE)<=Bukkit.getCurrentTick()&&ai.available(wolf,key);}
    @Override public boolean shouldStayActive(){return mode!=null&&ai.working(wolf,mode)&&site.equals(ai.state(wolf).site)&&(mode==NPCWorkMode.BREED?work.valid(wolf,first)&&work.valid(wolf,second):mode==NPCWorkMode.SHEEP_SHEARING?first instanceof Sheep sheep&&work.validSheep(wolf,sheep):hive!=null&&hive.getWorld().isChunkLoaded(hive.getX()>>4,hive.getZ()>>4)&&work.fullHive(wolf,hive));}
    @Override public void tick(){
        if(!shouldStayActive()){stop();return;}if(!ai.claim(wolf,resources)){finish("storage_busy",20,false);return;}
        if(mode==NPCWorkMode.BREED){var animal=phase==0?first:second;if(animal.isLoveMode()){if(phase++==1)finish("ready",20,false);return;}if(!work.ready(wolf,animal)){finish("waiting_for_animals",20,false);return;}var movement=navigation.move(animal,1.8);if(!reached(movement))return;wolf.lookAt(animal);if(!work.feed(wolf,animal,first,second,phase)){finish("need_animals_and_food",20,false);return;}if(phase++==1)finish("ready",20,false);return;}
        var movement=mode==NPCWorkMode.SHEEP_SHEARING?navigation.move(first,1.8):navigation.move(hive,2.5);if(!reached(movement))return;
        boolean success;try{success=mode==NPCWorkMode.SHEEP_SHEARING?work.shear(wolf,(Sheep)first):work.honey(wolf,hive);}catch(RuntimeException failure){LoggerFactory.getLogger(NPCHusbandryGoal.class).error("NPC {} failed husbandry work (mode={}, target={}, hive={})",wolf.getUniqueId(),mode,first==null?null:first.getUniqueId(),hive==null?null:hive.getLocation(),failure);finish("work_action_failed",100,false);return;}String reason=ai.state(wolf).status;int delay=success?(mode==NPCWorkMode.SHEEP_SHEARING?10:20):reason.equals("need_smoke")?40:reason.equals("need_shears_in_hand")||reason.equals("need_harvest_tool")?20:100;finish(success?"ready":reason,delay,!success&&delay==100);
    }
    private boolean reached(NPCWorkNavigation.Result movement){if(movement==NPCWorkNavigation.Result.FAILED){finish("unreachable",10,true);return false;}if(movement==NPCWorkNavigation.Result.MOVING){ai.state(wolf).status="walking";return false;}return true;}
    private void finish(String reason,int delay,boolean reject){if(reject){if(first!=null)rejected.put(first.getUniqueId(),Bukkit.getCurrentTick()+200);if(hive!=null)rejected.put(new NPCWorkSite.Pos(hive.getX(),hive.getY(),hive.getZ()),Bukkit.getCurrentTick()+200);}stop();ai.state(wolf).status=reason;nextSearch=Bukkit.getCurrentTick()+delay;}
    @Override public void stop(){navigation.stop();ai.restoreTools(wolf);ai.release(wolf);first=null;second=null;hive=null;resources=List.of();nextSearch=Bukkit.getCurrentTick()+10;}
    @Override public GoalKey<Wolf> getKey(){return ai.husbandryKey;}
    @Override public EnumSet<GoalType> getTypes(){return EnumSet.of(GoalType.MOVE,GoalType.LOOK);}
}
