package cc.thonly.reverie_dreams.polymer.entity;

import cc.thonly.reverie_dreams.entity.misc.OreEspEntity;
import eu.pb4.polymer.core.api.entity.PolymerEntity;
import net.minecraft.world.entity.EntityType;
import xyz.nucleoid.packettweaker.PacketContext;

public record OreEspImpl(OreEspEntity oreEspEntity) implements PolymerEntity {
    @Override
    public EntityType<?> getPolymerEntityType(PacketContext packetContext) {
        return EntityType.BLOCK_DISPLAY;
    }
}
