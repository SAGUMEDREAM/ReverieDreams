package cc.thonly.reverie_dreams.item.weapon;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.*;

class LunarBulletVisualTest {
    private static JsonObject resource(String path) throws Exception {
        try (var stream=LunarBulletVisualTest.class.getResourceAsStream("/assets/reverie_dreams/"+path)) {
            assertNotNull(stream,path);
            return JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
        }
    }
    @Test void everyBulletFaceRetainsOneTexelPerModelUnit() throws Exception {
        for (String kind:new String[]{"standard","pellet","sniper"}) {
            var model=resource("models/item/lunar_bullet_"+kind+".json");
            for (var value:model.getAsJsonArray("elements")) {
                var e=value.getAsJsonObject();double[] size=new double[3];
                for(int axis=0;axis<3;axis++)size[axis]=e.getAsJsonArray("to").get(axis).getAsDouble()-e.getAsJsonArray("from").get(axis).getAsDouble();
                for(var entry:e.getAsJsonObject("faces").entrySet()) {
                    var uv=entry.getValue().getAsJsonObject().getAsJsonArray("uv");
                    String face=entry.getKey();
                    double w=face.equals("east")||face.equals("west")?size[2]:size[0];
                    double h=face.equals("up")||face.equals("down")?size[2]:size[1];
                    assertEquals(w,Math.abs(uv.get(2).getAsDouble()-uv.get(0).getAsDouble()),1e-8);
                    assertEquals(h,Math.abs(uv.get(3).getAsDouble()-uv.get(1).getAsDouble()),1e-8);
                }
            }
        }
    }
    @Test void itemDefinitionsResolveAndAllThreeSilhouettesHaveDistinctLengths() throws Exception {
        int index=0;
        for(String kind:new String[]{"pellet","standard","sniper"}) {
            String name="lunar_bullet_"+kind;
            assertEquals("reverie_dreams:item/"+name,resource("items/"+name+".json").getAsJsonObject("model").get("model").getAsString());
            double low=Double.POSITIVE_INFINITY,high=Double.NEGATIVE_INFINITY;
            for(var value:resource("models/item/"+name+".json").getAsJsonArray("elements")) {
                var e=value.getAsJsonObject();low=Math.min(low,e.getAsJsonArray("from").get(2).getAsDouble());high=Math.max(high,e.getAsJsonArray("to").get(2).getAsDouble());
            }
            assertEquals(new int[]{2,6,10}[index++],high-low);
        }
    }
}
