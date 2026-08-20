package cc.thonly.reverie_dreams.client;

import cc.thonly.reverie_dreams.api.proxy.SafeClientAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

public class SafeClientAccessImpl implements SafeClientAccess {

    @Override
    public boolean isClientSide() {
        return true;
    }

    @Override
    public Player getClientPlayer() {
        return Minecraft.getInstance().player;
    }

    @Override
    public ClientInstanceAccess getClientInstance() {
        return new ClientInstanceAccess() {
            @Override
            public Minecraft get() {
                return Minecraft.getInstance();
            }

            @Override
            public void execute(Runnable command) {
                this.get().execute(command);
            }
        };
    }
}
