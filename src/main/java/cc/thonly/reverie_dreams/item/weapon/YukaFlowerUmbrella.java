package cc.thonly.reverie_dreams.item.weapon;

import cc.thonly.minecraft.api.ItemLeftClickCallback;
import cc.thonly.reverie_dreams.item.base.SwordItem;
import cc.thonly.reverie_dreams.registry.tag.RDBlockTags;
import cc.thonly.reverie_dreams.registry.tag.RDItemTags;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class YukaFlowerUmbrella extends SwordItem {
    public static final ToolMaterial INSTANCE = new ToolMaterial(RDBlockTags.EMPTY, 800, 7.25f, 5.5f, 10, RDItemTags.SILVER_TOOL_MATERIALS);
    private static final double SWEEP_RADIUS = 2.5;

    public YukaFlowerUmbrella(float attackDamage, float attackSpeed, Properties settings) {
        super(INSTANCE, attackDamage, attackSpeed, settings);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand interactionHand) {
        super.use(level, player, interactionHand);
        if (!level.isClientSide) {
            player.addEffect(new MobEffectInstance(MobEffects.SPEED, 15, 4));
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
        return ItemUseAnimation.BLOCK;
    }

    static {
        ItemLeftClickCallback.EVENT.register((level, player, hand) -> {
            if (level instanceof ServerLevel world) {
                ItemStack itemStack = player.getItemInHand(hand);
                if (itemStack.getItem() instanceof YukaFlowerUmbrella) {
                    Vec3 center = player.position().add(0, player.getBbHeight() * 0.5, 0);
                    List<Entity> targets = world.getEntities(
                            player,
                            player.getBoundingBox().inflate(SWEEP_RADIUS),
                            (e) -> e instanceof LivingEntity && e != player
                    );
                    for (Entity target : targets) {
                        if (!(target instanceof LivingEntity living)) {
                            continue;
                        }
                        if (target == player) {
                            continue;
                        }
                        if (target == player.getVehicle()){
                            continue;
                        }
                        Vec3 toTarget = living.position().subtract(center).normalize();
                        Vec3 look = player.getLookAngle().normalize();
                        if (toTarget.dot(look) < 0.2) {
                            continue;
                        }
                        player.attack(living);
                        living.knockback(0.5, player.getX() - living.getX(), player.getZ() - living.getZ());
                    }
                }
            }
        });
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                if (player.isUsingItem()) {
                    ItemStack using = player.getUseItem();
                    if (using.getItem() instanceof YukaFlowerUmbrella) {
                        Entity vehicle = player.getVehicle();
                        //noinspection ConditionCoveredByFurtherCondition
                        if (vehicle != null && vehicle instanceof LivingEntity livingVehicle) {
                            livingVehicle.addEffect(new MobEffectInstance(MobEffects.SPEED, 15, 4));
                            livingVehicle.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 15, 2));
                        }
                        player.addEffect(new MobEffectInstance(MobEffects.SPEED, 15, 4));
                        player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 15, 2));
                    }
                }
            }
        });
    }
}
