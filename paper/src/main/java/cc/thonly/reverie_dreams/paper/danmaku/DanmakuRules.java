package cc.thonly.reverie_dreams.paper.danmaku;

import cc.thonly.reverie_dreams.paper.util.Ids;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.momirealms.craftengine.bukkit.api.CraftEngineItems;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.DyedItemColor;
import java.io.IOException;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Color;
import org.bukkit.DyeColor;

/** Source component adapter; inventory identity remains a normal CE item and all overrides live on the stack. */
public final class DanmakuRules {
    public static final NamespacedKey PROPERTIES = new NamespacedKey("reverie_dreams", "danmaku_properties");
    public static final NamespacedKey TRAJECTORY = new NamespacedKey("reverie_dreams", "danmaku_trajectory_config");
    static final Gson JSON = new Gson();
    public record Type(String id, String damageType, float damage, float scale, float speed, boolean tile,
                       boolean infinite, int speedVersion, int durability, int cooldownTicks, float hitboxScale) {}
    public record Properties(String templateId, int count, float damage, String damageType, float scale, float speed,
                             float acceleration, boolean tile, boolean infinite, int speedVersion, float hitboxScale) {
        public float actualSpeed() { return Math.clamp(speedVersion >= 1 ? speed : speed*.5f,0,4); }
        public float collisionScale() { float value=hitboxScale>0?hitboxScale:scale; return Float.isFinite(value)?Math.clamp(value,.25f,4):1; }
        public Properties withCount(int count) { return new Properties(templateId,count,damage,damageType,scale,speed,acceleration,tile,infinite,speedVersion,hitboxScale); }
        public Properties withTemplate(String template) { return new Properties(template,count,damage,damageType,scale,speed,acceleration,tile,infinite,speedVersion,hitboxScale); }
        public Properties withSpeed(float value,int version) { return new Properties(templateId,count,damage,damageType,scale,value,acceleration,tile,infinite,version,hitboxScale); }
        public Properties withDamage(float value) { return new Properties(templateId,count,value,damageType,scale,speed,acceleration,tile,infinite,speedVersion,hitboxScale); }
        public Properties withInfinite(boolean value) { return new Properties(templateId,count,damage,damageType,scale,speed,acceleration,tile,value,speedVersion,hitboxScale); }
    }
    private final Map<String,Type> types=new LinkedHashMap<>();
    private final Map<String,Type> items=new LinkedHashMap<>();
    DanmakuRules() {
        try(var reader=new InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream("/migration/danmaku-domain.json")),StandardCharsets.UTF_8)) {
            JsonObject root=JsonParser.parseReader(reader).getAsJsonObject();
            if(root.get("schemaVersion").getAsInt()!=1)throw new IllegalArgumentException("Unsupported danmaku domain schema");
            for(var value:root.getAsJsonArray("types")) { Type type=JSON.fromJson(value,Type.class); types.put(type.id,type); }
            for(var entry:root.getAsJsonObject("items").entrySet()) items.put(entry.getKey(),types.get(entry.getValue().getAsJsonObject().get("type").getAsString()));
        } catch(IOException failure) { throw new IllegalStateException("Cannot read danmaku domain",failure); }
    }
    public List<Type> types() { return List.copyOf(types.values()); }
    public Type type(ItemStack stack) {
        if(stack==null || stack.isEmpty()) return null;
        var id=CraftEngineItems.getCustomItemId(stack); return id==null?null:items.get(id.asString());
    }
    public Properties defaults(Type type) {
        return new Properties(Ids.MOD_ID + ":single",1,type.damage,type.damageType,type.scale,type.speed,0,type.tile,type.infinite,type.speedVersion,type.hitboxScale);
    }
    public Properties properties(ItemStack stack) {
        Type type=type(stack); if(type==null) throw new IllegalArgumentException("Not a migrated ordinary danmaku");
        String encoded=stack.getPersistentDataContainer().get(PROPERTIES,PersistentDataType.STRING);
        if(encoded==null) return defaults(type);
        JsonObject value=JsonParser.parseString(encoded).getAsJsonObject();
        // Field names match the source component, including legacy missing speed_version=0.
        return new Properties(value.get("templateId").getAsString(),value.get("count").getAsInt(),value.get("damage").getAsFloat(),
                value.has("damage_type")?value.get("damage_type").getAsString():"reverie_dreams:danmaku_generic",
                value.get("scale").getAsFloat(),value.get("speed").getAsFloat(),value.get("acceleration").getAsFloat(),
                value.get("tile").getAsBoolean(),value.get("infinite").getAsBoolean(),value.has("speed_version")?value.get("speed_version").getAsInt():0,
                value.has("hitbox_scale")?value.get("hitbox_scale").getAsFloat():-1);
    }
    public DanmakuMath.Config trajectory(ItemStack stack) {
        String json=stack.getPersistentDataContainer().get(TRAJECTORY,PersistentDataType.STRING);
        if(json==null) return DanmakuMath.Config.EMPTY;
        JsonObject v=JsonParser.parseString(json).getAsJsonObject();
        return new DanmakuMath.Config(integer(v,"count"),decimal(v,"spread"),decimal(v,"radius"),integer(v,"layers"),decimal(v,"spacing"),decimal(v,"forward"),integer(v,"face_outward"));
    }
    private static int integer(JsonObject value,String key) { return value.has(key)?value.get(key).getAsInt():-1; }
    private static float decimal(JsonObject value,String key) { return value.has(key)?value.get(key).getAsFloat():-1; }
    static JsonObject encode(Properties p) {
        JsonObject v=new JsonObject();v.addProperty("templateId",p.templateId);v.addProperty("count",p.count);v.addProperty("damage",p.damage);
        v.addProperty("damage_type",p.damageType);v.addProperty("scale",p.scale);v.addProperty("speed",p.speed);v.addProperty("acceleration",p.acceleration);
        v.addProperty("tile",p.tile);v.addProperty("infinite",p.infinite);v.addProperty("speed_version",p.speedVersion);v.addProperty("hitbox_scale",p.hitboxScale);return v;
    }
    /** Returns a cloned stack preserving durability, colour and unrelated PDC. */
    public ItemStack configure(ItemStack original,Properties properties,DanmakuMath.Config config) {
        if(type(original)==null) throw new IllegalArgumentException("Not an ordinary danmaku");
        ItemStack result=original.clone(); JsonObject c=new JsonObject();
        c.addProperty("count",config.count());c.addProperty("spread",config.spread());c.addProperty("radius",config.radius());c.addProperty("layers",config.layers());
        c.addProperty("spacing",config.spacing());c.addProperty("forward",config.forward());c.addProperty("face_outward",config.faceOutward());
        result.editMeta(meta->{meta.getPersistentDataContainer().set(PROPERTIES,PersistentDataType.STRING,encode(properties).toString());meta.getPersistentDataContainer().set(TRAJECTORY,PersistentDataType.STRING,c.toString());});
        return result;
    }
    public ItemStack create(String type,String colour) {
        String path=type.substring(type.indexOf(':')+1);String id=Ids.MOD_ID + ":danmaku/"+path+(colour==null||colour.equals("default")?"":"/"+colour);
        if(!items.containsKey(id)) throw new IllegalArgumentException("Unknown ordinary danmaku item: "+id);
        var definition=CraftEngineItems.byId(id);if(definition==null) throw new IllegalStateException("CE item is not loaded: "+id);
        return definition.buildBukkitItem();
    }

    /** Exact DanmakuDyeRecipe RGB/brightness blend; keeps damage, trajectory and all unrelated metadata. */
    public ItemStack dye(ItemStack original,List<DyeColor> dyes) {
        if(type(original)==null||dyes.isEmpty())throw new IllegalArgumentException("Danmaku and at least one dye are required");
        int r=0,g=0,b=0,brightness=0,count=0;
        var existing=original.getData(DataComponentTypes.DYED_COLOR);
        if(existing!=null) {var colour=existing.color();r+=colour.getRed();g+=colour.getGreen();b+=colour.getBlue();brightness+=Math.max(colour.getRed(),Math.max(colour.getGreen(),colour.getBlue()));count++;}
        for(var dye:dyes){var colour=dye.getColor();r+=colour.getRed();g+=colour.getGreen();b+=colour.getBlue();brightness+=Math.max(colour.getRed(),Math.max(colour.getGreen(),colour.getBlue()));count++;}
        int rr=r/count,gg=g/count,bb=b/count;float average=brightness/(float)count,maximum=Math.max(rr,Math.max(gg,bb));
        int rgb=maximum==0?0:((int)(rr*average/maximum)<<16)|((int)(gg*average/maximum)<<8)|(int)(bb*average/maximum);
        ItemStack result=original.asOne();result.setData(DataComponentTypes.DYED_COLOR,
                DyedItemColor.dyedItemColor(Color.fromRGB(rgb)));
        result.setData(DataComponentTypes.ITEM_NAME,original.effectiveName().color(TextColor.color(rgb)));
        return result;
    }
}
