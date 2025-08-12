package cc.thonly.mystias_izakaya.registry;

import cc.thonly.mystias_izakaya.MystiasIzakaya;
import cc.thonly.mystias_izakaya.component.CraftingConflict;
import cc.thonly.mystias_izakaya.component.DrinkProperty;
import cc.thonly.mystias_izakaya.component.FoodProperty;
import cc.thonly.mystias_izakaya.api.FoodPropertyLoaderCallback;
import cc.thonly.reverie_dreams.registry.RegistryManager;
import cc.thonly.reverie_dreams.registry.StandaloneRegistry;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

@Slf4j
public class MIRegistryManager extends RegistryManager {
    public static final StandaloneRegistry<FoodProperty> FOOD_PROPERTY = ofEntry(FoodProperty.class, MystiasIzakaya.id("food_property"))
            .codec(FoodProperty.CODEC)
            .reloadable(FoodProperties::reload)
            .build(FoodProperties::bootstrap);
    public static final StandaloneRegistry<DrinkProperty> DRINK_PROPERTY = ofEntry(DrinkProperty.class, MystiasIzakaya.id("drink_property"))
            .codec(DrinkProperty.CODEC)
            .reloadable(DrinkProperties::reload)
            .build(DrinkProperties::bootstrap);
    public static final StandaloneRegistry<CraftingConflict> CRAFTING_CONFLICT = ofEntry(CraftingConflict.class, MystiasIzakaya.id("crafting_conflict"))
            .codec(CraftingConflict.CODEC)
            .reloadable(CraftingConflict::reload)
            .build(CraftingConflict::bootstrap);

    public static void bootstrap() {
        FOOD_PROPERTY.apply();
        FoodPropertyLoaderCallback.EVENT.register((world, user, property) -> {
            if (property.is(FoodProperties.COOL)) {
                user.setOnFire(false);
                user.setFireTicks(0);
            }
            if (property.is(FoodProperties.SPICY)) {
                user.setOnFire(true);
                user.setFireTicks(2 * 20);
            }
            if (property.is(FoodProperties.BIZARRE)) {
                user.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 15 * 20, 1));
            }
            if (property.is(FoodProperties.GOURMET)) {
                user.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, 25 * 20, 1));
            }
            if (property.is(FoodProperties.MOUNTAIN_DELICACY)) {
                user.addStatusEffect(new StatusEffectInstance(StatusEffects.LUCK, 90 * 20, 1));
            }
            if (property.is(FoodProperties.PHOTOGENIC)) {
                user.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 60 * 20, 1));
            }
            if (property.is(FoodProperties.CURSE)) {
                user.addStatusEffect(new StatusEffectInstance(StatusEffects.BAD_OMEN, 120 * 20, 1));
            }
            if (property.is(FoodProperties.TOXIC)) {
                user.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 60 * 20, 1));
            }
            if (property.is(FoodProperties.LARGE_PARTITION)) {
                user.addStatusEffect(new StatusEffectInstance(StatusEffects.SATURATION, 1, 1));
            }
            if (property.is(FoodProperties.SWEET)) {
                user.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 35 * 20));
            }
            if (property.is(FoodProperties.UNBELIEVABLE)) {
                user.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 35 * 20));
            }
            if (property.is(FoodProperties.LEGENDARY)) {
                user.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 35 * 20));
            }
            if (property.is(FoodProperties.FILLING)) {
                user.addStatusEffect(new StatusEffectInstance(StatusEffects.SATURATION, 1, 1));
            }
            if (property.is(FoodProperties.POWER_SURGE)) {
                user.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 45 * 20));
            }
            if (property.is(FoodProperties.OCEAN_FLAVOR)) {
                user.addStatusEffect(new StatusEffectInstance(StatusEffects.WATER_BREATHING, 30 * 20));
            }
            if (property.is(FoodProperties.DARK_CUISINE)) {
                user.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 15 * 20));
                user.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 10 * 20));
            }
        });
    }
}
