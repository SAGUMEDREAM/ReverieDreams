package cc.thonly.reverie_dreams.block;

import cc.thonly.reverie_dreams.block.decor.RakedGravelBlock;
import cc.thonly.reverie_dreams.block.decor.RakedGravelBlock.Mode;
import cc.thonly.reverie_dreams.block.decor.RakedGravelBlock.Shape;
import cc.thonly.reverie_dreams.block.decor.RakedGravelGeometry;
import cc.thonly.reverie_dreams.event.block.RakedGravelEventHandlers;
import com.mojang.authlib.GameProfile;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import sun.misc.Unsafe;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class RakedGravelTest {
    private static Unsafe unsafe;
    private static RakedGravelBlock block;
    private static final BlockPos POS = new BlockPos(0,64,0);

    @BeforeAll static void bootstrap() throws Exception {
        SharedConstants.tryDetectVersion(); Bootstrap.bootStrap();
        var field=Unsafe.class.getDeclaredField("theUnsafe"); field.setAccessible(true); unsafe=(Unsafe)field.get(null);
        var holders=MappedRegistry.class.getDeclaredField("unregisteredIntrusiveHolders");
        var frozen=MappedRegistry.class.getDeclaredField("frozen"); holders.setAccessible(true); frozen.setAccessible(true);
        Object previous=holders.get(BuiltInRegistries.BLOCK); boolean wasFrozen=frozen.getBoolean(BuiltInRegistries.BLOCK);
        try {
            frozen.setBoolean(BuiltInRegistries.BLOCK,false); holders.set(BuiltInRegistries.BLOCK,new IdentityHashMap<>());
            block=new RakedGravelBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GRAVEL)
                    .setId(ResourceKey.create(Registries.BLOCK,Identifier.parse("test:raked_gravel"))));
        } finally { holders.set(BuiltInRegistries.BLOCK,previous); frozen.setBoolean(BuiltInRegistries.BLOCK,wasFrozen); }
        // In 26.x item components are normally bound by the world's registry load.
        // These tests use vanilla tool classes with their normal durability inputs.
        for (Item item : new Item[]{Items.IRON_HOE,Items.IRON_SHOVEL,Items.DIAMOND_HOE})
            if (!item.builtInRegistryHolder().areComponentsBound()) item.builtInRegistryHolder().bindComponents(
                    DataComponentMap.builder().set(DataComponents.MAX_STACK_SIZE,1)
                            .set(DataComponents.MAX_DAMAGE,item==Items.DIAMOND_HOE?1561:250).set(DataComponents.DAMAGE,0).build());
        for (Item item : new Item[]{Items.AIR,Items.STICK})
            if (!item.builtInRegistryHolder().areComponentsBound()) item.builtInRegistryHolder().bindComponents(
                    DataComponentMap.builder().set(DataComponents.MAX_STACK_SIZE,64).build());
    }

    @Test void autoLayoutsJoinACompleteCornerRingAndCapAfterRemoval() throws Exception {
        FixtureLevel level=level();
        BlockPos[] ring={POS,POS.east(),POS.south(),POS.south().east()};
        for(var pos:ring) level.states.put(pos,block.defaultBlockState());
        Shape[] expected={Shape.SOUTH_EAST,Shape.SOUTH_WEST,Shape.NORTH_EAST,Shape.NORTH_WEST};
        for(int i=0;i<ring.length;i++) {
            var state=block.withLayout(level.getBlockState(ring[i]),level,ring[i]);
            assertEquals(expected[i],state.getValue(RakedGravelBlock.SHAPE));
            assertEquals(3,RakedGravelBlock.visualConnections(state));
        }
        level.states.remove(POS.east());
        var updated=block.withLayout(block.defaultBlockState(),level,POS);
        assertEquals(Shape.NORTH_SOUTH,updated.getValue(RakedGravelBlock.SHAPE));
        assertFalse(updated.getValue(RakedGravelBlock.NORTH));
        assertTrue(updated.getValue(RakedGravelBlock.SOUTH));
    }

    @Test void denseFloorsRemainStraightAndManualDirectionsSurviveNeighborChanges() throws Exception {
        FixtureLevel level=level();
        for(int x=-1;x<=1;x++) for(int z=-1;z<=1;z++) level.states.put(POS.offset(x,0,z),block.defaultBlockState());
        assertEquals(Shape.NORTH_SOUTH,block.withLayout(block.defaultBlockState(),level,POS).getValue(RakedGravelBlock.SHAPE));
        assertEquals(Shape.EAST_WEST,block.withLayout(block.defaultBlockState().setValue(RakedGravelBlock.AXIS,Direction.Axis.X),level,POS).getValue(RakedGravelBlock.SHAPE));
        var fixed=block.rake(block.defaultBlockState(),false,level,POS);
        assertEquals(Mode.STRAIGHT,fixed.getValue(RakedGravelBlock.MODE));
        level.states.clear(); level.states.put(POS.north(),block.defaultBlockState());
        assertEquals(Shape.EAST_WEST,block.withLayout(fixed,level,POS).getValue(RakedGravelBlock.SHAPE));
        assertEquals(0,RakedGravelBlock.visualConnections(block.withLayout(fixed,level,POS)));
    }

    @Test void toolModesCycleAndFourRotationsReturnToTheOriginalCurve() throws Exception {
        FixtureLevel level=level(); var state=block.defaultBlockState();
        state=block.rake(state,true,level,POS); assertEquals(Mode.STRAIGHT,state.getValue(RakedGravelBlock.MODE));
        state=block.rake(state,true,level,POS); assertEquals(Mode.CURVE,state.getValue(RakedGravelBlock.MODE));
        var original=state; for(int i=0;i<4;i++) state=block.rake(state,false,level,POS);
        assertSame(original,state);
        state=block.rake(state,true,level,POS); assertEquals(Mode.AUTO,state.getValue(RakedGravelBlock.MODE));
        assertTrue(state.getValue(RakedGravelBlock.SHAPE).straight());
    }

    @Test void structureTransformsKeepEveryConnectionAligned() {
        for(var shape:Shape.values()) {
            var state=block.defaultBlockState().setValue(RakedGravelBlock.SHAPE,shape)
                    .setValue(RakedGravelBlock.link(shape.first),true);
            var rotated=state.rotate(Rotation.CLOCKWISE_90);
            assertTrue(rotated.getValue(RakedGravelBlock.link(shape.first.getClockWise())));
            assertSame(state,rotated.rotate(Rotation.COUNTERCLOCKWISE_90));
            for(var mirror:new Mirror[]{Mirror.FRONT_BACK,Mirror.LEFT_RIGHT}) assertSame(state,state.mirror(mirror).mirror(mirror));
        }
    }

    @Test void allModelsCoverExactlyOneSurfaceAndSharedGroovesMeetAtTheSamePixels() {
        for(var shape:Shape.values()) for(int links=0;links<4;links++) {
            int[][] coverage=new int[16][16];
            for(var patch:RakedGravelGeometry.patches(shape,links))
                for(int z=patch.z1();z<patch.z2();z++) for(int x=patch.x1();x<patch.x2();x++) {
                    coverage[z][x]++;
                    assertEquals(RakedGravelGeometry.groove(shape,links,x,z),patch.groove());
                }
            for(var row:coverage) for(int pixel:row) assertEquals(1,pixel);
            for(var side:Direction.Plane.HORIZONTAL) if(shape.has(side)) {
                for(int p=0;p<16;p++) {
                    int x=side==Direction.EAST?15:side==Direction.WEST?0:p;
                    int z=side==Direction.SOUTH?15:side==Direction.NORTH?0:p;
                    assertFalse(RakedGravelGeometry.groove(shape,0,x,z),"Unconnected ends must close");
                    for(var other:Shape.values()) if(other.has(side.getOpposite())) {
                        int ox=side.getAxis()==Direction.Axis.X?15-x:x;
                        int oz=side.getAxis()==Direction.Axis.Z?15-z:z;
                        assertEquals(RakedGravelGeometry.groove(shape,3,x,z),RakedGravelGeometry.groove(other,3,ox,oz));
                    }
                }
            }
        }
    }

    @Test void reciprocalLinksDoNotJoinTwoIncompatibleManualPatterns() throws Exception {
        FixtureLevel level=level();
        var ns=block.defaultBlockState().setValue(RakedGravelBlock.MODE,Mode.STRAIGHT);
        var ew=ns.setValue(RakedGravelBlock.SHAPE,Shape.EAST_WEST);
        level.states.put(POS,ns); level.states.put(POS.south(),ew);
        assertFalse(block.withLayout(ns,level,POS).getValue(RakedGravelBlock.SOUTH));
        assertFalse(block.withLayout(ew,level,POS.south()).getValue(RakedGravelBlock.NORTH));
    }

    @Test void survivalHoeRakesOnceAndShovelRestoresTheSameGravel() throws Exception {
        FixtureLevel level=level(); FixturePlayer player=player(level); player.main=new ItemStack(Items.IRON_HOE);
        assertEquals(InteractionResult.SUCCESS_SERVER,use(player,InteractionHand.MAIN_HAND,Direction.UP));
        assertTrue(level.getBlockState(POS).is(block)); assertEquals(1,player.main.getDamageValue());
        assertEquals(1,level.writes); assertEquals(1,level.sounds); assertEquals(1,level.events);
        player.main=new ItemStack(Items.IRON_SHOVEL);
        assertEquals(InteractionResult.SUCCESS_SERVER,use(player,InteractionHand.MAIN_HAND,Direction.UP));
        assertTrue(level.getBlockState(POS).is(Blocks.GRAVEL)); assertEquals(1,player.main.getDamageValue());
    }

    @Test void creativeOffhandChangesOnlyTheBlockAndKeepsToolDurability() throws Exception {
        FixtureLevel level=level(); FixturePlayer player=player(level); player.abilities.instabuild=true;
        player.off=new ItemStack(Items.DIAMOND_HOE);
        assertEquals(InteractionResult.SUCCESS_SERVER,use(player,InteractionHand.OFF_HAND,Direction.UP));
        assertEquals(0,player.off.getDamageValue()); assertTrue(player.main.isEmpty()); assertEquals(1,level.writes);
    }

    @Test void clientPredictionDoesNotWriteOrConsumeDurability() throws Exception {
        FixtureLevel level=level(); level.client=true; FixturePlayer player=player(level); player.main=new ItemStack(Items.IRON_HOE);
        assertEquals(InteractionResult.SUCCESS,use(player,InteractionHand.MAIN_HAND,Direction.UP));
        assertEquals(0,level.writes); assertEquals(0,player.main.getDamageValue()); assertEquals(0,level.sounds); assertEquals(0,level.events);
    }

    @Test void coveredSidesAndPermissionFailuresLeaveGravelUntouched() throws Exception {
        FixtureLevel level=level(); FixturePlayer player=player(level); player.main=new ItemStack(Items.IRON_HOE);
        assertEquals(InteractionResult.PASS,use(player,InteractionHand.MAIN_HAND,Direction.NORTH));
        level.states.put(POS.above(),Blocks.STONE.defaultBlockState()); assertEquals(InteractionResult.PASS,use(player,InteractionHand.MAIN_HAND,Direction.UP));
        level.states.remove(POS.above()); player.canBuild=false; assertEquals(InteractionResult.PASS,use(player,InteractionHand.MAIN_HAND,Direction.UP));
        player.canBuild=true; player.spectator=true; assertEquals(InteractionResult.PASS,use(player,InteractionHand.MAIN_HAND,Direction.UP));
        player.spectator=false; level.permitted=false; assertEquals(InteractionResult.PASS,use(player,InteractionHand.MAIN_HAND,Direction.UP));
        level.permitted=true; player.canUse=false; assertEquals(InteractionResult.PASS,use(player,InteractionHand.MAIN_HAND,Direction.UP));
        assertEquals(0,level.writes); assertEquals(0,player.main.getDamageValue());
    }

    @Test void failedWritesAndWrongToolsProduceNoSideEffects() throws Exception {
        FixtureLevel level=level(); FixturePlayer player=player(level); player.main=new ItemStack(Items.STICK);
        assertEquals(InteractionResult.PASS,use(player,InteractionHand.MAIN_HAND,Direction.UP));
        player.main=new ItemStack(Items.IRON_HOE); level.acceptWrites=false;
        assertEquals(InteractionResult.FAIL,use(player,InteractionHand.MAIN_HAND,Direction.UP));
        assertEquals(0,player.main.getDamageValue()); assertEquals(0,level.sounds); assertEquals(0,level.events);
    }

    private static InteractionResult use(FixturePlayer player,InteractionHand hand,Direction side) { return RakedGravelEventHandlers.interact(player,hand,POS,side,block); }
    private static FixtureLevel level() throws Exception {
        var level=(FixtureLevel)unsafe.allocateInstance(FixtureLevel.class); level.states=new HashMap<>();
        level.states.put(POS,Blocks.GRAVEL.defaultBlockState()); level.permitted=true; level.acceptWrites=true; return level;
    }
    private static FixturePlayer player(FixtureLevel level) throws Exception {
        var player=(FixturePlayer)unsafe.allocateInstance(FixturePlayer.class); player.fixtureLevel=level;
        player.main=ItemStack.EMPTY; player.off=ItemStack.EMPTY; player.abilities=new Abilities(); player.canBuild=true; player.canUse=true; return player;
    }
    private static class FixtureLevel extends ServerLevel {
        Map<BlockPos,BlockState> states; boolean client,permitted,acceptWrites; int writes,sounds,events;
        private FixtureLevel() { super(null,null,null,null,null,null,false,0,List.of(),false); }
        @Override public boolean isClientSide() { return client; }
        @Override public BlockState getBlockState(BlockPos pos) { return states.getOrDefault(pos,Blocks.AIR.defaultBlockState()); }
        @Override public boolean setBlock(BlockPos pos,BlockState state,int flags) { if(!acceptWrites)return false; states.put(pos.immutable(),state); writes++; return true; }
        @Override public void scheduleTick(BlockPos pos,Block target,int delay) { }
        @Override public boolean mayInteract(Entity entity,BlockPos pos) { return permitted; }
        @Override public void playSound(Entity entity,BlockPos pos,SoundEvent sound,SoundSource source,float volume,float pitch) { sounds++; }
        @Override public void gameEvent(Holder<GameEvent> event,Vec3 pos,GameEvent.Context context) { events++; }
    }
    private static class FixturePlayer extends Player {
        FixtureLevel fixtureLevel; ItemStack main,off; Abilities abilities; boolean canBuild,canUse,spectator;
        private FixturePlayer() { super(null,new GameProfile(UUID.randomUUID(),"raked-gravel-test")); }
        @Override public GameType gameMode() { return GameType.SURVIVAL; }
        @Override public Level level() { return fixtureLevel; }
        @Override public boolean mayBuild() { return canBuild; }
        @Override public boolean isSpectator() { return spectator; }
        @Override public boolean isShiftKeyDown() { return false; }
        @Override public boolean mayUseItemAt(BlockPos pos,Direction side,ItemStack stack) { return canUse; }
        @Override public ItemStack getItemInHand(InteractionHand hand) { return hand==InteractionHand.MAIN_HAND?main:off; }
        @Override public Abilities getAbilities() { return abilities; }
    }
}
