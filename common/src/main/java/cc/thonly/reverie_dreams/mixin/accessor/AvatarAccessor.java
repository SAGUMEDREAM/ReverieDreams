package cc.thonly.reverie_dreams.mixin.accessor;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.HumanoidArm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Avatar.class)
public interface AvatarAccessor {
    @Accessor("DATA_PLAYER_MODE_CUSTOMISATION")
    static EntityDataAccessor<Byte> getPlayerModelParts() {
        throw new UnsupportedOperationException();
    }

    @Accessor("DATA_PLAYER_MAIN_HAND")
    static EntityDataAccessor<HumanoidArm> getPlayerMainHand() {
        throw new UnsupportedOperationException();
    }
}