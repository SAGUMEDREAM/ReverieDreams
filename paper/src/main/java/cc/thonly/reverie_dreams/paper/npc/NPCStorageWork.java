package cc.thonly.reverie_dreams.paper.npc;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Wolf;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.ItemStack;
import java.util.ArrayList;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import net.momirealms.craftengine.bukkit.api.CraftEngineItems;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Sound;
import org.bukkit.plugin.java.JavaPlugin;

/** Real furnace jobs and source classification shipment bookkeeping; no synthetic production timers. */
final class NPCStorageWork {
 record FurnaceJob(NPCContainerWorld.Access access,int cargoSlot,int furnaceSlot,boolean collecting,ItemStack sample){FurnaceJob{sample=sample.clone();}}
 record SortOutcome(int moved,NPCStorageRules.Job next,String status){}
 private final NPCAiService ai;final NPCStorageRules rules;private final NPCInventoryTransfer transfer;
 private final Map<UUID,NPCStorageRules.Job> shipments=new HashMap<>();
 NPCStorageWork(JavaPlugin plugin,NPCAiService ai){this.ai=ai;rules=new NPCStorageRules(plugin);transfer=new NPCInventoryTransfer(ai);}
 List<NPCContainerWorld.Access> furnaces(Wolf wolf){return NPCContainerWorld.positions(wolf.getWorld(),ai.state(wolf).site).stream().map(NPCContainerWorld::furnace).filter(Objects::nonNull).sorted(Comparator.comparingDouble(a->a.block().getLocation().add(.5,.5,.5).distanceSquared(wolf.getLocation()))).toList();}
 boolean available(Wolf wolf,NPCContainerWorld.Access access){return access.parts().stream().allMatch(block->ai.available(wolf,block));}
 List<Object> keys(NPCContainerWorld.Access access){return access.parts().stream().map(ai::resource).map(key->(Object)key).toList();}
 List<Object> keys(NPCStorageRules.Job job){var keys=new ArrayList<Object>(keys(job.destination().access()));if(job.pickup())keys.addAll(keys(job.source().access()));return keys;}
 static boolean fuel(FurnaceInventory inventory,ItemStack stack){if(stack==null||stack.isEmpty())return false;var id=CraftEngineItems.getCustomItemId(stack);if(id!=null){var definition=CraftEngineItems.byId(id);return definition!=null&&definition.settings().fuelTime()>0;}return inventory.isFuel(stack);}
 private static boolean room(FurnaceInventory inventory,int slot,ItemStack stack){var current=inventory.getItem(slot);return (current==null||current.isEmpty()||current.isSimilar(stack))&&(current==null?0:current.getAmount())<Math.min(inventory.getMaxStackSize(),stack.getMaxStackSize());}
 FurnaceJob furnacePlan(Wolf wolf,List<NPCContainerWorld.Access> furnaces){
  if(furnaces.isEmpty()){ai.state(wolf).status="no_furnace";return null;}var cargo=ai.inventory(wolf);boolean blocked=false;
  for(var access:furnaces)if(available(wolf,access))for(int slot=2;slot>=1;slot--){var inv=(FurnaceInventory)access.inventory();var stack=inv.getItem(slot);if(stack==null||stack.isEmpty()||slot==1&&fuel(inv,stack))continue;if(NPCStorageRules.capacity(cargo,stack,18,99)==0){blocked=true;continue;}return new FurnaceJob(access,-1,slot,true,stack);}
  for(var access:furnaces)if(available(wolf,access)){var inv=(FurnaceInventory)access.inventory();var fuel=inv.getFuel();if(inv.canSmelt(inv.getSmelting())&&(fuel==null?0:fuel.getAmount())<8)for(int slot=0;slot<18;slot++)if(!cargo[slot].isEmpty()&&fuel(inv,cargo[slot])&&room(inv,1,cargo[slot]))return new FurnaceJob(access,slot,1,false,cargo[slot]);}
  for(var access:furnaces)if(available(wolf,access)){var inv=(FurnaceInventory)access.inventory();for(int slot=0;slot<18;slot++)if(!cargo[slot].isEmpty()&&inv.canSmelt(cargo[slot])&&room(inv,0,cargo[slot]))return new FurnaceJob(access,slot,0,false,cargo[slot]);}
  if(blocked)ai.state(wolf).status="inventory_full";else if(furnaces.stream().anyMatch(a->{var inv=(FurnaceInventory)a.inventory();var fuel=inv.getFuel();return inv.canSmelt(inv.getSmelting())&&(fuel==null||fuel.isEmpty());}))ai.state(wolf).status="need_fuel";else if(furnaces.stream().allMatch(a->{var item=((FurnaceInventory)a.inventory()).getSmelting();return item==null||item.isEmpty();}))ai.state(wolf).status="need_smelt_input";else ai.state(wolf).status="waiting_for_smelting";return null;
 }
 boolean liveFurnace(Wolf wolf,FurnaceJob job){return ai.working(wolf,NPCWorkMode.SMELT)&&ai.state(wolf).site.contains(job.access.block().getLocation())&&job.access.same(NPCContainerWorld.furnace(job.access.block()));}
 int furnace(Wolf wolf,FurnaceJob job){
  if(!liveFurnace(wolf,job))return 0;var live=NPCContainerWorld.furnace(job.access.block());var inv=(FurnaceInventory)live.inventory();
  if(job.collecting&&job.furnaceSlot==1&&fuel(inv,inv.getFuel()))return 0;
  int moved;if(job.collecting)moved=transfer.move(wolf,live,false,job.furnaceSlot,job.sample,job.sample.getAmount(),slot->slot<18,()->liveFurnace(wolf,job)&&(job.furnaceSlot!=1||!fuel(inv,inv.getFuel())));
  else {var cargo=ai.inventory(wolf);if(job.cargoSlot<0||!cargo[job.cargoSlot].isSimilar(job.sample))return 0;BooleanSupplier valid=()->liveFurnace(wolf,job)&&(job.furnaceSlot==0?inv.canSmelt(ai.inventory(wolf)[job.cargoSlot]):fuel(inv,ai.inventory(wolf)[job.cargoSlot])&&inv.canSmelt(inv.getSmelting()));moved=transfer.move(wolf,live,true,job.cargoSlot,job.sample,cargo[job.cargoSlot].getAmount(),slot->slot==job.furnaceSlot,valid);}
  ai.state(wolf).status=moved>0?"ready":job.collecting?"inventory_full":"waiting_for_smelting";if(moved>0){wolf.swingMainHand();ai.action(wolf);}return moved;
 }
 NPCStorageRules.Plan sortPlan(Wolf wolf){return sortPlan(wolf,store->true);}
 NPCStorageRules.Plan sortPlan(Wolf wolf,Predicate<NPCStorageRules.Store> filter){var previous=shipments.get(wolf.getUniqueId());if(previous!=null)return new NPCStorageRules.Plan(previous,"ready");return rules.plan(ai.inventory(wolf),rules.scan(wolf.getWorld(),ai.state(wolf).site),ai.state(wolf).storageSettings,wolf.getLocation(),store->filter.test(store)&&available(wolf,store.access()));}
 NPCStorageRules.Store live(Wolf wolf,NPCStorageRules.Store expected){var access=NPCContainerWorld.storage(expected.access().block());return expected.access().same(access)&&access.parts().stream().allMatch(b->ai.state(wolf).site.contains(b.getLocation()))?rules.store(access):null;}
 boolean liveSort(Wolf wolf,NPCStorageRules.Job job){if(!ai.working(wolf,NPCWorkMode.CHEST_CLASSIFICATION))return false;var destination=live(wolf,job.destination());var source=job.pickup()?live(wolf,job.source()):null;return destination!=null&&(!job.pickup()||source!=null&&source.rule().mode()==NPCStorageRules.Mode.INPUT)&&rules.accepts(destination,job.sample(),ai.state(wolf).storageSettings);}
 SortOutcome sort(Wolf wolf,NPCStorageRules.Job job){
  if(!liveSort(wolf,job)){forget(wolf);return new SortOutcome(0,null,"no_matching_storage");}var settings=ai.state(wolf).storageSettings;var destination=live(wolf,job.destination());int moved;NPCStorageRules.Job next=null;
  if(job.pickup()){
   var source=live(wolf,job.source());int slot=NPCStorageRules.cargoSlot(ai.inventory(wolf),job.sample());if(slot<0)return new SortOutcome(0,null,"inventory_full");
   int amount=Math.min(job.amount(),NPCStorageRules.capacity(NPCSnapshot.copy(destination.access().inventory().getContents()),job.sample(),destination.access().inventory().getSize(),destination.access().inventory().getMaxStackSize()));
   moved=transfer.move(wolf,source.access(),false,job.slot(),job.sample(),amount,i->i==slot,()->liveSort(wolf,job));
   if(moved>0)next=new NPCStorageRules.Job(null,destination,slot,job.sample(),moved,true);
  }else {
   if(job.slot()<0||job.slot()>=18){forget(wolf);return new SortOutcome(0,null,"sorting_finished");}
   int amount=job.routed()?job.amount():Math.min(job.amount(),NPCStorageRules.exportable(ai.inventory(wolf),job.slot(),settings));
   moved=transfer.move(wolf,destination.access(),true,job.slot(),job.sample(),amount,i->i<destination.access().inventory().getSize(),()->liveSort(wolf,job));
  }
  if(next==null)forget(wolf);else shipments.put(wolf.getUniqueId(),next);String status=moved>0?"ready":job.pickup()?"no_matching_storage":"storage_full";ai.state(wolf).status=status;if(moved>0){wolf.swingMainHand();wolf.getWorld().playSound(wolf.getLocation(),Sound.BLOCK_CHEST_OPEN,1,1);ai.action(wolf);}return new SortOutcome(moved,next,status);
 }
 boolean reached(Wolf wolf,NPCContainerWorld.Access access,double reach){for(var part:access.parts()){var center=part.getLocation().add(.5,.5,.5);if(center.getWorld()!=wolf.getWorld()||center.distanceSquared(wolf.getLocation())>reach*reach)continue;var delta=center.toVector().subtract(wolf.getEyeLocation().toVector());var hit=delta.lengthSquared()<1.0e-9?null:wolf.getWorld().rayTraceBlocks(wolf.getEyeLocation(),delta,delta.length(),FluidCollisionMode.NEVER,true);if(hit==null||hit.getHitBlock()!=null&&access.parts().contains(hit.getHitBlock()))return true;}return false;}
 void forget(Wolf wolf){shipments.remove(wolf.getUniqueId());}
}
