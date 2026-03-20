package cc.thonly.reverie_dreams.datagen;

import cc.thonly.reverie_dreams.advancement.*;
import cc.thonly.reverie_dreams.block.KitchenBlockType;
import cc.thonly.reverie_dreams.registry.content.FumoTypes;
import cc.thonly.reverie_dreams.registry.content.advancements.RDAdvancements;
import cc.thonly.reverie_dreams.registry.content.block.KitchenBlocks;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import cc.thonly.reverie_dreams.registry.content.block.RDWoodBlocks;
import cc.thonly.reverie_dreams.registry.content.item.RDDrinkItems;
import cc.thonly.reverie_dreams.registry.content.item.RDFoodItems;
import cc.thonly.reverie_dreams.registry.content.item.RDIngredientItems;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import cc.thonly.reverie_dreams.registry.tag.RDEntityTypeTags;
import cc.thonly.reverie_dreams.registry.tag.RDItemTags;
import cc.thonly.reverie_dreams.util.advancements.SimpleTriggerFactory;
import cc.thonly.reverie_dreams.util.advancements.SimpleTriggerKeys;
import cc.thonly.reverie_dreams.world.dimension.WorldInit;
import cc.thonly.reverie_dreams.world.gen.ModStructures;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricAdvancementProvider;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.advancements.criterion.*;
import net.minecraft.core.ClientAsset;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.Structure;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

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

        AdvancementHolder root = registerAdvancement(context, RDAdvancements.ROOT, Advancement.Builder.advancement()
                .display(makeDisplayInfo(RDItems.ICON, RDAdvancements.ROOT, AdvancementType.TASK))
                .addCriterion("get_item", InventoryChangeTrigger.TriggerInstance.hasItems(RDItems.GUIDEBOOK))
        );

        AdvancementHolder shinyCoins = registerAdvancement(context, RDAdvancements.SHINY_COINS, Advancement.Builder.advancement()
                .parent(root)
                .display(makeDisplayInfo(RDItems.GOLD_COIN, RDAdvancements.SHINY_COINS, AdvancementType.TASK))
                .addCriterion("get_coin", InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(itemLookup, RDItemTags.COINS)))
        );

        AdvancementHolder fumofumo = registerAdvancement(context, RDAdvancements.FUMOFUMO, Advancement.Builder.advancement()
                .parent(root)
                .display(makeDisplayInfo(FumoTypes.CIRNO.item(), RDAdvancements.FUMOFUMO, AdvancementType.TASK))
                .addCriterion("get_fumo", InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(itemLookup, RDItemTags.FUMO)))
        );

        AdvancementHolder danmakuTable = registerAdvancement(context, RDAdvancements.DANMAKU_TABLE, Advancement.Builder.advancement()
                .parent(root)
                .display(makeDisplayInfo(RDBlocks.DANMAKU_CRAFTING_TABLE, RDAdvancements.DANMAKU_TABLE, AdvancementType.TASK))
                .addCriterion("get_danmaku_crafting_table", InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(itemLookup, RDBlocks.DANMAKU_CRAFTING_TABLE)))
        );

        AdvancementHolder danmakuWars = registerAdvancement(context, RDAdvancements.DANMAKU_WARS, Advancement.Builder.advancement()
                .parent(danmakuTable)
                .display(makeDisplayInfo(RDItems.DANMAKU, RDAdvancements.DANMAKU_WARS, AdvancementType.TASK))
                .addCriterion("get_danmaku_item", InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(itemLookup, RDItemTags.DANMAKU_ITEM)))
        );

        AdvancementHolder danmakuUpgrade = registerAdvancement(context, RDAdvancements.DANMAKU_UPGRADE, Advancement.Builder.advancement()
                .parent(danmakuWars)
                .display(makeDisplayInfo(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE, RDAdvancements.DANMAKU_UPGRADE, AdvancementType.TASK))
                .addCriterion("get_danmaku_upgrade", SimpleTriggerFactory.create(SimpleTriggerKeys.DANMAKU_UPGRADE).createCriterion())
        );

        AdvancementHolder abandonedShrine = registerAdvancement(context, RDAdvancements.ABANDONED_SHRINE, Advancement.Builder.advancement()
                .parent(root)
                .display(makeDisplayInfo(RDItems.HAKUREI_CANE, RDAdvancements.ABANDONED_SHRINE, AdvancementType.TASK))
                .addCriterion("into_abandoned_shrine", PlayerTrigger.TriggerInstance.located(LocationPredicate.Builder.inStructure(structureLookup.getOrThrow(ModStructures.ABANDONED_ALTAR))))
        );

        AdvancementHolder enterDreams = registerAdvancement(context, RDAdvancements.ENTER_DREAM, Advancement.Builder.advancement()
                .parent(root)
                .display(makeDisplayInfo(Blocks.RED_BED, RDAdvancements.ENTER_DREAM, AdvancementType.TASK))
                .addCriterion("enter_dream", ChangeDimensionTrigger.TriggerInstance.changedDimensionTo(WorldInit.DREAM_WORLD))
        );

        AdvancementHolder rehabilitationExpert = registerAdvancement(context, RDAdvancements.REHABILITATION_EXPERT, Advancement.Builder.advancement()
                .parent(root)
                .display(makeDisplayInfo(RDItems.WIND_BLESSING_CANE, RDAdvancements.REHABILITATION_EXPERT, AdvancementType.TASK))
                .addCriterion("kill_yokai", KilledTrigger.TriggerInstance.playerKilledEntity(EntityPredicate.Builder.entity().of(holderLookup, RDEntityTypeTags.YOKAI)))
        );

        AdvancementHolder burst = registerAdvancement(context, RDAdvancements.BURST, Advancement.Builder.advancement()
                .parent(rehabilitationExpert)
                .display(makeDisplayInfo(RDItems.BOMB, RDAdvancements.BURST, AdvancementType.TASK))
                .addCriterion("burst", UseItemTrigger.TriggerInstance.usedItem(itemLookup, RDItems.BOMB))
        );

        AdvancementHolder levelUp = registerAdvancement(context, RDAdvancements.LEVEL_UP, Advancement.Builder.advancement()
                .parent(rehabilitationExpert)
                .display(makeDisplayInfo(RDItems.UPGRADED_HEALTH, RDAdvancements.LEVEL_UP, AdvancementType.TASK))
                .addCriterion("level_up", UseItemTrigger.TriggerInstance.usedItem(itemLookup, RDItems.UPGRADED_HEALTH))
        );

        AdvancementHolder woodWithSpiritualPower = registerAdvancement(context, RDAdvancements.WOOD_WITH_SPIRITUAL_POWER, Advancement.Builder.advancement()
                .parent(rehabilitationExpert)
                .display(makeDisplayInfo(RDWoodBlocks.SPIRITUAL.log(), RDAdvancements.WOOD_WITH_SPIRITUAL_POWER, AdvancementType.TASK))
                .addCriterion("get_spiritual_wood", InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(itemLookup, RDWoodBlocks.SPIRITUAL.log(), RDWoodBlocks.SPIRITUAL.strippedLog(), RDWoodBlocks.BLESSED_SPIRITUAL_LOG)))
        );

        AdvancementHolder craftingInGensokyoAltar = registerAdvancement(context, RDAdvancements.GENSOKYO_ALTAR_CRAFTING, Advancement.Builder.advancement()
                .parent(woodWithSpiritualPower)
                .display(makeDisplayInfo(RDBlocks.GENSOKYO_ALTAR, RDAdvancements.GENSOKYO_ALTAR_CRAFTING, AdvancementType.TASK))
                .addCriterion("crafting_in_gensokyo_altar", SimpleTriggerFactory.create(SimpleTriggerKeys.GENSOKYO_ALTAR_CRAFTING).createCriterion())
        );

        AdvancementHolder laylaPrismriver = registerAdvancement(context, RDAdvancements.LAYLA_PRISMRIVER, Advancement.Builder.advancement()
                .parent(craftingInGensokyoAltar)
                .display(makeDisplayInfo(RDItems.TRUMPET, RDAdvancements.LAYLA_PRISMRIVER, AdvancementType.TASK))
                .addCriterion("get_musical_instruments", InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(itemLookup, RDItemTags.MUSICAL_INSTRUMENTS)))
        );

        AdvancementHolder psychologist = registerAdvancement(context, RDAdvancements.PSYCHOLOGIST, Advancement.Builder.advancement()
                .parent(craftingInGensokyoAltar)
                .display(makeDisplayInfo(RDItems.SATORI_EYE, RDAdvancements.PSYCHOLOGIST, AdvancementType.TASK))
                .addCriterion("use_item_satori_eye", UseItemTrigger.TriggerInstance.usedItem(itemLookup, RDItems.SATORI_EYE))
        );

        AdvancementHolder takePhoto = registerAdvancement(context, RDAdvancements.TAKING_PHOTO, Advancement.Builder.advancement()
                .parent(craftingInGensokyoAltar)
                .display(makeDisplayInfo(RDItems.TENGU_CAMERA, RDAdvancements.TAKING_PHOTO, AdvancementType.TASK))
                .addCriterion("take_photo", UseItemTrigger.TriggerInstance.usedItem(itemLookup, RDItems.TENGU_CAMERA))
        );

        AdvancementHolder makeFriend = registerAdvancement(context, RDAdvancements.MAKE_FRIEND, Advancement.Builder.advancement()
                .parent(craftingInGensokyoAltar)
                .display(makeDisplayInfo(RDItems.ROLE_ICON, RDAdvancements.MAKE_FRIEND, AdvancementType.TASK))
                .addCriterion("make_friend", SimpleTriggerFactory.create(SimpleTriggerKeys.MAKING_FRIEND).createCriterion())
        );

        AdvancementHolder iWillTakeYourSoul = registerAdvancement(context, RDAdvancements.I_WILL_TAKE_YOUR_SOUL, Advancement.Builder.advancement()
                .parent(craftingInGensokyoAltar)
                .display(makeDisplayInfo(RDItems.DEATH_SCYTHE, RDAdvancements.I_WILL_TAKE_YOUR_SOUL, AdvancementType.TASK))
                .addCriterion("i_will_take_your_soul", InventoryChangeTrigger.TriggerInstance.hasItems(RDItems.DEATH_SCYTHE))
        );

        AdvancementHolder touhouMystiasIzakaya = registerAdvancement(context, RDAdvancements.TOUHOU_MYSTIA_IZAKAYA, Advancement.Builder.advancement()
                .parent(root)
                .display(makeDisplayInfo(RDItems.MYSTIA_ICON, RDAdvancements.TOUHOU_MYSTIA_IZAKAYA, AdvancementType.TASK))
                .addCriterion("crafting_kitchenware", InventoryChangeTrigger.TriggerInstance.hasItems(KitchenBlockType.getBlockItemArrays()))
        );

        AdvancementHolder aCelestialBeingDescendedToEarth = registerAdvancement(context, RDAdvancements.A_CELESTIAL_BEING_DESCENDED_TO_EARTH, Advancement.Builder.advancement()
                .parent(touhouMystiasIzakaya)
                .display(makeDisplayInfo(RDIngredientItems.PEACH, RDAdvancements.A_CELESTIAL_BEING_DESCENDED_TO_EARTH, AdvancementType.TASK))
                .addCriterion("a_celestial_being_descended_to_earth", SimpleTriggerFactory.create(SimpleTriggerKeys.EAT_PEACH).createCriterion())
        );

        AdvancementHolder cookingByMyself = registerAdvancement(context, RDAdvancements.COOKING_BY_MYSELF, Advancement.Builder.advancement()
                .parent(touhouMystiasIzakaya)
                .display(makeDisplayInfo(KitchenBlocks.COOKING_POT, RDAdvancements.COOKING_BY_MYSELF, AdvancementType.TASK))
                .addCriterion("cooking_food", SimpleTriggerFactory.create(SimpleTriggerKeys.KITCHEN_COOKING).createCriterion())
        );

        AdvancementHolder cookingByMyselfAmount5Tag = registerAdvancement(context, RDAdvancements.COOKING_BY_MYSELF_AMOUNT_5_TAG, Advancement.Builder.advancement()
                .parent(cookingByMyself)
                .display(makeDisplayInfo(Items.CAKE, RDAdvancements.COOKING_BY_MYSELF_AMOUNT_5_TAG, AdvancementType.TASK))
                .addCriterion("cooking_food_for_5_tag", SimpleTriggerFactory.create(SimpleTriggerKeys.KITCHEN_COOKING_AMOUNT_OF_5_TAG).createCriterion())
        );

        AdvancementHolder darkCuisine = registerAdvancement(context, RDAdvancements.DARK_CUISINE, Advancement.Builder.advancement()
                .parent(cookingByMyself)
                .display(makeDisplayInfo(RDFoodItems.DARK_CUISINE, RDAdvancements.DARK_CUISINE, AdvancementType.TASK))
                .addCriterion("dark_cuisine", SimpleTriggerFactory.create(SimpleTriggerKeys.KITCHEN_DARK_CUISINE).createCriterion())
        );

        AdvancementHolder delicacy = registerAdvancement(context, RDAdvancements.DELICACY, Advancement.Builder.advancement()
                .parent(cookingByMyself)
                .display(makeDisplayInfo(RDFoodItems.RICE_BALL, RDAdvancements.DELICACY, AdvancementType.TASK))
                .addCriterion("eat_cooked_food", SimpleTriggerFactory.create(SimpleTriggerKeys.EAT_FOOD).createCriterion())
        );

        AdvancementHolder fineWine = registerAdvancement(context, RDAdvancements.FINE_WINE, Advancement.Builder.advancement()
                .parent(touhouMystiasIzakaya)
                .display(makeDisplayInfo(RDDrinkItems.BEER, RDAdvancements.FINE_WINE, AdvancementType.TASK))
                .addCriterion("having_drink", SimpleTriggerFactory.create(SimpleTriggerKeys.HAVING_DRINK).createCriterion())
        );

        AdvancementProvider.root = false;
    }

    public static DisplayInfo makeDisplayInfo(ItemLike item, ResourceKey<Advancement> key, AdvancementType type) {
        DisplayInfo displayInfo = new DisplayInfo(new ItemStack(item),
                RDAdvancements.getTitleComponent(key),
                RDAdvancements.getDescriptionComponent(key),
                AdvancementProvider.root ? Optional.of(RDAdvancements.ADVANCEMENT_BACKGROUND).map(ClientAsset.ResourceTexture::new): Optional.empty(),
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
