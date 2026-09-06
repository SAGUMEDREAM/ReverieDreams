package cc.thonly.reverie_dreams.item.prop;

import cc.thonly.reverie_dreams.component.RoleFollowerArchive;
import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponentTypes;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class SoulCardItem extends Item {
    public static final SoundEvent SOUND = SoundEvents.BUCKET_FILL;

    public SoulCardItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level contextWorld = context.getLevel();
        Player player = context.getPlayer();
        if (!contextWorld.isClientSide() && contextWorld instanceof ServerLevel world && player != null) {
            ItemStack stack = context.getItemInHand();
            DataComponentType<RoleFollowerArchive> dataComponentType = RDDataComponentTypes.ROLE_FOLLOWER_ARCHIVE.value();
            RoleFollowerArchive followerArchive = stack.get(dataComponentType);
            if (followerArchive == null) {
                return InteractionResult.PASS;
            }
            BaseNPCLikeEntity entity = followerArchive.respawn(world, context.getClickedPos().above(), world.registryAccess());
            entity.setOwner(player);
            world.playSound(null, player.getX(), player.getY(), player.getZ(), SOUND, player.getSoundSource(), 2.0f, 1.0f);
            stack.set(DataComponents.ITEM_NAME, stack.getItem().getDefaultInstance().getItemName());
            stack.remove(dataComponentType);
            player.swing(context.getHand());
            return InteractionResult.SUCCESS_SERVER;
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return super.isFoil(stack) || stack.has(RDDataComponentTypes.ROLE_FOLLOWER_ARCHIVE.value());
    }
}
