package cc.thonly.reverie_dreams.item.prop;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.component.DanmakuProperties;
import cc.thonly.reverie_dreams.entity.misc.DanmakuEntity;
import cc.thonly.reverie_dreams.item.base.IDanmakuItem;
import cc.thonly.reverie_dreams.item.base.SwordItem;
import cc.thonly.reverie_dreams.item.material.SilverMaterial;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponents;
import cc.thonly.reverie_dreams.registry.content.item.RDEntityHolderItems;
import cc.thonly.reverie_dreams.sound.RDSoundEvents;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@SuppressWarnings("unchecked")
@Setter
@Getter
public class Knife extends SwordItem implements IDanmakuItem {

    public Knife(float attackDamage, float attackSpeed, Properties settings) {
        super(
                SilverMaterial.INSTANCE,
                attackDamage + 3.0f,
                attackSpeed - 2f,
                settings
        );
    }

    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        ItemStack heldItemStack = user.getItemInHand(hand);
        ItemStack itemStack = new ItemStack(RDEntityHolderItems.KNIFE_DISPLAY.asItem());
        DataComponentMap components = heldItemStack.getComponents();
        Iterator<TypedDataComponent<?>> iterator = components.stream().iterator();
        while (iterator.hasNext()) {
            TypedDataComponent<Object> next = (TypedDataComponent<Object>) iterator.next();
            itemStack.set(next.type(), next.value());
        }
        DanmakuProperties properties = itemStack.getOrDefault(RDDataComponents.DANMAKU_PROPERTIES.value(), DanmakuProperties.ofDefault());
        if (!world.isClientSide() && world instanceof ServerLevel serverWorld && user instanceof ServerPlayer player) {
            ItemCooldowns cooldownManager = player.getCooldowns();
            for (int i = 0; i < properties.count(); i++) {
                this.shoot(serverWorld, user, hand);
            }
            cooldownManager.addCooldown(heldItemStack, 10);
            if (!properties.infinite()) {
                itemStack.hurtWithoutBreaking(1, user);
            }
            world.playSound(null, user.getX(), user.getY(), user.getZ(), RDSoundEvents.FIRE, SoundSource.NEUTRAL, 1f, 1.0f);
            return InteractionResult.SUCCESS_SERVER;
        }
        user.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResult.SUCCESS;
    }

    public void shoot(ServerLevel serverWorld, Player user, InteractionHand hand) {
        this.spawn(serverWorld, user, hand);
    }

    public void spawn(ServerLevel serverWorld, Player user, InteractionHand hand) {
        ItemStack heldItemStack = user.getItemInHand(hand);
        ItemStack itemStack = new ItemStack(RDEntityHolderItems.KNIFE_DISPLAY.asItem());
        DataComponentMap components = heldItemStack.getComponents();
        Iterator<TypedDataComponent<?>> iterator = components.stream().iterator();
        while (iterator.hasNext()) {
            @SuppressWarnings("rawtypes")
            TypedDataComponent next = iterator.next();
            if (next.type() == DataComponents.ITEM_MODEL) {
                continue;
            }
            itemStack.set(next.type(), next.value());
        }
        ItemStack stack = itemStack.copy();
        float pitch = user.getXRot();
        float yaw = user.getYRot();
        DanmakuProperties properties = stack.getOrDefault(RDDataComponents.DANMAKU_PROPERTIES.value(), DanmakuProperties.ofDefault());
        properties = properties.withDamage(4.5f).withSpeed(1.5f);

        List<DanmakuEntity> list = new ArrayList<>();
        DanmakuEntity danmakuEntity = DanmakuEntity.create(
                serverWorld, user, stack.copy(),
                user.getX(), user.getY(), user.getZ(),
                pitch,
                yaw,
                0.4f
        );
        list.add(danmakuEntity);
        for (int i = 0; i < 3; i++) {
            int i1 = ReverieDreams.RD.nextInt(-5, 5);
            list.add(DanmakuEntity.create(
                    serverWorld, user, stack.copy(),
                    user.getX(), user.getY(), user.getZ(),
                    pitch + i1 / 1.5f,
                    yaw + i1,
                    0.4f
            ));
        }
        list.forEach(serverWorld::addFreshEntity);
    }
}
