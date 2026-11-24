package cc.thonly.reverie_dreams.paper.npc;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import io.papermc.paper.datacomponent.DataComponentTypes;
import net.momirealms.craftengine.bukkit.api.CraftEngineItems;
import net.momirealms.craftengine.core.util.Key;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Material;
import org.bukkit.entity.ItemFrame;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.Block;

/** Source StorageRule/Planner semantics on detached real inventory snapshots. */
final class NPCStorageRules {
 enum Mode{AUTO,ITEM,CATEGORY,INPUT,OVERFLOW,CONFLICT}
 record Sample(ItemStack item,int rotation){Sample{item=item.asOne();}Mode mode(){return switch(Math.floorMod(rotation,8)/2){case 0->Mode.ITEM;case 1->Mode.CATEGORY;case 2->Mode.INPUT;default->Mode.OVERFLOW;};}}
 record Rule(Mode mode,List<Sample> samples){Rule{samples=List.copyOf(samples);}}
 record Store(NPCContainerWorld.Access access,Rule rule){}
 record Job(Store source,Store destination,int slot,ItemStack sample,int amount,boolean routed){Job{sample=sample.asOne();}boolean pickup(){return source!=null;}Store visiting(){return pickup()?source:destination;}}
 record Plan(Job job,String status){}
 private final Map<String,List<String>> additions=new HashMap<>();
 NPCStorageRules(JavaPlugin plugin){try(var in=new InputStreamReader(Objects.requireNonNull(plugin.getResource("migration/npc-storage-domain.json")),StandardCharsets.UTF_8)){var tags=JsonParser.parseReader(in).getAsJsonObject().getAsJsonObject("sourceCategoryTagAdditions");for(var entry:tags.entrySet()){var values=new ArrayList<String>();for(var value:entry.getValue().getAsJsonArray())values.add(value.getAsString());additions.put(entry.getKey(),List.copyOf(values));}}catch(IOException failure){throw new IllegalStateException("Cannot load NPC source storage tags",failure);}}
 static Rule rule(List<Sample> samples){var nonempty=samples.stream().filter(s->!s.item.isEmpty()).toList();if(nonempty.isEmpty())return new Rule(Mode.AUTO,List.of());var mode=nonempty.getFirst().mode();return new Rule(nonempty.stream().anyMatch(s->s.mode()!=mode)?Mode.CONFLICT:mode,nonempty);}
 List<Store> scan(World world,NPCWorkSite area){
  var positions=NPCContainerWorld.positions(world,area);var seen=new HashSet<Block>();var stores=new ArrayList<Store>();
  for(var block:positions){if(seen.contains(block))continue;var access=NPCContainerWorld.storage(block);if(access==null||access.parts().stream().anyMatch(p->!area.contains(p.getLocation())))continue;seen.addAll(access.parts());stores.add(store(access));}
  return stores;
 }
 Store store(NPCContainerWorld.Access access){
  var samples=new ArrayList<Sample>();for(var part:access.parts())for(var entity:part.getWorld().getNearbyEntities(part.getLocation().add(.5,.5,.5),2.5,2.5,2.5))if(entity instanceof ItemFrame frame&&!frame.isDead()&&frame.getLocation().getBlock().getRelative(frame.getAttachedFace()).equals(part))samples.add(new Sample(frame.getItem(),frame.getRotation().ordinal()));
  return new Store(access,rule(samples));
 }
 private boolean tag(ItemStack item,String name){return tag(item,name,new HashSet<>());}
 private boolean tag(ItemStack item,String name,Set<String> visited){
  if(!visited.add(name))return false;String id=NPCAiService.itemId(item);for(var entry:additions.getOrDefault(name,List.of()))if(entry.equals(id)||entry.startsWith("#")&&tag(item,entry.substring(1),visited))return true;
  var custom=CraftEngineItems.getCustomItemId(item);if(custom!=null){var definition=CraftEngineItems.byId(custom);return definition!=null&&definition.settings().tags().contains(Key.of(name));}
  var key=NamespacedKey.fromString(name);var tag=key==null?null:Bukkit.getTag(Tag.REGISTRY_ITEMS,key,Material.class);return tag!=null&&tag.isTagged(item.getType());
 }
 boolean accepts(Store store,ItemStack stack,NPCStorageSettings settings){
  if(stack.isEmpty()||store.rule.mode==Mode.INPUT||store.rule.mode==Mode.CONFLICT)return false;if(store.rule.mode==Mode.OVERFLOW)return true;
  if(store.rule.mode==Mode.ITEM||store.rule.mode==Mode.CATEGORY){for(var sample:store.rule.samples){if(store.rule.mode==Mode.ITEM){if(NPCAiService.itemId(sample.item).equals(NPCAiService.itemId(stack)))return true;continue;}boolean categorized=false;for(var name:settings.categoryTags())if(tag(sample.item,name)){if(tag(stack,name))return true;categorized=true;break;}if(!categorized&&NPCAiService.itemId(sample.item).equals(NPCAiService.itemId(stack)))return true;}return false;}
  boolean empty=true;for(var existing:NPCSnapshot.copy(store.access.inventory().getContents())){if(existing.isEmpty())continue;empty=false;if(!existing.isSimilar(stack))return false;}return !empty||settings.claimEmpty();
 }
 static int capacity(ItemStack[] items,ItemStack stack,int limit,int max){int total=0;for(int i=0;i<Math.min(limit,items.length);i++)if(items[i].isEmpty()||items[i].isSimilar(stack))total+=Math.max(0,Math.min(max,stack.getMaxStackSize())-items[i].getAmount());return total;}
 static boolean merge(ItemStack[] items,ItemStack stack,int max){for(var item:items)if(!item.isEmpty()&&item.isSimilar(stack)&&item.getAmount()<Math.min(max,stack.getMaxStackSize()))return true;return false;}
 int priority(Store store,ItemStack stack){if(store.rule.mode==Mode.OVERFLOW)return 4;var items=NPCSnapshot.copy(store.access.inventory().getContents());if(merge(items,stack,store.access.inventory().getMaxStackSize()))return 0;if(store.rule.mode==Mode.ITEM||store.rule.mode==Mode.CATEGORY)return 1;return Arrays.stream(items).allMatch(ItemStack::isEmpty)?3:2;}
 static int exportable(ItemStack[] cargo,int slot,NPCStorageSettings settings){if(slot<0||slot>=18)return 0;var stack=cargo[slot];if(stack.isEmpty())return 0;int reserve=stack.hasData(DataComponentTypes.FOOD)?settings.reserveFood():stack.hasData(DataComponentTypes.MAX_DAMAGE)?settings.reserveTools():0;for(int i=0;i<slot&&reserve>0;i++)if(NPCAiService.itemId(cargo[i]).equals(NPCAiService.itemId(stack)))reserve-=cargo[i].getAmount();return Math.max(0,stack.getAmount()-Math.max(0,reserve));}
 Store destination(List<Store> stores,ItemStack stack,NPCStorageSettings settings,Location position,Predicate<Store> available){return stores.stream().filter(s->available.test(s)&&accepts(s,stack,settings)&&capacity(NPCSnapshot.copy(s.access.inventory().getContents()),stack,s.access.inventory().getSize(),s.access.inventory().getMaxStackSize())>0).min(Comparator.<Store>comparingInt(s->priority(s,stack)).thenComparingDouble(s->s.access.block().getLocation().add(.5,.5,.5).distanceSquared(position))).orElse(null);}
 Plan plan(ItemStack[] cargo,List<Store> stores,NPCStorageSettings settings,Location position,Predicate<Store> available){
  if(stores.isEmpty())return new Plan(null,"no_chest");boolean export=false,matching=false,busy=false;
  for(int slot=0;slot<18;slot++){int amount=exportable(cargo,slot,settings);if(amount<=0)continue;export=true;var stack=cargo[slot];var destination=destination(stores,stack,settings,position,available);if(destination!=null)return new Plan(new Job(null,destination,slot,stack,amount,false),"ready");for(var store:stores)if(accepts(store,stack,settings)){matching=true;busy|=!available.test(store)&&capacity(NPCSnapshot.copy(store.access.inventory().getContents()),stack,store.access.inventory().getSize(),store.access.inventory().getMaxStackSize())>0;}}
  boolean input=false,needsSpace=false;
  for(var source:stores.stream().sorted(Comparator.comparingDouble(s->s.access.block().getLocation().add(.5,.5,.5).distanceSquared(position))).toList()){
   if(source.rule.mode!=Mode.INPUT)continue;if(!available.test(source)){busy|=!source.access.inventory().isEmpty();continue;}
   var contents=NPCSnapshot.copy(source.access.inventory().getContents());for(int slot=0;slot<contents.length;slot++){var stack=contents[slot];if(stack.isEmpty())continue;input=true;var destination=destination(stores,stack,settings,position,available);int room=capacity(cargo,stack,18,99);for(var store:stores)if(accepts(store,stack,settings)){matching=true;busy|=!available.test(store)&&capacity(NPCSnapshot.copy(store.access.inventory().getContents()),stack,store.access.inventory().getSize(),store.access.inventory().getMaxStackSize())>0;}needsSpace|=destination!=null&&room<=0;if(destination==null||room<=0)continue;int amount=Math.min(stack.getAmount(),Math.min(room,capacity(NPCSnapshot.copy(destination.access.inventory().getContents()),stack,destination.access.inventory().getSize(),destination.access.inventory().getMaxStackSize())));return new Plan(new Job(source,destination,slot,stack,amount,true),"ready");}
  }
  if(busy)return new Plan(null,"storage_busy");if(!export&&!input)return new Plan(null,"sorting_finished");if(stores.stream().anyMatch(s->s.rule.mode==Mode.CONFLICT))return new Plan(null,"storage_rule_conflict");if(export)return new Plan(null,matching?"storage_full":"no_matching_storage");return new Plan(null,needsSpace?"inventory_full":matching?"storage_full":"no_matching_storage");
 }
 static int cargoSlot(ItemStack[] cargo,ItemStack sample){for(int pass=0;pass<2;pass++)for(int i=0;i<18;i++)if((pass==0)!=cargo[i].isEmpty()&&(cargo[i].isEmpty()||cargo[i].isSimilar(sample))&&cargo[i].getAmount()<Math.min(99,sample.getMaxStackSize()))return i;return -1;}
}
