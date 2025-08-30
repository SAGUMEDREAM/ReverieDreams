package cc.thonly.polymer.entity;

import cc.thonly.reverie_dreams.entity.KillerBeeEntity;
import eu.pb4.polymer.core.api.entity.PolymerEntity;
import net.minecraft.entity.EntityType;
import xyz.nucleoid.packettweaker.PacketContext;

public record KillerBeeImpl(KillerBeeEntity killerBee) implements PolymerEntity {
    @Override
    public EntityType<?> getPolymerEntityType(PacketContext context) {
        return EntityType.BEE;
    }
}
