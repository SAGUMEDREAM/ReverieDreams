package cc.thonly.reverie_dreams.entity.ai.goal;

import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.api.item.ItemStackHelper;
import cc.thonly.reverie_dreams.registry.content.skin.GensokyoSkinTypes;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.phys.Vec3;

public class EatGoal extends Goal {
    private final BaseNPCLikeEntity maid;
    int slot = -1;
    int dealyTick = 0;

    public EatGoal(BaseNPCLikeEntity maid) {
//        this.setControls(EnumSet.of(Goal.Control.MOVE));
        this.maid = maid;
    }


    @Override
    public boolean canUse() {
        if (GensokyoSkinTypes.YUYUKO.get().equals(this.maid.getSkin())){
            return maid.getNutrition() < 20;
        }
        return (maid.getNutrition() < 20 && maid.getHealth() < this.maid.getMaxHealth()) || maid.getNutrition() < 10;
    }

    @Override
    public void start() {
        findFood();
    }

    @Override
    public void tick() {
        dealyTick--;
        ServerLevel world = (ServerLevel) maid.level();
        if (dealyTick <= 0) {
            stop();
            return;
        }
        if (slot == -1) {
            findFood();
            return;
        }
        ItemStack stack = maid.getInventory().getItem(slot);
        if (((ItemStackHelper) (Object) stack).reverie_dreams$isFood()) {

            Vec3 eyePos = maid.getEyePosition();

            //干饭粒子
            world.sendParticles(
                    new ItemParticleOption(ParticleTypes.ITEM, ItemStackTemplate.fromNonEmptyStack(stack)), // 粒子类型 + 物品
                    eyePos.x + maid.getLookAngle().x / 3.0, eyePos.y, eyePos.z + maid.getLookAngle().x / 3.0,
                    2,
                    world.getRandom().nextGaussian() * 0.05, world.getRandom().nextGaussian() * 0.05, world.getRandom().nextGaussian() * 0.05
                    , world.getRandom().nextGaussian() * 0.05
            );
            //干饭声音
            if (dealyTick % 3 == 0) {
                maid.swing(InteractionHand.MAIN_HAND);
                world.playSound(
                        null,
                        maid.getX(),
                        maid.getY(),
                        maid.getZ(),
                        SoundEvents.GENERIC_EAT,
                        SoundSource.AMBIENT,
                        0.5F,
                        Mth.randomBetween(world.getRandom(), 0.9F, 1.0F)
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
        Integer slot = maid.getInventory().findSlot(stack -> ((ItemStackHelper) (Object) stack).reverie_dreams$isFood());
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
        ServerLevel world = (ServerLevel) maid.level();
        if (dealyTick > 0 || slot == -1) {
            return;
        }
        ItemStack stack = maid.getInventory().getItem(slot);
        if (((ItemStackHelper) (Object) stack).reverie_dreams$isFood()) {
            DataComponentMap components = stack.getComponents();
            FoodProperties foodComponent = components.get(DataComponents.FOOD);
            if (foodComponent != null) {
                int nutritionValue = foodComponent.nutrition();
                int saturationValue = Math.round(foodComponent.saturation());
                maid.setNutrition(maid.getNutrition() + nutritionValue);
                maid.setSaturation(maid.getSaturation() + saturationValue);
                stack.shrink(1);
                world.playSound(
                        null,
                        maid.getX(),
                        maid.getY(),
                        maid.getZ(),
                        SoundEvents.PLAYER_BURP,
                        SoundSource.AMBIENT,
                        0.5F,
                        Mth.randomBetween(world.getRandom(), 0.9F, 1.0F)
                );
            }
        }
    }

}
