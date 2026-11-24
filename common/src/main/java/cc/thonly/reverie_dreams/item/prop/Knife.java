package cc.thonly.reverie_dreams.item.prop;

import cc.thonly.reverie_dreams.component.DanmakuProperties;
import cc.thonly.reverie_dreams.data.danmaku.DanmakuVolleyBudget;
import cc.thonly.reverie_dreams.entity.misc.DanmakuEntity;
import cc.thonly.reverie_dreams.item.base.IDanmakuItem;
import cc.thonly.reverie_dreams.item.base.SwordItem;
import cc.thonly.reverie_dreams.item.material.SilverMaterial;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponentTypes;
import cc.thonly.reverie_dreams.registry.content.item.RDEntityHolderItems;
import cc.thonly.reverie_dreams.registry.content.sound.RDSoundEvents;
import cc.thonly.reverie_dreams.util.entity.EntityHelper;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.tags.EntityTypeTags;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@SuppressWarnings({"unchecked", "deprecation"})
@Setter
@Getter
public class Knife extends SwordItem implements IDanmakuItem {
    private static final float PROJECTILE_SPEED = 3.0f;
    private static final float MAIN_DAMAGE = 5.5f;
    private static final float SIDE_DAMAGE = 4.5f;
    private static final float[] YAW_OFFSETS = {0.0f, -6.0f, -3.0f, 3.0f, 6.0f};

    public Knife(float attackDamage, float attackSpeed, Properties settings) {
        super(
                SilverMaterial.INSTANCE,
                attackDamage + 3.0f,
                attackSpeed - 2.0f,
                settings
        );
    }

    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        ItemStack heldItemStack = user.getItemInHand(hand);
        ItemStack itemStack = createDisplayStack(heldItemStack);
        DanmakuProperties properties = itemStack.getOrDefault(RDDataComponentTypes.DANMAKU_PROPERTIES.value(), DanmakuProperties.ofDefault());

        if (!world.isClientSide() && world instanceof ServerLevel serverWorld && user instanceof ServerPlayer player) {
            ItemCooldowns cooldownManager = player.getCooldowns();
            if (cooldownManager.isOnCooldown(heldItemStack)) {
                return InteractionResult.FAIL;
            }
            cooldownManager.addCooldown(heldItemStack, 10 * 4);

            DanmakuVolleyBudget.begin(DanmakuVolleyBudget.MAX_BULLETS_PER_USE);
            try {
                for (int i = 0; i < properties.count(); i++) {
                    this.shoot(serverWorld, user, hand);
                }
            } finally {
                DanmakuVolleyBudget.end();
            }

            if (!properties.infinite() && !player.hasInfiniteMaterials()) {
                EquipmentSlot slot = hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
                heldItemStack.hurtAndBreak(5, player, slot);
            }
            world.playSound(null, user.blockPosition(), RDSoundEvents.FIRE.value(), SoundSource.NEUTRAL, 1.0f, 1.0f);
            return InteractionResult.SUCCESS_SERVER;
        }
        user.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResult.SUCCESS;
    }

    public void shoot(ServerLevel serverWorld, Player user, InteractionHand hand) {
        this.spawn(serverWorld, user, hand);
    }

    public void spawn(ServerLevel serverWorld, Player user, InteractionHand hand) {
        ItemStack itemStack = createDisplayStack(user.getItemInHand(hand));
        DanmakuProperties base = itemStack
                .getOrDefault(RDDataComponentTypes.DANMAKU_PROPERTIES.value(), DanmakuProperties.ofDefault())
                .withSpeed(PROJECTILE_SPEED);

        float pitch = user.getXRot();
        float yaw = user.getYRot();
        List<DanmakuEntity> projectiles = new ArrayList<>(YAW_OFFSETS.length);
        for (int i = 0; i < YAW_OFFSETS.length; i++) {
            if (!DanmakuVolleyBudget.consume()) {
                break;
            }
            float damage = i == 0 ? MAIN_DAMAGE : SIDE_DAMAGE;
            DanmakuProperties shotProperties = base.withDamage(damage);
            ItemStack shotStack = itemStack.copy();
            shotStack.set(RDDataComponentTypes.DANMAKU_PROPERTIES.value(), shotProperties);

            DanmakuEntity entity = DanmakuEntity.create(
                    serverWorld, user, shotStack,
                    user.getX(), user.getEyeY(), user.getZ(),
                    pitch,
                    yaw + YAW_OFFSETS[i],
                    0.35f
            );
            entity.setDanmakuProperties(shotProperties.copy());
            entity.setHitCallback((self, target, dealtDamage) -> {
                if (target.getType().builtInRegistryHolder().is(EntityTypeTags.UNDEAD)) {
                    EntityHelper.hurt(serverWorld, target, user.damageSources().magic(), 2.0f);
                }
            });
            projectiles.add(entity);
        }
        projectiles.forEach(serverWorld::addFreshEntity);
    }

    private static ItemStack createDisplayStack(ItemStack heldItemStack) {
        ItemStack display = new ItemStack(RDEntityHolderItems.KNIFE_DISPLAY.asItem());
        DataComponentMap components = heldItemStack.getComponents();
        Iterator<TypedDataComponent<?>> iterator = components.stream().iterator();
        while (iterator.hasNext()) {
            @SuppressWarnings("rawtypes")
            TypedDataComponent next = iterator.next();
            if (next.type() == DataComponents.ITEM_MODEL) {
                continue;
            }
            display.set(next.type(), next.value());
        }
        return display;
    }
}
