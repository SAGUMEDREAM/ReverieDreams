package cc.thonly.reverie_dreams.mixin.entity;

import cc.thonly.reverie_dreams.inventory.storage.StorageRule;
import cc.thonly.reverie_dreams.inventory.storage.StorageWorld;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Locale;

@Mixin(ItemFrame.class)
public abstract class ItemFrameStorageMixin {
    @Inject(method = "interact", at = @At("RETURN"))
    private void reverie_dreams$storageHint(Player player, InteractionHand hand, Vec3 location,
                                            CallbackInfoReturnable<InteractionResult> cir) {
        ItemFrame frame = (ItemFrame) (Object) this;
        if (!(player instanceof ServerPlayer serverPlayer) || !(frame.level() instanceof ServerLevel level)
                || !cir.getReturnValue().consumesAction() || frame.getItem().isEmpty())
            return;
        var pos = StorageWorld.attachedTo(frame);
        if (!level.hasChunkAt(pos) || !StorageWorld.supported(level.getBlockEntity(pos)))
            return;
        var mode = StorageRule.modeForRotation(frame.getRotation()).name().toLowerCase(Locale.ROOT);
        var rule = StorageWorld.resolve(level, pos);
        if (rule != null && rule.rule().mode() == StorageRule.Mode.CONFLICT) {
            serverPlayer.sendOverlayMessage(Component.translatable("gui.reverie_dreams.task_reason.storage_rule_conflict"));
        } else {
            serverPlayer.sendOverlayMessage(Component.translatable("message.reverie_dreams.storage.mode." + mode,
                    frame.getItem().getHoverName()));
        }
    }
}
