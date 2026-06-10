package cc.thonly.reverie_dreams.mixin;

import cc.thonly.reverie_dreams.server.CustomClickActionRegistry;
import com.mojang.authlib.GameProfile;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.common.ServerboundCustomClickActionPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Slf4j
@Mixin(ServerCommonPacketListenerImpl.class)
public abstract class ServerCommonNetworkHandlerMixin {

    @Shadow
    protected abstract GameProfile playerProfile();

    @Inject(method = "handleCustomClickAction", at = @At("TAIL"))
    private void handleCustomClick(ServerboundCustomClickActionPacket packet, CallbackInfo ci) {
//        Identifier id = packet.id();
//        Optional<Tag> payload = packet.payload();
//        System.out.println(id);
//        if (payload.isPresent() && payload.get() instanceof CompoundTag tag) {
//            System.out.println(tag.toString());
//        }
        try {
            CustomClickActionRegistry.handle(this.playerProfile(), packet);
        } catch (Exception err) {
            log.error("Can't parse custom click action packet", err
            );
        }
    }
}
