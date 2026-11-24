package cc.thonly.reverie_dreams.world.dream;

import cc.thonly.reverie_dreams.world.dream.scene.DreamScene;
import cc.thonly.reverie_dreams.world.gen.feature.DreamTrialRoom;
import cc.thonly.reverie_dreams.world.gen.feature.DreamTrialRoomConfig;
import com.google.gson.JsonParser;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.server.Bootstrap;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.world.level.block.state.BlockState;

import static org.junit.jupiter.api.Assertions.*;

class DreamArchipelagoTest {
    private static final Path ROOT = Path.of(System.getProperty("reverie.dream.workspace", ".."));

    @BeforeAll static void bootstrap() { SharedConstants.tryDetectVersion(); Bootstrap.bootStrap(); }

    @Test void naturalDreamGenerationHasArchipelagoWithoutLegacyRoomsOrRandomGrassIslands() throws Exception {
        var biome = JsonParser.parseString(Files.readString(resource("biome/dream.json"))).getAsJsonObject();
        var features = biome.getAsJsonArray("features");
        String all = features.toString();
        assertTrue(all.contains("reverie_dreams:dream_archipelago"));
        assertTrue(all.contains("reverie_dreams:dream_world_grid"));
        assertTrue(all.contains("reverie_dreams:floating_dream_crystal_placed"));
        assertFalse(all.contains("trial_room"));
        assertFalse(all.contains("dream_floating_island_placed"));
        assertEquals(4, features.get(0).getAsJsonArray().size());
    }

    @Test void archipelagoPlacementRunsAtTheOwningChunkWithoutRandomOffsetsOrRarity() throws Exception {
        var configured = JsonParser.parseString(Files.readString(resource("configured_feature/dream_archipelago.json"))).getAsJsonObject();
        assertEquals("reverie_dreams:dream_archipelago", configured.get("type").getAsString());
        var placed = JsonParser.parseString(Files.readString(resource("placed_feature/dream_archipelago.json"))).getAsJsonObject();
        assertEquals("reverie_dreams:dream_archipelago", placed.get("feature").getAsString());
        var modifiers = placed.getAsJsonArray("placement");
        assertEquals(1, modifiers.size(), "Every generating chunk must paint its own deterministic slice exactly once");
        assertEquals("minecraft:biome", modifiers.get(0).getAsJsonObject().get("type").getAsString());
    }

    @Test void oldDatapackTrialRoomReferencesCannotPlaceSpawnersOrOtherBlocks() {
        var noWrites = (WorldGenLevel) Proxy.newProxyInstance(WorldGenLevel.class.getClassLoader(),
                new Class<?>[]{WorldGenLevel.class}, (proxy, method, args) -> {
                    throw new AssertionError("Disabled legacy feature touched the world: " + method.getName());
                });
        var feature = new DreamTrialRoom(DreamTrialRoomConfig.CODEC);
        for (String type : new String[]{"minecraft:zombie", "minecraft:skeleton"}) {
            var context = new FeaturePlaceContext<>(Optional.empty(), noWrites, null, RandomSource.create(42),
                    new BlockPos(-16, 128, 16), new DreamTrialRoomConfig(Identifier.parse(type)));
            assertFalse(feature.place(context));
        }
    }

    @Test void cachedScenesDefensivelyCopyMutablePositionsMapsAndExits() {
        var mutable = new BlockPos.MutableBlockPos(-2, -4, 3);
        var blocks = new HashMap<BlockPos, BlockState>();
        blocks.put(mutable, Blocks.STONE.defaultBlockState());
        var exits = new HashMap<Direction, BlockPos>();
        exits.put(Direction.EAST, mutable);
        var scene = new DreamScene("test", blocks, mutable, exits);
        mutable.set(99, 99, 99);
        blocks.clear();
        exits.clear();
        assertEquals(Blocks.STONE.defaultBlockState(), scene.blocks().get(new BlockPos(-2, -4, 3)));
        assertEquals(new BlockPos(-2, -4, 3), scene.landing());
        assertEquals(new BlockPos(-2, -4, 3), scene.exits().get(Direction.EAST));
        assertThrows(UnsupportedOperationException.class, () -> scene.blocks().clear());
        assertThrows(UnsupportedOperationException.class, () -> scene.exits().clear());
    }

    @Test void sceneBoundsIncludeAirClearanceAndNegativeFoundationCoordinates() {
        var scene = new DreamScene("bounds", Map.of(new BlockPos(-18, -7, -14), Blocks.STONE.defaultBlockState(),
                new BlockPos(18, 15, 14), Blocks.AIR.defaultBlockState()), BlockPos.ZERO, Map.of());
        assertEquals(new BlockPos(-18, -7, -14), scene.min());
        assertEquals(new BlockPos(18, 15, 14), scene.max());
    }

    @Test void invalidEmptyScenesAndVerticalBridgePortsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> new DreamScene("empty", Map.of(), BlockPos.ZERO, Map.of()));
        assertThrows(IllegalArgumentException.class, () -> new DreamScene("vertical", Map.of(BlockPos.ZERO,
                Blocks.STONE.defaultBlockState()), BlockPos.ZERO, Map.of(Direction.UP, BlockPos.ZERO)));
    }

    private static Path resource(String path) {
        return ROOT.resolve("common/src/generated/resources/data/reverie_dreams/worldgen/" + path);
    }
}
