package cc.thonly.reverie_dreams.api.proxy;

import cc.thonly.reverie_dreams.ReverieDreams;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.world.entity.player.Player;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public interface SafeClientAccess {
    AtomicReference<SafeClientAccess> ref = new AtomicReference<>(new DefaultImpl());

    boolean isClientSide();

    Player getClientPlayer();

    ClientInstanceAccess getClientInstance();

    static SafeClientAccess safeClientAccess() {
        return ref.get();
    }

    interface ClientInstanceAccess extends Supplier<Object> {
        Object get();

        void execute(Runnable command);
    }

    @Slf4j
    class DefaultImpl implements SafeClientAccess {

        @Override
        public boolean isClientSide() {
            return false;
        }

        @Override
        public Player getClientPlayer() {
            return null;
        }

        @Override
        public ClientInstanceAccess getClientInstance() {
            return new ClientInstanceAccess() {
                @Override
                public Object get() {
                    return null;
                }

                @Override
                public void execute(Runnable command) {
                    log.error("Client does's not exists");
                }
            };
        }
    }
}
