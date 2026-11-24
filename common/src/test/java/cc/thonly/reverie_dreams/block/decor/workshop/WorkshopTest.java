package cc.thonly.reverie_dreams.block.decor.workshop;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import sun.misc.Unsafe;

import javax.imageio.ImageIO;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Arrays;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.entity.BlockEntity;

import static org.junit.jupiter.api.Assertions.*;

/** Real block handlers and Minecraft voxel math; only world/player plumbing is replaced. */
class WorkshopTest {
    static final Map<WorkshopKind,Block> BLOCKS=new EnumMap<>(WorkshopKind.class);
    static Unsafe unsafe;
    @BeforeAll static void bootstrap() throws Exception {
        SharedConstants.tryDetectVersion();Bootstrap.bootStrap();
        var f=Unsafe.class.getDeclaredField("theUnsafe");f.setAccessible(true);unsafe=(Unsafe)f.get(null);
        var registry=BuiltInRegistries.BLOCK;
        var holders=MappedRegistry.class.getDeclaredField("unregisteredIntrusiveHolders");
        var frozen=MappedRegistry.class.getDeclaredField("frozen");holders.setAccessible(true);frozen.setAccessible(true);
        var old=holders.get(registry);boolean freeze=frozen.getBoolean(registry);
        try {
            frozen.setBoolean(registry,false);holders.set(registry,new IdentityHashMap<>());
            for(var kind:WorkshopKind.values()) {
                var p=BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK,Identifier.fromNamespaceAndPath("test",kind.id())));
                BLOCKS.put(kind,switch(kind.mode()) {
                    case STORAGE -> new WorkshopStorageBlock(kind,p);
                    case DESK -> new TestDesk(p);
                    case COUNTER -> new WorkshopCounterBlock(p);
                    case BENCH -> new WorkshopBenchBlock(p);
                    case BRIDGE -> new WorkshopBridgeBlock(kind,p);
                    case RAILING -> new FenceBlock(p);
                    default -> new WorkshopDecorBlock(kind,p);
                });
            }
        }finally{holders.set(registry,old);frozen.setBoolean(registry,freeze);}
        for(Item item:new Item[]{Items.AIR,Items.INK_SAC,Items.BOOK,Items.STICK})
            if(!item.builtInRegistryHolder().areComponentsBound())item.builtInRegistryHolder().bindComponents(DataComponentMap.builder().set(DataComponents.MAX_STACK_SIZE,64).build());
    }
    static JsonObject json(String path) throws Exception {
        try(var in=WorkshopTest.class.getResourceAsStream("/"+path)){
            assertNotNull(in,path);return JsonParser.parseString(new String(in.readAllBytes(),StandardCharsets.UTF_8)).getAsJsonObject();
        }
    }
    @Test void everyVisibleFaceUsesOneOpaqueTexelPerUnitAndShapesMatch() throws Exception {
        var atlas=ImageIO.read(WorkshopTest.class.getResourceAsStream("/assets/reverie_dreams/textures/block/japanese_set2.png"));
        assertEquals(128,atlas.getWidth());assertEquals(128,atlas.getHeight());
        var manifest=json("data/reverie_dreams/japanese_set2_shapes.json");int faces=0;
        for(var entry:manifest.entrySet()) {
            String id=entry.getKey();var model=json("assets/reverie_dreams/models/block/set2/"+id+".json");var shape=Shapes.empty();
            boolean rack=model.getAsJsonObject("textures").get("atlas").getAsString().equals("reverie_dreams:block/shrine_racks");
            var faceAtlas=rack?ImageIO.read(WorkshopTest.class.getResourceAsStream("/assets/reverie_dreams/textures/block/shrine_racks.png")):atlas;
            double density=faceAtlas.getWidth()/16.0;
            for(var value:model.getAsJsonArray("elements")) {
                var e=value.getAsJsonObject();var a=e.getAsJsonArray("from");var b=e.getAsJsonArray("to");
                shape=Shapes.or(shape,Block.box(a.get(0).getAsDouble(),a.get(1).getAsDouble(),a.get(2).getAsDouble(),b.get(0).getAsDouble(),b.get(1).getAsDouble(),b.get(2).getAsDouble()));
                for(int axis=0;axis<3;axis++) {assertEquals(Math.rint(a.get(axis).getAsDouble()),a.get(axis).getAsDouble(),id);assertTrue(b.get(axis).getAsDouble()>a.get(axis).getAsDouble(),id);}
                for(var face:e.getAsJsonObject("faces").entrySet()) {
                    int u=face.getKey().matches("east|west")?2:0,v=face.getKey().matches("up|down")?2:1;
                    var uv=face.getValue().getAsJsonObject().getAsJsonArray("uv");
                    assertEquals(b.get(u).getAsDouble()-a.get(u).getAsDouble(),(uv.get(2).getAsDouble()-uv.get(0).getAsDouble())*density,id);
                    assertEquals(b.get(v).getAsDouble()-a.get(v).getAsDouble(),(uv.get(3).getAsDouble()-uv.get(1).getAsDouble())*density,id);
                    for(int y=(int)(uv.get(1).getAsDouble()*density);y<uv.get(3).getAsDouble()*density;y++)for(int x=(int)(uv.get(0).getAsDouble()*density);x<uv.get(2).getAsDouble()*density;x++)assertEquals(255,faceAtlas.getRGB(x,y)>>>24,id);
                    faces++;
                }
            }
            var rotated=Shapes.rotateHorizontal(shape);
            for(var direction:Direction.Plane.HORIZONTAL)assertFalse(Shapes.joinIsNotEmpty(rotated.get(direction),WorkshopShapes.shape(id,direction),BooleanOp.NOT_SAME),id+direction);
        }
        assertEquals(107,manifest.size());assertTrue(faces>4000);
    }
    @Test void allStatesHaveValidSelectionAndDeliberateCollision() {
        for(var kind:WorkshopKind.values())for(var state:BLOCKS.get(kind).getStateDefinition().getPossibleStates()) {
            if(state.getBlock() instanceof WorkshopDecorBlock block){assertFalse(block.getShape(state,null,BlockPos.ZERO,CollisionContext.empty()).isEmpty(),kind.id());assertEquals(!kind.collision(),block.getCollisionShape(state,null,BlockPos.ZERO,CollisionContext.empty()).isEmpty(),kind.id());}
            if(state.getBlock() instanceof WorkshopStorageBlock block)assertFalse(block.getShape(state,null,BlockPos.ZERO,CollisionContext.empty()).isEmpty(),kind.id());
        }
        assertEquals(10,Arrays.stream(WorkshopKind.values()).filter(WorkshopKind::storage).count());
    }
    static LevelReader level(Map<BlockPos,BlockState> states) {
        return (LevelReader)Proxy.newProxyInstance(LevelReader.class.getClassLoader(),new Class[]{LevelReader.class},(proxy,method,args)->switch(method.getName()) {
            case "getBlockState" -> states.getOrDefault(args[0],Blocks.AIR.defaultBlockState());
            case "getBlockEntity" -> null;
            default -> throw new UnsupportedOperationException(method.getName());
        });
    }
    @Test void counterReconnectsAfterRemovalAndRotatesItsJoinMask() {
        var block=(WorkshopCounterBlock)BLOCKS.get(WorkshopKind.CONNECTED_COUNTER);var state=block.defaultBlockState();
        var full=level(Map.of(BlockPos.ZERO.north(),state,BlockPos.ZERO.east(),state,BlockPos.ZERO.south(),state,BlockPos.ZERO.west(),state));
        var joined=block.updateShape(state,full,null,BlockPos.ZERO,Direction.NORTH,BlockPos.ZERO.north(),state,RandomSource.create());
        assertEquals(15,WorkshopConnections.localMask(joined));
        var single=block.updateShape(joined,level(Map.of(BlockPos.ZERO.east(),state)),null,BlockPos.ZERO,Direction.NORTH,BlockPos.ZERO.north(),Blocks.AIR.defaultBlockState(),RandomSource.create());
        assertEquals(2,WorkshopConnections.localMask(single));
        for(var rotation:Rotation.values())assertEquals(2,WorkshopConnections.localMask(block.rotate(single,rotation)));
        for(var mirror:Mirror.values())assertEquals(single,block.mirror(block.mirror(single,mirror),mirror));
    }
    @Test void benchJoinsOnlyMatchingFacingAcrossItsSeatAndKeepsHeight() {
        var block=(WorkshopBenchBlock)BLOCKS.get(WorkshopKind.SHOP_BENCH);var state=block.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING,Direction.NORTH);
        var surroundings=level(Map.of(BlockPos.ZERO.west(),state,BlockPos.ZERO.east(),state.setValue(HorizontalDirectionalBlock.FACING,Direction.SOUTH),BlockPos.ZERO.north(),state));
        var joined=block.updateShape(state,surroundings,null,BlockPos.ZERO,Direction.WEST,BlockPos.ZERO.west(),state,RandomSource.create());
        assertEquals(8,WorkshopConnections.localMask(joined));assertEquals(.5,block.seatHeight());
        assertFalse(joined.getValue(WorkshopBenchBlock.OCCUPIED));
    }
    @Test void bridgesHaveTwoPixelStepsAndOpenCentralHeadroom() {
        for(var kind:new WorkshopKind[]{WorkshopKind.TAIKO_BRIDGE_APPROACH,WorkshopKind.TAIKO_BRIDGE_RISE,WorkshopKind.TAIKO_BRIDGE_CROWN}){
            var block=(WorkshopBridgeBlock)BLOCKS.get(kind);var state=block.defaultBlockState();
            var shape=block.getCollisionShape(state,null,BlockPos.ZERO,CollisionContext.empty());double previous=-1;
            for(int i=0;i<4;i++) {
                var hit=shape.clip(new Vec3(.5,3,(i*4+2)/16.0),new Vec3(.5,-1,(i*4+2)/16.0),BlockPos.ZERO);assertNotNull(hit);
                double height=hit.getLocation().y*16;double expected=kind==WorkshopKind.TAIKO_BRIDGE_CROWN?16:(kind==WorkshopKind.TAIKO_BRIDGE_RISE?16:8)-i*2;
                assertEquals(expected,height,1e-6);if(previous>=0)assertTrue(Math.abs(previous-height)<=2);previous=height;
            }
            assertTrue(block.getFluidState(state.setValue(BlockStateProperties.WATERLOGGED,true)).is(Fluids.WATER));
            assertTrue(block.getFluidState(state).isEmpty());
        }
    }
    record Fixture(World world,TestPlayer player,TestDesk desk){}
    Fixture fixture() throws Exception {
        var world=(World)unsafe.allocateInstance(World.class);world.acceptChanges=true;var player=(TestPlayer)unsafe.allocateInstance(TestPlayer.class);
        player.inventory=(TestInventory)unsafe.allocateInstance(TestInventory.class);player.inventory.accepts=true;
        var desk=(TestDesk)BLOCKS.get(WorkshopKind.FUMIZUKUE);world.state=desk.defaultBlockState();return new Fixture(world,player,desk);
    }
    @Test void deskPlacesExactlyOneAndRetrievesOnlyOnceThroughItemFirstDispatch() throws Exception {
        for(var hand:InteractionHand.values()){
            var f=fixture();var held=new ItemStack(Items.INK_SAC,3);
            assertTrue(f.desk.useItemOn(held,f.world.state,f.world,BlockPos.ZERO,f.player,hand,null).consumesAction());assertEquals(2,held.getCount());
            assertEquals(WorkshopDeskBlock.Accessory.INKSTONE,f.world.state.getValue(WorkshopDeskBlock.ACCESSORY));
            retrieve(f);assertEquals(1,f.player.inventory.received);assertEquals(WorkshopDeskBlock.Accessory.NONE,f.world.state.getValue(WorkshopDeskBlock.ACCESSORY));
            // Second click opens the drawer; the cleared state has no item to give again.
            f.desk.useWithoutItem(f.world.state,f.world,BlockPos.ZERO,f.player,hit());assertEquals(1,f.player.inventory.received);
        }
    }
    @Test void deskCreativePlacementAndFailedWorldChangesDoNotConsume() throws Exception {
        var f=fixture();f.player.creative=true;var books=new ItemStack(Items.BOOK,4);
        f.desk.useItemOn(books,f.world.state,f.world,BlockPos.ZERO,f.player,InteractionHand.MAIN_HAND,null);assertEquals(4,books.getCount());
        f=fixture();f.world.acceptChanges=false;var ink=new ItemStack(Items.INK_SAC,4);
        assertEquals(InteractionResult.FAIL,f.desk.useItemOn(ink,f.world.state,f.world,BlockPos.ZERO,f.player,InteractionHand.MAIN_HAND,null));assertEquals(4,ink.getCount());
        f.world.state=f.world.state.setValue(WorkshopDeskBlock.ACCESSORY,WorkshopDeskBlock.Accessory.BOOKS);
        assertEquals(InteractionResult.FAIL,f.desk.useWithoutItem(f.world.state,f.world,BlockPos.ZERO,f.player,hit()));assertEquals(0,f.player.inventory.received);
    }
    @Test void fullInventoryDropsOnlyOneAccessoryAndCustomItemsArePreserved() throws Exception {
        var f=fixture();f.world.state=f.world.state.setValue(WorkshopDeskBlock.ACCESSORY,WorkshopDeskBlock.Accessory.BOOKS);f.player.inventory.accepts=false;
        retrieve(f);assertEquals(1,f.player.drops);assertEquals(1,f.player.dropped.getCount());assertTrue(f.player.dropped.is(Items.BOOK));
        f.desk.useWithoutItem(f.world.state,f.world,BlockPos.ZERO,f.player,hit());assertEquals(1,f.player.drops);
        f=fixture();var custom=new ItemStack(Items.BOOK);custom.set(DataComponents.CUSTOM_NAME,Component.literal("Keep my name"));
        f.desk.useItemOn(custom,f.world.state,f.world,BlockPos.ZERO,f.player,InteractionHand.MAIN_HAND,null);
        assertEquals(1,custom.getCount());assertEquals(WorkshopDeskBlock.Accessory.NONE,f.world.state.getValue(WorkshopDeskBlock.ACCESSORY));
    }
    @Test void deskClientPredictionCannotMutateOrGiveItems() throws Exception {
        var f=fixture();f.world.client=true;var ink=new ItemStack(Items.INK_SAC,4);
        f.desk.useItemOn(ink,f.world.state,f.world,BlockPos.ZERO,f.player,InteractionHand.MAIN_HAND,null);assertEquals(4,ink.getCount());
        f.world.state=f.world.state.setValue(WorkshopDeskBlock.ACCESSORY,WorkshopDeskBlock.Accessory.INKSTONE);retrieve(f);
        assertEquals(0,f.player.inventory.received);assertEquals(WorkshopDeskBlock.Accessory.INKSTONE,f.world.state.getValue(WorkshopDeskBlock.ACCESSORY));
    }
    static BlockHitResult hit(){return new BlockHitResult(new Vec3(.5,.7,.5),Direction.UP,BlockPos.ZERO,false);}
    static void retrieve(Fixture f){
        var result=f.world.state.useItemOn(ItemStack.EMPTY,f.world,f.player,InteractionHand.MAIN_HAND,hit());
        if(result instanceof InteractionResult.TryEmptyHandInteraction)result=f.desk.useWithoutItem(f.world.state,f.world,BlockPos.ZERO,f.player,hit());
        assertTrue(result.consumesAction());
    }
    static class TestDesk extends WorkshopDeskBlock {
        TestDesk(Properties p){super(p);}
        @Override protected Item accessoryItem(Accessory accessory){return accessory==Accessory.INKSTONE?Items.INK_SAC:Items.BOOK;}
    }
    static class World extends ServerLevel {
        BlockState state;boolean client,acceptChanges;
        World(){super(null,null,null,null,null,null,false,0,List.of(),false);}
        @Override public boolean isClientSide(){return client;}
        @Override public BlockState getBlockState(BlockPos pos){return state;}
        @Override public BlockEntity getBlockEntity(BlockPos pos){return null;}
        @Override public boolean setBlock(BlockPos pos,BlockState next,int flags){if(!acceptChanges)return false;state=next;return true;}
        @Override public void playSound(Entity excluded,BlockPos pos,SoundEvent sound,SoundSource source,float volume,float pitch) {}
    }
    static class TestPlayer extends Player {
        boolean creative;TestInventory inventory;int drops;ItemStack dropped;
        TestPlayer(){super(null,null);}
        @Override public GameType gameMode(){return creative?GameType.CREATIVE:GameType.SURVIVAL;}
        @Override public boolean hasInfiniteMaterials(){return creative;}
        @Override public boolean isShiftKeyDown(){return false;}
        @Override public boolean mayBuild(){return true;}
        @Override public Inventory getInventory(){return inventory;}
        @Override public ItemEntity drop(ItemStack stack,boolean randomThrow){drops++;dropped=stack.copy();return null;}
    }
    static class TestInventory extends Inventory {
        int received;boolean accepts;
        TestInventory(){super(null,null);}
        @Override public boolean add(ItemStack stack){if(!accepts)return false;received+=stack.getCount();stack.setCount(0);return true;}
    }
}
