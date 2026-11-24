package cc.thonly.reverie_dreams.polymer.helper;

import cc.thonly.reverie_dreams.polymer.entity.bridge.PolymerHolderEntity;
import cc.thonly.reverie_dreams.polymer.entity.bridge.TickHolderEntity;
import net.minecraft.SharedConstants;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import sun.misc.Unsafe;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ServerEntityHolderQueueTest {
    private static Unsafe unsafe;

    @BeforeAll
    static void bootstrap() throws Exception {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        var field = Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        unsafe = (Unsafe) field.get(null);
    }

    @AfterEach
    void clearTickFixtures() {
        TickHolderEntity.LIST.clear();
        TickHolderEntity.ELEMENT_BINDS.clear();
    }

    @Test
    void clientLevelEntityNeverInvokesServerAttachmentCallback() throws Exception {
        var queue = new ServerEntityHolderQueue();
        var client = (ClientLevel) unsafe.allocateInstance(ClientLevel.class);
        var calls = new AtomicInteger();
        queue.add(holder(calls::incrementAndGet), entity(client));
        queue.drain(server());
        assertEquals(0, calls.get());
    }

    @Test
    void serverEntityCreatesExactlyOnce() throws Exception {
        var owner = server();
        var queue = new ServerEntityHolderQueue();
        var calls = new AtomicInteger();
        queue.add(holder(calls::incrementAndGet), entity(world(owner)));
        queue.drain(owner);
        queue.drain(owner);
        assertEquals(1, calls.get());
    }

    @Test
    void entityRemovedBeforeDeferredCreationIsDiscarded() throws Exception {
        var owner = server();
        var queue = new ServerEntityHolderQueue();
        var source = entity(world(owner));
        var calls = new AtomicInteger();
        queue.add(holder(calls::incrementAndGet), source);
        var reason = Entity.class.getDeclaredField("removalReason");
        reason.setAccessible(true);
        reason.set(source, Entity.RemovalReason.DISCARDED);
        queue.drain(owner);
        assertEquals(0, calls.get());
    }

    @Test
    void changingToClientLevelAfterEnqueueCannotCreateServerAttachment() throws Exception {
        var owner = server();
        var source = entity(world(owner));
        var queue = new ServerEntityHolderQueue();
        var calls = new AtomicInteger();
        queue.add(holder(calls::incrementAndGet), source);
        source.world = (ClientLevel) unsafe.allocateInstance(ClientLevel.class);
        queue.drain(owner);
        assertEquals(0, calls.get());
    }

    @Test
    void previousServerEntitiesAreNotCreatedByAnotherServer() throws Exception {
        var queue = new ServerEntityHolderQueue();
        var calls = new AtomicInteger();
        queue.add(holder(calls::incrementAndGet), entity(world(server())));
        queue.drain(server());
        assertEquals(0, calls.get());
    }

    @Test
    void creationCallbackCanEnqueueAnotherHolderWithoutIteratorFailure() throws Exception {
        var owner = server();
        var source = entity(world(owner));
        var queue = new ServerEntityHolderQueue();
        var calls = new AtomicInteger();
        queue.add(holder(() -> {
            calls.incrementAndGet();
            queue.add(holder(calls::incrementAndGet), source);
        }), source);
        queue.drain(owner);
        queue.drain(owner);
        assertEquals(2, calls.get());
    }

    @Test
    void concurrentProducersDoNotLoseOrDuplicateServerCreations() throws Exception {
        var owner = server();
        var source = entity(world(owner));
        var queue = new ServerEntityHolderQueue();
        var calls = new AtomicInteger();
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            var thread = new Thread(() -> {
                for (int j = 0; j < 256; j++) queue.add(holder(calls::incrementAndGet), source);
            });
            threads.add(thread);
            thread.start();
        }
        for (var thread : threads) thread.join();
        queue.drain(owner);
        queue.drain(owner);
        assertEquals(1024, calls.get());
    }

    @Test
    void tickingRejectsClientAndPreviousServerSources() throws Exception {
        var owner = server();
        var calls = new AtomicInteger();
        TickHolderEntity.addTickHolder(tickHolder(entity((ClientLevel) unsafe.allocateInstance(ClientLevel.class)), calls));
        TickHolderEntity.addTickHolder(tickHolder(entity(world(server())), calls));
        TickHolderEntity.addTickHolder(tickHolder(entity(world(owner)), calls));
        TickHolderEntity.tick(owner);
        assertEquals(1, calls.get());
        assertEquals(1, TickHolderEntity.LIST.size());
    }

    private static PolymerHolderEntity holder(Runnable callback) {
        return new PolymerHolderEntity() {
            @Override public void onCreated() { callback.run(); }
        };
    }

    private static TickHolderEntity tickHolder(LivingEntity source, AtomicInteger calls) {
        return new TickHolderEntity() {
            @Override public LivingEntity getSource() { return source; }
            @Override public void onTick() { calls.incrementAndGet(); }
        };
    }

    // As in the common entity tests, skip engine constructors. Only real level
    // type, server identity and removal state are needed; no window/world runs.
    private static MinecraftServer server() throws Exception {
        return (DedicatedServer) unsafe.allocateInstance(DedicatedServer.class);
    }

    private static FixtureLevel world(MinecraftServer owner) throws Exception {
        var level = (FixtureLevel) unsafe.allocateInstance(FixtureLevel.class);
        level.owner = owner;
        return level;
    }

    private static FixtureEntity entity(Level world) throws Exception {
        var entity = (FixtureEntity) unsafe.allocateInstance(FixtureEntity.class);
        entity.world = world;
        return entity;
    }

    private static class FixtureEntity extends ArmorStand {
        Level world;
        private FixtureEntity() { super(EntityType.ARMOR_STAND, null); }
        @Override public Level level() { return this.world; }
    }

    private static class FixtureLevel extends ServerLevel {
        MinecraftServer owner;
        private FixtureLevel() { super(null, null, null, null, null, null, false, 0, List.of(), false); }
        @Override public MinecraftServer getServer() { return this.owner; }
    }
}
