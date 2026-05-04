package cc.thonly.reverie_dreams.item.prop;

import cc.thonly.reverie_dreams.RDMPHooks;
import cc.thonly.reverie_dreams.networking.payload.StartScreenshotPacket;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponents;
import cc.thonly.reverie_dreams.registry.tag.RDItemTags;
import cc.thonly.reverie_dreams.server.SessionManager;
import cc.thonly.reverie_dreams.util.PlatformContext;
import lombok.extern.slf4j.Slf4j;
import net.blay09.mods.balm.Balm;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.UUID;

@Slf4j
public class TenguCameraItem extends Item {

    public TenguCameraItem(Properties settings) {
        super(settings);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (PlatformContext.hasPolymer()) {
            return this.useByPolymer(level, player, hand);
        }
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            ItemStack stack = player.getItemInHand(hand);
            if (player.isShiftKeyDown()) {
                int fov = stack.getOrDefault(RDDataComponents.FOV.value(), 75);
                int newFov = fov + 1;
                if (newFov < 30) newFov = 30;
                if (newFov > 110) newFov = 110;

                stack.set(RDDataComponents.FOV.value(), newFov);

                serverPlayer.sendSystemMessage(
                        Component.literal("§aFov: " + newFov),
                        true
                );

                return InteractionResult.SUCCESS_SERVER;
            }
            Inventory inventory = player.getInventory();
            ItemStack cunsumeStack = ItemStack.EMPTY;
            for (ItemStack itemStack : inventory) {
                if (itemStack.isEmpty()) {
                    continue;
                }
                if (itemStack.is(RDItemTags.REPLACEABLE_BLANK_PHOTOS)) {
                    cunsumeStack = itemStack;
                    break;
                }
            }
            if (cunsumeStack.isEmpty() && !player.isCreative()) {
                return InteractionResult.FAIL;
            }
            UUID sessionId = UUID.randomUUID();
            SessionManager.startSession(serverPlayer.getUUID(), sessionId);
            Balm.networking().sendTo(serverPlayer, new StartScreenshotPacket(sessionId));
            return InteractionResult.SUCCESS_SERVER;
        }
        return InteractionResult.SUCCESS;
    }

    public InteractionResult useByPolymer(Level level, Player player, InteractionHand hand) {
        return RDMPHooks.TenguCameraItemUseCallback.EVENT.invoker().handle(level, player, hand);
    }

}
