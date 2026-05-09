package cc.thonly.reverie_dreams.entity.ai.goal;

import cc.thonly.reverie_dreams.block.entity.FoodDisplayBlockEntity;
import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.inf.IItemStack;
import cc.thonly.reverie_dreams.recipe.ItemStackWrapper;
import cc.thonly.reverie_dreams.recipe.ItemWrapper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.gamerules.GameRules;

public class NPCEatFoodDispalyGoal extends MoveToBlockGoal {

    protected int timer;

    public NPCEatFoodDispalyGoal(BaseNPCLikeEntity maid, final double speed, final int range, final int maxYDifference) {
        super(maid, speed, range, maxYDifference);
    }

    @Override
    public double acceptedDistance() {
        return 2.0;
    }

    @Override
    public boolean shouldRecalculatePath() {
        return this.tryTicks % 100 == 0;
    }

    private boolean isFoodDisplay(LevelReader world, BlockPos pos) {
        if (world.getBlockEntity(pos) instanceof FoodDisplayBlockEntity isdBlockEntity) {
            ItemStackWrapper item = isdBlockEntity.getItem();
            return ((IItemStack) (Object) item.getItemStack()).reverie_dreams$isFood();
        }
        return false;
    }

    @Override
    protected boolean isValidTarget(LevelReader world, BlockPos pos) {
        return isFoodDisplay(world, pos);
    }

    @Override
    public void tick() {
        if (this.isReachedTarget()) {
            if (this.timer >= 40) {
                this.eatBerries();
            } else {
                this.timer++;
            }
        } else if (!this.isReachedTarget() && mob.getRandom().nextFloat() < 0.05F) {
            this.mob.playSound(SoundEvents.FOX_SNIFF, 1.0F, 1.0F);
        }

        super.tick();
    }

    protected void eatBerries() {
        BaseNPCLikeEntity maid = (BaseNPCLikeEntity) this.mob;
        Level world = maid.level();
        if (getServerLevel(world).getGameRules().get(GameRules.MOB_GRIEFING)) {
            if (isFoodDisplay(world, this.blockPos)) {
                FoodDisplayBlockEntity displayBlockEntity = (FoodDisplayBlockEntity) world.getBlockEntity(blockPos);
                //noinspection DataFlowIssue
                ItemStackWrapper item = displayBlockEntity.getItem();
                DataComponentMap components = item.getItemStack().getComponents();
                FoodProperties foodComponent = components.get(DataComponents.FOOD);
                if (foodComponent != null) {
                    maid.swing(InteractionHand.MAIN_HAND);
                    int nutritionValue = foodComponent.nutrition();
                    int saturationValue = Math.round(foodComponent.saturation());
                    maid.setNutrition(maid.getNutrition() + nutritionValue);
                    maid.setSaturation(maid.getSaturation() + saturationValue);
                    displayBlockEntity.setItem(ItemWrapper.empty().build());
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


    @Override
    public boolean canUse() {
        BaseNPCLikeEntity maid = (BaseNPCLikeEntity) this.mob;
        return ((maid.getNutrition() < 20 && maid.getHealth() < 20) || maid.getNutrition() < 10) && super.canUse();
    }

    @Override
    public void start() {
        this.timer = 0;
        super.start();
    }
}