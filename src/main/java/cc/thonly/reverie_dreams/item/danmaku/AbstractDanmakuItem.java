package cc.thonly.reverie_dreams.item.danmaku;

import cc.thonly.reverie_dreams.Touhou;
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
import java.util.function.Consumer;

@Setter
@Getter
@ToString
public abstract class AbstractDanmakuItem extends Item implements IDanmakuItem {
    public static final Integer DEFAULT_COUNT = 3;

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
        Boolean isInfinite = itemStack.getOrDefault(ModDataComponentTypes.Danmaku.INFINITE, false);
        if (!world.isClientSide && world instanceof ServerLevel serverWorld) {
            var cooldownManager = user.getCooldowns();
            if (cooldownManager.isOnCooldown(itemStack)) {
                return InteractionResult.FAIL;
            }
            cooldownManager.addCooldown(itemStack, 10);
            for (int i = 0; i < itemStack.getOrDefault(ModDataComponentTypes.Danmaku.COUNT, DEFAULT_COUNT); i++) {
                this.shoot(serverWorld, user, hand);
            }
            if (!isInfinite) {
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

//    @Override
//    public void appendTooltip(ItemStack stack, TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type) {
//        Float damage = stack.getOrDefault(ModDataComponentTypes.Danmaku.DAMAGE, null);
//        Float scale = stack.getOrDefault(ModDataComponentTypes.Danmaku.SCALE, null);
//        Float speed = stack.getOrDefault(ModDataComponentTypes.Danmaku.SPEED, null);
//        Integer count = stack.getOrDefault(ModDataComponentTypes.Danmaku.COUNT, DEFAULT_COUNT);
//        String templateType = stack.getOrDefault(ModDataComponentTypes.Danmaku.TEMPLATE, Touhou.id("single").toString());
//
//        textConsumer.accept(Text.empty().append(Text.translatable("item.tooltip.damage")).append(String.valueOf(damage)));
//        textConsumer.accept(Text.empty().append(Text.translatable("item.tooltip.speed")).append(String.valueOf(speed)));
//        textConsumer.accept(Text.empty().append(Text.translatable("item.tooltip.count")).append(String.valueOf(count)));
//        textConsumer.accept(Text.empty().append(Text.translatable("item.tooltip.base_type")).append(Text.translatable(Identifier.of(templateType).toTranslationKey())));
//    }

    public abstract void shoot(ServerLevel serverWorld, LivingEntity user, InteractionHand hand);
}
