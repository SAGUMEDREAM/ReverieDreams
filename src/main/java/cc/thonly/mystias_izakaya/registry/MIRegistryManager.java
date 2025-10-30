package cc.thonly.mystias_izakaya.registry;

import cc.thonly.mystias_izakaya.MystiasIzakaya;
import cc.thonly.mystias_izakaya.api.DrinkPropertyLoaderCallback;
import cc.thonly.mystias_izakaya.api.FoodPropertyLoaderCallback;
import cc.thonly.mystias_izakaya.component.CraftingConflict;
import cc.thonly.mystias_izakaya.component.DrinkProperty;
import cc.thonly.mystias_izakaya.component.FoodProperty;
import cc.thonly.reverie_dreams.registry.IntrinsicalRegister;
import cc.thonly.reverie_dreams.registry.RegistryManager;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

@Slf4j
@SuppressWarnings("unchecked")
public class MIRegistryManager extends RegistryManager {
    public static final IntrinsicalRegister<FoodProperty> FOOD_PROPERTY = MIRegistryManager.<FoodProperty>ofEntry(MystiasIzakaya.id("food_property"))
            .codec(FoodProperty.CODEC)
            .reloadBuilder(FoodProperties::reload)
            .builder(FoodProperties::bootstrap);
    public static final IntrinsicalRegister<DrinkProperty> DRINK_PROPERTY = MIRegistryManager.<DrinkProperty>ofEntry(MystiasIzakaya.id("drink_property"))
            .codec(DrinkProperty.CODEC)
            .reloadBuilder(DrinkProperties::reload)
            .builder(DrinkProperties::bootstrap);
    public static final IntrinsicalRegister<CraftingConflict> CRAFTING_CONFLICT = MIRegistryManager.<CraftingConflict>ofEntry(MystiasIzakaya.id("crafting_conflict"))
            .codec(CraftingConflict.CODEC)
            .reloadBuilder(CraftingConflict::reload)
            .builder(CraftingConflict::bootstrap);

    public static void bootstrap() {
        for (var entry : ROOT.entrySet()) {
            IntrinsicalRegister<?> registry = entry.getValue();
            registry.build();
        }

        FoodPropertyLoaderCallback.EVENT.register((world, user, property) -> {
            if (world.isClientSide) {
                return;
            }
            if (property.is(FoodProperties.COOL)) {
                user.setSharedFlagOnFire(false);
                user.setRemainingFireTicks(0);
            }
            if (property.is(FoodProperties.SPICY)) {
                user.setSharedFlagOnFire(true);
                user.setRemainingFireTicks(2 * 20);
            }
            if (property.is(FoodProperties.BIZARRE)) {
                user.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 2 * 20, 1));
            }
            if (property.is(FoodProperties.GOURMET)) {
                user.addEffect(new MobEffectInstance(MobEffects.HASTE, 25 * 20, 1));
            }
            if (property.is(FoodProperties.MOUNTAIN_DELICACY)) {
                user.addEffect(new MobEffectInstance(MobEffects.LUCK, 90 * 20, 1));
            }
            if (property.is(FoodProperties.PHOTOGENIC)) {
                user.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 60 * 20, 1));
            }
            if (property.is(FoodProperties.CURSE)) {
                user.addEffect(new MobEffectInstance(MobEffects.BAD_OMEN, 120 * 20, 1));
            }
            if (property.is(FoodProperties.TOXIC)) {
                user.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 60 * 20, 1));
            }
            if (property.is(FoodProperties.LARGE_PARTITION)) {
                user.addEffect(new MobEffectInstance(MobEffects.SATURATION, 1, 1));
            }
            if (property.is(FoodProperties.SWEET)) {
                user.addEffect(new MobEffectInstance(MobEffects.SPEED, 35 * 20));
            }
            if (property.is(FoodProperties.UNBELIEVABLE)) {
                user.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 35 * 20));
            }
            if (property.is(FoodProperties.LEGENDARY)) {
                user.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 35 * 20));
            }
            if (property.is(FoodProperties.FILLING)) {
                user.addEffect(new MobEffectInstance(MobEffects.SATURATION, 1, 1));
            }
            if (property.is(FoodProperties.POWER_SURGE)) {
                user.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 45 * 20));
            }
            if (property.is(FoodProperties.OCEAN_FLAVOR)) {
                user.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 30 * 20));
            }
            if (property.is(FoodProperties.DARK_CUISINE)) {
                user.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 15 * 20));
                user.addEffect(new MobEffectInstance(MobEffects.POISON, 10 * 20));
            }
        });
        DrinkPropertyLoaderCallback.EVENT.register((world, user, property) -> {
            if (world.isClientSide) {
                return;
            }
            if (property.is(DrinkProperties.LOW_ALCOHOL)) {
                user.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 3 * 20));
            }
            if (property.is(DrinkProperties.MID_ALCOHOL)) {
                user.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 9 * 20));
            }
            if (property.is(DrinkProperties.HIGH_ALCOHOL)) {
                user.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 27 * 20));
            }
            if (property.is(DrinkProperties.CAN_ADD_ICE)) {
                user.setSharedFlagOnFire(false);
                user.setTicksFrozen(20);
            }
            if (property.is(DrinkProperties.SWEET)) {
                user.addEffect(new MobEffectInstance(MobEffects.SPEED, 10 * 20));
            }
            if (property.is(DrinkProperties.REFRESHING)) {
                user.addEffect(new MobEffectInstance(MobEffects.HASTE, 20 * 20));
                user.addEffect(new MobEffectInstance(MobEffects.SPEED, 20 * 20));
            }
            if (property.is(DrinkProperties.BITTER)) {
                user.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 20));
            }
        });
    }
}
