package cc.thonly.reverie_dreams.entity.npc;

import net.minecraft.world.entity.animal.parrot.Parrot;
import org.jspecify.annotations.Nullable;

public interface ClientNPCSimulator {
    default boolean isModelPartShown(Object part) {
        return true;
    }
    default Parrot.@Nullable Variant getParrotVariantOnShoulder(Object part) {
        return null;
    }

    default boolean showExtraEars() {
        return false;
    }

    default boolean isPlayerUpsideDown() {
        return false;
    }

    ServerAvatarState avatarState();
}
