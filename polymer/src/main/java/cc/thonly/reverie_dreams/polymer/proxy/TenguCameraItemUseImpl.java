package cc.thonly.reverie_dreams.polymer.proxy;

import cc.thonly.reverie_dreams.proxy.TenguCameraItemUse;
import cc.thonly.reverie_dreams.registry.content.advancements.RDCriteriaTriggers;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponents;
import cc.thonly.reverie_dreams.registry.tag.RDItemTags;
import cc.thonly.reverie_dreams.sound.RDSoundEvents;
import cc.thonly.reverie_dreams.util.sound.SoundEventPlayUtils;
import de.tomalbrc.cameraobscura.ModConfig;
import de.tomalbrc.cameraobscura.command.CameraCommand;
import de.tomalbrc.cameraobscura.render.renderer.CanvasImageRenderer;
import eu.pb4.mapcanvas.api.core.CanvasImage;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class TenguCameraItemUseImpl implements TenguCameraItemUse {
    @Override
    public InteractionResult handle(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {

            int fov = stack.getOrDefault(RDDataComponents.FOV.value(), 75);

            float pitch = player.getXRot();

            int delta = pitch < 0 ? +1 : -1;

            int newFov = fov + delta;

            if (newFov < 30) newFov = 30;
            if (newFov > 110) newFov = 110;

            stack.set(RDDataComponents.FOV.value(), newFov);

            ((ServerPlayer) player).sendSystemMessage(
                    Component.literal("§a" + newFov),
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
        ModConfig instance = ModConfig.getInstance();
        instance.renderEntities = true;
        try {
            CanvasImageRenderer renderer = new CanvasImageRenderer(player, 128, 128, instance.renderDistance);
            int fov = stack.getOrDefault(RDDataComponents.FOV.value(), 75);
            int oldFov = instance.fov;
            instance.fov = fov;
            ItemStack finalCunsumeStack = cunsumeStack;
            CompletableFuture.supplyAsync(renderer::render).thenAcceptAsync((mapImage) -> {
                instance.fov = oldFov;
                player.getCooldowns().addCooldown(player.getItemInHand(hand), 20 * 4);
                player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
                level.playSound(null, player.blockPosition(), RDSoundEvents.PHOTO.value(), SoundSource.PLAYERS);
                if (!finalCunsumeStack.isEmpty()) {
                    finalCunsumeStack.consume(1, player);
                }
                RDCriteriaTriggers.USE_ITEM.value().trigger((ServerPlayer) player, finalCunsumeStack);
                this.finalizeTenguCamera(mapImage, (ServerPlayer) player);
            }, level.getServer());
        } catch (Exception err) {
            log.error("Can't render canvas", err);
        }
        return InteractionResult.SUCCESS_SERVER;
    }

    private void finalizeTenguCamera(CanvasImage canvasImage, ServerPlayer player) {
        if (player != null && !player.isRemoved()) {
            SoundEventPlayUtils.playSound(player, SoundEvents.SPYGLASS_STOP_USING, SoundSource.NEUTRAL, 1.0f, 1.0f);
            List<ItemStack> items = CameraCommand.mapItems(canvasImage, player.level());
            items.forEach((x) -> {
                if (!player.addItem(x)) {
                    player.spawnAtLocation(player.level(), x);
                }

            });
        }
    }
}
