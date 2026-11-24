package cc.thonly.reverie_dreams.paper;

import cc.thonly.reverie_dreams.paper.danmaku.DanmakuMath;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DanmakuMathTest {
    @Test
    void minecraftAnglesAndFanKeepTheOriginalForwardDirection() {
        var east = DanmakuMath.direction(0, -90);
        assertEquals(1, east.x(), 1e-7);
        assertEquals(0, east.y(), 1e-7);
        var up = DanmakuMath.direction(-90, 0);
        assertEquals(1, up.y(), 1e-7);
        var fan = DanmakuMath.volley("reverie_dreams:fan", 0, 0, DanmakuMath.Config.EMPTY, 1);
        assertEquals(5, fan.size());
        assertEquals(0, fan.get(2).direction().x(), 1e-7);
        assertEquals(1, fan.get(2).direction().z(), 1e-7);
        assertEquals(-fan.get(0).direction().x(), fan.get(4).direction().x(), 1e-7);
    }

    @Test
    void repeatedTemplatesShareOneBudgetAndUnsupportedScriptsNeverBecomeOrdinaryShots() {
        for (String template : new String[]{"single", "triple", "fan", "bullet", "star", "heart", "x", "round", "ring", "ring_view", "ring_horizontal"}) {
            assertEquals(64, DanmakuMath.volley("reverie_dreams:" + template, 0, 0, DanmakuMath.Config.EMPTY, Integer.MAX_VALUE).size(), template);
        }
        assertThrows(IllegalArgumentException.class, () -> DanmakuMath.volley("reverie_dreams:custom", 0, 0, DanmakuMath.Config.EMPTY, 1));
        assertThrows(IllegalArgumentException.class, () -> DanmakuMath.volley("another_plugin:fan", 0, 0, DanmakuMath.Config.EMPTY, 1));
    }
}
