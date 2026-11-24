package cc.thonly.reverie_dreams.item.prop;

import cc.thonly.reverie_dreams.entity.misc.DreamCocoonEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class DreamCocoonItem extends Item {
    public DreamCocoonItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (!(target instanceof Mob mob) || !mob.isAlive() || mob.isVehicle() || mob.isPassenger() || mob.isSleeping()
                || mob.getType() == EntityType.ENDER_DRAGON || mob.getType() == EntityType.WITHER
                || mob.getType() == EntityType.WARDEN) {
            return InteractionResult.PASS;
        }
        if (player.level() instanceof ServerLevel level) {
            if (!player.mayInteract(level, mob.blockPosition()) || !DreamCocoonEntity.wrap(level, mob)) {
                return InteractionResult.FAIL;
            }
            stack.consume(1, player);
            return InteractionResult.SUCCESS_SERVER;
        }
        return InteractionResult.SUCCESS;
    }
}
