package cc.thonly.reverie_dreams.registry;

import cc.thonly.reverie_dreams.data.CraftingConflict;
import cc.thonly.reverie_dreams.data.DrinkProperty;
import cc.thonly.reverie_dreams.data.FoodProperty;
import cc.thonly.reverie_dreams.data.skin.CustomSkinConfig;
import cc.thonly.reverie_dreams.data.skin.CustomType;
import cc.thonly.reverie_dreams.data.skin.SkinType;
import cc.thonly.reverie_dreams.registry.impl.RegistrySyncer;
import cc.thonly.reverie_dreams.registry.syncer.CraftingConflictSyncer;
import cc.thonly.reverie_dreams.registry.syncer.CustomSkinSyncer;
import cc.thonly.reverie_dreams.registry.syncer.DrinkPropertySyncer;
import cc.thonly.reverie_dreams.registry.syncer.FoodPropertySyncer;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Supplier;

@Slf4j
public class RegistrySyncers {
    public static final Supplier<RegistrySyncer<FoodProperty, FoodProperty.Data>> FOOD_PROPERTY = new FoodPropertySyncer();
    public static final Supplier<RegistrySyncer<DrinkProperty, DrinkProperty.Data>> DRINK_PROPERTY = new DrinkPropertySyncer();
    public static final Supplier<RegistrySyncer<CraftingConflict, CraftingConflict>> CRAFTING_CONFLICT = new CraftingConflictSyncer();
    public static final Supplier<RegistrySyncer<CustomType, CustomSkinConfig>> CUSTOM_SKIN = new CustomSkinSyncer();
}
