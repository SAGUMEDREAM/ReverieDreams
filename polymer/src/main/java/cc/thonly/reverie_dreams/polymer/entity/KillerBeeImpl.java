package cc.thonly.reverie_dreams.polymer.entity;

import cc.thonly.reverie_dreams.entity.KillerBee;
import eu.pb4.polymer.core.api.entity.PolymerEntity;
import net.minecraft.world.entity.EntityType;
import xyz.nucleoid.packettweaker.PacketContext;

public record KillerBeeImpl(KillerBee killerBee) implements PolymerEntity {
    @Override
    public EntityType<?> getPolymerEntityType(PacketContext context) {
        return EntityType.BEE;
    }
}
