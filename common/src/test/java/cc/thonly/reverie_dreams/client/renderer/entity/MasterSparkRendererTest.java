package cc.thonly.reverie_dreams.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

class MasterSparkRendererTest {
    @Test void longAndShortBeamsUseTheSameSmallVertexBudget() {
        int full = vertices(1, 1, 40);
        assertTrue(full > 0 && full <= 1600, "Unexpected beam geometry cost: " + full);
        assertEquals(full, vertices(1, 1, 8));
    }

    @Test void chargeAndPartialExpansionProduceFiniteGeometry() {
        assertTrue(vertices(0.5f, 0, 0) > 0);
        assertTrue(vertices(1, 0.25f, 40) > 0);
        assertTrue(vertices(1, 0.25f, 0.1f) > 0);
    }

    private int vertices(float charge, float strength, float length) {
        AtomicInteger count = new AtomicInteger();
        VertexConsumer consumer = (VertexConsumer) Proxy.newProxyInstance(VertexConsumer.class.getClassLoader(), new Class<?>[] { VertexConsumer.class }, (proxy, method, args) -> {
            if (method.getName().equals("addVertex")) {
                count.incrementAndGet();
                for (int i = args.length - 3; i < args.length; i++) assertTrue(Float.isFinite(((Number) args[i]).floatValue()));
            }
            if (method.getName().equals("setColor") && args.length == 4) {
                for (Object channel : args) assertTrue((int) channel >= 0 && (int) channel <= 255);
            }
            return method.getReturnType() == void.class ? null : proxy;
        });
        MasterSparkRenderer.emitGeometry(new PoseStack().last(), consumer, 1234.5f, charge, strength, length);
        return count.get();
    }
}
