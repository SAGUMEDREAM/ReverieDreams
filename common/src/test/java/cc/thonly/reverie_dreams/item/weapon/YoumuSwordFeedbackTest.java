package cc.thonly.reverie_dreams.item.weapon;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.junit.jupiter.api.AfterAll;
import static org.junit.jupiter.api.Assertions.*;

class YoumuSwordFeedbackTest {
    @Test void counterImmediatelyReplacesThePartialChargeBar() {
        var text=YoumuSwordFeedback.chargeText(2,true);
        assertTrue(text.getSiblings().isEmpty());
        assertEquals("item.reverie_dreams.youmu.counter_ready",
                ((TranslatableContents)text.getContents()).getKey());
    }
    @AfterAll static void exportReviewGeometry() throws Exception {
        String output=System.getProperty("youmu.preview.path");
        if(output==null)return;
        var data=new JsonObject();
        var stages=new JsonArray();
        for(long held:new long[]{0,8,16,50}){
            var stage=new JsonObject();stage.addProperty("percent",YoumuSwordFeedback.chargePercent(held));
            stage.addProperty("key",YoumuSwordFeedback.chargeKey(held,false));stages.add(stage);
        }
        data.add("stages",stages);
        for(String shape:new String[]{"wave","ring"}){
            var points=shape.equals("wave")?YoumuSwordFeedback.crescent(Vec3.ZERO,new Vec3(0,0,1),1.1,13):YoumuSwordFeedback.ring(Vec3.ZERO,1.5,16);
            var array=new JsonArray();
            for(Vec3 p:points){var point=new JsonArray();point.add(p.x);point.add(p.y);point.add(p.z);array.add(point);}data.add(shape,array);
        }
        Files.writeString(Path.of(output),data.toString());
    }
    @Test void chargeBarContainsExactlyTenCellsAndLocalizedStatus() {
        for(long held:new long[]{0,7,8,15,16,50}) {
            var message=YoumuSwordFeedback.chargeText(held,false);
            String bar=message.getSiblings().get(0).getString();
            assertEquals(10,bar.length());
            assertEquals(YoumuSwordFeedback.chargePercent(held)/10,bar.chars().filter(c->c=='■').count());
            var status=(TranslatableContents)message.getSiblings().get(2).getContents();
            assertEquals("item.reverie_dreams.youmu."+YoumuSwordFeedback.chargeKey(held,false),status.getKey());
        }
    }
    @Test void chargeBoundariesAndCounterPriorityMatchCombat() {
        assertEquals(0, YoumuSwordFeedback.chargePercent(-1));
        assertEquals(50, YoumuSwordFeedback.chargePercent(YoumuSwordCombat.MIN_CHARGE));
        assertEquals(100, YoumuSwordFeedback.chargePercent(YoumuSwordCombat.FULL_CHARGE));
        assertEquals(100, YoumuSwordFeedback.chargePercent(60));
        assertEquals("charging", YoumuSwordFeedback.chargeKey(7,false));
        assertEquals("charge_ready", YoumuSwordFeedback.chargeKey(8,false));
        assertEquals("charged", YoumuSwordFeedback.chargeKey(16,false));
        assertEquals("expiring", YoumuSwordFeedback.chargeKey(50,false));
        assertEquals("counter_ready", YoumuSwordFeedback.chargeKey(50,true));
    }
    @Test void crescentPointsAreFiniteBoundedAndPerpendicularForEveryAim() {
        Vec3 center=new Vec3(3,4,5);
        for(Vec3 aim:new Vec3[]{new Vec3(0,0,1),new Vec3(0,1,0),new Vec3(0,-1,0),new Vec3(1,.5,1),Vec3.ZERO}) {
            var points=YoumuSwordFeedback.crescent(center,aim,1.1,13);
            assertEquals(13,points.size());
            Vec3 forward=aim.lengthSqr()<1e-8?new Vec3(0,0,1):aim.normalize();
            for(Vec3 point:points) {
                Vec3 delta=point.subtract(center);
                assertTrue(Double.isFinite(delta.length()));
                assertTrue(delta.length()<=1.100001);
                assertEquals(0,delta.dot(forward),1e-8);
            }
            assertEquals(2.2,points.getFirst().distanceTo(points.getLast()),1e-8);
        }
    }
    @Test void purificationRingHasExactRadiusAndStaysNearFeet() {
        Vec3 at=new Vec3(4,.12,6);
        for(double radius:new double[]{.5,.75,1,1.5}) {
            var points=YoumuSwordFeedback.ring(at,radius,16);
            assertEquals(16,points.size());
            for(Vec3 point:points){assertEquals(at.y,point.y);assertEquals(radius,point.distanceTo(at),1e-8);}
        }
    }
}
