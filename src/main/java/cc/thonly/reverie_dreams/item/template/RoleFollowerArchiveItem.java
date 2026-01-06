package cc.thonly.reverie_dreams.item.template;

import cc.thonly.polymer.item.IBasicPolymerItem;
import cc.thonly.reverie_dreams.component.RoleFollowerArchive;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class RoleFollowerArchiveItem extends Item implements IBasicPolymerItem {
    public static final SoundEvent SOUND = SoundEvents.BUCKET_FILL;

    public RoleFollowerArchiveItem(Properties settings) {
        super(settings);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level contextWorld = context.getLevel();
        Player player = context.getPlayer();
        if (!contextWorld.isClientSide() && contextWorld instanceof ServerLevel world && player != null) {
            ItemStack stack = context.getItemInHand();
            RoleFollowerArchive followerArchive = stack.get(RDDataComponents.ROLE_FOLLOWER_ARCHIVE);
            if (followerArchive == null) {
                return InteractionResult.PASS;
            }
            boolean canRespawn = stack.getOrDefault(RDDataComponents.ROLE_CAN_RESPAWN, false);
            if (!canRespawn) {
                return InteractionResult.PASS;
            }
            followerArchive.respawn(world, context.getClickedPos().above(), world.registryAccess());
            world.playSound(null, player.getX(), player.getY(), player.getZ(), SOUND, player.getSoundSource(), 2.0f, 1.0f);
            stack.consume(1, player);
            player.swing(context.getHand());
            return InteractionResult.SUCCESS_SERVER;
        }
        return InteractionResult.SUCCESS;
    }

}
