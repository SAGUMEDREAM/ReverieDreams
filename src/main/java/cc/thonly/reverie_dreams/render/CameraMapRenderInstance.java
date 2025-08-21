package cc.thonly.reverie_dreams.render;

import eu.pb4.mapcanvas.api.core.DrawableCanvas;
import eu.pb4.mapcanvas.api.core.PlayerCanvas;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

public class CameraMapRenderInstance {
    private final ServerPlayerEntity player;
    private final PlayerCanvas playerCanvas = DrawableCanvas.create();
    public CameraMapRenderInstance(ServerPlayerEntity player) {
        this.player = player;
    }

    private void init() {
        this.playerCanvas.addPlayer(this.player);
    }

    public ItemStack make() {
        return this.playerCanvas.asStack();
    }
}
