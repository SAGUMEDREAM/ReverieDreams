package cc.thonly.reverie_dreams.entity.npc.container;

import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodConstants;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class NPCFoodDataContainer {
    @Getter
    @Setter
    private int nutrition = 20;
    @Getter
    @Setter
    private int saturation = 20;          //饱食
    @Getter
    @Setter
    private float exhaustionLevel = 0;    //消耗
    @Getter
    @Setter
    protected int hungerTick = 20;

    private final BaseNPCLikeEntity npc;
    private final RandomSource random;

    public NPCFoodDataContainer(BaseNPCLikeEntity npc, RandomSource random) {
        this.npc = npc;
        this.random = random;
    }

    public void eat(net.minecraft.world.food.FoodProperties properties) {
        this.addFoodData(properties.nutrition(), properties.saturation());
    }

    public void eat(int food, float saturationModifier) {
        this.addFoodData(food, FoodConstants.saturationByModifier(food, saturationModifier));
    }

    public void addFoodData(int food, float saturation) {
        this.setNutrition(Mth.clamp(food + this.getNutrition(), 0, 20));
        this.setSaturation((int) Mth.clamp(saturation + this.getSaturation(), 0.0F, (float) this.getNutrition()));
    }

    public void reduceHunger(float value) {
        float remaining = value;

        while (remaining > 0.0f) {
            if (this.saturation > 0.0f) {
                float delta = Math.min(0.5f, Math.min(this.saturation, remaining));
                this.saturation -= delta;
                remaining -= delta;
            } else if (this.nutrition > 0.0f) {
                float delta = Math.min(0.5f, Math.min(this.nutrition, remaining));
                this.nutrition -= delta;
                remaining -= delta;
            } else {
                break;
            }
        }
    }

    public void tick() {

    }

    public void updateHunger() {
        if (this.npc.consumeHunger()) {
            this.nutrition = Math.clamp(this.nutrition, 0, 20);
            this.saturation = Math.clamp(this.saturation, 0, this.nutrition);
            if (exhaustionLevel >= 4) {
                if (this.saturation > 0) {
                    this.saturation--;
                    this.exhaustionLevel = 0;
                } else if (this.nutrition > 0) {
                    this.nutrition--;
                    this.exhaustionLevel = 0;
                }
            }
        }
    }

    public void updateHungerConsumption() {
        this.hungerTick--;
        if (this.hungerTick <= 0) {
            this.hungerTick = 20;
            int hungerEffectLevel = 0;
            MobEffectInstance hungerEff = this.npc.getEffect(MobEffects.HUNGER);
            if (hungerEff != null) {
                hungerEffectLevel = hungerEff.getAmplifier();
                // System.out.println("饥饿消耗 "+ hungerEffectLevel);
            }
            this.exhaustionLevel += (float) (hungerEffectLevel * 0.1);
            if (this.npc.getNavigation().isInProgress()) {
                this.exhaustionLevel += 0.015F;//无法检测具体行为 按0.015计算 略微提高消耗
                // System.out.println("寻路增加消耗");
            }
        }
    }

    public void addAdditionalSaveData(ValueOutput view) {
        view.putFloat("Food.Nutrition", this.nutrition);
        view.putFloat("Food.Saturation", this.saturation);
        view.putFloat("Food.ExhaustionLevel", this.exhaustionLevel);
    }

    public void readAdditionalSaveData(ValueInput view) {
        this.nutrition = view.getIntOr("Food.Nutrition", view.getIntOr("FoodNutrition", 20));
        this.saturation = view.getIntOr("Food.Saturation", view.getIntOr("FoodSaturation", 20));
        this.exhaustionLevel = view.getIntOr("Food.ExhaustionLevel", view.getIntOr("FoodExhaustionLevel", 0));
    }
}
