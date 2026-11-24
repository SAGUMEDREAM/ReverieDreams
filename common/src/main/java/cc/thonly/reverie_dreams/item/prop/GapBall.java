package cc.thonly.reverie_dreams.item.prop;

import cc.thonly.reverie_dreams.component.GapRecorder;
import cc.thonly.reverie_dreams.entity.misc.GapPortalEntity;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponentTypes;
import cc.thonly.reverie_dreams.server.dialog.gap.GapDialog;
import cc.thonly.reverie_dreams.util.entity.PlayerHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.List;

public class GapBall extends Item {

    public GapBall(Properties settings) {
        super(settings);
    }

    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        ItemStack stack = user.getItemInHand(hand);
        if (world.isClientSide() || !(user instanceof ServerPlayer player)) {
            return InteractionResult.SUCCESS;
        }

        // Sneak-use is the configuration dialog. A normal use on a block opens a rift;
        // using it on air also opens the dialog so the player can record a destination.
        if (player.isShiftKeyDown()) {
            GapDialog.open(player, hand);
            return InteractionResult.SUCCESS_SERVER;
        }

        BlockHitResult hit = PlayerHelper.getPlayerLookBlock(player, player.blockInteractionRange());
        if (hit.getType() != HitResult.Type.BLOCK) {
            GapDialog.open(player, hand);
            return InteractionResult.SUCCESS_SERVER;
        }

        GapRecorder target = findTarget(player, stack);
        if (target == null) {
            player.sendSystemMessage(Component.translatable("dialog.gap.no_target"), false);
            GapDialog.open(player, hand);
            return InteractionResult.SUCCESS_SERVER;
        }

        Identifier targetId = Identifier.tryParse(target.world());
        if (targetId == null) {
            player.sendSystemMessage(Component.translatable(
                    "dialog.gap.invalid_data", target.world()), false);
            return InteractionResult.SUCCESS_SERVER;
        }
        ResourceKey<Level> targetDimension = ResourceKey.create(Registries.DIMENSION, targetId);
        ServerLevel serverLevel = (ServerLevel) player.level();
        if (serverLevel.getServer() == null || serverLevel.getServer().getLevel(targetDimension) == null) {
            player.sendSystemMessage(Component.translatable(
                    "dialog.gap.dimension_missing", targetId), false);
            return InteractionResult.SUCCESS_SERVER;
        }

        BlockPos space = hit.getBlockPos().relative(hit.getDirection());
        BlockState below = serverLevel.getBlockState(space);
        BlockState above = serverLevel.getBlockState(space.above());
        if (!below.canBeReplaced() || !above.canBeReplaced()) {
            player.sendSystemMessage(Component.translatable("dialog.gap.no_space"), false);
            return InteractionResult.SUCCESS_SERVER;
        }

        GapPortalEntity portal = new GapPortalEntity(serverLevel, targetDimension, target.value(), target.name());
        portal.setPos(space.getX() + 0.5, space.getY(), space.getZ() + 0.5);
        portal.setYRot(player.getYRot());
        portal.setXRot(0.0F);
        serverLevel.addFreshEntity(portal);

        player.getCooldowns().addCooldown(stack, 5 * 20);
        player.sendSystemMessage(Component.translatable("dialog.gap.opened", target.name()), false);
        return InteractionResult.SUCCESS_SERVER;
    }

    private GapRecorder findTarget(ServerPlayer player, ItemStack stack) {
        List<GapRecorder> recorders = stack.getOrDefault(
                RDDataComponentTypes.GAP_RECORDER.value(),
                List.of()
        );
        int selected = stack.getOrDefault(RDDataComponentTypes.GAP_TARGET_INDEX.value(), 0);
        if (selected >= 0 && selected < recorders.size() && recorders.get(selected).enable()) {
            return recorders.get(selected);
        }
        for (int i = 0; i < recorders.size(); i++) {
            GapRecorder recorder = recorders.get(i);
            if (recorder.enable()) {
                stack.set(RDDataComponentTypes.GAP_TARGET_INDEX.value(), i);
                player.getInventory().setChanged();
                return recorder;
            }
        }
        return null;
    }
}