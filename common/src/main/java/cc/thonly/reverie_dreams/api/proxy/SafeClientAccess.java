package cc.thonly.reverie_dreams.api.proxy;

import net.minecraft.world.entity.player.Player;

import java.util.concurrent.atomic.AtomicReference;

public interface SafeClientAccess {
    AtomicReference<SafeClientAccess> ref = new AtomicReference<>(new DefaultImpl());

    Player getClientPlayer();

    static SafeClientAccess safeClientAccess() {
        return ref.get();
    }

    class DefaultImpl implements SafeClientAccess {

        @Override
        public Player getClientPlayer() {
            return null;
        }

    }
}
