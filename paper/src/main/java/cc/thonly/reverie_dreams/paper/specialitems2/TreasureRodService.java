package cc.thonly.reverie_dreams.paper.specialitems2;

import com.google.gson.JsonObject;
import net.kyori.adventure.text.Component;
import net.momirealms.craftengine.bukkit.api.CraftEngineBlocks;
import net.momirealms.craftengine.bukkit.util.BlockStateUtils;
import net.momirealms.craftengine.core.util.Key;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.EntitiesUnloadEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Material;

/** Exact source scan/grouping with public native glowing BlockDisplays. No client-only OreEspRenderer or NMS. */
public final class TreasureRodService implements Listener {
    public static final NamespacedKey MARKER=new NamespacedKey("reverie_dreams","ore_esp_marker");
    public record Marker(UUID entity,Location location,long expireAt,String ore,int color){}
    private record WorldPosition(UUID world,OreScanRules.Position position){}
    private record Active(BlockDisplay display,String ore,int color,long expireAt){}
    private final Map<WorldPosition,Active> markers=new LinkedHashMap<>();
    private final Set<String> sourceOres=new HashSet<>();
    private final Map<String,Integer> colors=new HashMap<>();
    private final List<String> oreTags=new ArrayList<>();
    TreasureRodService(JsonObject domain){var data=domain.getAsJsonObject("treasureRod");data.getAsJsonArray("sourceCustomOreIds").forEach(x->sourceOres.add(x.getAsString()));data.getAsJsonObject("customColors").entrySet().forEach(x->colors.put(x.getKey(),x.getValue().getAsInt()));data.getAsJsonArray("tags").forEach(x->oreTags.add(x.getAsString()));}
    public List<Marker> snapshots(){SpecialItemsExpansion.thread();return markers.values().stream().map(m->new Marker(m.display.getUniqueId(),m.display.getLocation(),m.expireAt,m.ore,m.color)).toList();}
    public String oreId(Block block){var custom=CraftEngineBlocks.getCustomBlockState(block);if(custom!=null){String id=custom.owner().value().id().asString();if(sourceOres.contains(id))return id;for(String tag:oreTags)if(custom.customBlockState().hasTag(Key.of(tag)))return id;return null;}
        if(block.getType()==Material.NETHER_QUARTZ_ORE||block.getType()==Material.ANCIENT_DEBRIS)return block.getType().getKey().asString();
        for(String tag:oreTags){Tag<Material> definition=Bukkit.getTag(Tag.REGISTRY_BLOCKS,NamespacedKey.fromString(tag),Material.class);if(definition!=null&&definition.isTagged(block.getType()))return block.getType().getKey().asString();}return null;
    }
    private int color(Block block,String id){if(colors.containsKey(id))return colors.get(id);String[] tags={"diamond_ores","emerald_ores","lapis_ores","redstone_ores","gold_ores","iron_ores","copper_ores","coal_ores"};int[] values={0x4AEDD9,0x17DD62,0x3A5BFF,0xFF3B3B,0xFCEE4B,0xE0B096,0xE07A3F,0x9AA0A6};
        for(int i=0;i<tags.length;i++){var tag=Bukkit.getTag(Tag.REGISTRY_BLOCKS,NamespacedKey.minecraft(tags[i]),Material.class);if(tag!=null&&tag.isTagged(block.getType()))return values[i];}
        for(String kind:new String[]{"quartz","netherite_scrap"}){var tag=Bukkit.getTag(Tag.REGISTRY_BLOCKS,new NamespacedKey("c","ores/"+kind),Material.class);if(tag!=null&&tag.isTagged(block.getType()))return kind.equals("quartz")?0xF2ECE4:0x8C6E5F;}
        if(block.getType()==Material.NETHER_QUARTZ_ORE)return 0xF2ECE4;if(block.getType()==Material.ANCIENT_DEBRIS)return 0x8C6E5F;return 0xE8E8E8;
    }
    /** Read-only headless scan API: origin is source player POSITION, not eye position. Requires all source scan chunks already loaded. */
    public OreScanRules.Scan inspect(Location origin){SpecialItemsExpansion.thread();var world=origin.getWorld();int x=origin.getBlockX(),y=origin.getBlockY(),z=origin.getBlockZ();
        // Avoid source's synchronous chunk generation stall; no cost is charged when use() finds an incomplete scan.
        for(int cx=(x-8)>>4;cx<=(x+8)>>4;cx++)for(int cz=(z-8)>>4;cz<=(z+8)>>4;cz++)if(!world.isChunkLoaded(cx,cz))throw new IllegalStateException("Ore scan requires preloaded chunks");
        var ores=new ArrayList<OreScanRules.Ore>();for(int dz=z-8;dz<=z+8;dz++)for(int dy=y-8;dy<=y+4;dy++)for(int dx=x-8;dx<=x+8;dx++){if(dy<world.getMinHeight()||dy>=world.getMaxHeight())continue;var block=world.getBlockAt(dx,dy,dz);String id=oreId(block);if(id!=null)ores.add(new OreScanRules.Ore(new OreScanRules.Position(dx,dy,dz),id,color(block,id)));}
        return OreScanRules.build(ores,origin.getX(),origin.getY(),origin.getZ());
    }
    public void display(Player player,OreScanRules.Scan scan){SpecialItemsExpansion.thread();if(scan.ores().isEmpty()){player.sendMessage(Component.translatable("message.reverie_dreams.treasure_hunting_rod.not_found"));sound(player,false,0);return;}
        var nearest=scan.ores().getFirst();var p=nearest.position();var origin=player.getLocation();player.sendMessage(Component.translatable("message.reverie_dreams.treasure_hunting_rod.find",Component.text(scan.nearestDistance()),Component.text(p.x()-origin.getBlockX()),Component.text(p.y()-origin.getBlockY()),Component.text(p.z()-origin.getBlockZ())).append(Component.space()).append(name(nearest.id())));
        Component distribution=Component.empty();for(int i=0;i<scan.distribution().size();i++){var entry=scan.distribution().get(i);if(i>0)distribution=distribution.append(Component.translatable("message.reverie_dreams.treasure_hunting_rod.separator"));distribution=distribution.append(Component.translatable("message.reverie_dreams.treasure_hunting_rod.distribution.entry",name(entry.id()),Component.text(entry.count())));}player.sendMessage(Component.translatable("message.reverie_dreams.treasure_hunting_rod.distribution",distribution));
        player.sendActionBar(Component.translatable("message.reverie_dreams.treasure_hunting_rod.summary",name(nearest.id()),Component.text(scan.nearestDistance()),Component.text(scan.veins().getFirst().size()),Component.text(scan.ores().size())));
        spawn(player,scan);sound(player,true,scan.nearestDistance());
    }
    public int spawn(Player player,OreScanRules.Scan scan){return spawn(player.getLocation(),scan);}
    /** Shared world presentation does not require a fake player; public API also serves other source scan consumers. */
    public int spawn(Location origin,OreScanRules.Scan scan){SpecialItemsExpansion.thread();var world=origin.getWorld();long expire=world.getGameTime()+100;int count=0;
        for(var ore:scan.targets()){var key=new WorldPosition(world.getUID(),ore.position());var existing=markers.get(key);if(existing!=null&&existing.display.isValid()){markers.put(key,new Active(existing.display,existing.ore,existing.color,expire));count++;continue;}
            var pos=ore.position();var block=world.getBlockAt(pos.x(),pos.y(),pos.z());var custom=CraftEngineBlocks.getCustomBlockState(block);var data=custom==null?block.getBlockData():BlockStateUtils.fromBlockData(custom.visualBlockState().minecraftState());
            var display=world.spawn(new Location(world,pos.x(),pos.y(),pos.z()),BlockDisplay.class,entity->{entity.setBlock(data);entity.setGlowing(true);entity.setGlowColorOverride(Color.fromRGB(ore.color()));entity.setGravity(false);entity.setInvulnerable(true);entity.setPersistent(false);entity.getPersistentDataContainer().set(MARKER,PersistentDataType.INTEGER,1);});
            if(display.isValid()){markers.put(key,new Active(display,ore.id(),ore.color(),expire));count++;}
        }return count;
    }
    private static Component name(String id){return Component.translatable("block."+id.replace(':','.').replace('/','.'));}
    private static void sound(Player player,boolean found,int distance){float pitch=found?Math.clamp(1.6f-distance*.05f,.8f,1.6f):.6f;player.getWorld().playSound(player.getEyeLocation(),found?Sound.BLOCK_NOTE_BLOCK_PLING:Sound.BLOCK_NOTE_BLOCK_BASS,SoundCategory.PLAYERS,found?1:.8f,pitch);}
    void tick(){for(var entry:List.copyOf(markers.entrySet())){var marker=entry.getValue();if(!marker.display.isValid()){markers.remove(entry.getKey());continue;}if(Bukkit.getServer().getServerTickManager().isFrozen(marker.display))continue;var pos=entry.getKey().position;var world=marker.display.getWorld();if(world.getGameTime()>=marker.expireAt||oreId(world.getBlockAt(pos.x(),pos.y(),pos.z()))==null){marker.display.remove();markers.remove(entry.getKey());}}}
    void disable(){for(var marker:markers.values())marker.display.remove();markers.clear();}
    @EventHandler public void unload(EntitiesUnloadEvent event){for(Entity entity:event.getEntities())if(entity.getPersistentDataContainer().has(MARKER)){markers.entrySet().removeIf(x->x.getValue().display.getUniqueId().equals(entity.getUniqueId()));entity.remove();}}
}
