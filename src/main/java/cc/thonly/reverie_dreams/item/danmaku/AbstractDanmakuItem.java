package cc.thonly.reverie_dreams.item.danmaku;

import cc.thonly.reverie_dreams.component.DanmakuProperties;
import cc.thonly.reverie_dreams.component.ModDataComponentTypes;
import cc.thonly.reverie_dreams.item.base.IDanmakuItem;
import cc.thonly.reverie_dreams.sound.SoundEventInit;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
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
        if (!world.isClientSide && world instanceof ServerLevel serverWorld) {
            var cooldownManager = user.getCooldowns();
            if (cooldownManager.isOnCooldown(itemStack)) {
                return InteractionResult.FAIL;
            }
            cooldownManager.addCooldown(itemStack, 10);
            DanmakuProperties properties = itemStack.getOrDefault(ModDataComponentTypes.DANMAKU_PROPERTIES, DanmakuProperties.ofDefault());
            for (int i = 0; i < properties.getCount(); i++) {
                this.shoot(serverWorld, user, hand);
            }
            if (!properties.isInfinite()) {
                itemStack.hurtWithoutBreaking(1, user);
                if (itemStack.isDamageableItem() && itemStack.getDamageValue() >= itemStack.getMaxDamage()) {
                    itemStack.shrink(1);
                }
            }

            world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEventInit.FIRE, SoundSource.NEUTRAL, 1f, 1.0f);
            user.swing(hand);
            return InteractionResult.SUCCESS_SERVER;
        }
        user.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResult.SUCCESS;
    }

    public abstract void shoot(ServerLevel serverWorld, LivingEntity user, InteractionHand hand);
}
