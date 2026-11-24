package cc.thonly.reverie_dreams.polymer.entity.holder;

import com.google.gson.JsonParser;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class GapPortalHolderTest {
    @Test
    void portalCenterStaysAtParticleHeightWhileSpinning() {
        for (int age = 10; age < 580; age += 11) {
            var transform = GapPortalHolder.transformation(age, 600);
            var center = transform.transformPosition(new Vector3f());
            assertEquals(0, center.x, 0.00001f);
            assertEquals(1.05f, center.y, 0.00001f);
            assertEquals(0, center.z, 0.00001f);
            var normal = transform.transformDirection(new Vector3f(0, 0, 1)).normalize();
            assertEquals(1, normal.z, 0.00001f);
        }
    }

    @Test
    void openPortalMatchesNativeWidthAndHeight() {
        float age = 40;
        float pulse = 1 + (float) Math.sin(age * 0.25f) * 0.06f;
        var transform = GapPortalHolder.transformation(age, 600);
        assertEquals(1.55f * pulse, transform.transformDirection(new Vector3f(1, 0, 0)).length(), 0.00001f);
        assertEquals(2.35f * pulse, transform.transformDirection(new Vector3f(0, 2, 0)).length(), 0.00001f);
    }

    @Test
    void portalFadesAtBothEndsOfItsActualLifetime() {
        for (int lifetime : new int[]{600, 3600}) {
            assertEquals(0, GapPortalHolder.transformation(0, lifetime).transformDirection(new Vector3f(1, 0, 0)).length());
            assertEquals(0, GapPortalHolder.transformation(lifetime, lifetime).transformDirection(new Vector3f(1, 0, 0)).length());
            assertTrue(GapPortalHolder.transformation(lifetime - 10, lifetime).transformDirection(new Vector3f(1, 0, 0)).length() > 0);
        }
    }

    @Test
    void itemGeometryIsCenteredOnTheDisplayPivot() throws Exception {
        var path = Path.of("../common/src/main/resources/assets/reverie_dreams/models/item/holder/gap_portal_display.json");
        var element = JsonParser.parseString(Files.readString(path)).getAsJsonObject().getAsJsonArray("elements").get(0).getAsJsonObject();
        for (int axis = 0; axis < 3; axis++) {
            assertEquals(8, (element.getAsJsonArray("from").get(axis).getAsFloat() + element.getAsJsonArray("to").get(axis).getAsFloat()) / 2);
        }
    }
}
