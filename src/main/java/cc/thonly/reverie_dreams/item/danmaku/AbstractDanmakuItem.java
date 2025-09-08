package cc.thonly.reverie_dreams.item.danmaku;

import cc.thonly.reverie_dreams.Touhou;
import cc.thonly.reverie_dreams.component.ModDataComponentTypes;
import cc.thonly.reverie_dreams.item.base.IDanmakuItem;
import cc.thonly.reverie_dreams.sound.SoundEventInit;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.function.Consumer;

@Setter
@Getter
@ToString
public abstract class AbstractDanmakuItem extends Item implements IDanmakuItem {
    public static final Integer DEFAULT_COUNT = 3;

    public AbstractDanmakuItem(Settings settings) {
        super(settings.maxCount(1));
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if (!context.getWorld().isClient()) {
            use((ServerWorld) context.getWorld(),
                    (PlayerEntity) context.getPlayer(),
                    context.getHand());
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        World world = user.getWorld();
        if (!world.isClient()) {
            use((ServerWorld) world,
                    user,
                    hand);
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        Boolean isInfinite = itemStack.getOrDefault(ModDataComponentTypes.Danmaku.INFINITE, false);
        if (!world.isClient && world instanceof ServerWorld serverWorld) {
            var cooldownManager = user.getItemCooldownManager();
            if (cooldownManager.isCoolingDown(itemStack)) {
                return ActionResult.FAIL;
            }
            cooldownManager.set(itemStack, 10);
            for (int i = 0; i < itemStack.getOrDefault(ModDataComponentTypes.Danmaku.COUNT, DEFAULT_COUNT); i++) {
                this.shoot(serverWorld, user, hand);
            }
            if (!isInfinite) {
                itemStack.damage(1, user);
                if (itemStack.isDamageable() && itemStack.getDamage() >= itemStack.getMaxDamage()) {
                    itemStack.decrement(1);
                }
            }

            world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEventInit.FIRE, SoundCategory.NEUTRAL, 1f, 1.0f);
            user.swingHand(hand);
            return ActionResult.SUCCESS_SERVER;
        }
        user.incrementStat(Stats.USED.getOrCreateStat(this));
        return ActionResult.SUCCESS;
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type) {
        Float damage = stack.getOrDefault(ModDataComponentTypes.Danmaku.DAMAGE, null);
        Float scale = stack.getOrDefault(ModDataComponentTypes.Danmaku.SCALE, null);
        Float speed = stack.getOrDefault(ModDataComponentTypes.Danmaku.SPEED, null);
        Integer count = stack.getOrDefault(ModDataComponentTypes.Danmaku.COUNT, DEFAULT_COUNT);
        String templateType = stack.getOrDefault(ModDataComponentTypes.Danmaku.TEMPLATE, Touhou.id("single").toString());

        textConsumer.accept(Text.empty().append(Text.translatable("item.tooltip.damage")).append(String.valueOf(damage)));
        textConsumer.accept(Text.empty().append(Text.translatable("item.tooltip.speed")).append(String.valueOf(speed)));
        textConsumer.accept(Text.empty().append(Text.translatable("item.tooltip.count")).append(String.valueOf(count)));
        textConsumer.accept(Text.empty().append(Text.translatable("item.tooltip.base_type")).append(Text.translatable(Identifier.of(templateType).toTranslationKey())));
    }

    public abstract void shoot(ServerWorld serverWorld, LivingEntity user, Hand hand);
}
