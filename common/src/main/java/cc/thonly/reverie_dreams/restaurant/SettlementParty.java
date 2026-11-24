package cc.thonly.reverie_dreams.restaurant;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

/** A server-side player or NPC that can receive a restaurant settlement. */
public interface SettlementParty {
    LivingEntity getBaseEntity();

    void settle(Entity source, Settlement settlement);

    /** The mixin adds this interface to ServerPlayer, so javac needs the Object cast. */
    static SettlementParty of(ServerPlayer player) {
        return (SettlementParty) (Object) player;
    }
}
