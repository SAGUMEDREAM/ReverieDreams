package cc.thonly.reverie_dreams.data.danmaku.stg.motion;

import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuPattern;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.Unpooled;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.Identifier;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNull;

class DanmakuMotionCodecTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void dataPackPatternAllowsMotionSegmentWithoutPayload() {
        var json = JsonParser.parseString("""
                {"emitters":[{"bullet":{"type":"reverie_dreams:rice",
                "motion":{"segments":[{"at":20,"type":"rotate","value":1.5}]}}}]}
                """);
        DanmakuPattern pattern = DanmakuPattern.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();
        assertEquals(List.of(MotionSegment.rotate(20, 1.5f)), pattern.emitters().getFirst().bullet().motion().segments());
    }

    @Test
    void savedMotionAllowsSplitPayloadWithoutType() {
        var payload = new CompoundTag();
        payload.putInt("count", 5);
        payload.putFloat("speed", 0.6f);
        var split = new CompoundTag();
        split.putString("type", "split");
        split.put("payload", payload);
        var segments = new ListTag();
        segments.add(split);
        var saved = new CompoundTag();
        saved.put("segments", segments);

        MotionPayload loaded = DanmakuMotion.CODEC.parse(NbtOps.INSTANCE, saved)
                .getOrThrow().segments().getFirst().payload();
        assertNull(loaded.type());
        assertEquals(5, loaded.count());
        assertEquals(0.6f, loaded.speed());
    }

    @Test
    void fixedVectorChangesHeadingAndCanReverseAVolley() {
        var state = new DanmakuMotionState();
        state.applyVectorAcceleration(new Vec3(0, 0, 1));
        state.applyVectorAcceleration(new Vec3(1, 0, 0));
        assertEquals(Math.sqrt(2), state.currentSpeed(), 1e-9);
        assertEquals(1 / Math.sqrt(2), state.currentDirection().x, 1e-9);
        assertEquals(1 / Math.sqrt(2), state.currentDirection().z, 1e-9);
        state.applyVectorAcceleration(new Vec3(-1, 0, -1));
        assertEquals(0, state.currentSpeed(), 1e-9);
        state.applyVectorAcceleration(new Vec3(-1, 0, 0));
        assertTrue(state.currentDirection().x < -0.999);
    }

    @Test
    void entityMotionPacketRoundTripsSegmentsWithAndWithoutPayload() {
        var rice = Identifier.fromNamespaceAndPath("reverie_dreams", "rice");
        var motion = DanmakuMotion.of(new Vec3(0, 0, 1), 0, 0, List.of(
                MotionSegment.setSpeed(3, 0),
                MotionSegment.accelerateVector(4, new Vec3(-0.02, 0, 0.03), 5),
                MotionSegment.split(10, MotionPayload.split(5, 35, 0.6f)),
                MotionSegment.transform(20, MotionPayload.transform(rice, 0xabcdef, 1.2f))
        ));
        var buffer = Unpooled.buffer();
        try {
            DanmakuMotion.STREAM_CODEC.encode(buffer, motion);
            assertEquals(motion, DanmakuMotion.STREAM_CODEC.decode(buffer));
            assertEquals(0, buffer.readableBytes());
        } finally {
            buffer.release();
        }
    }
}
