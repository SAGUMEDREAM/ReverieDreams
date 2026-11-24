package cc.thonly.reverie_dreams.block.decor.living;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.IdentityHashMap;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.RenderShape;

import static org.junit.jupiter.api.Assertions.*;

class JapaneseLivingTest {
    private static final String[] MODELS = {"shrine_bell", "bell_rope", "bell_rope_middle", "wind_chime", "oshiire", "oshiire_open",
            "tansu", "tansu_open", "futon_head", "futon_foot", "futon_item", "folded_futon", "irori", "irori_lit", "jizaikagi",
            "jizaikagi_middle", "timber_white_wall", "timber_white_wall_horizontal", "timber_white_wall_cross"};
    private static HangingOrnamentBlock bell, rope, hook;
    private static ItemStack ropeItem;
    private static FutonBlock futon;
    private static LivingStorageBlock storage;
    private static IroriBlock hearth;
    @BeforeAll static void bootstrap() throws Exception {
        SharedConstants.tryDetectVersion(); Bootstrap.bootStrap();
        var registry = BuiltInRegistries.BLOCK;
        var holders = MappedRegistry.class.getDeclaredField("unregisteredIntrusiveHolders");
        var frozen = MappedRegistry.class.getDeclaredField("frozen");
        holders.setAccessible(true); frozen.setAccessible(true);
        Object previous = holders.get(registry); boolean wasFrozen = frozen.getBoolean(registry);
        try {
            frozen.setBoolean(registry, false); holders.set(registry, new IdentityHashMap<>());
            bell = new HangingOrnamentBlock(HangingOrnamentBlock.Kind.BELL, properties("test_bell"));
            rope = new HangingOrnamentBlock(HangingOrnamentBlock.Kind.ROPE, properties("test_rope"));
            hook = new HangingOrnamentBlock(HangingOrnamentBlock.Kind.HOOK, properties("test_hook"));
            futon = new FutonBlock(properties("test_futon"));
            storage = new LivingStorageBlock(properties("test_storage"));
            hearth = new IroriBlock(properties("test_hearth"));
        } finally { holders.set(registry, previous); frozen.setBoolean(registry, wasFrozen); }
        var items = BuiltInRegistries.ITEM;
        Object previousItems = holders.get(items); boolean itemsFrozen = frozen.getBoolean(items);
        try {
            frozen.setBoolean(items, false); holders.set(items, new IdentityHashMap<>());
            var item = new BlockItem(rope, new Item.Properties().setId(
                    ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("test", "rope"))));
            item.builtInRegistryHolder().bindComponents(DataComponentMap.builder()
                    .set(DataComponents.MAX_STACK_SIZE, 64).build());
            ropeItem = new ItemStack(item);
        } finally { holders.set(items, previousItems); frozen.setBoolean(items, itemsFrozen); }
    }
    private static BlockBehaviour.Properties properties(String id) {
        return BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("reverie_dreams", id)));
    }
    private static JsonObject json(String path) throws Exception {
        try (var input = JapaneseLivingTest.class.getResourceAsStream("/" + path)) {
            assertNotNull(input, path);
            return JsonParser.parseString(new String(input.readAllBytes(), StandardCharsets.UTF_8)).getAsJsonObject();
        }
    }
    private static JsonObject model(String id) throws Exception { return json("assets/reverie_dreams/models/block/" + id + ".json"); }
    private static VoxelShape modelShape(String id) throws Exception {
        VoxelShape shape = Shapes.empty();
        for (var element : model(id).getAsJsonArray("elements")) {
            var e = element.getAsJsonObject(); var a = e.getAsJsonArray("from"); var b = e.getAsJsonArray("to");
            shape = Shapes.or(shape, Block.box(a.get(0).getAsDouble(), a.get(1).getAsDouble(), a.get(2).getAsDouble(),
                    b.get(0).getAsDouble(), b.get(1).getAsDouble(), b.get(2).getAsDouble()));
        }
        return shape;
    }
    @Test void allModelFacesUseOneOpaqueTexelPerModelUnit() throws Exception {
        var atlas = ImageIO.read(JapaneseLivingTest.class.getResourceAsStream("/assets/reverie_dreams/textures/block/japanese_living.png"));
        assertEquals(64, atlas.getWidth()); assertEquals(64, atlas.getHeight());
        int checked = 0;
        for (String id : MODELS) for (var element : model(id).getAsJsonArray("elements")) {
            var e = element.getAsJsonObject(); var from = e.getAsJsonArray("from"); var to = e.getAsJsonArray("to");
            for (int i = 0; i < 3; i++) { assertEquals(Math.rint(from.get(i).getAsDouble()), from.get(i).getAsDouble()); assertTrue(to.get(i).getAsDouble() > from.get(i).getAsDouble()); }
            for (var face : e.getAsJsonObject("faces").entrySet()) {
                var uv = face.getValue().getAsJsonObject().getAsJsonArray("uv");
                int uAxis = face.getKey().equals("east") || face.getKey().equals("west") ? 2 : 0;
                int vAxis = face.getKey().equals("up") || face.getKey().equals("down") ? 2 : 1;
                assertEquals(to.get(uAxis).getAsDouble() - from.get(uAxis).getAsDouble(), (uv.get(2).getAsDouble()-uv.get(0).getAsDouble())*4, id);
                assertEquals(to.get(vAxis).getAsDouble() - from.get(vAxis).getAsDouble(), (uv.get(3).getAsDouble()-uv.get(1).getAsDouble())*4, id);
                for (int y=(int)(uv.get(1).getAsDouble()*4); y<uv.get(3).getAsDouble()*4; y++)
                    for (int x=(int)(uv.get(0).getAsDouble()*4); x<uv.get(2).getAsDouble()*4; x++) assertEquals(255, atlas.getRGB(x,y)>>>24, id);
                checked++;
            }
        }
        assertTrue(checked > 1000);
    }
    @Test void selectableShapesFollowTheExactModelInEveryDirection() throws Exception {
        Map<String,String> pairs = Map.of("bell_end","shrine_bell", "rope_end","bell_rope", "rope_middle","bell_rope_middle",
                "chime_end","wind_chime", "hook_end","jizaikagi", "hook_middle","jizaikagi_middle",
                "futon_head","futon_head", "futon_foot","futon_foot", "folded_futon","folded_futon");
        for (var pair : pairs.entrySet()) {
            var expected = Shapes.rotateHorizontal(modelShape(pair.getValue()));
            for (var facing : Direction.Plane.HORIZONTAL)
                assertFalse(Shapes.joinIsNotEmpty(expected.get(facing), JapaneseLivingShapes.shape(pair.getKey(), facing), BooleanOp.NOT_SAME), pair.toString());
        }
    }
    @Test void hangingChainsRequireTheCorrectSupport() {
        assertTrue(rope.canSurvive(rope.defaultBlockState(), level(Map.of(BlockPos.ZERO.above(), bell.defaultBlockState())), BlockPos.ZERO));
        assertTrue(rope.canSurvive(rope.defaultBlockState(), level(Map.of(BlockPos.ZERO.above(), rope.defaultBlockState())), BlockPos.ZERO));
        assertFalse(rope.canSurvive(rope.defaultBlockState(), level(Map.of(BlockPos.ZERO.above(), hook.defaultBlockState())), BlockPos.ZERO));
        assertFalse(bell.canSurvive(bell.defaultBlockState(), level(Map.of()), BlockPos.ZERO));
        assertTrue(bell.canSurvive(bell.defaultBlockState(), level(Map.of(BlockPos.ZERO.above(), Blocks.STONE.defaultBlockState())), BlockPos.ZERO));
    }
    private static LevelReader level(Map<BlockPos, BlockState> states) {
        return (LevelReader) Proxy.newProxyInstance(LevelReader.class.getClassLoader(), new Class[]{LevelReader.class}, (proxy, method, args) -> {
            if (method.getName().equals("getBlockState")) return states.getOrDefault(args[0], Blocks.AIR.defaultBlockState());
            if (method.getName().equals("getBlockEntity")) return null;
            throw new UnsupportedOperationException(method.getName());
        });
    }
    @Test void futonRemainsATwoPartBedAndUsesBakedModels() {
        assertEquals(BedPart.FOOT, futon.defaultBlockState().getValue(FutonBlock.PART));
        assertNull(futon.newBlockEntity(BlockPos.ZERO, futon.defaultBlockState()));
        assertEquals(RenderShape.MODEL, futon.defaultBlockState().getRenderShape());
        assertTrue(JapaneseLivingShapes.north("futon_foot").max(Direction.Axis.Y) <= 4.0/16.0);
    }
    @Test void storageAndLightStatesHaveNoStaleDefaults() {
        assertFalse(storage.defaultBlockState().getValue(LivingStorageBlock.OPEN));
        assertEquals(8, storage.getStateDefinition().getPossibleStates().size());
        assertFalse(hearth.defaultBlockState().getValue(IroriBlock.LIT));
    }
    @Test void holdingAnExtensionLetsTheItemPlaceInsteadOfRinging() {
        for (var block : new HangingOrnamentBlock[]{bell, rope, hook})
            assertEquals(InteractionResult.PASS, block.useItemOn(ropeItem, block.defaultBlockState(),
                    null, BlockPos.ZERO, null, InteractionHand.MAIN_HAND, null));
    }
}
