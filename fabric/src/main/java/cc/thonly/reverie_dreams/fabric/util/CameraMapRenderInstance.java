package cc.thonly.reverie_dreams.fabric.util;

import eu.pb4.mapcanvas.api.core.DrawableCanvas;
import eu.pb4.mapcanvas.api.core.PlayerCanvas;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class CameraMapRenderInstance {
    private final ServerPlayer player;
    private final PlayerCanvas playerCanvas = DrawableCanvas.create();
    public CameraMapRenderInstance(ServerPlayer player) {
        this.player = player;
    }

    private void init() {
        this.playerCanvas.addPlayer(this.player);
    }

    public ItemStack make() {
        return this.playerCanvas.asStack();
    }
}
