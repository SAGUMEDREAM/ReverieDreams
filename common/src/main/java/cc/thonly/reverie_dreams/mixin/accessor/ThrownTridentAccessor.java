package cc.thonly.reverie_dreams.mixin.accessor;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.projectile.arrow.ThrownTrident;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ThrownTrident.class)
public interface ThrownTridentAccessor {
    @Accessor("ID_LOYALTY")
    static EntityDataAccessor<Byte> reverie_dreams$getLoyaltyId() {
        throw new AssertionError();
    }

    @Accessor("ID_FOIL")
    static EntityDataAccessor<Boolean> reverie_dreams$getFoilId() {
        throw new AssertionError();
    }
}
