package cc.thonly.reverie_dreams.entity.ai.goal;

import cc.thonly.reverie_dreams.entity.npc.NPCEntityImpl;
import cc.thonly.reverie_dreams.entity.skin.RoleSkin;
import cc.thonly.reverie_dreams.entity.skin.RoleSkins;
import cc.thonly.reverie_dreams.interfaces.IItemStack;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.EnumSet;

public class EatGoal extends Goal {
    private final NPCEntityImpl maid;
    int slot = -1;
    int dealyTick = 0;

    public EatGoal(NPCEntityImpl maid) {
//        this.setControls(EnumSet.of(Goal.Control.MOVE));
        this.maid = maid;
    }


    @Override
    public boolean canStart() {
        if (RoleSkins.YUYUKO.get().equals(this.maid.getSkin())){
            return maid.getNutrition() < 20;
        }
        return (maid.getNutrition() < 20 && maid.getHealth() < 20) || maid.getNutrition() < 10;
    }

    @Override
    public void start() {
        findFood();
    }

    @Override
    public void tick() {
        dealyTick--;
        ServerWorld world = (ServerWorld) maid.getWorld();
        if (dealyTick <= 0) {
            stop();
            return;
        }
        if (slot == -1) {
            findFood();
            return;
        }
        ItemStack stack = maid.getInventory().getStack(slot);
        if (((IItemStack) (Object) stack).isFood()) {

            Vec3d eyePos = maid.getEyePos();

            //干饭粒子
            world.spawnParticles(
                    new ItemStackParticleEffect(ParticleTypes.ITEM, stack), // 粒子类型 + 物品
                    eyePos.x + maid.getRotationVector().x / 3.0, eyePos.y, eyePos.z + maid.getRotationVector().x / 3.0,
                    2,
                    world.random.nextGaussian() * 0.05, world.random.nextGaussian() * 0.05, world.random.nextGaussian() * 0.05
                    , world.random.nextGaussian() * 0.05
            );
            //干饭声音
            if (dealyTick % 3 == 0) {
                maid.swingHand(Hand.MAIN_HAND);
                world.playSound(
                        null,
                        maid.getX(),
                        maid.getY(),
                        maid.getZ(),
                        SoundEvents.ENTITY_GENERIC_EAT,
                        SoundCategory.AMBIENT,
                        0.5F,
                        MathHelper.nextBetween(world.random, 0.9F, 1.0F)
                );
            }
        } else findFood();
        super.tick();
    }


    @Override
    public void stop() {
        //double speed = maid.getAttributeBaseValue(EntityAttributes.MOVEMENT_SPEED);
        //maid.getAttributeInstance(EntityAttributes.MOVEMENT_SPEED).setBaseValue(speed*2);
        eat();
        dealyTick = 30;
        this.slot = -1;
        //重置槽位/延迟 避免重复吃 我不知道为啥他会调用2次stop
        super.stop();
    }

    private boolean findFood() {
        Integer slot = maid.getInventory().findSlot(stack -> ((IItemStack) (Object) stack).isFood());
        if (slot == null) {
            this.slot = -1;
            return false;
        } else {
            this.slot = slot;
            dealyTick = 30;
        }
        return true;
    }

    public void eat() {
        ServerWorld world = (ServerWorld) maid.getWorld();
        if (dealyTick > 0 || slot == -1) {
            return;
        }
        ItemStack stack = maid.getInventory().getStack(slot);
        if (((IItemStack) (Object) stack).isFood()) {
            ComponentMap components = stack.getComponents();
            FoodComponent foodComponent = components.get(DataComponentTypes.FOOD);
            if (foodComponent != null) {
                int nutritionValue = foodComponent.nutrition();
                int saturationValue = Math.round(foodComponent.saturation());
                maid.setNutrition(maid.getNutrition() + nutritionValue);
                maid.setSaturation(maid.getSaturation() + saturationValue);
                stack.decrement(1);
                world.playSound(
                        null,
                        maid.getX(),
                        maid.getY(),
                        maid.getZ(),
                        SoundEvents.ENTITY_PLAYER_BURP,
                        SoundCategory.AMBIENT,
                        0.5F,
                        MathHelper.nextBetween(world.random, 0.9F, 1.0F)
                );
            }
        }
    }

}
