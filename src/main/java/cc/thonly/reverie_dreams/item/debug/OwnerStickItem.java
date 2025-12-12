package cc.thonly.reverie_dreams.item.debug;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class OwnerStickItem extends Item {
    public OwnerStickItem(Properties settings) {
        super(settings);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player user, LivingEntity entity, InteractionHand hand) {
        Level world = user.level();
        if (world.isClientSide()) return InteractionResult.SUCCESS;
        if (entity instanceof TamableAnimal target) {
            target.setOwnerUUID(user.getUUID());
            ((ServerLevel) world).sendParticles(ParticleTypes.HEART, target.getX(), target.getY() + 1.0, target.getZ(), 5, 0.5, 0.5, 0.5, 0.1);
        }
        return InteractionResult.SUCCESS_SERVER;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}
