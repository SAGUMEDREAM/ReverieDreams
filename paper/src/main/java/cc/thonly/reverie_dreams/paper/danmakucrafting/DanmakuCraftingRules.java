package cc.thonly.reverie_dreams.paper.danmakucrafting;

import cc.thonly.reverie_dreams.paper.util.Ids;

import cc.thonly.reverie_dreams.paper.danmaku.DanmakuMath;
import cc.thonly.reverie_dreams.paper.danmaku.DanmakuModule;
import cc.thonly.reverie_dreams.paper.danmaku.DanmakuRules;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.UseCooldown;
import net.kyori.adventure.key.Key;
import net.momirealms.craftengine.bukkit.api.CraftEngineItems;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

/** Raw source rows and source component adapters. All result building completes before any slot debit. */
@SuppressWarnings("UnstableApiUsage")
public final class DanmakuCraftingRules {
    public static final NamespacedKey SHAPE=new NamespacedKey("reverie_dreams","shape");
    public record Plan(String recipeId,ItemStack output,int[] costs) {}
    public record RepairPreview(ItemStack result,int materialCount,int durabilityRestored) {}
    private final JsonObject domain;
    private final List<JsonObject> recipes=new ArrayList<>(),shapes=new ArrayList<>();
    DanmakuCraftingRules() {
        try(var reader=new InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream("/migration/danmakucrafting-domain.json")),StandardCharsets.UTF_8)) {
            domain=JsonParser.parseReader(reader).getAsJsonObject();
            if(domain.get("schemaVersion").getAsInt()!=1)throw new IllegalArgumentException("Unsupported danmaku crafting domain");
            for(var value:domain.getAsJsonArray("tableRecipes"))recipes.add(value.getAsJsonObject());
            for(var value:domain.getAsJsonArray("shapeRecipes"))shapes.add(value.getAsJsonObject());
        } catch(IOException failure) {throw new IllegalStateException("Cannot read danmaku crafting recipes",failure);}
    }
    public List<JsonObject> tableRecipes(){return recipes.stream().map(JsonObject::deepCopy).toList();}
    public List<JsonObject> shapeRecipes(){return shapes.stream().map(JsonObject::deepCopy).toList();}
    public List<String> templates(){return domain.getAsJsonObject("templateConfigs").keySet().stream().toList();}
    public static String id(ItemStack item){if(empty(item))return "minecraft:air";var id=CraftEngineItems.getCustomItemId(item);return id==null?item.getType().getKey().asString():id.asString();}
    public static ItemStack create(String id){var definition=CraftEngineItems.byId(id);return definition==null?null:definition.buildBukkitItem();}
    public ItemStack createCreator(String type){
        String outputId=type.contains(":")?type:Ids.MOD_ID + ":danmaku/"+type;
        JsonObject row=shapes.stream().filter(s->s.getAsJsonObject("result").getAsJsonObject("components").getAsJsonObject("reverie_dreams:shape").get("id").getAsString().equals(outputId)).findFirst().orElse(null);
        if(row==null)return null;
        ItemStack result=create(Ids.MOD_ID + ":danmaku_recipe_creator");if(result==null)return null;
        JsonObject component=row.getAsJsonObject("result").getAsJsonObject("components").getAsJsonObject("reverie_dreams:shape");
        result.editMeta(meta->meta.getPersistentDataContainer().set(SHAPE,PersistentDataType.STRING,component.toString()));
        result.setData(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE,true);return result;
    }
    public boolean[][] shapeFor(String type){
        String outputId=type.contains(":")?type:Ids.MOD_ID + ":danmaku/"+type;
        for(var row:shapes)if(row.getAsJsonObject("result").getAsJsonObject("components").getAsJsonObject("reverie_dreams:shape").get("id").getAsString().equals(outputId))return matrix(row.getAsJsonArray("shape"));
        return null;
    }
    public boolean[][] shapeFor(ItemStack creator){
        try{String raw=creator.getPersistentDataContainer().get(SHAPE,PersistentDataType.STRING);return raw==null?null:shapeFor(JsonParser.parseString(raw).getAsJsonObject().get("id").getAsString());}
        catch(RuntimeException malformed){return null;}
    }
    public ItemStack draw(ItemStack creator,boolean[][] matrix){
        if(!id(creator).equals(Ids.MOD_ID + ":danmaku_recipe_creator")||matrix==null)return null;
        for(var row:shapes)if(Arrays.deepEquals(matrix,matrix(row.getAsJsonArray("shape")))){
            String type=row.getAsJsonObject("result").getAsJsonObject("components").getAsJsonObject("reverie_dreams:shape").get("id").getAsString();
            ItemStack result=createCreator(type);if(result==null)return null;result.setAmount(creator.getAmount());return result;
        }
        return null;
    }
    private static boolean[][] matrix(JsonArray rows){boolean[][] result=new boolean[rows.size()][];for(int y=0;y<rows.size();y++){var row=rows.get(y).getAsJsonArray();result[y]=new boolean[row.size()];for(int x=0;x<row.size();x++)result[y][x]=row.get(x).getAsBoolean();}return result;}
    public ItemStack createTemplate(String name){
        String key=name.contains(":")?name:Ids.MOD_ID + ":"+name;
        JsonObject config=domain.getAsJsonObject("templateConfigs").getAsJsonObject(key);if(config==null)return null;
        ItemStack result=create(Ids.MOD_ID + ":spell_card_template");if(result==null)return null;
        JsonObject properties=new JsonObject();properties.addProperty("templateId",key);properties.addProperty("count",1);properties.addProperty("damage",2);
        properties.addProperty("damage_type","reverie_dreams:danmaku_generic");properties.addProperty("scale",1);properties.addProperty("speed",.5);
        properties.addProperty("acceleration",0);properties.addProperty("tile",false);properties.addProperty("infinite",false);properties.addProperty("speed_version",1);properties.addProperty("hitbox_scale",-1);
        result.editMeta(meta->{meta.getPersistentDataContainer().set(DanmakuRules.PROPERTIES,PersistentDataType.STRING,properties.toString());meta.getPersistentDataContainer().set(DanmakuRules.TRAJECTORY,PersistentDataType.STRING,config.toString());});return result;
    }
    public Plan table(ItemStack[] input){
        if(input.length!=5)return null;
        String[] keys={"dye","core","power","point","material"};
        for(var recipe:recipes){boolean matched=true;int[] costs=new int[5];
            for(int i=0;i<5;i++){
                var required=recipe.getAsJsonObject(keys[i]);costs[i]=required.get("count").getAsInt();
                if(!id(input[i]).equals(required.get("id").getAsString())||empty(input[i])||input[i].getAmount()<costs[i]){matched=false;break;}
                if(i==4){var shape=required.getAsJsonObject("components").getAsJsonObject("reverie_dreams:shape");ItemStack expected=createCreator(shape.get("id").getAsString());if(expected==null||!input[i].isSimilar(expected)){matched=false;break;}}
            }
            if(!matched)continue;
            var out=recipe.getAsJsonObject("output");var components=out.getAsJsonObject("components");
            String sourceId=out.get("id").getAsString(),type=sourceId.substring(sourceId.lastIndexOf('/')+1),dye=recipe.getAsJsonObject("dye").get("id").getAsString();
            String colour=dye.substring(dye.indexOf(':')+1,dye.length()-4);
            ItemStack result;
            try{result=DanmakuModule.service().rules().create(type,colour);}catch(IllegalArgumentException|IllegalStateException missing){return new Plan(recipe.get("id").getAsString(),null,costs);}
            result.setAmount(out.get("count").getAsInt());
            // Source refreshes this group for every produced stack, independent from the type's firing cooldown ticks.
            result.setData(DataComponentTypes.USE_COOLDOWN,UseCooldown.useCooldown(.5f).cooldownGroup(Key.key("minecraft",UUID.randomUUID().toString())).build());
            return new Plan(recipe.get("id").getAsString(),result,costs);
        }
        return null;
    }
    public Plan strengthen(ItemStack[] input){
        if(input.length!=2||empty(input[0])||empty(input[1]))return null;
        var rules=DanmakuModule.service().rules();if(rules.type(input[0])==null)return null;
        var original=rules.properties(input[0]);var properties=original;var config=rules.trajectory(input[0]);String off=id(input[1]),operation;
        if(off.equals(Ids.MOD_ID + ":spell_card_template")){
            String encoded=input[1].getPersistentDataContainer().get(DanmakuRules.PROPERTIES,PersistentDataType.STRING);if(encoded==null)return null;
            JsonObject p=JsonParser.parseString(encoded).getAsJsonObject();String template=p.get("templateId").getAsString();
            if(!templates().contains(template))return new Plan("unsupported_template",null,new int[]{1,1});
            properties=original.withTemplate(template);String raw=input[1].getPersistentDataContainer().get(DanmakuRules.TRAJECTORY,PersistentDataType.STRING);
            config=raw==null?DanmakuMath.Config.EMPTY:rules.trajectory(input[1]);operation="template";
        }else if(off.equals(Ids.MOD_ID + ":speed_feather")){
            float speed=original.actualSpeed()+.25f;if(speed>2.5f)return null;properties=original.withSpeed(speed,1);operation="speed";
        }else if(off.equals("minecraft:slime_block")){
            int count=original.count()+1;if(count>=3)return null;properties=original.withCount(count);operation="count";
        }else if(off.equals("minecraft:iron_sword")){
            float damage=original.damage()+.25f;if(damage>=5.5f)return null;properties=original.withDamage(damage);operation="damage";
        }else return null;
        ItemStack result=rules.configure(input[0],properties,config);
        // A template lacking trajectory config removes the main stack's config in the source; retain that absence.
        if(off.equals(Ids.MOD_ID + ":spell_card_template")&&!input[1].getPersistentDataContainer().has(DanmakuRules.TRAJECTORY,PersistentDataType.STRING))result.editMeta(meta->meta.getPersistentDataContainer().remove(DanmakuRules.TRAJECTORY));
        return new Plan(Ids.MOD_ID + ":strengthen/"+operation,result,new int[]{1,1});
    }
    public RepairPreview previewRepair(ItemStack item,ItemStack ingredient){
        if(empty(item)||empty(ingredient)||!id(ingredient).equals(Ids.MOD_ID + ":power")||DanmakuModule.service().rules().type(item)==null)return null;
        int max=item.getDataOrDefault(DataComponentTypes.MAX_DAMAGE,0),damage=item.getDataOrDefault(DataComponentTypes.DAMAGE,0),each=max/4;
        if(damage<=0||each<=0)return null;int used=Math.min(ingredient.getAmount(),(damage+each-1)/each),repaired=Math.min(damage,used*each);
        ItemStack result=item.clone();result.setData(DataComponentTypes.DAMAGE,damage-repaired);return new RepairPreview(result,used,repaired);
    }
    static boolean empty(ItemStack item){return item==null||item.isEmpty();}
}
