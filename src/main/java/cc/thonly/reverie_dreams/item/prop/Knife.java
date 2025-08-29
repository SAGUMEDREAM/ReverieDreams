package cc.thonly.reverie_dreams.item.prop;

import cc.thonly.reverie_dreams.component.ModDataComponentTypes;
import cc.thonly.reverie_dreams.entity.ModEntityHolders;
import cc.thonly.reverie_dreams.entity.misc.DanmakuEntity;
import cc.thonly.reverie_dreams.item.base.IDanmakuItem;
import cc.thonly.reverie_dreams.item.base.SwordItem;
import cc.thonly.reverie_dreams.item.material.SilverMaterial;
import cc.thonly.reverie_dreams.sound.SoundEventInit;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.component.Component;
import net.minecraft.component.ComponentMap;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

@SuppressWarnings("unchecked")
@Setter
@Getter
public class Knife extends SwordItem implements IDanmakuItem {

    public Knife(float attackDamage, float attackSpeed, Settings settings) {
        super(
                SilverMaterial.INSTANCE,
                attackDamage + 3.0f,
                attackSpeed - 2f,
                settings
        );
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack heldItemStack = user.getStackInHand(hand);
        ItemStack itemStack = new ItemStack(ModEntityHolders.KNIFE_DISPLAY);
        ComponentMap components = heldItemStack.getComponents();
        Iterator<Component<?>> iterator = components.stream().iterator();
        while (iterator.hasNext()) {
            Component<Object> next = (Component<Object>) iterator.next();
            itemStack.set(next.type(), next.value());
        }
        Boolean isInfinite = itemStack.getOrDefault(ModDataComponentTypes.Danmaku.INFINITE, false);
        if (!world.isClient && world instanceof ServerWorld serverWorld && user instanceof ServerPlayerEntity player) {
            ItemCooldownManager cooldownManager = player.getItemCooldownManager();
            for (int i = 0; i < itemStack.getOrDefault(ModDataComponentTypes.Danmaku.COUNT, 1); i++) {
                this.shoot(serverWorld, user, hand);
            }
            cooldownManager.set(heldItemStack, 10);
            if (!isInfinite) {
                itemStack.damage(1, user);
            }
            world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEventInit.FIRE, SoundCategory.NEUTRAL, 1f, 1.0f);
            return ActionResult.SUCCESS_SERVER;
        }
        user.incrementStat(Stats.USED.getOrCreateStat(this));
        return ActionResult.SUCCESS;
    }

    public void shoot(ServerWorld serverWorld, PlayerEntity user, Hand hand) {
        this.spawn(serverWorld, user, hand);
    }

    public void spawn(ServerWorld serverWorld, PlayerEntity user, Hand hand) {
        Random random = new Random();
        ItemStack heldItemStack = user.getStackInHand(hand);
        ItemStack itemStack = new ItemStack(ModEntityHolders.KNIFE_DISPLAY);
        ComponentMap components = heldItemStack.getComponents();
        Iterator<Component<?>> iterator = components.stream().iterator();
        while (iterator.hasNext()) {
            Component<Object> next = (Component<Object>) iterator.next();
            itemStack.set(next.type(), next.value());
        }
        ItemStack stack = itemStack.copy();
        float pitch = user.getPitch();
        float yaw = user.getYaw();

        Item item = stack.getItem();

        List<DanmakuEntity> list = new ArrayList<>();
        DanmakuEntity danmakuEntity = new DanmakuEntity(
                (LivingEntity) user,
                serverWorld,
                user.getX(),
                user.getY(),
                user.getZ(),
                stack.copy(),
                pitch,
                yaw,
                1.4f,
                0f,
                5.0f,
                0.4f
        );
        list.add(danmakuEntity);
        for (int i = 0; i < 3; i++) {
            int i1 = random.nextInt(-5, 5);
            list.add(new DanmakuEntity(
                    (LivingEntity) user,
                    serverWorld,
                    user.getX(),
                    user.getY(),
                    user.getZ(),
                    stack.copy(),
                    pitch+i1/1.5f,
                    yaw+i1,
                    1.4f,
                    0f,
                    5.0f,
                    0.4f
            ));
        }
        list.forEach(serverWorld::spawnEntity);
    }
}
