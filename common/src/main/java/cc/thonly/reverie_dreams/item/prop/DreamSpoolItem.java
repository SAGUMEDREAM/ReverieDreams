package cc.thonly.reverie_dreams.item.prop;

import cc.thonly.reverie_dreams.entity.misc.GapPortalEntity;
import cc.thonly.reverie_dreams.world.dimension.RDBuiltinLevels;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class DreamSpoolItem extends Item {
    public DreamSpoolItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player user, InteractionHand hand) {
        if (!(user instanceof ServerPlayer player) || !(level instanceof ServerLevel server)) {
            return InteractionResult.SUCCESS;
        }
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(stack))
            return InteractionResult.FAIL;
        if (!server.dimension().equals(RDBuiltinLevels.DREAM_WORLD)) {
            player.sendSystemMessage(Component.translatable("message.reverie_dreams.dream_spool.only_in_dream"), true);
            return InteractionResult.FAIL;
        }
        BlockPos entrance = player.blockPosition().relative(player.getDirection(), 2);
        GapPortalEntity portal = GapPortalEntity.dreamReturn(server);
        portal.setPos(entrance.getX() + 0.5, entrance.getY(), entrance.getZ() + 0.5);
        portal.setYRot(player.getYRot());
        if (!server.isLoaded(entrance) || !player.mayInteract(server, entrance)
                || !server.noBlockCollision(portal, portal.getBoundingBox()) || !server.addFreshEntity(portal)) {
            player.sendSystemMessage(Component.translatable("dialog.gap.no_space"), true);
            return InteractionResult.FAIL;
        }
        player.getCooldowns().addCooldown(stack, 10 * 20);
        return InteractionResult.SUCCESS_SERVER;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        return player == null ? InteractionResult.PASS : use(context.getLevel(), player, context.getHand());
    }
}
