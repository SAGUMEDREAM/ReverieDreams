package cc.thonly.reverie_dreams.item.weapon;

import cc.thonly.reverie_dreams.item.base.SwordItem;
import cc.thonly.reverie_dreams.registry.tag.RDBlockTags;
import cc.thonly.reverie_dreams.registry.tag.RDItemTags;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.hurtingprojectile.windcharge.WindCharge;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.level.Level;

public class SwordOfHisou extends SwordItem {
    public static final ToolMaterial HISOU = new ToolMaterial(RDBlockTags.EMPTY, 1561, 8.0f, 4.5f, 10, RDItemTags.PEACH);
    private static final float LIGHTNING_CHANCE = 0.15F;

    public SwordOfHisou(float attackDamage, float attackSpeed, Properties settings) {
        super(HISOU, attackDamage, attackSpeed, settings);
    }
    @Override
    public void hurtEnemy(
            ItemStack stack,
            LivingEntity target,
            LivingEntity attacker
    ) {
        Level world = target.level();

        if (!world.isClientSide() && world instanceof ServerLevel serverLevel) {
            if (serverLevel.getRandom().nextFloat() < LIGHTNING_CHANCE) {
                LightningBolt lightningBolt = new LightningBolt(EntityType.LIGHTNING_BOLT, serverLevel);
                BlockPos blockPos = target.getOnPos();
                lightningBolt.setPos(blockPos.getX(), blockPos.getY(), blockPos.getZ());
                lightningBolt.setVisualOnly(false);

                serverLevel.addFreshEntity(lightningBolt);

                serverLevel.playSound(
                        null,
                        target.getX(),
                        target.getY(),
                        target.getZ(),
                        SoundEvents.LIGHTNING_BOLT_THUNDER,
                        SoundSource.WEATHER,
                        1.0F,
                        0.8F + serverLevel.getRandom().nextFloat() * 0.2F
                );
            }

            if (serverLevel.isRaining() && serverLevel.getRandom().nextFloat() < 0.3F) {
                if (serverLevel.isThundering()) {
                    target.setRemainingFireTicks(2 * 20);
                    serverLevel.playSound(
                            null,
                            attacker.getX(),
                            attacker.getY(),
                            attacker.getZ(),
                            SoundEvents.LIGHTNING_BOLT_THUNDER,
                            SoundSource.NEUTRAL,
                            0.5F,
                            0.4F / (
                                    serverLevel.getRandom().nextFloat() * 0.4F
                                            + 0.8F
                            )
                    );
                }

                Projectile.spawnProjectileFromRotation(
                        (w, s, st) ->
                                new WindCharge(
                                        w,
                                        s.getX(),
                                        s.getY(),
                                        s.getZ(),
                                        s.getDeltaMovement()
                                ),
                        serverLevel,
                        this.getDefaultInstance(),
                        attacker,
                        0.0F,
                        1.5F,
                        1.0F
                );

                serverLevel.playSound(
                        null,
                        attacker.getX(),
                        attacker.getY(),
                        attacker.getZ(),
                        SoundEvents.WIND_CHARGE_THROW,
                        SoundSource.NEUTRAL,
                        0.5F,
                        0.4F / (
                                serverLevel.getRandom().nextFloat() * 0.4F
                                        + 0.8F
                        )
                );
            }
        }

        super.hurtEnemy(stack, target, attacker);
    }
}
