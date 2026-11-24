package cc.thonly.reverie_dreams.paper.npc;
import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.entity.Wolf;

/** Native SMELT + real classification planner; re-resolve containers/rules after navigation. */
final class NPCStorageGoal implements Goal<Wolf>{
 private final NPCAiService ai;private final Wolf wolf;private final NPCStorageWork storage;private final NPCWorkNavigation navigation;
 private NPCWorkMode mode;private NPCWorkSite site;private NPCStorageWork.FurnaceJob furnace;private NPCStorageRules.Job job;private int nextSearch;
 private final Map<NPCAiService.Resource,Integer> rejected=new HashMap<>();
 NPCStorageGoal(NPCAiService ai,Wolf wolf){this.ai=ai;this.wolf=wolf;storage=ai.storage;navigation=new NPCWorkNavigation(wolf);}
 private List<Object> resources(){return furnace!=null?storage.keys(furnace.access()):job!=null?storage.keys(job):List.of();}
 @Override public boolean shouldActivate(){
  var state=ai.state(wolf);if((state.mode!=NPCWorkMode.SMELT&&state.mode!=NPCWorkMode.CHEST_CLASSIFICATION)||!ai.working(wolf,state.mode))return false;
  int now=Bukkit.getCurrentTick();if(mode!=state.mode||!state.site.equals(site)){mode=state.mode;site=state.site;furnace=null;job=null;rejected.clear();storage.forget(wolf);nextSearch=now+(mode==NPCWorkMode.CHEST_CLASSIFICATION?Math.floorMod(wolf.getEntityId(),20):0);}
  if(now<nextSearch)return false;nextSearch=now+20;rejected.values().removeIf(until->until<=now);
  if(mode==NPCWorkMode.SMELT)furnace=storage.furnacePlan(wolf,storage.furnaces(wolf).stream().filter(access->!rejected.containsKey(ai.resource(access.block()))).toList());
  else {var plan=storage.sortPlan(wolf,store->!rejected.containsKey(ai.resource(store.access().block())));job=plan.job();state.status=plan.status();if(job==null){nextSearch=now+(plan.status().equals("sorting_finished")?40:20);return false;}}
  if(furnace==null&&job==null)return false;if(!ai.claim(wolf,resources())){furnace=null;job=null;return false;}return true;
 }
 @Override public boolean shouldStayActive(){return ai.working(wolf,mode)&&site.equals(ai.state(wolf).site)&&(furnace!=null?storage.liveFurnace(wolf,furnace):job!=null&&storage.liveSort(wolf,job));}
 @Override public void tick(){
  if(!shouldStayActive()){stop();return;}if(!ai.claim(wolf,resources())){finish("storage_busy",20);return;}
  var parts=furnace!=null?furnace.access().parts():job.visiting().access().parts();var move=navigation.moveParts(parts,furnace!=null?2.5:3);
  if(move==NPCWorkNavigation.Result.FAILED){for(var key:resources())if(key instanceof NPCAiService.Resource resource)rejected.put(resource,Bukkit.getCurrentTick()+200);finish("unreachable",10);return;}
  if(move!=NPCWorkNavigation.Result.REACHED){ai.state(wolf).status="walking";return;}
  wolf.lookAt(parts.getFirst().getLocation().add(.5,.5,.5));
  if(furnace!=null){storage.furnace(wolf,furnace);finish(ai.state(wolf).status,10);return;}
  var outcome=storage.sort(wolf,job);if(outcome.next()!=null){ai.release(wolf);job=outcome.next();furnace=null;ai.claim(wolf,resources());navigation.stop();}else finish(outcome.status(),10);
 }
 private void finish(String reason,int delay){stop();ai.state(wolf).status=reason;nextSearch=Bukkit.getCurrentTick()+delay;}
 @Override public void stop(){navigation.stop();ai.release(wolf);storage.forget(wolf);furnace=null;job=null;nextSearch=Bukkit.getCurrentTick()+10;}
 @Override public GoalKey<Wolf> getKey(){return ai.storageKey;}
 @Override public EnumSet<GoalType> getTypes(){return EnumSet.of(GoalType.MOVE,GoalType.LOOK);}
}
