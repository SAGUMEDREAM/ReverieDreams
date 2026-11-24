package cc.thonly.reverie_dreams.paper.equipment;

import cc.thonly.reverie_dreams.paper.util.Ids;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.GenericGameEvent;
import org.bukkit.inventory.EquipmentSlot;

/** Explicit sound-source bridge. GenericGameEvent is a vibration subset, never described as every sound. */
final class EquipmentSoundBridge implements Listener {
    private record Source(UUID source, UUID world, double x, double y, double z) { }
    private static final class Beam {
        final Location start, end;
        int step = 1;
        Beam(Location start, Location end) { this.start = start; this.end = end; }
    }
    private final EquipmentModule module;
    private final ConcurrentLinkedQueue<Source> sources = new ConcurrentLinkedQueue<>();
    private final ArrayList<Beam> beams = new ArrayList<>();
    EquipmentSoundBridge(EquipmentModule module) { this.module = module; }

    void record(UUID source, UUID world, double x, double y, double z) {
        if (world == null || !Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)) return;
        sources.add(new Source(source, world, x, y, z));
    }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onVibration(GenericGameEvent event) {
        if (!Bukkit.isPrimaryThread()) return;
        var location = event.getLocation(); var entity = event.getEntity();
        record(entity == null ? null : entity.getUniqueId(), event.getWorld().getUID(), location.getX(), location.getY(), location.getZ());
    }
    void tick() {
        Source source;
        while ((source = sources.poll()) != null) {
            var world = Bukkit.getWorld(source.world()); if (world == null) continue;
            var location = new Location(world, source.x(), source.y(), source.z());
            Entity origin = source.source() == null ? null : Bukkit.getEntity(source.source());
            if (origin instanceof Player player && player.getGameMode() == GameMode.SPECTATOR) continue;
            for (var player : world.getPlayers()) {
                if (player.getGameMode() == GameMode.SPECTATOR || player.getUniqueId().equals(source.source())
                        || !module.wears(player, EquipmentSlot.HEAD, Ids.MOD_ID + ":earphone")
                        || player.getLocation().distanceSquared(location) >= 32.0 * 32.0) continue;
                beams.add(new Beam(location.clone(), player.getLocation()));
            }
        }
        // Source ParticleTickerManager increments once in hasNext and again in next: t=.05,.15,...,1.05.
        for (var iterator = beams.iterator(); iterator.hasNext();) {
            var beam = iterator.next();
            if (beam.step > 21) { iterator.remove(); continue; }
            double t = beam.step / 20.0; beam.step += 2;
            var delta = beam.end.toVector().subtract(beam.start.toVector()).multiply(t);
            var point = beam.start.clone().add(delta);
            point.getWorld().spawnParticle(Particle.SONIC_BOOM, point, 1, 0, 0, 0, 0.1);
        }
    }
    void clear() { sources.clear(); beams.clear(); }
}
