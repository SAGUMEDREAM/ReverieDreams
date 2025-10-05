package cc.thonly.reverie_dreams.item.prop;

import cc.thonly.reverie_dreams.util.entity.EntityUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class ExorcismPaperItem extends Item {
    private static final int EFFECT_TICK = 20 * 60 * 2;

    public ExorcismPaperItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        World world = user.getWorld();
        DynamicRegistryManager registryManager = world.getRegistryManager();
        if (!world.isClient) {
            if (entity != null && EntityUtil.isInTag(registryManager, entity, EntityTypeTags.UNDEAD)) {
                entity.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, EFFECT_TICK, 100));
                entity.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, EFFECT_TICK, 100));
                stack.decrementUnlessCreative(1, user);
                return ActionResult.SUCCESS_SERVER;
            } else {
                return ActionResult.FAIL;
            }
        }
        return super.useOnEntity(stack, user, entity, hand);
    }
}
