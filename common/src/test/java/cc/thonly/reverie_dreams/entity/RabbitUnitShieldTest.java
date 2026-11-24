package cc.thonly.reverie_dreams.entity;

import cc.thonly.reverie_dreams.entity.variant.RabbitUnitEquipment;
import com.geckolib.loading.loader.GeckoLibGsonLoader;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.SharedConstants;
import net.minecraft.client.resources.model.cuboid.CuboidModel;
import net.minecraft.resources.Identifier;
import net.minecraft.server.Bootstrap;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class RabbitUnitShieldTest {
    @BeforeAll static void bootstrap() {SharedConstants.tryDetectVersion();Bootstrap.bootStrap();}

    @Test void idleShieldFacesOutwardOnBothActualBakedHandBones() throws Exception {
        var loader=new GeckoLibGsonLoader();
        for(int variant=0;variant<3;variant++) {
            var name="rabbit_unit_"+variant;
            var baked=loader.bakeGeckoLibModelFile(Identifier.parse("reverie_dreams:"+name),
                    json("assets/reverie_dreams/geckolib/models/entity/"+name+".geo.json"));
            for(var arm:HumanoidArm.values()) {
                var hand=baked.getBone(arm==HumanoidArm.LEFT?"left_hand":"right_hand").orElseThrow();
                float handX=0;for(var bone=hand;bone!=null;bone=bone.parent())handX+=bone.pivotX();
                var normal=combined(arm,false).transformDirection(new Vector3f(0,0,-1)).normalize();
                assertTrue(normal.x*handX>0,"Idle plate points into the soldier: "+name+" "+arm+" normal="+normal);
                assertEquals(1,Math.abs(normal.x),.000001);
            }
        }
    }

    @Test void vanillaHandleCenterStaysAtThePalmInEveryHandAndPose() throws Exception {
        for(var arm:HumanoidArm.values())for(boolean blocking:new boolean[]{false,true}) {
            var grip=combined(arm,blocking).transformPosition(new Vector3f(0,0,2f/16));
            assertEquals(0,grip.distance(new Vector3f(0,-.25f/16,0)),.000001,arm+" blocking="+blocking);
        }
    }

    @Test void existingGuardMountAndRightIdleMountRemainUnchanged() {
        for(var arm:HumanoidArm.values())for(boolean blocking:new boolean[]{false,true}) {
            if(arm==HumanoidArm.LEFT&&!blocking)continue;
            Vector3f offset=!blocking?new Vector3f(0,.75f,2):arm==HumanoidArm.LEFT
                    ?new Vector3f(-.033355086300f,-2.388980338775f,1.325271839791f)
                    :new Vector3f(.465720321114f,3.607600519094f,.543892020109f);
            var old=new Matrix4f().translate(offset.div(16)).rotateX(-Mth.HALF_PI).translate(0,.125f,-.0625f)
                    .translate(0,.125f,arm==HumanoidArm.RIGHT?-.25f:.25f);
            if(arm==HumanoidArm.LEFT)old.rotateY(Mth.PI);
            assertTrue(old.equals(RabbitUnitEquipment.shieldHandTransform(new Matrix4f(),arm,blocking),.000001f));
        }
    }

    @Test void animatedHandKeepsItsGripInBothItemBranches() throws Exception {
        for(var arm:HumanoidArm.values())for(boolean blocking:new boolean[]{false,true})for(int frame=0;frame<=20;frame++) {
            var hand=new Matrix4f().translate(.4f,.8f,-.3f).rotateXYZ(frame*.03f,frame*.02f,-frame*.01f);
            var expected=hand.transformPosition(new Vector3f(0,-.25f/16,0));
            var actual=new Matrix4f(hand).mul(combined(arm,blocking)).transformPosition(new Vector3f(0,0,2f/16));
            assertEquals(0,expected.distance(actual),.000001);
        }
    }

    @Test void carriersRetainTheActualVanillaShieldRendererAndTransform() throws Exception {
        for(boolean blocking:new boolean[]{false,true}) {
            String name="shield"+(blocking?"_blocking":"");
            var model=json("assets/reverie_dreams/items/rabbit_unit_equipment/"+name+".json").getAsJsonObject("model");
            assertEquals("minecraft:special",model.get("type").getAsString());
            assertEquals("minecraft:item/"+name,model.get("base").getAsString());
            assertEquals("minecraft:shield",model.getAsJsonObject("model").get("type").getAsString());
            var scale=model.getAsJsonObject("transformation").getAsJsonArray("scale");
            assertEquals(1,scale.get(0).getAsInt());assertEquals(-1,scale.get(1).getAsInt());assertEquals(-1,scale.get(2).getAsInt());
        }
    }

    private static Matrix4f combined(HumanoidArm arm,boolean blocking) throws Exception {
        var path="assets/minecraft/models/item/shield"+(blocking?"_blocking":"")+".json";
        try(var stream=RabbitUnitShieldTest.class.getClassLoader().getResourceAsStream(path)) {
            assertNotNull(stream,path);var model=CuboidModel.fromStream(new InputStreamReader(stream,StandardCharsets.UTF_8));
            var context=arm==HumanoidArm.LEFT?ItemDisplayContext.THIRD_PERSON_LEFT_HAND:ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
            var pose=new PoseStack();model.transforms().getTransform(context).apply(context.leftHand(),pose.last());
            return RabbitUnitEquipment.shieldHandTransform(new Matrix4f(),arm,blocking).mul(pose.last().pose()).scale(1,-1,-1);
        }
    }

    private static JsonObject json(String path) throws Exception {
        try(var stream=RabbitUnitShieldTest.class.getClassLoader().getResourceAsStream(path)) {
            assertNotNull(stream,path);return JsonParser.parseReader(new InputStreamReader(stream,StandardCharsets.UTF_8)).getAsJsonObject();
        }
    }
}
