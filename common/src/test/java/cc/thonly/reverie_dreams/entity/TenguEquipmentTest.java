package cc.thonly.reverie_dreams.entity;

import cc.thonly.reverie_dreams.entity.variant.TenguEquipment;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.SharedConstants;
import net.minecraft.client.resources.model.cuboid.CuboidModel;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class TenguEquipmentTest {
    @BeforeAll static void bootstrap() {SharedConstants.tryDetectVersion();Bootstrap.bootStrap();}

    @Test void actualItemDisplaysKeepBothIdleAndRaisedHandlesAtThePalm() throws Exception {
        for(var arm:HumanoidArm.values())for(boolean using:new boolean[]{false,true}) {
            var actual=combined(arm,using).transformPosition(new Vector3f(.5f,.5f,8.5f/16));
            assertEquals(0,actual.distance(new Vector3f(0,-.25f/16,0)),.000001,arm+" using="+using);
        }
    }

    @Test void leftShieldDoesNotReceiveTheVanillaExtraHalfTurn() throws Exception {
        for(boolean using:new boolean[]{false,true}) {
            var left=combined(HumanoidArm.LEFT,using).transformDirection(new Vector3f(0,0,-1)).normalize();
            var right=combined(HumanoidArm.RIGHT,using).transformDirection(new Vector3f(0,0,-1)).normalize();
            assertEquals(-right.x,left.x,.000001);
            assertEquals(right.y,left.y,.000001);
            assertEquals(right.z,left.z,.000001);
            assertTrue(left.x<0,"Left shield's front was flipped through its handle");
        }
    }

    @Test void gripFollowsAnimatedHandWithoutJumpingBetweenItemBranches() throws Exception {
        for(var arm:HumanoidArm.values())for(int frame=0;frame<=20;frame++) {
            var hand=new Matrix4f().translate(.4f,.8f,-.3f).rotateXYZ(frame*.03f,frame*.02f,-frame*.01f);
            var expected=hand.transformPosition(new Vector3f(0,-.25f/16,0));
            for(boolean using:new boolean[]{false,true}) {
                var actual=new Matrix4f(hand).mul(combined(arm,using)).transformPosition(new Vector3f(.5f,.5f,8.5f/16));
                assertEquals(0,expected.distance(actual),.000001);
            }
        }
    }

    @Test void itemDisplayCarriersSelectActualIdleAndBlockingResources() throws Exception {
        for(boolean using:new boolean[]{false,true}) {
            String suffix=using?"_blocking":"";
            try(var stream=getClass().getResourceAsStream("/assets/reverie_dreams/items/tengu_equipment/shield"+suffix+".json")) {
                assertNotNull(stream);
                var model=JsonParser.parseReader(new InputStreamReader(stream,StandardCharsets.UTF_8)).getAsJsonObject().getAsJsonObject("model");
                assertEquals("minecraft:model",model.get("type").getAsString());
                assertEquals("reverie_dreams:item/tengu_shield"+suffix,model.get("model").getAsString());
            }
        }
    }

    private static Matrix4f combined(HumanoidArm arm,boolean using) throws Exception {
        var context=arm==HumanoidArm.LEFT?ItemDisplayContext.THIRD_PERSON_LEFT_HAND:ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
        try(var stream=TenguEquipmentTest.class.getResourceAsStream("/assets/reverie_dreams/models/item/tengu_shield"+(using?"_blocking":"")+".json")) {
            assertNotNull(stream);var model=CuboidModel.fromStream(new InputStreamReader(stream,StandardCharsets.UTF_8));
            var pose=new PoseStack();model.transforms().getTransform(context).apply(context.leftHand(),pose.last());
            return TenguEquipment.shieldHandTransform(new Matrix4f(),arm,using).mul(pose.last().pose());
        }
    }
}
