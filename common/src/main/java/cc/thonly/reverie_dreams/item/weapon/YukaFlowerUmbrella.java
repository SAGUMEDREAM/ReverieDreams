package cc.thonly.reverie_dreams.item.weapon;

import cc.thonly.reverie_dreams.item.base.SwordItem;
import cc.thonly.reverie_dreams.registry.tag.RDBlockTags;
import cc.thonly.reverie_dreams.registry.tag.RDItemTags;
import dev.architectury.event.events.common.TickEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.level.Level;

public class YukaFlowerUmbrella extends SwordItem {
    public static final ToolMaterial INSTANCE = new ToolMaterial(RDBlockTags.EMPTY, 800, 7.0f, 5.5f, 10, RDItemTags.SILVER_TOOL_MATERIALS);
    private static final double SWEEP_RADIUS = 2.5;

    public YukaFlowerUmbrella(float attackDamage, float attackSpeed, Properties settings) {
        super(INSTANCE, attackDamage, attackSpeed, settings.spear(INSTANCE, 1.15F, 1.2F, 0.4F, 2.5F, 7.0F, 5.5F, 5.1F, 8.75F, 4.6F).fireResistant());
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand interactionHand) {
        super.use(level, player, interactionHand);
        if (!level.isClientSide()) {
            player.addEffect(new MobEffectInstance(MobEffects.SPEED, 1, 0, false, false, true));
            return InteractionResult.PASS;
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity user) {
        return 40;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.SPEAR;
    }

    static {
        TickEvent.PLAYER_POST.register(player -> {
            if (player.isUsingItem()) {
                ItemStack using = player.getUseItem();
                if (using.getItem() instanceof YukaFlowerUmbrella) {
                    Entity vehicle = player.getVehicle();
                    //noinspection ConditionCoveredByFurtherCondition
                    if (vehicle != null && vehicle instanceof LivingEntity livingVehicle) {
                        livingVehicle.addEffect(new MobEffectInstance(MobEffects.SPEED, 1, 2, false, false, true));
                        livingVehicle.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 1, 1));
                    }
                    player.addEffect(new MobEffectInstance(MobEffects.SPEED, 1, 2, false, false, true));
                    player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 1, 1));
                }
            }
        });
    }
}
