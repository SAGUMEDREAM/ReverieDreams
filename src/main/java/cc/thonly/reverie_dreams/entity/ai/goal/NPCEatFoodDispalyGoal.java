package cc.thonly.reverie_dreams.entity.ai.goal;

import cc.thonly.mystias_izakaya.block.entity.ItemStackDisplayBlockEntity;
import cc.thonly.reverie_dreams.entity.npc.NPCEntityImpl;
import cc.thonly.reverie_dreams.interfaces.IItemStack;
import cc.thonly.reverie_dreams.recipe.ItemStackRecipeWrapper;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.ai.goal.MoveToTargetPosGoal;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

public class NPCEatFoodDispalyGoal extends MoveToTargetPosGoal {

    protected int timer;

    public NPCEatFoodDispalyGoal(NPCEntityImpl maid, final double speed, final int range, final int maxYDifference) {
        super(maid, speed, range, maxYDifference);
    }

    @Override
    public double getDesiredDistanceToTarget() {
        return 2.0;
    }

    @Override
    public boolean shouldResetPath() {
        return this.tryingTime % 100 == 0;
    }

    private boolean isFoodDisplay(WorldView world, BlockPos pos) {
        if (world.getBlockEntity(pos) instanceof ItemStackDisplayBlockEntity isdBlockEntity) {
            ItemStackRecipeWrapper item = isdBlockEntity.getItem();
            return ((IItemStack) (Object) item.getItemStack()).isFood();
        }
        return false;
    }

    @Override
    protected boolean isTargetPos(WorldView world, BlockPos pos) {
        return isFoodDisplay(world, pos);
    }

    @Override
    public void tick() {
        if (this.hasReached()) {
            if (this.timer >= 40) {
                this.eatBerries();
            } else {
                this.timer++;
            }
        } else if (!this.hasReached() && mob.getRandom().nextFloat() < 0.05F) {
            this.mob.playSound(SoundEvents.ENTITY_FOX_SNIFF, 1.0F, 1.0F);
        }

        super.tick();
    }

    protected void eatBerries() {
        NPCEntityImpl maid = (NPCEntityImpl) this.mob;
        World world = maid.getWorld();
        if (castToServerWorld(world).getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING)) {
            if (isFoodDisplay(world, this.targetPos)) {
                ItemStackDisplayBlockEntity displayBlockEntity = (ItemStackDisplayBlockEntity) world.getBlockEntity(targetPos);
                ItemStackRecipeWrapper item = displayBlockEntity.getItem();
                ComponentMap components = item.getItemStack().getComponents();
                FoodComponent foodComponent = components.get(DataComponentTypes.FOOD);
                if (foodComponent != null) {
                    maid.swingHand(Hand.MAIN_HAND);
                    int nutritionValue = foodComponent.nutrition();
                    int saturationValue = Math.round(foodComponent.saturation());
                    maid.setNutrition(maid.getNutrition() + nutritionValue);
                    maid.setSaturation(maid.getSaturation() + saturationValue);
                    //displayBlockEntity.setItem(new ItemStackRecipeWrapper(new ItemStack(Blocks.AIR)));
                    displayBlockEntity.setItem(ItemStackRecipeWrapper.empty());
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


    @Override
    public boolean canStart() {
        NPCEntityImpl maid = (NPCEntityImpl) this.mob;
        return ((maid.getNutrition() < 20 && maid.getHealth() < 20) || maid.getNutrition() < 10) && super.canStart();
    }

    @Override
    public void start() {
        this.timer = 0;
        super.start();
    }
}