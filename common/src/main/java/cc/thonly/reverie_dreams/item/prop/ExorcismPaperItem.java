package cc.thonly.reverie_dreams.item.prop;

import cc.thonly.reverie_dreams.util.entity.EntityHelper;
import net.minecraft.core.RegistryAccess;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ExorcismPaperItem extends Item {
    private static final int EFFECT_TICK = 20 * 60 * 2;

    public ExorcismPaperItem(Properties settings) {
        super(settings);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player user, LivingEntity entity, InteractionHand hand) {
        Level world = user.level();
        RegistryAccess registryManager = world.registryAccess();
        if (!world.isClientSide()) {
            ItemCooldowns cooldowns = user.getCooldowns();
            if (EntityHelper.is(entity, EntityTypeTags.UNDEAD) && !cooldowns.isOnCooldown(stack)) {
                entity.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, EFFECT_TICK, 100));
                entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, EFFECT_TICK, 100));
                cooldowns.addCooldown(stack, 100);
                return InteractionResult.SUCCESS_SERVER;
            } else {
                return InteractionResult.FAIL;
            }
        }
        return super.interactLivingEntity(stack, user, entity, hand);
    }
}
