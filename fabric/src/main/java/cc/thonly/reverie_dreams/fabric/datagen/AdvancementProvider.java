package cc.thonly.reverie_dreams.fabric.datagen;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.advancement.AdvancementIcons;
import cc.thonly.reverie_dreams.advancement.UseItemTrigger;
import cc.thonly.reverie_dreams.registry.content.FumoTypes;
import cc.thonly.reverie_dreams.registry.content.advancements.RDBuiltInAdvancements;
import cc.thonly.reverie_dreams.registry.content.block.RDKitchenBlocks;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import cc.thonly.reverie_dreams.registry.content.block.RDWoodBlocks;
import cc.thonly.reverie_dreams.registry.content.item.RDBeverageItems;
import cc.thonly.reverie_dreams.registry.content.item.RDCuisineItems;
import cc.thonly.reverie_dreams.registry.content.item.RDIngredientItems;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import cc.thonly.reverie_dreams.registry.tag.RDEntityTypeTags;
import cc.thonly.reverie_dreams.registry.tag.RDItemTags;
import cc.thonly.reverie_dreams.util.advancements.SimpleTriggerFactory;
import cc.thonly.reverie_dreams.util.advancements.SimpleTriggerKeys;
import cc.thonly.reverie_dreams.world.dimension.RDBuiltInDimensions;
import cc.thonly.reverie_dreams.world.dimension.RDBuiltinLevels;
import cc.thonly.reverie_dreams.world.gen.RDBuiltinStructures;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricAdvancementProvider;
import net.minecraft.advancements.*;
import net.minecraft.advancements.criterion.*;
import net.minecraft.core.ClientAsset;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import cc.thonly.keine.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.Structure;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@SuppressWarnings("deprecation")
public class AdvancementProvider extends FabricAdvancementProvider {
    private static boolean root = true;

