package cc.thonly.polymer.entity;

import cc.thonly.reverie_dreams.entity.WildPigEntity;
import eu.pb4.polymer.core.api.entity.PolymerEntity;
import net.minecraft.world.entity.EntityType;
import xyz.nucleoid.packettweaker.PacketContext;

public record WildPigImpl(WildPigEntity wildPig) implements PolymerEntity {

    @Override
    public EntityType<?> getPolymerEntityType(PacketContext context) {
        return EntityType.PIG;
    }

}
