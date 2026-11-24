package cc.thonly.reverie_dreams.server;

import cc.thonly.reverie_dreams.block.entity.RemoteBlockEntity;
import net.minecraft.server.MinecraftServer;
import java.util.*;

/** Only loaded endpoints participate; saved signal levels cannot keep an absent sender on. */
public final class RemoteSignalManager {
    private static final RemoteSignalManager INSTANCE = new RemoteSignalManager();
    private record Channel(String name, String token) {}
    private final Map<Channel, Set<RemoteBlockEntity>> senders = new HashMap<>();
    private final Map<Channel, Set<RemoteBlockEntity>> receivers = new HashMap<>();
    private Channel channel(RemoteBlockEntity e) { return new Channel(e.getSignalName(), e.getSignalToken()); }
    public void clear() { senders.clear(); receivers.clear(); }
    public void reloadAll(MinecraftServer server) { clear(); }
    public void saveAll(MinecraftServer server) {} // Existing lifecycle hook; levels are runtime state.
    public void watch(RemoteBlockEntity e) {
        if (e.isEmpty()) { e.receive(false); return; }
        receivers.computeIfAbsent(channel(e), key -> Collections.newSetFromMap(new IdentityHashMap<>())).add(e);
        e.receive(isOccupied(e));
    }
    public void setValue(RemoteBlockEntity e, boolean powered) {
        if (e.isEmpty() || e.getRemoteType() != RemoteBlockEntity.RemoteType.SERVER) return;
        Channel key = channel(e);
        if (powered) senders.computeIfAbsent(key, ignored -> Collections.newSetFromMap(new IdentityHashMap<>())).add(e);
        else removeFrom(senders, key, e);
        publish(key);
    }
    public boolean isOccupied(RemoteBlockEntity e) {
        Set<RemoteBlockEntity> active = senders.get(channel(e));
        return active != null && !active.isEmpty();
    }
    public void remove(RemoteBlockEntity e) {
        Channel key = channel(e);
        removeFrom(receivers, key, e); removeFrom(senders, key, e); publish(key);
    }
    private void removeFrom(Map<Channel, Set<RemoteBlockEntity>> map, Channel key, RemoteBlockEntity e) {
        Set<RemoteBlockEntity> endpoints = map.get(key);
        if (endpoints != null && endpoints.remove(e) && endpoints.isEmpty()) map.remove(key);
    }
    private void publish(Channel key) {
        var listeners = receivers.get(key);
        if (listeners == null) return;
        boolean powered = senders.containsKey(key);
        for (var receiver : List.copyOf(listeners)) if (!receiver.isRemoved()) receiver.receive(powered);
    }
    public static RemoteSignalManager access() { return INSTANCE; }
}
