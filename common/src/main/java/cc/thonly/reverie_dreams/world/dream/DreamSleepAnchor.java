package cc.thonly.reverie_dreams.world.dream;

import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;

public interface DreamSleepAnchor {
    @Nullable BlockPos reverie_dreams$getDreamSleepAnchor();

    void reverie_dreams$setDreamSleepAnchor(@Nullable BlockPos pos);
}
