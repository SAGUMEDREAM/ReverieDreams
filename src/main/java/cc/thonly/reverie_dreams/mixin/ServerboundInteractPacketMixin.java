package cc.thonly.reverie_dreams.mixin;

import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ServerboundInteractPacket.class)
public class ServerboundInteractPacketMixin {
//    @Shadow @Final private int entityId;
//
//    @Shadow @Final private boolean usingSecondaryAction;
//
//    @Shadow @Final private ServerboundInteractPacket.Action action;
//
//    @Inject(method = "<init>(IZLnet/minecraft/network/protocol/game/ServerboundInteractPacket$Action;)V", at = @At("TAIL"))
//    public void test(int i, boolean bl, ServerboundInteractPacket.Action action, CallbackInfo ci) {
//        System.out.println(this.entityId);
//        System.out.println(this.action);
//        System.out.println(this.usingSecondaryAction);
//    }
}
