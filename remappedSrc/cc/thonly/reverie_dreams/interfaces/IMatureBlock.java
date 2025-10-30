package cc.thonly.reverie_dreams.interfaces;

import java.util.Optional;

public interface IMatureBlock {
//    boolean isMature(BlockState state);

    static Optional<IMatureBlock> tryCastIMatureBlock(Object object) {
        if (object instanceof IMatureBlock) {
            return Optional.of((IMatureBlock) object);
        }
        return Optional.empty();
    }
}
