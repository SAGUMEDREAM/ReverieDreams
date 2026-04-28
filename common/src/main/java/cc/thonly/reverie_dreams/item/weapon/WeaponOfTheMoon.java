package cc.thonly.reverie_dreams.item.weapon;

import cc.thonly.reverie_dreams.component.DanmakuProperties;
import cc.thonly.reverie_dreams.entity.misc.DanmakuEntity;
import cc.thonly.reverie_dreams.registry.content.RDDamageTypes;
import cc.thonly.reverie_dreams.registry.content.advancements.RDCriteriaTriggers;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("NullableProblems")
public class WeaponOfTheMoon extends Item {
    private static final ItemStack BULLET = new ItemStack(Items.COPPER_NUGGET);
    private static final DanmakuProperties BULLET_PROPERTIES = new DanmakuProperties(
            DanmakuProperties.DEFAULT_TEMPLATE_ID,
            1,
            3,
            RDDamageTypes.DANMAKU_GENERIC,
            0.4f,
            6f,
            0.1f,
            true,
            true
    );

    public WeaponOfTheMoon(Properties properties) {
        super(properties);
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    public void tryShoot(LivingEntity owner, ServerLevel world, @Nullable InteractionHand hand) {
        this.tryShoot(owner, world, hand, 0);
    }

    public void tryShoot(LivingEntity owner, ServerLevel world, @Nullable InteractionHand hand, float d) {
        if (owner instanceof Player player && hand != null) {
            ItemStack offStack = player.getItemInHand(InteractionHand.OFF_HAND);
            if (!offStack.is(Items.COPPER_NUGGET) && !player.isCreative()) {
                return;
            }
            if (!player.isCreative()) {
                offStack.shrink(1);
            }
            var itemCooldowns = player.getCooldowns();
            itemCooldowns.addCooldown(player.getItemInHand(hand), 2);
        }
        DanmakuEntity danmakuEntity = DanmakuEntity.create(world, owner, BULLET.copy(), owner.getXRot(), owner.getYRot(), 0.4f);
        danmakuEntity.shootFromRotation(owner, owner.getXRot(), owner.getYRot(), 0, BULLET_PROPERTIES.speed(), 0.4f);
        danmakuEntity.setDanmakuProperties(BULLET_PROPERTIES.copy());
        owner.turn(0.0F, -2.0F);
        world.addFreshEntity(danmakuEntity);
        world.playSound(null,
                owner.getX(), owner.getY(), owner.getZ(),
                SoundEvents.BLAZE_HURT, SoundSource.PLAYERS,
                2.0f, 1.0f
        );
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide() && level instanceof ServerLevel world) {
            ItemStack itemStack = player.getItemInHand(hand);
            this.tryShoot(player, world, hand);
            player.awardStat(Stats.ITEM_USED.get(this));
            RDCriteriaTriggers.USE_ITEM.value().trigger((ServerPlayer) player, itemStack);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity interactionTarget, InteractionHand usedHand) {
        return this.use(player.level(), player, usedHand);
    }
}
