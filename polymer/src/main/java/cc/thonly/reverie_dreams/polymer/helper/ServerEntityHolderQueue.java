package cc.thonly.reverie_dreams.polymer.helper;

import cc.thonly.reverie_dreams.polymer.entity.bridge.PolymerHolderEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

final class ServerEntityHolderQueue {
    private final Queue<Pending> pending = new ConcurrentLinkedQueue<>();

    void add(PolymerHolderEntity holder, Entity source) {
        if (source.level() instanceof ServerLevel) {
            this.pending.add(new Pending(holder, source));
        }
    }

    void drain(MinecraftServer server) {
        Pending next;
        while ((next = this.pending.poll()) != null) {
            Entity source = next.source();
            if (!source.isRemoved() && source.level() instanceof ServerLevel level && level.getServer() == server) {
                next.holder().onCreated();
            }
        }
    }

    private record Pending(PolymerHolderEntity holder, Entity source) {}
}
