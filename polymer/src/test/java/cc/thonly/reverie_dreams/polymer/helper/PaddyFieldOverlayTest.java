package cc.thonly.reverie_dreams.polymer.helper;

import cc.thonly.reverie_dreams.block.PaddyFieldBlock;
import cc.thonly.reverie_dreams.polymer.block.RealSingleStatePolymerBlock;
import com.google.gson.JsonParser;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FarmlandBlock;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import sun.misc.Unsafe;

import java.nio.file.Files;
import java.nio.file.Path;
import com.google.gson.JsonObject;

import static org.junit.jupiter.api.Assertions.*;

class PaddyFieldOverlayTest {
    @BeforeAll
    static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void paddyUsesVanillaWetFarmlandWithoutFactoryModelRegistration() throws Exception {
        var field = Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        var paddy = (PaddyFieldBlock) ((Unsafe) field.get(null)).allocateInstance(PaddyFieldBlock.class);
        var overlay = assertInstanceOf(RealSingleStatePolymerBlock.class, PolymerBlockHelper.requestBlockOverlay(paddy));
        for (int moisture = 0; moisture <= FarmlandBlock.MAX_MOISTURE; moisture++) {
            var state = overlay.getPolymerBlockState(Blocks.FARMLAND.defaultBlockState().setValue(FarmlandBlock.MOISTURE, moisture), null);
            assertTrue(state.is(Blocks.FARMLAND));
            assertEquals(FarmlandBlock.MAX_MOISTURE, state.getValue(FarmlandBlock.MOISTURE));
        }
    }

    @Test
    void blockAndInventoryResourcesReferenceTheExistingVanillaModel() throws Exception {
        var block = json("assets/reverie_dreams/blockstates/paddy_field.json");
        assertEquals("minecraft:block/farmland_moist", block.getAsJsonObject("variants").getAsJsonObject("").get("model").getAsString());
        var item = json("assets/reverie_dreams/items/paddy_field.json").getAsJsonObject("model");
        assertEquals("minecraft:model", item.get("type").getAsString());
        assertEquals("minecraft:block/farmland_moist", item.get("model").getAsString());
        assertNotNull(getClass().getClassLoader().getResource("assets/minecraft/models/block/farmland_moist.json"));
    }

    private JsonObject json(String path) throws Exception {
        try (var reader = Files.newBufferedReader(Path.of("../common/src/generated/resources", path))) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
    }
}