    public AdvancementProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registryLookup) {
        super(output, registryLookup);
    }

    @Override
    public void generateAdvancement(HolderLookup.Provider provider, Consumer<AdvancementHolder> context) {
        final HolderLookup.RegistryLookup<Item> itemLookup = provider.lookupOrThrow(Registries.ITEM);
        final HolderLookup.RegistryLookup<Block> blockLookup = provider.lookupOrThrow(Registries.BLOCK);
        final HolderLookup.RegistryLookup<EntityType<?>> holderLookup = provider.lookupOrThrow(Registries.ENTITY_TYPE);
        final HolderLookup.RegistryLookup<Structure> structureLookup = provider.lookupOrThrow(Registries.STRUCTURE);

        AdvancementHolder root = registerAdvancement(context, RDBuiltInAdvancements.ROOT, Advancement.Builder.advancement()
                .display(makeDisplayInfo(RDItems.ICON, RDBuiltInAdvancements.ROOT, AdvancementType.TASK))
                .addCriterion("get_item", InventoryChangeTrigger.TriggerInstance.hasItems(RDItems.GUIDEBOOK))
        );

        AdvancementHolder shinyCoins = registerAdvancement(context, RDBuiltInAdvancements.SHINY_COINS, Advancement.Builder.advancement()
                .parent(root)
                .requirements(AdvancementRequirements.Strategy.OR)
                .display(makeDisplayInfo(RDItems.GOLD_COIN, RDBuiltInAdvancements.SHINY_COINS, AdvancementType.TASK))
                .addCriterion("get_coin", InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(itemLookup, RDItemTags.COIN)))
        );

        AdvancementHolder fumofumo = registerAdvancement(context, RDBuiltInAdvancements.FUMOFUMO, Advancement.Builder.advancement()
                .parent(root)
                .requirements(AdvancementRequirements.Strategy.OR)
                .display(makeDisplayInfo(FumoTypes.CIRNO.item(), RDBuiltInAdvancements.FUMOFUMO, AdvancementType.TASK))
                .addCriterion("get_fumo", InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(itemLookup, RDItemTags.FUMO)))
        );

        AdvancementHolder danmakuTable = registerAdvancement(context, RDBuiltInAdvancements.DANMAKU_TABLE, Advancement.Builder.advancement()
                .parent(root)
                .requirements(AdvancementRequirements.Strategy.OR)
                .display(makeDisplayInfo(RDBlocks.DANMAKU_CRAFTING_TABLE, RDBuiltInAdvancements.DANMAKU_TABLE, AdvancementType.TASK))
                .addCriterion("get_danmaku_crafting_table", InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(itemLookup, RDBlocks.DANMAKU_CRAFTING_TABLE)))
        );

        AdvancementHolder danmakuWars = registerAdvancement(context, RDBuiltInAdvancements.DANMAKU_WARS, Advancement.Builder.advancement()
                .parent(danmakuTable)
                .requirements(AdvancementRequirements.Strategy.OR)
                .display(makeDisplayInfo(RDItems.DANMAKU, RDBuiltInAdvancements.DANMAKU_WARS, AdvancementType.TASK))
                .addCriterion("get_danmaku_item", InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(itemLookup, RDItemTags.DANMAKU_ITEM)))
        );

        AdvancementHolder danmakuUpgrade = registerAdvancement(context, RDBuiltInAdvancements.DANMAKU_UPGRADE, Advancement.Builder.advancement()
                .parent(danmakuWars)
                .requirements(AdvancementRequirements.Strategy.OR)
                .display(makeDisplayInfo(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE, RDBuiltInAdvancements.DANMAKU_UPGRADE, AdvancementType.TASK))
                .addCriterion("get_danmaku_upgrade", SimpleTriggerFactory.create(SimpleTriggerKeys.DANMAKU_UPGRADE).createCriterion())
        );

        AdvancementHolder abandonedShrine = registerAdvancement(context, RDBuiltInAdvancements.ABANDONED_SHRINE, Advancement.Builder.advancement()
                .parent(root)
                .requirements(AdvancementRequirements.Strategy.OR)
                .display(makeDisplayInfo(RDItems.HAKUREI_CANE, RDBuiltInAdvancements.ABANDONED_SHRINE, AdvancementType.TASK))
                .addCriterion("into_abandoned_shrine", PlayerTrigger.TriggerInstance.located(LocationPredicate.Builder.inStructure(structureLookup.getOrThrow(RDBuiltinStructures.ABANDONED_ALTAR))))
        );

        AdvancementHolder enterDreams = registerAdvancement(context, RDBuiltInAdvancements.ENTER_DREAM, Advancement.Builder.advancement()
                .parent(root)
                .requirements(AdvancementRequirements.Strategy.OR)
                .display(makeDisplayInfo(Blocks.RED_BED, RDBuiltInAdvancements.ENTER_DREAM, AdvancementType.TASK))
                .addCriterion("enter_dream", ChangeDimensionTrigger.TriggerInstance.changedDimensionTo(RDBuiltinLevels.DREAM_WORLD))
        );

        AdvancementHolder rehabilitationExpert = registerAdvancement(context, RDBuiltInAdvancements.REHABILITATION_EXPERT, Advancement.Builder.advancement()
                .parent(root)
                .requirements(AdvancementRequirements.Strategy.OR)
                .display(makeDisplayInfo(RDItems.WIND_BLESSING_CANE, RDBuiltInAdvancements.REHABILITATION_EXPERT, AdvancementType.TASK))
                .addCriterion("kill_yokai", KilledTrigger.TriggerInstance.playerKilledEntity(EntityPredicate.Builder.entity().of(holderLookup, RDEntityTypeTags.YOKAI)))
        );

        AdvancementHolder burst = registerAdvancement(context, RDBuiltInAdvancements.BURST, Advancement.Builder.advancement()
                .parent(rehabilitationExpert)
                .requirements(AdvancementRequirements.Strategy.OR)
                .display(makeDisplayInfo(RDItems.BOMB, RDBuiltInAdvancements.BURST, AdvancementType.TASK))
                .addCriterion("burst", UseItemTrigger.TriggerInstance.usedItem(itemLookup, RDItems.BOMB))
        );

        AdvancementHolder levelUp = registerAdvancement(context, RDBuiltInAdvancements.LEVEL_UP, Advancement.Builder.advancement()
                .parent(rehabilitationExpert)
                .requirements(AdvancementRequirements.Strategy.OR)
                .display(makeDisplayInfo(RDItems.UPGRADED_HEALTH, RDBuiltInAdvancements.LEVEL_UP, AdvancementType.TASK))
                .addCriterion("level_up", UseItemTrigger.TriggerInstance.usedItem(itemLookup, RDItems.UPGRADED_HEALTH))
        );

        AdvancementHolder woodWithSpiritualPower = registerAdvancement(context, RDBuiltInAdvancements.WOOD_WITH_SPIRITUAL_POWER, Advancement.Builder.advancement()
                .parent(rehabilitationExpert)
                .requirements(AdvancementRequirements.Strategy.OR)
                .display(makeDisplayInfo(RDWoodBlocks.SPIRITUAL_BUNDLE.log(), RDBuiltInAdvancements.WOOD_WITH_SPIRITUAL_POWER, AdvancementType.TASK))
                .addCriterion("get_spiritual_wood", InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(itemLookup, RDWoodBlocks.SPIRITUAL_BUNDLE.log(), RDWoodBlocks.SPIRITUAL_BUNDLE.strippedLog(), RDWoodBlocks.BLESSED_SPIRITUAL_LOG)))
        );

        AdvancementHolder craftingInGensokyoAltar = registerAdvancement(context, RDBuiltInAdvancements.GENSOKYO_ALTAR_CRAFTING, Advancement.Builder.advancement()
                .parent(woodWithSpiritualPower)
                .requirements(AdvancementRequirements.Strategy.OR)
                .display(makeDisplayInfo(RDBlocks.GENSOKYO_ALTAR, RDBuiltInAdvancements.GENSOKYO_ALTAR_CRAFTING, AdvancementType.TASK))
                .addCriterion("crafting_in_gensokyo_altar", SimpleTriggerFactory.create(SimpleTriggerKeys.GENSOKYO_ALTAR_CRAFTING).createCriterion())
        );

        AdvancementHolder laylaPrismriver = registerAdvancement(context, RDBuiltInAdvancements.LAYLA_PRISMRIVER, Advancement.Builder.advancement()
                .parent(craftingInGensokyoAltar)
                .requirements(AdvancementRequirements.Strategy.OR)
                .display(makeDisplayInfo(RDItems.TRUMPET, RDBuiltInAdvancements.LAYLA_PRISMRIVER, AdvancementType.TASK))
                .addCriterion("get_musical_instruments", InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(itemLookup, RDItemTags.MUSICAL_INSTRUMENTS)))
        );

        AdvancementHolder psychologist = registerAdvancement(context, RDBuiltInAdvancements.PSYCHOLOGIST, Advancement.Builder.advancement()
                .parent(craftingInGensokyoAltar)
                .requirements(AdvancementRequirements.Strategy.OR)
                .display(makeDisplayInfo(RDItems.SATORI_EYE, RDBuiltInAdvancements.PSYCHOLOGIST, AdvancementType.TASK))
                .addCriterion("use_item_satori_eye", UseItemTrigger.TriggerInstance.usedItem(itemLookup, RDItems.SATORI_EYE))
        );

        AdvancementHolder takePhoto = registerAdvancement(context, RDBuiltInAdvancements.TAKING_PHOTO, Advancement.Builder.advancement()
                .parent(craftingInGensokyoAltar)
                .requirements(AdvancementRequirements.Strategy.OR)
                .display(makeDisplayInfo(RDItems.TENGU_CAMERA, RDBuiltInAdvancements.TAKING_PHOTO, AdvancementType.TASK))
                .addCriterion("take_photo", UseItemTrigger.TriggerInstance.usedItem(itemLookup, RDItems.TENGU_CAMERA))
        );

        AdvancementHolder makeFriend = registerAdvancement(context, RDBuiltInAdvancements.MAKE_FRIEND, Advancement.Builder.advancement()
                .parent(craftingInGensokyoAltar)
                .requirements(AdvancementRequirements.Strategy.OR)
                .display(makeDisplayInfo(RDItems.ROLE_ICON, RDBuiltInAdvancements.MAKE_FRIEND, AdvancementType.TASK))
                .addCriterion("make_friend", SimpleTriggerFactory.create(SimpleTriggerKeys.MAKING_FRIEND).createCriterion())
        );

        AdvancementHolder iWillTakeYourSoul = registerAdvancement(context, RDBuiltInAdvancements.I_WILL_TAKE_YOUR_SOUL, Advancement.Builder.advancement()
                .parent(craftingInGensokyoAltar)
                .requirements(AdvancementRequirements.Strategy.OR)
                .display(makeDisplayInfo(RDItems.DEATH_SCYTHE, RDBuiltInAdvancements.I_WILL_TAKE_YOUR_SOUL, AdvancementType.TASK))
                .addCriterion("i_will_take_your_soul", InventoryChangeTrigger.TriggerInstance.hasItems(RDItems.DEATH_SCYTHE))
        );

        AdvancementHolder touhouMystiasIzakaya = registerAdvancement(context, RDBuiltInAdvancements.TOUHOU_MYSTIA_IZAKAYA, Advancement.Builder.advancement()
                .parent(root)
                .requirements(AdvancementRequirements.Strategy.OR)
                .display(makeDisplayInfo(RDItems.MYSTIA_ICON, RDBuiltInAdvancements.TOUHOU_MYSTIA_IZAKAYA, AdvancementType.TASK))
                .addCriterion("crafting_kitchenware", InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(itemLookup, RDItemTags.KITCHENWARE)))
        );

        AdvancementHolder aCelestialBeingDescendedToEarth = registerAdvancement(context, RDBuiltInAdvancements.A_CELESTIAL_BEING_DESCENDED_TO_EARTH, Advancement.Builder.advancement()
                .parent(touhouMystiasIzakaya)
                .requirements(AdvancementRequirements.Strategy.OR)
                .display(makeDisplayInfo(RDIngredientItems.PEACH, RDBuiltInAdvancements.A_CELESTIAL_BEING_DESCENDED_TO_EARTH, AdvancementType.TASK))
                .addCriterion("a_celestial_being_descended_to_earth", SimpleTriggerFactory.create(SimpleTriggerKeys.EAT_PEACH).createCriterion())
        );

        AdvancementHolder cookingByMyself = registerAdvancement(context, RDBuiltInAdvancements.COOKING_BY_MYSELF, Advancement.Builder.advancement()
                .parent(touhouMystiasIzakaya)
                .requirements(AdvancementRequirements.Strategy.OR)
                .display(makeDisplayInfo(RDKitchenBlocks.COOKING_POT, RDBuiltInAdvancements.COOKING_BY_MYSELF, AdvancementType.TASK))
                .addCriterion("cooking_food", SimpleTriggerFactory.create(SimpleTriggerKeys.KITCHEN_COOKING).createCriterion())
        );

        AdvancementHolder cookingByMyselfAmount5Tag = registerAdvancement(context, RDBuiltInAdvancements.COOKING_BY_MYSELF_AMOUNT_5_TAG, Advancement.Builder.advancement()
                .parent(cookingByMyself)
                .requirements(AdvancementRequirements.Strategy.OR)
                .display(makeDisplayInfo(Items.CAKE, RDBuiltInAdvancements.COOKING_BY_MYSELF_AMOUNT_5_TAG, AdvancementType.TASK))
                .addCriterion("cooking_food_for_5_tag", SimpleTriggerFactory.create(SimpleTriggerKeys.KITCHEN_COOKING_AMOUNT_OF_5_TAG).createCriterion())
        );

        AdvancementHolder darkCuisine = registerAdvancement(context, RDBuiltInAdvancements.DARK_CUISINE, Advancement.Builder.advancement()
                .parent(cookingByMyself)
                .requirements(AdvancementRequirements.Strategy.OR)
                .display(makeDisplayInfo(RDCuisineItems.DARK_CUISINE, RDBuiltInAdvancements.DARK_CUISINE, AdvancementType.TASK))
                .addCriterion("dark_cuisine", SimpleTriggerFactory.create(SimpleTriggerKeys.KITCHEN_DARK_CUISINE).createCriterion())
        );

        AdvancementHolder delicacy = registerAdvancement(context, RDBuiltInAdvancements.DELICACY, Advancement.Builder.advancement()
                .parent(cookingByMyself)
                .requirements(AdvancementRequirements.Strategy.OR)
                .display(makeDisplayInfo(RDCuisineItems.RICE_BALL, RDBuiltInAdvancements.DELICACY, AdvancementType.TASK))
                .addCriterion("eat_cooked_food", SimpleTriggerFactory.create(SimpleTriggerKeys.EAT_FOOD).createCriterion())
        );

        AdvancementHolder fineWine = registerAdvancement(context, RDBuiltInAdvancements.FINE_WINE, Advancement.Builder.advancement()
                .parent(touhouMystiasIzakaya)
                .requirements(AdvancementRequirements.Strategy.OR)
                .display(makeDisplayInfo(RDBeverageItems.BEER, RDBuiltInAdvancements.FINE_WINE, AdvancementType.TASK))
                .addCriterion("having_drink", SimpleTriggerFactory.create(SimpleTriggerKeys.HAVING_DRINK).createCriterion())
        );

        AdvancementHolder touhouPeopleCanFly = registerAdvancement(context, RDBuiltInAdvancements.TOUHOU_PEOPLE_CAN_FLY, Advancement.Builder.advancement()
                .parent(craftingInGensokyoAltar)
                .requirements(AdvancementRequirements.Strategy.OR)
                .display(makeDisplayInfo(Items.ELYTRA, RDBuiltInAdvancements.TOUHOU_PEOPLE_CAN_FLY, AdvancementType.TASK))
                .addCriterion("riding_broom", SimpleTriggerFactory.create(SimpleTriggerKeys.TOUHOU_PEOPLE_CAN_FLY).createCriterion())
        );

        AdvancementHolder welcomeToTheMoonTour = registerAdvancement(context, RDBuiltInAdvancements.WELCOME_TO_THE_MOON_TOUR, Advancement.Builder.advancement()
                .parent(root)
                .requirements(AdvancementRequirements.Strategy.OR)
                .display(makeDisplayInfo(RDBlocks.MOON_STONE.block(), RDBuiltInAdvancements.WELCOME_TO_THE_MOON_TOUR, AdvancementType.TASK))
                .addCriterion("enter_moon", ChangeDimensionTrigger.TriggerInstance.changedDimensionTo(RDBuiltinLevels.getMoon()))
        );

        AdvancementHolder servingDishesByThrowing = registerAdvancement(context, RDBuiltInAdvancements.SERVING_DISHES_BY_THROWING, Advancement.Builder.advancement()
                .parent(cookingByMyself)
                .requirements(AdvancementRequirements.Strategy.OR)
                .display(makeDisplayInfo(Items.BOW, RDBuiltInAdvancements.SERVING_DISHES_BY_THROWING, AdvancementType.TASK))
                .addCriterion("throwing_dishes", SimpleTriggerFactory.create(SimpleTriggerKeys.SERVING_DISHES_BY_THROWING).createCriterion())
        );

        AdvancementHolder treasuresBeneathTheMoon = registerAdvancement(context, RDBuiltInAdvancements.TREASURES_BENEATH_THE_MOON, Advancement.Builder.advancement()
                .parent(welcomeToTheMoonTour)
                .requirements(AdvancementRequirements.Strategy.AND)
                .display(makeDisplayInfo(Items.CHEST, RDBuiltInAdvancements.TREASURES_BENEATH_THE_MOON, AdvancementType.TASK))
                .addCriterion("open_chest", SimpleTriggerFactory.create(SimpleTriggerKeys.OPEN_CHEST).createCriterion())
                .addCriterion("in_the_moon", PlayerTrigger.TriggerInstance.located(LocationPredicate.Builder.location().setDimension(RDBuiltinLevels.getMoon())))
        );

        AdvancementHolder waiter = registerAdvancement(context, RDBuiltInAdvancements.WAITER, Advancement.Builder.advancement()
                .parent(touhouMystiasIzakaya)
                .requirements(AdvancementRequirements.Strategy.OR)
                .display(makeDisplayInfo(RDBlocks.PLATE, RDBuiltInAdvancements.WAITER, AdvancementType.TASK))
                .addCriterion("waiter", SimpleTriggerFactory.create(SimpleTriggerKeys.WAITER).createCriterion())
        );

        AdvancementHolder youkaiSecretBrew = registerAdvancement(context, RDBuiltInAdvancements.YOUKAI_SECRET_BREW, Advancement.Builder.advancement()
                .parent(touhouMystiasIzakaya)
                .requirements(AdvancementRequirements.Strategy.OR)
                .display(makeDisplayInfo(RDBlocks.BREWING_BARREL, RDBuiltInAdvancements.YOUKAI_SECRET_BREW, AdvancementType.TASK))
                .addCriterion("brewing_wine", SimpleTriggerFactory.create(SimpleTriggerKeys.YOUKAI_SECRET_BREW).createCriterion())
        );

        this.generateHidedAdvancement(provider, context, root);
        AdvancementProvider.root = false;
    }

    public void generateHidedAdvancement(HolderLookup.Provider provider, Consumer<AdvancementHolder> context, AdvancementHolder root) {
        DisplayInfo askingForMoneydisplayInfo = new DisplayInfo(AdvancementIcons.ASKING_FOR_MONEY.create(),
                RDBuiltInAdvancements.getTitleComponent(RDBuiltInAdvancements.ASKING_FOR_MONEY),
                RDBuiltInAdvancements.getDescriptionComponent(RDBuiltInAdvancements.ASKING_FOR_MONEY),
                Optional.of(RDBuiltInAdvancements.ADVANCEMENT_BACKGROUND).map(ClientAsset.ResourceTexture::new),
                AdvancementType.CHALLENGE,
                true,
                true,
                true
        );
        AdvancementHolder askingForMoney = registerAdvancement(context, RDBuiltInAdvancements.ASKING_FOR_MONEY, Advancement.Builder.advancement()
                .parent(root)
                .requirements(AdvancementRequirements.Strategy.OR)
                .display(askingForMoneydisplayInfo)
                .addCriterion("pay_1500", SimpleTriggerFactory.create(SimpleTriggerKeys.ASKING_FOR_MONEY).createCriterion())
        );
    }

    public static DisplayInfo makeDisplayInfo(ItemLike item, ResourceKey<Advancement> key, AdvancementType type) {
        DisplayInfo displayInfo = new DisplayInfo(new ItemStackTemplate(item.asItem()).create(),
                RDBuiltInAdvancements.getTitleComponent(key),
                RDBuiltInAdvancements.getDescriptionComponent(key),
                AdvancementProvider.root ? Optional.of(RDBuiltInAdvancements.ADVANCEMENT_BACKGROUND).map(ClientAsset.ResourceTexture::new) : Optional.empty(),
                type,
                true,
                true,
                false
        );
        AdvancementProvider.root = false;
        return displayInfo;
    }

    public static AdvancementHolder registerAdvancement(Consumer<AdvancementHolder> context, ResourceKey<Advancement> key, Advancement.Builder builder) {
        AdvancementHolder holder = builder.build(key.identifier());
        context.accept(holder);
        return holder;
    }
}
