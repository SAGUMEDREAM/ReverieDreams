package cc.thonly.reverie_dreams.paper.npc;

import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;
import java.util.EnumSet;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;

/** Source speed=1.4, stopping distance=2, navigation retry=10 ticks. No teleport or chunk loads. */
final class NPCFollowGoal implements Goal<Wolf> {
    private final NPCService service;
    private final Wolf carrier;
    private final GoalKey<Wolf> key;
    private int nextAttempt;

    NPCFollowGoal(NPCService service, Wolf carrier, GoalKey<Wolf> key) {
        this.service = service; this.carrier = carrier; this.key = key;
    }

    private Player owner() {
        if (!carrier.isValid() || carrier.isDead() || carrier.isLeashed() || carrier.getVehicle() != null
                || service.mode(carrier) != NPCMode.FOLLOW || carrier.getTarget() != null) return null;
        var ownerId = service.ownerId(carrier);
        var owner = ownerId == null ? null : Bukkit.getPlayer(ownerId);
        if (owner == null || !owner.isOnline() || owner.isDead() || owner.getWorld() != carrier.getWorld()) return null;
        double distance = owner.getLocation().distanceSquared(carrier.getLocation());
        return distance > 4.0 && distance <= 32.0 * 32.0 ? owner : null;
    }

    @Override public boolean shouldActivate() { return owner() != null; }
    @Override public boolean shouldStayActive() { return owner() != null; }
    @Override public void start() { nextAttempt = 0; }
    @Override public void stop() { carrier.getPathfinder().stopPathfinding(); }
    @Override public void tick() {
        var owner = owner();
        if (owner == null) { stop(); return; }
        carrier.lookAt(owner, 10.0f, carrier.getMaxHeadPitch());
        int now = Bukkit.getCurrentTick();
        if (now - nextAttempt < 0) return;
        nextAttempt = now + 10;
        carrier.getPathfinder().moveTo(owner, 1.4);
    }
    @Override public GoalKey<Wolf> getKey() { return key; }
    @Override public EnumSet<GoalType> getTypes() { return EnumSet.of(GoalType.MOVE, GoalType.LOOK); }
}
