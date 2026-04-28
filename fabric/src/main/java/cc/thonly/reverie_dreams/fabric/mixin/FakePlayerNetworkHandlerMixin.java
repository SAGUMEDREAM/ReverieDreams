package cc.thonly.reverie_dreams.fabric.mixin;

import cc.thonly.reverie_dreams.networking.FakePlayerNetworkHandler;
import net.fabricmc.fabric.impl.networking.UntrackedNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(FakePlayerNetworkHandler.class)
public class FakePlayerNetworkHandlerMixin implements UntrackedNetworkHandler {
}
