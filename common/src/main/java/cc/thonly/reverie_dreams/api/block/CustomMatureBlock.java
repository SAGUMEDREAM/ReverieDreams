package cc.thonly.reverie_dreams.api.block;

import java.util.Optional;

public interface CustomMatureBlock {
    static Optional<CustomMatureBlock> tryCastIMatureBlock(Object object) {
        if (object instanceof CustomMatureBlock) {
            return Optional.of((CustomMatureBlock) object);
        }
        return Optional.empty();
    }
}
