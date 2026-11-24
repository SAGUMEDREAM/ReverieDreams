package cc.thonly.reverie_dreams.polymer.entity;

import cc.thonly.reverie_dreams.entity.variant.TenguAppearance;
import com.geckolib.loading.loader.GeckoLibGsonLoader;
import com.geckolib.loading.math.MathParser;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.*;

class TenguModelTest {
    private static final Path RESOURCES=Path.of("../common/src/main/resources");
    @BeforeAll static void bootstrap(){SharedConstants.tryDetectVersion();Bootstrap.bootStrap();}
    private static JsonObject json(String path) throws Exception {
        return JsonParser.parseString(Files.readString((path.startsWith("model/") ? Path.of("src/main/resources") : RESOURCES).resolve(path))).getAsJsonObject();
    }
    @Test void sixteenModelsBakeWithUniqueRigIdentifiersAndExactAtlasDensity() throws Exception {
        var loader=new GeckoLibGsonLoader();var modelIds=new HashSet<>();
        assertEquals(16,TenguAppearance.values().length);
        for(var appearance:TenguAppearance.values()){
            String name=appearance.modelId().getPath();assertTrue(modelIds.add(name));
            assertEquals(appearance,TenguAppearance.of(appearance.crow(),appearance.modelId()));
            assertEquals(appearance,TenguAppearance.of(appearance.crow(),appearance.female(),appearance.index()));
            var model=loader.bakeGeckoLibModelFile(appearance.resourceId(),json("assets/reverie_dreams/geckolib/models/entity/"+name+".geo.json"));
            for(String bone:Set.of("head","body","waist_cloth","right_hand","left_hand","right_foot","left_foot"))assertTrue(model.getBone(bone).isPresent(),name+": "+bone);
            for(String bone:appearance.crow()?Set.of("right_wing","left_wing","right_wing_tip","left_wing_tip"):Set.of("tail"))assertTrue(model.getBone(bone).isPresent());
            for(String locator:Set.of("right_item_locator","left_item_locator","helmet_locator"))assertTrue(model.getLocator(locator).isPresent());
            var bb=json("model/reverie_dreams/"+name+".bbmodel");var uuids=new HashSet<String>();
            if(appearance.female()){
                String male=TenguAppearance.of(appearance.crow(),false,appearance.index()).modelId().getPath();
                var original=json("model/reverie_dreams/"+male+".bbmodel");
                assertEquals(original.getAsJsonArray("groups").size(),bb.getAsJsonArray("groups").size(),"Shared rig must keep its bones");
                for(int i=0;i<bb.getAsJsonArray("groups").size();i++){
                    var a=original.getAsJsonArray("groups").get(i).getAsJsonObject();var b=bb.getAsJsonArray("groups").get(i).getAsJsonObject();
                    assertEquals(a.get("name"),b.get("name"));assertEquals(a.get("origin"),b.get("origin"));
                }
                assertEquals(json("assets/reverie_dreams/geckolib/animations/entity/"+male+".animation.json"),json("assets/reverie_dreams/geckolib/animations/entity/"+name+".animation.json"),"Female rig must reuse the same motion tracks");
            }
            for(String array:Set.of("groups","elements"))for(var element:bb.getAsJsonArray(array))assertTrue(uuids.add(element.getAsJsonObject().get("uuid").getAsString()),name+": shared bone/cube UUID");
            byte[] atlas=Base64.getDecoder().decode(bb.getAsJsonArray("textures").get(0).getAsJsonObject().get("source").getAsString().split(",",2)[1]);
            assertArrayEquals(Files.readAllBytes(RESOURCES.resolve("assets/reverie_dreams/textures/entity/"+name+".png")),atlas);
            var image=ImageIO.read(new ByteArrayInputStream(atlas));assertEquals(256,image.getWidth());assertEquals(256,image.getHeight());
            for(var value:bb.getAsJsonArray("elements")){
                var cube=value.getAsJsonObject();if(!cube.get("type").getAsString().equals("cube"))continue;
                var from=cube.getAsJsonArray("from");var to=cube.getAsJsonArray("to");
                for(var face:cube.getAsJsonObject("faces").entrySet()){
                    var uv=face.getValue().getAsJsonObject().getAsJsonArray("uv");String side=face.getKey();
                    int u=side.equals("east")||side.equals("west")?2:0,v=side.equals("up")||side.equals("down")?2:1;
                    assertEquals(to.get(u).getAsDouble()-from.get(u).getAsDouble(),uv.get(2).getAsDouble()-uv.get(0).getAsDouble(),1e-6,name+" "+cube.get("name")+" "+side+" U density");
                    assertEquals(to.get(v).getAsDouble()-from.get(v).getAsDouble(),uv.get(3).getAsDouble()-uv.get(1).getAsDouble(),1e-6,name+" "+side+" V density");
                    for(int i=0;i<4;i++)assertTrue(uv.get(i).getAsDouble()>=0&&uv.get(i).getAsDouble()<=256);
                }
            }
        }
    }
    @Test void everyAnimationSurvivesGeckoBakingAndDeathDoesNotDoubleRotate() throws Exception {
        var loader=new GeckoLibGsonLoader();
        for(var appearance:TenguAppearance.values()){
            var data=json("assets/reverie_dreams/geckolib/animations/entity/"+appearance.modelId().getPath()+".animation.json");
            for(var clip:data.getAsJsonObject("animations").entrySet()){
                var a=clip.getValue().getAsJsonObject();double end=a.get("animation_length").getAsDouble();
                for(var bone:a.getAsJsonObject("bones").entrySet())for(var channel:bone.getValue().getAsJsonObject().entrySet()){
                    double previous=-1;for(String key:channel.getValue().getAsJsonObject().keySet()){
                        double t=Double.parseDouble(key);assertTrue(t>=previous&&t<=end+1e-6,clip.getKey()+" timestamp "+key);previous=t;
                    }
                }
            }
            var baked=loader.bakeGeckoLibAnimationsFile(appearance.resourceId(),data,MathParser.create());
            var required=new HashSet<>(Set.of("idle","walk","melee","block","hurt","death"));
            if (appearance.crow()) required.addAll(Set.of("takeoff", "fly", "glide", "land", "ranged", "photo", "photo_air"));
            else required.addAll(Set.of("walk_legs", "walk_legs_neutral"));
            for(String clip:required){var animation=baked.getAnimation("animation."+clip);assertNotNull(animation);assertTrue(animation.boneAnimations().length>0);}
            assertFalse(data.getAsJsonObject("animations").getAsJsonObject("animation.death").getAsJsonObject("bones").has("root"),"Native and Polymer already apply vanilla death roll");
            var gait=baked.getAnimation(appearance.crow() ? "animation.walk" : "animation.walk_legs");
            var root=Arrays.stream(gait.boneAnimations()).filter(b->b.boneName().equals("root")).findFirst().orElseThrow();
            assertTrue(root.positionKeyFrames().yKeyframes().length>=100,"Foot grounding must survive loader");
            if (!appearance.crow()) {
                Set<String> lower = Set.of("root", "right_leg", "right_lower_leg", "left_leg", "left_lower_leg");
                var clips = data.getAsJsonObject("animations");
                assertEquals(lower, clips.getAsJsonObject("animation.walk_legs").getAsJsonObject("bones").keySet());
                assertEquals(lower, clips.getAsJsonObject("animation.walk_legs_neutral").getAsJsonObject("bones").keySet());
                for (String upper : Set.of("idle", "walk", "melee", "block", "hurt", "death")) {
                    var tracks = clips.getAsJsonObject("animation." + upper).getAsJsonObject("bones");
                    assertTrue(lower.stream().noneMatch(tracks::has), upper + " competes for the gait bones");
                }
            }
        }
    }
}
