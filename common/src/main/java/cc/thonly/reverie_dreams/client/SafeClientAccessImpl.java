package cc.thonly.reverie_dreams.client;

import cc.thonly.reverie_dreams.api.proxy.SafeClientAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

public class SafeClientAccessImpl implements SafeClientAccess {
    @Override
    public Player getClientPlayer() {
        return Minecraft.getInstance().player;
    }
}
