package cc.thonly.reverie_dreams.item.weapon;

import cc.thonly.minecraft.api.ItemLeftClickCallback;
import cc.thonly.reverie_dreams.item.base.SwordItem;
import cc.thonly.reverie_dreams.registry.tag.RDBlockTags;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class DeathScytheItem extends SwordItem {
    public static final ToolMaterial TOOL_MATERIAL = new ToolMaterial(RDBlockTags.EMPTY, 1561, 8.5f, 5.5f, 10, ItemTags.NETHERITE_TOOL_MATERIALS);
    private static final double SWEEP_RADIUS = 2.5;

    public DeathScytheItem(float attackDamage, float attackSpeed, Properties settings) {
        super(TOOL_MATERIAL, attackDamage, attackSpeed, settings);
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        Level world = target.level();
        if (!world.isClientSide() && world instanceof ServerLevel serverWorld) {
            if (ThreadLocalRandom.current().nextFloat() < 0.28f) {
                attacker.setHealth(attacker.getHealth() + 3);
            }
        }
        super.hurtEnemy(stack, target, attacker);
    }

    static {
        ItemLeftClickCallback.EVENT.register((level, player, hand) -> {
            if (level instanceof ServerLevel world) {
                ItemStack itemStack = player.getItemInHand(hand);
                if (itemStack.getItem() instanceof DeathScytheItem) {
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
    }
}
