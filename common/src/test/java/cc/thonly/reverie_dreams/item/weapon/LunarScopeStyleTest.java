package cc.thonly.reverie_dreams.item.weapon;

import cc.thonly.reverie_dreams.client.scope.LunarScopeStyle;
import cc.thonly.reverie_dreams.client.scope.LunarScopeOverlay;
import org.junit.jupiter.api.Test;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterAll;
import static org.junit.jupiter.api.Assertions.*;

class LunarScopeStyleTest {
    @AfterAll static void exportOverlayReview() throws Exception {
        String output=System.getProperty("lunar.scope.preview.path");if(output==null)return;
        var data=new JsonObject();
        for(var type:LunarWeaponType.values()) {
            var rects=new JsonArray();
            LunarScopeOverlay.draw((x1,y1,x2,y2,color)->{
                var rect=new JsonArray();for(int value:new int[]{x1,y1,x2,y2,color})rect.add(value);rects.add(rect);
            },type,400,240,1);
            data.add(type.name(),rects);
        }
        Files.writeString(Path.of(output),data.toString());
    }
    @Test void serverSlownessAndDisabledDynamicFovProduceSameFullZoom() {
        for(var type:LunarWeaponType.values()) {
            float expected=70*LunarScopeStyle.fovScale(type);
            assertEquals(expected,LunarScopeStyle.fov(70,1,type,1),.0001);
            assertEquals(expected,LunarScopeStyle.fov(70*.625f,.625f,type,1),.0001);
            assertEquals(70*.625f,LunarScopeStyle.fov(70*.625f,.625f,type,0),.0001);
        }
    }
    @Test void aimingUsesSixTickThresholdAndDistinctBoundedZoom() {
        assertEquals(6,WeaponOfTheMoon.AIM_HOLD_TICKS);
        for(var type:LunarWeaponType.values()) {
            assertEquals(1,LunarScopeStyle.scale(type,0));
            assertTrue(LunarScopeStyle.scale(type,1)>.1f);
            assertTrue(LunarScopeStyle.scale(type,1)<1);
        }
        assertTrue(LunarScopeStyle.fovScale(LunarWeaponType.SNIPER)<LunarScopeStyle.fovScale(LunarWeaponType.RIFLE));
    }
    @Test void transitionIsBoundedAndReversesAfterRelease() {
        float value=0;
        for(int i=0;i<30;i++)value=LunarScopeStyle.approach(value,true,1.0/60);
        assertTrue(value>.99f);
        for(int i=0;i<30;i++)value=LunarScopeStyle.approach(value,false,1.0/60);
        assertTrue(value<.01f);
        assertEquals(0,LunarScopeStyle.approach(0,true,-1));
        assertTrue(LunarScopeStyle.approach(0,true,99)<1);
    }
    @Test void overlayRectsFitViewportAndSniperLeavesCenterVisible() {
        for(var type:LunarWeaponType.values())for(int[] size:new int[][]{{320,240},{854,480},{3440,1440}}) {
            int w=size[0],h=size[1];
            LunarScopeOverlay.draw((x1,y1,x2,y2,color)->{
                assertTrue(x1>=0&&x2<=w&&y1>=0&&y2<=h);
                assertTrue(x2>=x1&&y2>=y1);
                if(type==LunarWeaponType.SNIPER && (color&0xffffff)==0x05090d)
                    assertFalse(x1<w/2&&x2>w/2&&y1<h/2&&y2>h/2);
            },type,w,h,1);
        }
    }
}
