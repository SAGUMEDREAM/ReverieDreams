package cc.thonly.reverie_dreams.inf;

import java.util.Optional;

public interface IMatureBlock {
    static Optional<IMatureBlock> tryCastIMatureBlock(Object object) {
        if (object instanceof IMatureBlock) {
            return Optional.of((IMatureBlock) object);
        }
        return Optional.empty();
    }
}
