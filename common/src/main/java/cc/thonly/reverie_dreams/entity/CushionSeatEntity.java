package cc.thonly.reverie_dreams.entity;

import cc.thonly.reverie_dreams.registry.content.entity.RDEntityTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Its origin is the cushion surface, with no chair-height passenger lift. */
public class CushionSeatEntity extends SeatEntity {
    public CushionSeatEntity(Level level) { this(RDEntityTypes.CUSHION_SEAT.value(), level); }
    public CushionSeatEntity(EntityType<?> type, Level level) { super(type, level); }
    @Override public Vec3 getPassengerRidingPosition(Entity passenger) { return position(); }
}
