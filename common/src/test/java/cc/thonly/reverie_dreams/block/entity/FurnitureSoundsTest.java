package cc.thonly.reverie_dreams.block.entity;

import cc.thonly.reverie_dreams.block.decor.living.HangingOrnamentBlock;
import cc.thonly.reverie_dreams.block.decor.workshop.WorkshopKind;
import cc.thonly.reverie_dreams.workshop.FurnitureSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.*;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.*;
import org.junit.jupiter.api.*;
import java.util.*;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import static org.junit.jupiter.api.Assertions.*;

class FurnitureSoundsTest {
    @BeforeAll static void bootstrap() throws Exception { WorkshopInteractionTest.bootstrap(); }
    static class World extends ServerLevel {
        boolean client,acceptChanges;BlockState state;List<SoundEvent> sounds;List<Float> pitches;int scheduled;
        World(){super(null,null,null,null,null,null,false,0,List.of(),false);}
        @Override public boolean isClientSide(){return client;}
        @Override public boolean setBlock(BlockPos pos,BlockState state,int flags){if(!acceptChanges)return false;this.state=state;return true;}
        @Override public BlockState getBlockState(BlockPos pos){return state;}
        @Override public RandomSource getRandom(){return RandomSource.create(17);}
        @Override public void scheduleTick(BlockPos pos,Block block,int delay){scheduled++;}
        @Override public void playSound(Entity excluded,BlockPos pos,SoundEvent sound,SoundSource source,float volume,float pitch){
            assertNull(excluded,"initiating player must receive the same server broadcast");
            assertEquals(SoundSource.BLOCKS,source);assertTrue(volume>0&&volume<=.6f);
            sounds.add(sound);pitches.add(pitch);
        }
    }
    World world(boolean client) throws Exception {
        var level=(World)WorkshopInteractionTest.unsafe.allocateInstance(World.class);level.client=client;level.acceptChanges=true;level.sounds=new ArrayList<>();level.pitches=new ArrayList<>();return level;
    }
    static class Station extends WorkshopInteractionTest.Station {
        Station(){super(WorkshopKind.YAGEN,WorkshopInteractionTest.recipe(Items.BLAZE_ROD,Items.BLAZE_POWDER,3,8));}
        @Override public void setChanged(){} // Network and persistence are unrelated to sound dispatch.
    }
    static class Storage extends WorkshopInteractionTest.Storage { @Override public void setChanged(){} }
    @Test void broadcastIsServerOnlyAndOnce() throws Exception {
        var server=world(false);FurnitureSounds.play(server,BlockPos.ZERO,SoundEvents.BOOK_PUT,.3f,1);
        assertEquals(List.of(SoundEvents.BOOK_PUT),server.sounds);
        var prediction=world(true);FurnitureSounds.play(prediction,BlockPos.ZERO,SoundEvents.BOOK_PUT,.3f,1);
        assertTrue(prediction.sounds.isEmpty());FurnitureSounds.play(null,BlockPos.ZERO,SoundEvents.BOOK_PUT,.3f,1);
    }
    private HangingOrnamentBlock chime() throws Exception {
        var registry=BuiltInRegistries.BLOCK;
        var holders=MappedRegistry.class.getDeclaredField("unregisteredIntrusiveHolders");
        var frozen=MappedRegistry.class.getDeclaredField("frozen");holders.setAccessible(true);frozen.setAccessible(true);
        Object previous=holders.get(registry);boolean wasFrozen=frozen.getBoolean(registry);
        try {
            frozen.setBoolean(registry,false);holders.set(registry,new IdentityHashMap<>());
            return new HangingOrnamentBlock(HangingOrnamentBlock.Kind.CHIME,
                    BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK,Identifier.fromNamespaceAndPath("test","sound_chime"))));
        } finally {holders.set(registry,previous);frozen.setBoolean(registry,wasFrozen);}
    }
    @Test void bothAmbientToggleDirectionsRingExactlyOnce() throws Exception {
        var block=chime();var level=world(false);var player=new WorkshopInteractionTest().player();level.state=block.defaultBlockState();
        block.useWithoutItem(level.state,level,BlockPos.ZERO,player,null);
        assertTrue(level.state.getValue(HangingOrnamentBlock.POWERED));assertEquals(0,level.scheduled);
        assertEquals(List.of(SoundEvents.BELL_BLOCK),level.sounds);assertEquals(1.8f,level.pitches.getFirst());
        block.useWithoutItem(level.state,level,BlockPos.ZERO,player,null);
        assertFalse(level.state.getValue(HangingOrnamentBlock.POWERED));assertEquals(1,level.scheduled);
        assertEquals(List.of(SoundEvents.BELL_BLOCK,SoundEvents.BELL_BLOCK),level.sounds);
    }
    @Test void chimePredictionAndRejectedStateChangeAreSilent() throws Exception {
        var block=chime();var level=world(true);level.state=block.defaultBlockState();
        block.useWithoutItem(level.state,level,BlockPos.ZERO,null,null);assertTrue(level.sounds.isEmpty());
        level.client=false;level.acceptChanges=false;
        assertEquals(InteractionResult.FAIL,block.useWithoutItem(level.state,level,BlockPos.ZERO,null,null));assertTrue(level.sounds.isEmpty());
    }
    @Test void grindingCooldownAndEmptyStationCannotRepeatSound() throws Exception {
        var host=new Station();var level=world(false);host.setLevel(level);var player=new WorkshopInteractionTest().player();
        assertFalse(host.grind(0));assertTrue(level.sounds.isEmpty());
        assertTrue(host.insert(new ItemStack(Items.BLAZE_ROD),player));assertEquals(1,level.sounds.size());
        assertFalse(host.insert(new ItemStack(Items.BLAZE_ROD),player));assertEquals(1,level.sounds.size());
        for(int i=0;i<8;i++){assertTrue(host.grind(i*4));assertFalse(host.grind(i*4));}
        assertEquals(8,level.sounds.stream().filter(s->s==SoundEvents.GRINDSTONE_USE).count());
        assertFalse(host.grind(100));assertTrue(host.take(player));int sounds=level.sounds.size();assertFalse(host.take(player));assertEquals(sounds,level.sounds.size());
    }
    @Test void sortingAndSyncDoNotMakeIdleInventoriesNoisy() throws Exception {
        var host=new Storage();var level=world(false);host.setLevel(level);
        host.sortContents();assertTrue(level.sounds.isEmpty());
        host.setItem(0,new ItemStack(Items.STICK));host.setItem(1,new ItemStack(Items.BOOK));host.sortContents();
        assertEquals(List.of(SoundEvents.BUNDLE_INSERT),level.sounds);
        host.sortContents();host.setChanged();assertEquals(1,level.sounds.size());
    }
}
