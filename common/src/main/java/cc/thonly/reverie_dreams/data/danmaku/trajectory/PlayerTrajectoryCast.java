package cc.thonly.reverie_dreams.data.danmaku.trajectory;

import cc.thonly.reverie_dreams.data.danmaku.stg.motion.DanmakuMotion;
import cc.thonly.reverie_dreams.data.danmaku.stg.runtime.SpellCastOrigin;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Outstanding rounds for the two three-shot firing patterns.
 */
public final class PlayerTrajectoryCast {
    private static final List<PlayerTrajectoryCast> ACTIVE = new ArrayList<>();
    private static final int INTERVAL = 3;

    private final ServerLevel level;
    private final LivingEntity owner;
    @Nullable
    private final InteractionHand hand;
    private final ItemStack held;
    private final ItemStack ammunition;
    private final List<Vec3> directions;
    private final float speed;
    private final float damage;
    private final long startedAt;
    private int nextShot = 1;

    private PlayerTrajectoryCast(ServerLevel level, LivingEntity owner, @Nullable InteractionHand hand,
                                 ItemStack held, List<Vec3> directions, float speed, float damage) {
        this.level = level;
        this.owner = owner;
        this.hand = hand;
        this.held = held;
        this.ammunition = held.copy();
        this.directions = directions;
        this.speed = speed;
        this.damage = damage;
        this.startedAt = level.getGameTime();
    }

    static void start(ServerLevel level, @Nullable LivingEntity owner, @Nullable InteractionHand hand,
                      ItemStack stack, Vec3 source, List<Vec3> directions, float speed, float damage) {
        PlayerTrajectoryFlight.launch(level, owner, stack, source, source,
                DanmakuMotion.straight(directions.getFirst().scale(speed)), damage, null);
        if (owner != null) {
            // Never retain a second pending volley for the same caster.
            ACTIVE.removeIf(cast -> cast.owner == owner);
            ACTIVE.add(new PlayerTrajectoryCast(level, owner, hand, stack, directions, speed, damage));
        }
    }

    public static void tick(MinecraftServer server) {
        ACTIVE.removeIf(cast -> cast.level.getServer() == server && cast.advance());
        PlayerTrajectoryFlight.pruneSeeds();
    }

    public static void clear() {
        ACTIVE.clear();
        PlayerTrajectoryFlight.clearSeeds();
    }

    private boolean advance() {
        if (!this.owner.isAlive() || this.owner.level() != this.level
                || (this.hand != null && (this.held.isEmpty() || this.owner.getItemInHand(this.hand) != this.held))) {
            return true;
        }
        if (this.level.getGameTime() - this.startedAt >= this.nextShot * INTERVAL) {
            Vec3 body = SpellCastOrigin.fromBody(this.level, this.owner).position();
            PlayerTrajectoryFlight.launch(this.level, this.owner, this.ammunition, body, body,
                    DanmakuMotion.straight(this.directions.get(this.nextShot).scale(this.speed)), this.damage, null);
            this.nextShot++;
        }
        return this.nextShot >= this.directions.size();
    }
}
