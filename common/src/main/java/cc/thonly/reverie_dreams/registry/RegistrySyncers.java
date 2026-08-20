package cc.thonly.reverie_dreams.registry;

import cc.thonly.reverie_dreams.data.CraftingConflict;
import cc.thonly.reverie_dreams.data.BeverageProperty;
import cc.thonly.reverie_dreams.data.Customer;
import cc.thonly.reverie_dreams.data.FoodProperty;
import cc.thonly.reverie_dreams.data.skin.CustomSkinConfig;
import cc.thonly.reverie_dreams.data.skin.CustomType;
import cc.thonly.reverie_dreams.registry.impl.RegistrySyncer;
import cc.thonly.reverie_dreams.registry.syncer.*;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Supplier;

@Slf4j
public class RegistrySyncers {
    public static final Supplier<RegistrySyncer<FoodProperty, FoodProperty.Data>> FOOD_PROPERTY = new FoodPropertySyncer();
    public static final Supplier<RegistrySyncer<BeverageProperty, BeverageProperty.Data>> BEVERAGE_PROPERTY = new BeveragePropertySyncer();
    public static final Supplier<RegistrySyncer<CraftingConflict, CraftingConflict>> CRAFTING_CONFLICT = new CraftingConflictSyncer();
    public static final Supplier<RegistrySyncer<CustomType, CustomSkinConfig>> CUSTOM_SKIN = new CustomSkinSyncer();
    public static final Supplier<RegistrySyncer<Customer, Customer.Data>> CUSTOMER_DATA = new CustomerSyncer();
}
