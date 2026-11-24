package cc.thonly.reverie_dreams.paper.npc;

import cc.thonly.reverie_dreams.paper.util.Ids;

import io.papermc.paper.datacomponent.DataComponentTypes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import org.bukkit.Bukkit;
import org.bukkit.GameEvent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.block.Block;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Rabbit;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Wolf;
import org.bukkit.event.entity.EntityEnterLoveModeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import java.util.UUID;
import net.kyori.adventure.sound.Sound.Source;
import org.bukkit.block.Beehive;
import org.bukkit.entity.EntityType;

/** Original animal/hive transactions; goals reserve resources and use actual 24-slot stacks. */
final class NPCHusbandry {
    static final int MAX_ANIMALS=32, FOOD_RESERVE=2;
    record Region(UUID world,EntityType species,int chunkX,int chunkZ) { }
    private final NPCAiService ai;
    NPCHusbandry(NPCAiService ai){this.ai=ai;}
    List<Animals> animals(Wolf wolf){
        var site=ai.state(wolf).site;var center=new Location(wolf.getWorld(),site.x()+.5,site.y()+.5,site.z()+.5);
        return wolf.getWorld().getNearbyEntities(center,site.radius()+.5,site.height()+.5,site.radius()+.5).stream()
            .filter(e->e instanceof Animals).map(e->(Animals)e).filter(e->e.isValid()&&!e.isDead()&&!ai.service.isNPC(e)&&site.contains(e.getLocation())).toList();
    }
    boolean valid(Wolf wolf,Animals animal){return animal!=null&&animal.isValid()&&!animal.isDead()&&!ai.service.isNPC(animal)&&animal.getWorld()==wolf.getWorld()&&ai.state(wolf).site.contains(animal.getLocation())&&animal.isAdult()&&animal.getAge()==0;}
    boolean ready(Wolf wolf,Animals animal){return valid(wolf,animal)&&animal.customName()==null&&!animal.isLoveMode();}
    Predicate<ItemStack> food(Animals animal){return item->item!=null&&!item.isEmpty()&&(animal.isBreedItem(item)||(animal instanceof Pig||animal instanceof Rabbit)&&NPCAiService.itemId(item).equals(Ids.MOD_ID + ":ingredient/white_radish"));}
    int countFood(Wolf wolf,Animals animal){var wanted=food(animal);int count=0;var items=ai.inventory(wolf);for(int i=0;i<24;i++)if((i<18||i>=22)&&wanted.test(items[i]))count+=items[i].getAmount();return count;}
    static boolean withinLimit(int count,int lovers){return count+(lovers+1)/2<MAX_ANIMALS;}
    boolean belowLimit(Wolf wolf,Animals first,Animals second,int phase){
        var same=animals(wolf).stream().filter(a->a.getType()==first.getType()).toList();int lovers=(int)same.stream().filter(Animals::isLoveMode).count();
        int expected=(lovers+1)/2;if(phase==1&&first.isLoveMode()||phase==0&&second.isLoveMode())expected=lovers/2;return same.size()+expected<MAX_ANIMALS;
    }
    List<Object> breedKeys(Wolf wolf,Animals first,Animals second){
        var keys=new ArrayList<Object>();keys.add(first.getUniqueId());keys.add(second.getUniqueId());var site=ai.state(wolf).site;
        for(int x=(site.x()-site.radius())>>4;x<=(site.x()+site.radius())>>4;x++)for(int z=(site.z()-site.radius())>>4;z<=(site.z()+site.radius())>>4;z++)keys.add(new Region(site.worldId(),first.getType(),x,z));return keys;
    }
    boolean feed(Wolf wolf,Animals animal,Animals first,Animals second,int phase){
        if(!ai.working(wolf,NPCWorkMode.BREED)||!valid(wolf,first)||!valid(wolf,second)||!ready(wolf,animal)||wolf.getLocation().distanceSquared(animal.getLocation())>4||!belowLimit(wolf,first,second,phase))return false;
        int hand=ai.tools(wolf).ensure(food(animal));if(hand<0||countFood(wolf,animal)<=FOOD_RESERVE){ai.state(wolf).status="need_animals_and_food";return false;}
        var before=ai.inventory(wolf);var event=new EntityEnterLoveModeEvent(animal,null,600);Bukkit.getPluginManager().callEvent(event);
        if(event.isCancelled()||event.getTicksInLove()<=0||!Arrays.equals(before,ai.inventory(wolf))||!ready(wolf,animal)||!valid(wolf,first)||!valid(wolf,second)||!belowLimit(wolf,first,second,phase)||wolf.getLocation().distanceSquared(animal.getLocation())>4)return false;
        var stack=before[hand];var remainder=stack.getData(DataComponentTypes.USE_REMAINDER);stack.subtract(1);
        ItemStack extra=ItemStack.empty();if(remainder!=null){extra=remainder.transformInto().clone();if(stack.isEmpty()){before[hand]=extra;extra=ItemStack.empty();}else NPCAiService.insertCargo(before,extra);}
        ai.inventory(wolf,before);animal.setLoveModeTicks(event.getTicksInLove());animal.getWorld().spawnParticle(Particle.HEART,animal.getLocation().add(0,animal.getHeight()/2,0),7,.3,.3,.3,0);
        if(!extra.isEmpty())wolf.getWorld().dropItemNaturally(wolf.getLocation(),extra);swing(wolf,hand);ai.action(wolf);return true;
    }
    boolean shear(Wolf wolf,Sheep sheep){
        if(!ai.working(wolf,NPCWorkMode.SHEEP_SHEARING)||!validSheep(wolf,sheep)||wolf.getLocation().distanceSquared(sheep.getLocation())>4)return false;
        int hand=ai.tools(wolf).ensure(item->item.getType()==Material.SHEARS);if(hand<0){ai.state(wolf).status="need_shears_in_hand";return false;}
        sheep.shear(Source.PLAYER);
        if(sheep.readyToBeSheared())return false;
        wolf.damageItemStack(hand==22?EquipmentSlot.HAND:EquipmentSlot.OFF_HAND,1);ai.syncNativeEquipment(wolf);
        wolf.getWorld().sendGameEvent(wolf,GameEvent.SHEAR,sheep.getLocation().toVector());swing(wolf,hand);ai.action(wolf);return true;
    }
    boolean validSheep(Wolf wolf,Sheep sheep){return sheep!=null&&sheep.isValid()&&!sheep.isDead()&&sheep.getWorld()==wolf.getWorld()&&sheep.customName()==null&&sheep.readyToBeSheared()&&ai.state(wolf).site.contains(sheep.getLocation());}
    List<Block> hives(Wolf wolf){
        var site=ai.state(wolf).site;var world=wolf.getWorld();var result=new ArrayList<Block>();
        for(int x=(site.x()-site.radius())>>4;x<=(site.x()+site.radius())>>4;x++)for(int z=(site.z()-site.radius())>>4;z<=(site.z()+site.radius())>>4;z++){
            if(!world.isChunkLoaded(x,z))continue;
            for(var tile:world.getChunkAt(x,z).getTileEntities(false))if(tile instanceof Beehive&&site.contains(tile.getLocation()))result.add(tile.getBlock());
        }return result;
    }
    boolean fullHive(Wolf wolf,Block block){return block!=null&&block.getWorld()==wolf.getWorld()&&block.getWorld().isChunkLoaded(block.getX()>>4,block.getZ()>>4)&&ai.state(wolf).site.contains(block.getLocation())&&block.getState() instanceof Beehive&&block.getBlockData() instanceof org.bukkit.block.data.type.Beehive data&&data.getHoneyLevel()>=data.getMaximumHoneyLevel();}
    boolean readyHive(Wolf wolf,Block block){return fullHive(wolf,block)&&(!ai.state(wolf).requireSmoke||((Beehive)block.getState()).isSedated());}
    Predicate<ItemStack> honeyTool(Wolf wolf){
        String product=ai.state(wolf).honeyProduct;if(product.equals("honeycomb"))return item->item.getType()==Material.SHEARS;if(product.equals("honey"))return item->item.getType()==Material.GLASS_BOTTLE;
        var inventory=ai.inventory(wolf);for(int slot=0;slot<24;slot++)if((slot<18||slot>=22)&&inventory[slot].getType()==Material.SHEARS)return stack->stack.getType()==Material.SHEARS;
        return item->item.getType()==Material.SHEARS||item.getType()==Material.GLASS_BOTTLE;
    }
    boolean honey(Wolf wolf,Block block){
        if(!ai.working(wolf,NPCWorkMode.HONEYCOMB_HARVESTING)||!fullHive(wolf,block)||wolf.getLocation().distanceSquared(block.getLocation().add(.5,.5,.5))>6.25)return false;
        if(!readyHive(wolf,block)){ai.state(wolf).status="need_smoke";return false;}
        int hand=ai.tools(wolf).ensure(honeyTool(wolf));if(hand<0){ai.state(wolf).status="need_harvest_tool";return false;}
        var items=ai.inventory(wolf);var before=block.getBlockData();var next=(org.bukkit.block.data.type.Beehive)before.clone();next.setHoneyLevel(0);
        if(!ai.commit(wolf,block,before,next,NPCSnapshot.copy(items))){ai.state(wolf).status="work_write_refused";return false;}
        boolean smoky=block.getState() instanceof Beehive hive&&hive.isSedated();
        if(items[hand].getType()==Material.SHEARS){
            // Exact target bundled harvest/beehive is three honeycomb. Public LootContext cannot carry BLOCK_STATE/TOOL/BLOCK_ENTITY.
            wolf.getWorld().dropItemNaturally(block.getLocation().add(.5,.5,.5),new ItemStack(Material.HONEYCOMB,3));
            wolf.damageItemStack(hand==22?EquipmentSlot.HAND:EquipmentSlot.OFF_HAND,1);ai.syncNativeEquipment(wolf);
        }else{
            items[hand].subtract(1);var bottle=new ItemStack(Material.HONEY_BOTTLE);insertNonArmor(items,bottle);if(!bottle.isEmpty()&&items[hand].isEmpty()){items[hand]=bottle;bottle=ItemStack.empty();}
            ai.inventory(wolf,items);if(!bottle.isEmpty())wolf.getWorld().dropItemNaturally(wolf.getLocation(),bottle);
        }
        if(!smoky&&block.getState() instanceof Beehive hive)hive.releaseEntities();
        wolf.getWorld().sendGameEvent(wolf,GameEvent.SHEAR,block.getLocation().toVector());wolf.getWorld().playSound(wolf.getLocation(),Sound.ITEM_HONEY_BOTTLE_DRINK,SoundCategory.BLOCKS,1,1);
        swing(wolf,hand);ai.action(wolf);return true;
    }
    static void insertNonArmor(ItemStack[] items,ItemStack stack){
        for(int i=0;i<24&&!stack.isEmpty();i++)if((i<18||i>=22)&&!items[i].isEmpty()&&items[i].isSimilar(stack)){int moved=Math.min(stack.getAmount(),items[i].getMaxStackSize()-items[i].getAmount());if(moved>0){items[i].add(moved);stack.subtract(moved);}}
        NPCAiService.insertCargo(items,stack);
    }
    private static void swing(Wolf wolf,int hand){if(hand==22)wolf.swingMainHand();else wolf.swingOffHand();}
}
