package cc.thonly.reverie_dreams.item.weapon;

import cc.thonly.reverie_dreams.api.player.PlayerInputManagerAccess;
import cc.thonly.reverie_dreams.item.base.SpearItem;
import cc.thonly.reverie_dreams.registry.tag.RDBlockTags;
import cc.thonly.reverie_dreams.server.InputKey;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Position;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.arrow.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class Gungnir extends SpearItem implements ProjectileItem {
    public static final ToolMaterial GUNGNIR = new ToolMaterial(RDBlockTags.EMPTY, 1561, 8.0f, 5.8f, 11, ItemTags.NETHERITE_TOOL_MATERIALS);

    public Gungnir(Properties settings) {
        super(GUNGNIR, 0.95F, 1.3F, 0.5F, 2.6F, 8.0F, 6.75F, 5.1F, 16.25F, 4.8F, settings);
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.SPEAR;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity user) {
        return 72000;
    }

    @Override
    public boolean releaseUsing(ItemStack stack, Level world, LivingEntity user, int remainingUseTicks) {
        if (!(user instanceof Player player)) {
            return false;
        }
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return false;
        }
        PlayerInputManagerAccess inputManager = PlayerInputManagerAccess.polymerAccess();
        if (!(inputManager.isKeyDown(serverPlayer, InputKey.SPRINT) &&
                inputManager.isKeyDown(serverPlayer, InputKey.JUMP))
        ) {
            return false;
        }
        int i = this.getUseDuration(stack, user) - remainingUseTicks;
        if (i < 10) {
            return false;
        }
        float f = EnchantmentHelper.getTridentSpinAttackStrength(stack, player);
        if (f > 0.0f && !player.isInWaterOrRain()) {
            return false;
        }
        if (stack.nextDamageWillBreak()) {
            return false;
        }
        Holder<SoundEvent> registryEntry = EnchantmentHelper.pickHighestLevel(stack, EnchantmentEffectComponents.TRIDENT_SOUND).orElse(SoundEvents.TRIDENT_THROW);
        player.awardStat(Stats.ITEM_USED.get(this));
        if (world instanceof ServerLevel serverWorld) {
            stack.hurtWithoutBreaking(1, player);
            if (f == 0.0f) {
                ItemStack itemStack = stack.consumeAndReturn(1, player);
                ThrownTrident tridentEntity = Projectile.spawnProjectileFromRotation(ThrownTrident::new, serverWorld, itemStack, player, 0.0f, 2.5f, 1.0f);
                if (player.hasInfiniteMaterials()) {
                    tridentEntity.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
                }
                world.playSound(null, tridentEntity, registryEntry.value(), SoundSource.PLAYERS, 1.0f, 1.0f);
                return true;
            }
        }
        if (f > 0.0f) {
            float g = player.getYRot();
            float h = player.getXRot();
            float j = -Mth.sin(g * ((float) Math.PI / 180)) * Mth.cos(h * ((float) Math.PI / 180));
            float k = -Mth.sin(h * ((float) Math.PI / 180));
            float l = Mth.cos(g * ((float) Math.PI / 180)) * Mth.cos(h * ((float) Math.PI / 180));
            float m = Mth.sqrt(j * j + k * k + l * l);
            player.push(j *= f / m, k *= f / m, l *= f / m);
            player.startAutoSpinAttack(20, 8.0f, stack);
            if (player.onGround()) {
                float n = 1.1999999f;
                player.move(MoverType.SELF, new Vec3(0.0, 1.1999999284744263, 0.0));
            }
            world.playSound(null, player, registryEntry.value(), SoundSource.PLAYERS, 1.0f, 1.0f);
            return true;
        }
        return false;
    }

    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        ItemStack itemStack = user.getItemInHand(hand);
        if (itemStack.nextDamageWillBreak()) {
            return InteractionResult.FAIL;
        }
        if (EnchantmentHelper.getTridentSpinAttackStrength(itemStack, user) > 0.0f && !user.isInWaterOrRain()) {
            return InteractionResult.FAIL;
        }
        user.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public Projectile asProjectile(Level world, Position pos, ItemStack stack, Direction direction) {
        ThrownTrident tridentEntity = new ThrownTrident(world, pos.x(), pos.y(), pos.z(), stack.copyWithCount(1));
        tridentEntity.pickup = AbstractArrow.Pickup.ALLOWED;
        return tridentEntity;
    }
}
