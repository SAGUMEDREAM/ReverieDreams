package cc.thonly.reverie_dreams.paper.npc;
import java.util.List;
import org.bukkit.NamespacedKey;
/** Current source sorting defaults. Ordered category allowlist; arbitrary shared tags never infer a category. */
public record NPCStorageSettings(boolean claimEmpty,int reserveFood,int reserveTools,List<String> categoryTags){
 public NPCStorageSettings {if(reserveFood<0||reserveFood>2304||reserveTools<0||reserveTools>2304)throw new IllegalArgumentException("Invalid storage reserves");categoryTags=List.copyOf(categoryTags);if(categoryTags.size()>64||categoryTags.stream().anyMatch(s->NamespacedKey.fromString(s)==null))throw new IllegalArgumentException("Invalid category tags");}
 public static NPCStorageSettings defaults(){return new NPCStorageSettings(true,16,1,List.of("minecraft:logs","minecraft:planks","minecraft:wool","minecraft:saplings","minecraft:coals","c:ores","c:ingots","c:nuggets","c:gems"));}
}
