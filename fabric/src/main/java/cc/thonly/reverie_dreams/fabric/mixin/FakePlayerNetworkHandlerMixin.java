package cc.thonly.reverie_dreams.fabric.mixin;

import cc.thonly.reverie_dreams.networking.FakePlayerNetworkHandler;
import net.fabricmc.fabric.impl.networking.UntrackedPacketListener;
import org.spongepowered.asm.mixin.Mixin;

@SuppressWarnings("UnstableApiUsage")
@Mixin(FakePlayerNetworkHandler.class)
public class FakePlayerNetworkHandlerMixin implements UntrackedPacketListener {
}
