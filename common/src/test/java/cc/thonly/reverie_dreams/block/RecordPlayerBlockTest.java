package cc.thonly.reverie_dreams.block;

import cc.thonly.reverie_dreams.block.decor.RecordPlayerBlock;
import cc.thonly.reverie_dreams.event.block.RecordPlayerEventHandlers;
import net.minecraft.SharedConstants;
import net.minecraft.core.*;
import net.minecraft.core.registries.*;
import net.minecraft.resources.*;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.state.*;
import org.junit.jupiter.api.*;
import sun.misc.Unsafe;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class RecordPlayerBlockTest {
    static Unsafe unsafe;static RecordPlayerBlock block;
    @BeforeAll static void boot() throws Exception {
        SharedConstants.tryDetectVersion();Bootstrap.bootStrap();var f=Unsafe.class.getDeclaredField("theUnsafe");f.setAccessible(true);unsafe=(Unsafe)f.get(null);
        var holders=MappedRegistry.class.getDeclaredField("unregisteredIntrusiveHolders");var frozen=MappedRegistry.class.getDeclaredField("frozen");holders.setAccessible(true);frozen.setAccessible(true);
        var old=holders.get(BuiltInRegistries.BLOCK);boolean freeze=frozen.getBoolean(BuiltInRegistries.BLOCK);
        try{frozen.setBoolean(BuiltInRegistries.BLOCK,false);holders.set(BuiltInRegistries.BLOCK,new IdentityHashMap<>());block=new RecordPlayerBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK,Identifier.parse("test:record_player"))));}
        finally{holders.set(BuiltInRegistries.BLOCK,old);frozen.setBoolean(BuiltInRegistries.BLOCK,freeze);}
    }
    static Fixture fixture() throws Exception {var w=(World)unsafe.allocateInstance(World.class);w.state=block.defaultBlockState();var p=(TestPlayer)unsafe.allocateInstance(TestPlayer.class);p.world=w;p.build=true;return new Fixture(w,p);}
    record Fixture(World world,TestPlayer player) { }
    @Test void ordinaryRightClickTogglesBothDirections() throws Exception {
        var f=fixture();assertFalse(f.world.state.getValue(RecordPlayerBlock.PLAYING));
        block.useWithoutItem(f.world.state,f.world,BlockPos.ZERO,f.player,null);assertTrue(f.world.state.getValue(RecordPlayerBlock.PLAYING));assertEquals(1,f.world.sounds);
        block.useWithoutItem(f.world.state,f.world,BlockPos.ZERO,f.player,null);assertFalse(f.world.state.getValue(RecordPlayerBlock.PLAYING));assertEquals(2,f.world.sounds);
    }
    @Test void sneakEventOpensConfigurationWithoutChangingPlaybackOrRepeatingOffhand() throws Exception {
        var f=fixture();f.world.state=f.world.state.setValue(RecordPlayerBlock.PLAYING,true);f.player.sneak=true;
        assertEquals(InteractionResult.SUCCESS,RecordPlayerEventHandlers.onRightClickBlock(f.player,InteractionHand.MAIN_HAND,BlockPos.ZERO,Direction.UP));assertTrue(f.world.state.getValue(RecordPlayerBlock.PLAYING));assertEquals(0,f.world.sounds);
        assertEquals(InteractionResult.CONSUME,RecordPlayerEventHandlers.onRightClickBlock(f.player,InteractionHand.OFF_HAND,BlockPos.ZERO,Direction.UP));assertEquals(0,f.world.sounds);
    }
    @Test void regularEventPassesThroughAndDeniedPlayersCannotToggle() throws Exception {
        var f=fixture();assertEquals(InteractionResult.PASS,RecordPlayerEventHandlers.onRightClickBlock(f.player,InteractionHand.MAIN_HAND,BlockPos.ZERO,Direction.UP));
        f.player.build=false;assertEquals(InteractionResult.FAIL,block.setPlaying(f.world,BlockPos.ZERO,f.world.state,f.player,true));assertFalse(f.world.state.getValue(RecordPlayerBlock.PLAYING));
        f.player.build=true;f.world.denied=true;assertEquals(InteractionResult.FAIL,block.setPlaying(f.world,BlockPos.ZERO,f.world.state,f.player,true));assertEquals(0,f.world.sounds);
    }
    @Test void clientPredictionDoesNotMutateAndDedicatedSideHasNoTicker() throws Exception {
        var f=fixture();assertNull(block.getTicker(f.world,f.world.state,null));f.world.client=true;
        assertEquals(InteractionResult.SUCCESS,block.useItemOn(ItemStack.EMPTY,f.world.state,f.world,BlockPos.ZERO,f.player,InteractionHand.MAIN_HAND,null));assertFalse(f.world.state.getValue(RecordPlayerBlock.PLAYING));assertEquals(0,f.world.sounds);
    }
    static class World extends ServerLevel {
        BlockState state;boolean client,denied;int sounds;
        World(){super(null,null,null,null,null,null,false,0,List.of(),false);}
        @Override public BlockState getBlockState(BlockPos pos){return state;}
        @Override public boolean isClientSide(){return client;}
        @Override public boolean mayInteract(Entity entity,BlockPos pos){return !denied;}
        @Override public boolean setBlock(BlockPos pos,BlockState next,int flags){state=next;return true;}
        @Override public void playSound(Entity entity,BlockPos pos,SoundEvent event,SoundSource source,float volume,float pitch){sounds++;}
    }
    static class TestPlayer extends Player {
        World world;boolean sneak,build;
        TestPlayer(){super(null,null);}
        @Override public Level level(){return world;}
        @Override public boolean isShiftKeyDown(){return sneak;}
        @Override public boolean mayBuild(){return build;}
        @Override public GameType gameMode(){return GameType.SURVIVAL;}
    }
}
