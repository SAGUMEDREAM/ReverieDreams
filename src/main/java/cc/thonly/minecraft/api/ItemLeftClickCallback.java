package cc.thonly.minecraft.api;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public interface ItemLeftClickCallback {
    Event<ItemLeftClickCallback> EVENT = EventFactory.createArrayBacked(ItemLeftClickCallback.class, (listener) -> (world, player, hand) -> {
        for (ItemLeftClickCallback callback : listener) {
            callback.leftClick(world, player, hand);
        }
    });

    void leftClick(Level level, Player player, InteractionHand hand);
}
