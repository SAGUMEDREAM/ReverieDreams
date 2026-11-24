package cc.thonly.reverie_dreams.item.weapon;

import com.mojang.authlib.GameProfile;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import sun.misc.Unsafe;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class YoumuSwordLifecycleTest {
    private static Unsafe unsafe;
    @BeforeAll static void bootstrap() throws Exception {
        SharedConstants.tryDetectVersion(); Bootstrap.bootStrap();
        var field=Unsafe.class.getDeclaredField("theUnsafe");field.setAccessible(true);unsafe=(Unsafe)field.get(null);
        for(Item item:new Item[]{Items.DIAMOND_SWORD,Items.IRON_SWORD})if(!item.builtInRegistryHolder().areComponentsBound())
            item.builtInRegistryHolder().bindComponents(DataComponentMap.builder().set(DataComponents.MAX_STACK_SIZE,1).build());
    }
    @SuppressWarnings("unchecked") private static Map<Player,Object> states() throws Exception {
        var field=YoumuSwordCombat.class.getDeclaredField("STATES");field.setAccessible(true);
        return (Map<Player,Object>)field.get(null);
    }
    @AfterEach void clear() throws Exception { states().clear(); }

    @Test void clientTickMustNotEraseTheServerStateWithTheSameEntityId() throws Exception {
        var server=player(false);var client=player(true);
        assertNotSame(server,client);
        // Real Entity.equals/hashCode: mirrors on both sides share the network entity ID.
        assertEquals(server,client);assertEquals(server.hashCode(),client.hashCode());
        Object sentinel=new Object();states().put(server,sentinel);
        // A client callback must return before it even inspects the server-owned State.
        assertDoesNotThrow(()->YoumuSwordCombat.tick(client));
        assertSame(sentinel,states().get(server));
    }
    @Test void interleavedClientAndServerTicksKeepChargingThroughBothThresholds() throws Exception {
        var server=player(false);var client=player(true);
        server.held=new ItemStack(Items.DIAMOND_SWORD);server.using=true;
        Object state=seedState(server);
        set(state,"charge",server.held);set(state,"hand",InteractionHand.MAIN_HAND);
        for(int tick=1;tick<=16;tick++){
            server.world.time=tick;client.world.time=tick;
            YoumuSwordCombat.tick(server);
            YoumuSwordCombat.tick(client);
            YoumuSwordCombat.onUseTick(server.world,server,server.held);
            assertSame(state,states().get(server));assertTrue(server.using);
            assertSame(server.held,get(state,"charge"));
        }
        assertEquals(2,get(state,"chargeStage"));
        assertEquals(100,YoumuSwordFeedback.chargePercent(16));
    }
    @Test void cooldownAndReadinessAreIndependentForTheTwoSwords() throws Exception {
        var player=player(false);var longSword=new ItemStack(Items.DIAMOND_SWORD);var shortSword=new ItemStack(Items.IRON_SWORD);
        YoumuSwordCombat.cooldown(player,longSword,100);
        assertFalse(YoumuSwordCombat.ready(player,longSword));assertTrue(YoumuSwordCombat.ready(player,shortSword));
        advance(player,20);YoumuSwordCombat.cooldown(player,shortSword,30);advance(player,30);
        assertFalse(YoumuSwordCombat.ready(player,longSword));assertTrue(YoumuSwordCombat.ready(player,shortSword));
        assertEquals(.5f,player.cooldowns.getCooldownPercent(longSword,0),.0001f);
        advance(player,50);assertTrue(YoumuSwordCombat.ready(player,longSword));
    }
    @Test void whiteSwordCooldownDoesNotBlockOrResetTheLongSword() throws Exception {
        var player=player(false);var longSword=new ItemStack(Items.DIAMOND_SWORD);var shortSword=new ItemStack(Items.IRON_SWORD);
        YoumuSwordCombat.cooldown(player,shortSword,100);advance(player,20);
        assertTrue(YoumuSwordCombat.ready(player,longSword));assertFalse(YoumuSwordCombat.ready(player,shortSword));
        YoumuSwordCombat.cooldown(player,longSword,12);
        assertEquals(.8f,player.cooldowns.getCooldownPercent(shortSword,0),.0001f);
        advance(player,12);assertTrue(YoumuSwordCombat.ready(player,longSword));assertFalse(YoumuSwordCombat.ready(player,shortSword));
    }
    @Test void anExternalCooldownResetDoesNotLeaveStaleSwordState() throws Exception {
        var player=player(false);var sword=new ItemStack(Items.DIAMOND_SWORD);
        YoumuSwordCombat.cooldown(player,sword,100);
        player.cooldowns.removeCooldown(player.cooldowns.getCooldownGroup(sword));
        YoumuSwordCombat.tick(player);
        assertTrue(YoumuSwordCombat.ready(player,sword));assertFalse(states().containsKey(player));
    }
    private static void advance(FixturePlayer player,int ticks){for(int i=0;i<ticks;i++){player.world.time++;player.cooldowns.tick();}}
    private static Object seedState(FixturePlayer player) throws Exception{
        var method=YoumuSwordCombat.class.getDeclaredMethod("state",Player.class);method.setAccessible(true);return method.invoke(null,player);
    }
    private static void set(Object object,String name,Object value)throws Exception{var f=object.getClass().getDeclaredField(name);f.setAccessible(true);f.set(object,value);}
    private static Object get(Object object,String name)throws Exception{var f=object.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(object);}
    private static FixturePlayer player(boolean client) throws Exception {
        var world=(FixtureLevel)unsafe.allocateInstance(FixtureLevel.class);world.client=client;
        var player=(FixturePlayer)unsafe.allocateInstance(FixturePlayer.class);player.world=world;
        player.cooldowns=new ItemCooldowns();player.held=ItemStack.EMPTY;
        var position=Entity.class.getDeclaredField("position");position.setAccessible(true);position.set(player,Vec3.ZERO);
        var blockPosition=Entity.class.getDeclaredField("blockPosition");blockPosition.setAccessible(true);blockPosition.set(player,BlockPos.ZERO);
        return player;
    }
    private static class FixtureLevel extends ServerLevel {
        boolean client;long time;
        private FixtureLevel(){super(null,null,null,null,null,null,false,0,List.of(),false);}
        @Override public boolean isClientSide(){return client;}
        @Override public long getGameTime(){return time;}
        @Override public ResourceKey<Level> dimension(){return Level.OVERWORLD;}
        @Override public List<ServerPlayer> players(){return List.of();}
        @Override public void playSound(Entity source,BlockPos pos,SoundEvent event,SoundSource category,float volume,float pitch){}
    }
    private static class FixturePlayer extends Player {
        FixtureLevel world;ItemCooldowns cooldowns;ItemStack held;boolean using;
        private FixturePlayer(){super(null,new GameProfile(UUID.randomUUID(),"sword-lifecycle"));}
        @Override public Level level(){return world;}
        @Override public GameType gameMode(){return GameType.SURVIVAL;}
        @Override public ItemCooldowns getCooldowns(){return cooldowns;}
        @Override public boolean isAlive(){return true;}
        @Override public boolean isSpectator(){return false;}
        @Override public boolean isPassenger(){return false;}
        @Override public boolean isSleeping(){return false;}
        @Override public ItemStack getItemInHand(InteractionHand hand){return held;}
        @Override public ItemStack getMainHandItem(){return held;}
        @Override public ItemStack getOffhandItem(){return ItemStack.EMPTY;}
        @Override public ItemStack getUseItem(){return using?held:ItemStack.EMPTY;}
        @Override public boolean isUsingItem(){return using;}
        @Override public void stopUsingItem(){using=false;}
    }
}
