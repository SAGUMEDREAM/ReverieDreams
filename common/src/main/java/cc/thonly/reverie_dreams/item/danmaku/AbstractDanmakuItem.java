package cc.thonly.reverie_dreams.item.danmaku;

import cc.thonly.reverie_dreams.component.DanmakuProperties;
import cc.thonly.reverie_dreams.data.danmaku.DanmakuVolleyBudget;
import cc.thonly.reverie_dreams.item.base.IDanmakuItem;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponentTypes;
import cc.thonly.reverie_dreams.registry.content.sound.RDSoundEvents;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

@Setter
@Getter
@ToString
public abstract class AbstractDanmakuItem extends Item implements IDanmakuItem {
    public AbstractDanmakuItem(Properties settings) {
        super(settings.stacksTo(1));
    }

    @SuppressWarnings("RedundantCast")
    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!context.getLevel().isClientSide()) {
            use((ServerLevel) context.getLevel(),
                    (Player) context.getPlayer(),
                    context.getHand());
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player user, LivingEntity entity, InteractionHand hand) {
        Level world = user.level();
        if (!world.isClientSide()) {
            use((ServerLevel) world,
                    user,
                    hand);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        ItemStack itemStack = user.getItemInHand(hand);
        if (!world.isClientSide() && world instanceof ServerLevel serverWorld) {
            var cooldownManager = user.getCooldowns();
            if (cooldownManager.isOnCooldown(itemStack)) {
                return InteractionResult.FAIL;
            }
            int cooldownTicks = this instanceof DanmakuItem danmakuItem && danmakuItem.type() != null
                    ? Math.max(1, danmakuItem.type().getCooldownTicks())
                    : 10;
            cooldownManager.addCooldown(itemStack, cooldownTicks);
            DanmakuProperties properties = itemStack.getOrDefault(RDDataComponentTypes.DANMAKU_PROPERTIES.value(), DanmakuProperties.ofDefault());
            DanmakuVolleyBudget.begin(DanmakuVolleyBudget.MAX_BULLETS_PER_USE);
            try {
                this.shootVolley(serverWorld, user, hand, itemStack, properties);
            } finally {
                DanmakuVolleyBudget.end();
            }
            if (!properties.infinite()) {
                Integer cur = itemStack.get(DataComponents.DAMAGE);
                Integer max = itemStack.get(DataComponents.MAX_DAMAGE);
                if (cur != null && max != null && cur >= max) {
                    itemStack.setCount(0);
                }
                itemStack.hurtWithoutBreaking(1, user);
                if (itemStack.isDamageableItem() && itemStack.getDamageValue() >= itemStack.getMaxDamage()) {
                    itemStack.shrink(1);
                }
            }

            world.playSound(null, user.getX(), user.getY(), user.getZ(), RDSoundEvents.FIRE, SoundSource.NEUTRAL, 1f, 1.0f);
            user.swing(hand);
            return InteractionResult.SUCCESS_SERVER;
        }
        user.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResult.SUCCESS;
    }

    protected void shootVolley(ServerLevel level, LivingEntity owner, InteractionHand hand,
                               ItemStack stack, DanmakuProperties properties) {
        for (int i = 0; i < properties.count(); i++) {
            this.shoot(level, owner, hand);
        }
    }

    public abstract void shoot(ServerLevel serverWorld, LivingEntity user, InteractionHand hand);
}
