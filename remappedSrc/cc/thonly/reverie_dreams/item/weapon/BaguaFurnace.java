package cc.thonly.reverie_dreams.item.weapon;

import cc.thonly.reverie_dreams.danmaku.DanmakuTrajectory;
import cc.thonly.reverie_dreams.danmaku.DanmakuTypes;
import cc.thonly.reverie_dreams.entity.misc.BaguaFurnaceEntity;
import cc.thonly.reverie_dreams.entity.misc.DanmakuEntity;
import cc.thonly.reverie_dreams.server.DelayedTask;
import cc.thonly.reverie_dreams.sound.SoundEventInit;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class BaguaFurnace extends Item {
    public BaguaFurnace(Properties settings) {
        super(settings);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity user) {
        if (!world.isClientSide() && user instanceof ServerPlayer player) {
            ServerLevel serverWorld = (ServerLevel) world;
            BaguaFurnaceEntity baguaFurnaceEntity = new BaguaFurnaceEntity(serverWorld, user);
            serverWorld.addFreshEntity(baguaFurnaceEntity);

            FoodData hungerManager = player.getFoodData();
            hungerManager.eat(-6, -6);

            ItemCooldowns itemCooldownManager = player.getCooldowns();
            itemCooldownManager.addCooldown(stack, 20 * 40);
            if (!player.hasInfiniteMaterials()) {
                stack.hurtWithoutBreaking(15, player);
            }
        }
        return stack;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return Items.FLINT_AND_STEEL.useOn(context);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity user) {
        return 40;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.BOW;
    }

    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        ItemStack itemStack = user.getItemInHand(hand);
        if (!world.isClientSide && world instanceof ServerLevel serverWorld) {
            if (user.isShiftKeyDown()) {
                user.startUsingItem(hand);
                return InteractionResult.CONSUME;
            }
            world.playSound((Entity) null, user.getX(), user.getY(), user.getZ(), SoundEvents.SNOWBALL_THROW, SoundSource.NEUTRAL, 0.5F, 0.4F / (world.getRandom().nextFloat() * 0.4F + 0.8F));
            SmallFireball fireballEntity = new SmallFireball(serverWorld, user.getX(), user.getEyeY(), user.getZ(), new Vec3(0, 0, 0));
            fireballEntity.shootFromRotation(user, user.getXRot(), user.getYRot(), 0.0F, 1.5f, 1.0F);
            world.addFreshEntity(fireballEntity);
            ItemCooldowns itemCooldownManager = user.getCooldowns();
            itemCooldownManager.addCooldown(itemStack, 20);
            return InteractionResult.SUCCESS_SERVER;
        }

        user.awardStat(Stats.ITEM_USED.get(this));
        itemStack.consume(1, user);
        return InteractionResult.SUCCESS;
    }

}
