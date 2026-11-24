package cc.thonly.reverie_dreams.item.weapon;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.SharedConstants;
import net.minecraft.client.renderer.item.ClientItem;
import net.minecraft.client.renderer.item.ItemModels;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperties;
import net.minecraft.client.resources.model.cuboid.CuboidModel;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LevatinResourcesTest {
    private static final Path ROOT = Path.of(System.getProperty("laevateinn.workspace", ".."));
    private static final String ASSETS = "/resources/assets/reverie_dreams/";

    @BeforeAll static void clientCodecs() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        ItemModels.bootstrap();
        SelectItemModelProperties.bootstrap();
    }

    @Test void bothItemDefinitionsDecodeUsingTheActualMinecraftClientCodec() throws Exception {
        for (String file : List.of("common/src/generated" + ASSETS + "items/levatin.json",
                "common/src/main" + ASSETS + "items/levatin_flame.json")) {
            try (var reader = Files.newBufferedReader(ROOT.resolve(file))) {
                var input = JsonParser.parseReader(reader);
                var model = ClientItem.CODEC.parse(JsonOps.INSTANCE, input).getOrThrow();
                assertNotNull(model.model());
                var encoded = ClientItem.CODEC.encodeStart(JsonOps.INSTANCE, model).getOrThrow();
                assertEquals(input, encoded);
            }
        }
    }

    @Test void allFiveProductionModelsDecodeUsingTheActualMinecraftCuboidParser() throws Exception {
        for (String suffix : List.of("", "_staff", "_flame", "_staff_gui", "_flame_gui")) {
            try (var reader = Files.newBufferedReader(ROOT.resolve("common/src/main" + ASSETS + "models/item/levatin" + suffix + ".json"))) {
                var model = CuboidModel.fromStream(reader);
                assertNotNull(model);
                assertTrue(model.geometry() != null || model.parent() != null);
            }
        }
    }
}
