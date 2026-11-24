package cc.thonly.reverie_dreams.paper.npc;

import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Wolf;

/** Source incremental128-position scan, reservations, expected-state commit and rejection backoff. */
final class NPCFarmGoal implements Goal<Wolf> {
    private final NPCAiService ai;private final Wolf wolf;private final NPCWorkNavigation navigation;
    private final Map<NPCWorkSite.Pos,Integer> rejected=new HashMap<>();
    private NPCWorkSite site;private int cursor,nextSearch,nextFullScan;private Block target;private BlockData expected;
    NPCFarmGoal(NPCAiService ai,Wolf wolf){this.ai=ai;this.wolf=wolf;navigation=new NPCWorkNavigation(wolf);}
    @Override public boolean shouldActivate(){
        if(!ai.working(wolf,NPCWorkMode.FARM)||Bukkit.getCurrentTick()<nextSearch)return false;
        var newSite=ai.state(wolf).site;if(!newSite.equals(site)){site=newSite;cursor=0;nextFullScan=0;rejected.clear();}
        int now=Bukkit.getCurrentTick();nextSearch=now+5;if(now<nextFullScan)return false;
        rejected.values().removeIf(t->t<=now);boolean seeds=ai.selectedSeed(ai.inventory(wolf))>=0;
        for(int i=0;i<Math.min(128,site.size());i++){
            var pos=site.position(cursor++);if(cursor>=site.size()){cursor=0;nextFullScan=now+20;}
            if(rejected.getOrDefault(pos,Integer.MIN_VALUE)>now){if(cursor==0)break;continue;}
            var block=ai.loaded(wolf,pos);if(block!=null&&ai.available(wolf,block)&&(NPCAiService.crop(block,true)!=null||seeds&&ai.emptyPlot(block))){
                if(ai.claim(wolf,block)){target=block;expected=block.getBlockData();return true;}
            }if(cursor==0)break;
        }ai.state(wolf).status=seeds?"waiting_for_crops":"need_seeds";return false;
    }
    @Override public boolean shouldStayActive(){return target!=null&&ai.working(wolf,NPCWorkMode.FARM)&&site.equals(ai.state(wolf).site)&&target.getWorld().isChunkLoaded(target.getX()>>4,target.getZ()>>4)&&target.getBlockData().matches(expected);}
    @Override public void tick(){
        if(!shouldStayActive()){stop();return;}if(!ai.claim(wolf,target)){finish("storage_busy",20,false);return;}
        var movement=navigation.move(target,2.0);if(movement==NPCWorkNavigation.Result.FAILED){finish("unreachable",10,true);return;}
        if(movement==NPCWorkNavigation.Result.MOVING){ai.state(wolf).status="walking";return;}
        wolf.lookAt(target.getLocation().add(.5,.5,.5));boolean success=ai.farmOnce(wolf.getUniqueId(),ai.service.ownerId(wolf),target);
        String status=ai.state(wolf).status;finish(success?"ready":status,success?8:status.equals("need_matching_seeds")?20:40,!success&&status.equals("need_matching_seeds"));
    }
    private void finish(String reason,int delay,boolean reject){if(reject&&target!=null)rejected.put(new NPCWorkSite.Pos(target.getX(),target.getY(),target.getZ()),Bukkit.getCurrentTick()+200);stop();ai.state(wolf).status=reason;nextSearch=Bukkit.getCurrentTick()+delay;}
    @Override public void stop(){navigation.stop();ai.release(wolf);target=null;expected=null;nextSearch=Bukkit.getCurrentTick()+10;}
    @Override public GoalKey<Wolf> getKey(){return ai.farmKey;}
    @Override public EnumSet<GoalType> getTypes(){return EnumSet.of(GoalType.MOVE,GoalType.LOOK);}
}
