package cc.thonly.reverie_dreams.block.decor.workshop;

import cc.thonly.reverie_dreams.block.entity.WorkshopStationBlockEntity;
import cc.thonly.reverie_dreams.event.block.WorkshopRackEventHandlers;
import cc.thonly.reverie_dreams.workshop.WorkshopDisplays;
import net.minecraft.SharedConstants;
import net.minecraft.core.*;
import net.minecraft.core.component.*;
import net.minecraft.core.registries.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.*;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.*;
import sun.misc.Unsafe;
import java.util.*;
import net.minecraft.core.RegistryAccess;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import static org.junit.jupiter.api.Assertions.*;

/** Actual rack placement/join/interaction handlers, with only world and player I/O replaced. */
class WorkshopRackTest {
    static Unsafe unsafe;
    static WorkshopRackBlock ema, ofuda;
    static BlockEntityType<?> stationType;
    @BeforeAll static void boot() throws Exception {
        SharedConstants.tryDetectVersion(); Bootstrap.bootStrap();
        var f=Unsafe.class.getDeclaredField("theUnsafe");f.setAccessible(true);unsafe=(Unsafe)f.get(null);
        var holders=MappedRegistry.class.getDeclaredField("unregisteredIntrusiveHolders");
        var frozen=MappedRegistry.class.getDeclaredField("frozen");
        holders.setAccessible(true);frozen.setAccessible(true);
        Object old=holders.get(BuiltInRegistries.BLOCK);boolean freeze=frozen.getBoolean(BuiltInRegistries.BLOCK);
        try { frozen.setBoolean(BuiltInRegistries.BLOCK,false);holders.set(BuiltInRegistries.BLOCK,new IdentityHashMap<>());
            ema=new WorkshopRackBlock(WorkshopKind.EMA_RACK,BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK,Identifier.parse("test:ema_rack"))));
            ofuda=new WorkshopRackBlock(WorkshopKind.OFUDA_RACK,BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK,Identifier.parse("test:ofuda_rack"))));
        } finally {holders.set(BuiltInRegistries.BLOCK,old);frozen.setBoolean(BuiltInRegistries.BLOCK,freeze);}
        var constructor=BlockEntityType.class.getDeclaredConstructors()[0];constructor.setAccessible(true);
        Object oldTypes=holders.get(BuiltInRegistries.BLOCK_ENTITY_TYPE);boolean typesFrozen=frozen.getBoolean(BuiltInRegistries.BLOCK_ENTITY_TYPE);
        try {frozen.setBoolean(BuiltInRegistries.BLOCK_ENTITY_TYPE,false);holders.set(BuiltInRegistries.BLOCK_ENTITY_TYPE,new IdentityHashMap<>());
            stationType=(BlockEntityType<?>)constructor.newInstance(null,Set.of(ema,ofuda));
        } finally {holders.set(BuiltInRegistries.BLOCK_ENTITY_TYPE,oldTypes);frozen.setBoolean(BuiltInRegistries.BLOCK_ENTITY_TYPE,typesFrozen);}
        for(var item:List.of(Items.PAPER,Items.STICK,Items.AIR))if(!item.builtInRegistryHolder().areComponentsBound())item.builtInRegistryHolder().bindComponents(DataComponentMap.builder().set(DataComponents.MAX_STACK_SIZE,64).build());
    }
    static World world() throws Exception {var w=(World)unsafe.allocateInstance(World.class);w.states=new HashMap<>();w.entities=new HashMap<>();return w;}
    static TestPlayer player(World w) throws Exception {var p=(TestPlayer)unsafe.allocateInstance(TestPlayer.class);p.world=w;p.build=true;p.inventory=(TestInventory)unsafe.allocateInstance(TestInventory.class);p.inventory.accepts=true;return p;}
    static void place(World w,WorkshopRackBlock block,BlockPos pos,Direction facing) {
        var state=block.defaultBlockState().setValue(WorkshopRackBlock.FACING,facing);w.setBlock(pos,state,3);block.setPlacedBy(w,pos,state,null,ItemStack.EMPTY);
    }
    static void group(World w,BlockPos pos,int width,int index) {assertEquals(width,w.getBlockState(pos).getValue(WorkshopRackBlock.WIDTH));assertEquals(index,w.getBlockState(pos).getValue(WorkshopRackBlock.INDEX));assertEquals(w.getBlockState(pos).setValue(WorkshopRackBlock.HALF,DoubleBlockHalf.UPPER),w.getBlockState(pos.above()));}
    @Test void oneItemCreatesTwoHalvesAndFourColumnsAreCappedAtThree() throws Exception {
        for(Direction facing:Direction.Plane.HORIZONTAL){var w=world();Direction right=facing.getClockWise();for(int i=0;i<4;i++)place(w,ema,BlockPos.ZERO.relative(right,i),facing);
            for(int i=0;i<3;i++)group(w,BlockPos.ZERO.relative(right,i),3,i);group(w,BlockPos.ZERO.relative(right,3),1,0);
        }
    }
    @Test void reversePlacementAndGapInsertionJoinWithoutDependingOnCoordinates() throws Exception {
        var w=world();for(int x:new int[]{2,0,1})place(w,ema,new BlockPos(x,0,0),Direction.NORTH);
        for(int x=0;x<3;x++)group(w,new BlockPos(x,0,0),3,x);
        var other=world();for(int x:new int[]{2,1,0})place(other,ema,new BlockPos(x,0,0),Direction.NORTH);
        for(int x=0;x<3;x++)group(other,new BlockPos(x,0,0),3,x);
    }
    @Test void differentTypesAndFacingNeverJoin() throws Exception {
        var w=world();place(w,ema,BlockPos.ZERO,Direction.NORTH);place(w,ofuda,BlockPos.ZERO.east(),Direction.NORTH);place(w,ema,BlockPos.ZERO.west(),Direction.SOUTH);
        group(w,BlockPos.ZERO,1,0);group(w,BlockPos.ZERO.east(),1,0);group(w,BlockPos.ZERO.west(),1,0);
    }
    @Test void breakingMiddleFromTopRemovesOnlyItsColumnAndSplitsNeighbors() throws Exception {
        var w=world();for(int i=0;i<3;i++)place(w,ema,new BlockPos(i,0,0),Direction.NORTH);
        w.destroyBlock(new BlockPos(1,1,0),true);
        assertTrue(w.getBlockState(new BlockPos(1,0,0)).isAir());assertTrue(w.getBlockState(new BlockPos(1,1,0)).isAir());
        ema.tick(w.getBlockState(BlockPos.ZERO),w,BlockPos.ZERO,null);ema.tick(w.getBlockState(new BlockPos(2,0,0)),w,new BlockPos(2,0,0),null);
        group(w,BlockPos.ZERO,1,0);group(w,new BlockPos(2,0,0),1,0);
    }
    @Test void breakingAnEndKeepsOtherTwoJoinedAndQuiescent() throws Exception {
        var w=world();for(int i=0;i<3;i++)place(w,ema,new BlockPos(i,0,0),Direction.NORTH);
        w.destroyBlock(BlockPos.ZERO,true);ema.tick(w.getBlockState(BlockPos.ZERO.east()),w,BlockPos.ZERO.east(),null);
        group(w,BlockPos.ZERO.east(),2,0);group(w,new BlockPos(2,0,0),2,1);
        int notifications=w.notifications;ema.tick(w.getBlockState(BlockPos.ZERO.east()),w,BlockPos.ZERO.east(),null);assertEquals(notifications,w.notifications,"Unchanged groups must not schedule themselves forever");
    }
    @Test void occupiedHandSneakOnUpperTakesExactlyOneWithComponentsAndDoesNotInsert() throws Exception {
        var w=world();place(w,ema,BlockPos.ZERO,Direction.NORTH);var p=player(w);p.sneak=true;
        var host=new Station(BlockPos.ZERO,w.getBlockState(BlockPos.ZERO));w.entities.put(BlockPos.ZERO,host);
        var item=new ItemStack(Items.PAPER);item.set(DataComponents.CUSTOM_NAME,Component.literal("願い事"));host.setItem(0,item);host.setItem(1,new ItemStack(Items.PAPER));
        assertEquals(InteractionResult.SUCCESS,WorkshopRackEventHandlers.onRightClickBlock(p,InteractionHand.MAIN_HAND,BlockPos.ZERO.above(),Direction.NORTH));assertEquals(1,p.inventory.received);assertTrue(host.getItem(1).isEmpty());
        assertEquals(InteractionResult.CONSUME,WorkshopRackEventHandlers.onRightClickBlock(p,InteractionHand.OFF_HAND,BlockPos.ZERO.above(),Direction.NORTH));assertEquals(1,p.inventory.received);
        var held=new ItemStack(Items.STICK,8);ema.useItemOn(held,w.getBlockState(BlockPos.ZERO),w,BlockPos.ZERO,p,InteractionHand.MAIN_HAND,null);
        assertEquals(8,held.getCount());assertEquals(2,p.inventory.received);assertEquals("願い事",p.inventory.last.getHoverName().getString());assertTrue(host.isEmpty());
    }
    @Test void fullInventoryDropsOnlyRemovedItemAndPermissionsPreventTaking() throws Exception {
        var w=world();place(w,ofuda,BlockPos.ZERO,Direction.NORTH);var p=player(w);p.sneak=true;var host=new Station(BlockPos.ZERO,w.getBlockState(BlockPos.ZERO));w.entities.put(BlockPos.ZERO,host);host.setItem(0,new ItemStack(Items.PAPER));
        p.build=false;assertEquals(InteractionResult.FAIL,WorkshopRackEventHandlers.onRightClickBlock(p,InteractionHand.MAIN_HAND,BlockPos.ZERO,Direction.NORTH));assertFalse(host.isEmpty());
        p.build=true;p.inventory.accepts=false;WorkshopRackEventHandlers.onRightClickBlock(p,InteractionHand.MAIN_HAND,BlockPos.ZERO,Direction.NORTH);assertEquals(1,p.drops);assertTrue(host.isEmpty());
    }
    @Test void sixSlotsSurviveSaveAndDisplayInTwoRowsWithoutMovingNeighborInventory() throws Exception {
        var w=world();place(w,ema,BlockPos.ZERO,Direction.NORTH);var p=player(w);var host=new Station(BlockPos.ZERO,w.getBlockState(BlockPos.ZERO));w.entities.put(BlockPos.ZERO,host);
        for(int i=0;i<6;i++)assertTrue(host.insert(new ItemStack(Items.PAPER),p));assertFalse(host.insert(new ItemStack(Items.PAPER),p));assertEquals(6,host.getContainerSize());
        var parts=WorkshopDisplays.parts(host);assertEquals(6,parts.size());assertEquals(3,parts.stream().filter(part->part.y()>1).count());assertEquals(3,parts.stream().filter(part->part.y()<1).count());
        var access=RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
        var out=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,access);host.save(out);
        var restored=new Station(BlockPos.ZERO,w.getBlockState(BlockPos.ZERO));restored.load(TagValueInput.create(ProblemReporter.DISCARDING,access,out.buildResult()));assertEquals(6,restored.displayItems().stream().filter(i->!i.isEmpty()).count());
        place(w,ema,BlockPos.ZERO.east(),Direction.NORTH);assertSame(host,w.getBlockEntity(BlockPos.ZERO));assertEquals(6,host.displayItems().stream().filter(i->!i.isEmpty()).count());
    }
    @Test void upperHalfHasNoSecondInventoryAndMirrorsPreserveSectionEndpoints() {
        assertNull(ema.newBlockEntity(BlockPos.ZERO,ema.defaultBlockState().setValue(WorkshopRackBlock.HALF,DoubleBlockHalf.UPPER)));
        var state=ema.defaultBlockState().setValue(WorkshopRackBlock.WIDTH,3).setValue(WorkshopRackBlock.INDEX,0);
        for(var mirror:Mirror.values())assertEquals(state,ema.mirror(ema.mirror(state,mirror),mirror));
    }
    static class Station extends WorkshopStationBlockEntity {
        Station(BlockPos pos,BlockState state){super(stationType,pos,state);}
        @Override public boolean accepts(ItemStack stack){return stack.is(Items.PAPER);}
        void save(ValueOutput out){saveAdditional(out);}
        void load(ValueInput in){loadAdditional(in);}
    }
    static class World extends ServerLevel {
        Map<BlockPos,BlockState> states;Map<BlockPos,BlockEntity> entities;int notifications;
        World(){super(null,null,null,null,null,null,false,0,List.of(),false);}
        @Override public BlockState getBlockState(BlockPos pos){return states.getOrDefault(pos,Blocks.AIR.defaultBlockState());}
        @Override public BlockEntity getBlockEntity(BlockPos pos){return entities.get(pos);}
        @Override public boolean isClientSide(){return false;}
        @Override public boolean mayInteract(Entity player,BlockPos pos){return true;}
        @Override public boolean setBlock(BlockPos pos,BlockState state,int flags){BlockState old=getBlockState(pos);if(old==state)return false;states.put(pos.immutable(),state);return true;}
        @Override public void updateNeighborsAt(BlockPos pos,Block block){notifications++;}
        @Override public boolean destroyBlock(BlockPos pos,boolean drop){var old=getBlockState(pos);if(old.isAir())return false;states.put(pos.immutable(),Blocks.AIR.defaultBlockState());if(old.getBlock() instanceof WorkshopRackBlock rack)rack.affectNeighborsAfterRemoval(old,this,pos,false);return true;}
    }
    static class TestPlayer extends Player {
        World world;TestInventory inventory;boolean sneak,build;int drops;
        TestPlayer(){super(null,null);}
        @Override public Level level(){return world;}
        @Override public boolean isShiftKeyDown(){return sneak;}
        @Override public boolean mayBuild(){return build;}
        @Override public GameType gameMode(){return GameType.SURVIVAL;}
        @Override public boolean hasInfiniteMaterials(){return false;}
        @Override public Inventory getInventory(){return inventory;}
        @Override public ItemEntity drop(ItemStack stack,boolean random){drops++;return null;}
    }
    static class TestInventory extends Inventory {
        boolean accepts;int received;ItemStack last;
        TestInventory(){super(null,null);}
        @Override public boolean add(ItemStack stack){if(!accepts)return false;last=stack.copy();received+=stack.getCount();stack.setCount(0);return true;}
    }
}
