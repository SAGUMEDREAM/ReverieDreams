package cc.thonly.reverie_dreams.paper.npc;
import java.util.Arrays;
import java.util.function.BooleanSupplier;
import java.util.function.IntPredicate;
import org.bukkit.Bukkit;
import org.bukkit.entity.Wolf;
import org.bukkit.inventory.ItemStack;
import net.momirealms.craftengine.bukkit.plugin.BukkitCraftEngine;
import net.momirealms.craftengine.libraries.antigrieflib.Flag;
import org.bukkit.inventory.Inventory;

/** Single real source stack, merge-before-empty staging, optimistic commit and exact changed-slot rollback. */
final class NPCInventoryTransfer {
 private final NPCAiService ai;NPCInventoryTransfer(NPCAiService ai){this.ai=ai;}
 private static void write(Inventory inventory,ItemStack[] before,ItemStack[] after){for(int i=0;i<after.length;i++)if(!before[i].equals(after[i]))inventory.setItem(i,after[i].isEmpty()?null:after[i].clone());}
 private static void rollback(Inventory inventory,ItemStack[] before,ItemStack[] after){for(int i=0;i<after.length;i++)if(!before[i].equals(after[i])){var current=inventory.getItem(i);if((current==null?ItemStack.empty():current).equals(after[i]))inventory.setItem(i,before[i].isEmpty()?null:before[i].clone());}}
 int move(Wolf wolf,NPCContainerWorld.Access world,boolean fromNPC,int sourceSlot,ItemStack expected,int requested,IntPredicate destinationSlots,BooleanSupplier stillValid){
  if(requested<=0||!stillValid.getAsBoolean())return 0;var owner=Bukkit.getPlayer(ai.service.ownerId(wolf));if(owner!=null&&world.parts().stream().anyMatch(block->!BukkitCraftEngine.instance().antiGriefProvider().test(owner,Flag.OPEN_CONTAINER,block.getLocation())))return 0;
  var oldNPC=ai.inventory(wolf);var oldWorld=NPCSnapshot.copy(world.inventory().getContents());var npc=NPCSnapshot.copy(oldNPC);var container=NPCSnapshot.copy(oldWorld);var from=fromNPC?npc:container;var to=fromNPC?container:npc;
  if(sourceSlot<0||sourceSlot>=from.length||fromNPC&&sourceSlot>=18||from[sourceSlot].isEmpty()||!from[sourceSlot].isSimilar(expected))return 0;
  var source=from[sourceSlot];int remaining=Math.min(requested,source.getAmount()),moved=0,max=fromNPC?world.inventory().getMaxStackSize():99;
  for(int pass=0;pass<2;pass++)for(int slot=0;slot<to.length&&remaining>0;slot++){
   if(!destinationSlots.test(slot)||!fromNPC&&slot>=18||(pass==0)==to[slot].isEmpty()||!to[slot].isEmpty()&&!to[slot].isSimilar(source))continue;
   int amount=Math.min(remaining,Math.max(0,Math.min(max,source.getMaxStackSize())-to[slot].getAmount()));if(amount<=0)continue;to[slot]=source.asQuantity(to[slot].getAmount()+amount);remaining-=amount;moved+=amount;
  }if(moved<=0)return 0;from[sourceSlot]=source.getAmount()==moved?ItemStack.empty():source.asQuantity(source.getAmount()-moved);
  var event=new NPCInventoryTransferEvent(wolf.getUniqueId(),world.block(),fromNPC,source.asQuantity(moved));Bukkit.getPluginManager().callEvent(event);
  if(event.isCancelled()||!stillValid.getAsBoolean()||!Arrays.equals(oldNPC,ai.inventory(wolf))||!Arrays.equals(oldWorld,NPCSnapshot.copy(world.inventory().getContents())))return 0;
  try{
   write(world.inventory(),oldWorld,container);
   if(!stillValid.getAsBoolean()||!Arrays.equals(container,NPCSnapshot.copy(world.inventory().getContents()))||!Arrays.equals(oldNPC,ai.inventory(wolf))){rollback(world.inventory(),oldWorld,container);world.dirty().run();return 0;}
   ai.inventory(wolf,npc);
   if(!Arrays.equals(npc,ai.inventory(wolf))||!Arrays.equals(container,NPCSnapshot.copy(world.inventory().getContents())))throw new IllegalStateException("NPC inventory transaction changed during commit");
   world.dirty().run();return moved;
  }catch(RuntimeException failure){if(Arrays.equals(npc,ai.inventory(wolf)))ai.inventory(wolf,oldNPC);rollback(world.inventory(),oldWorld,container);world.dirty().run();throw failure;}
 }
}
