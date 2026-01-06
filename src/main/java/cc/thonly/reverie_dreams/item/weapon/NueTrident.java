package cc.thonly.reverie_dreams.item.weapon;

import cc.thonly.polymer.item.IBasicPolymerItem;
import cc.thonly.reverie_dreams.item.base.MiningToolItem;
import cc.thonly.reverie_dreams.registry.tag.RDBlockTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Position;
import net.minecraft.core.component.DataComponents;
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
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class NueTrident extends MiningToolItem implements ProjectileItem, IBasicPolymerItem {
    public static final ToolMaterial NUE_TRIDENT = new ToolMaterial(RDBlockTags.MIN_TOOL, 450, 4.5f, 5.5f, 1, ItemTags.NETHERITE_TOOL_MATERIALS);

    public NueTrident(float attackDamage, float attackSpeed, Item.Properties settings) {
        super(NUE_TRIDENT, attackDamage, attackSpeed, settings.attributes(createAttributeModifiers()).component(DataComponents.TOOL, createToolComponent()).enchantable(1).enchantable(1));
    }

    @Override
    public boolean canAttackBlock(BlockState blockState, Level level, BlockPos blockPos, Player player) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            boolean b = super.canAttackBlock(blockState, level, blockPos, serverPlayer);
            return b && !serverPlayer.hasInfiniteMaterials();
        }
        return super.canAttackBlock(blockState, level, blockPos, player);
    }

    public static ItemAttributeModifiers createAttributeModifiers() {
        return ItemAttributeModifiers.builder().add(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, 8.0, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND).add(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, -2.9f, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND).build();
    }

    public static Tool createToolComponent() {
        return new Tool(List.of(), 1.0f, 2);
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
        if (!(user instanceof Player playerEntity)) {
            return false;
        }
        int i = this.getUseDuration(stack, user) - remainingUseTicks;
        if (i < 10) {
            return false;
        }
        float f = EnchantmentHelper.getTridentSpinAttackStrength(stack, playerEntity);
        if (f > 0.0f && !playerEntity.isInWaterOrRain()) {
            return false;
        }
        if (stack.nextDamageWillBreak()) {
            return false;
        }
        Holder<SoundEvent> registryEntry = EnchantmentHelper.pickHighestLevel(stack, EnchantmentEffectComponents.TRIDENT_SOUND).orElse(SoundEvents.TRIDENT_THROW);
        playerEntity.awardStat(Stats.ITEM_USED.get(this));
        if (world instanceof ServerLevel serverWorld) {
            stack.hurtWithoutBreaking(1, playerEntity);
            if (f == 0.0f) {
                ItemStack itemStack = stack.consumeAndReturn(1, playerEntity);
                ThrownTrident tridentEntity = Projectile.spawnProjectileFromRotation(ThrownTrident::new, serverWorld, itemStack, playerEntity, 0.0f, 2.5f, 1.0f);
                if (playerEntity.hasInfiniteMaterials()) {
                    tridentEntity.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
                }
                world.playSound(null, tridentEntity, registryEntry.value(), SoundSource.PLAYERS, 1.0f, 1.0f);
                return true;
            }
        }
        if (f > 0.0f) {
            float g = playerEntity.getYRot();
            float h = playerEntity.getXRot();
            float j = -Mth.sin(g * ((float)Math.PI / 180)) * Mth.cos(h * ((float)Math.PI / 180));
            float k = -Mth.sin(h * ((float)Math.PI / 180));
            float l = Mth.cos(g * ((float)Math.PI / 180)) * Mth.cos(h * ((float)Math.PI / 180));
            float m = Mth.sqrt(j * j + k * k + l * l);
            playerEntity.push(j *= f / m, k *= f / m, l *= f / m);
            playerEntity.startAutoSpinAttack(20, 8.0f, stack);
            if (playerEntity.onGround()) {
                float n = 1.1999999f;
                playerEntity.move(MoverType.SELF, new Vec3(0.0, 1.1999999284744263, 0.0));
            }
            world.playSound(null, playerEntity, registryEntry.value(), SoundSource.PLAYERS, 1.0f, 1.0f);
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
