package cc.thonly.reverie_dreams.gui;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.gui.recipe.GuiOpeningPrevCallback;
import cc.thonly.reverie_dreams.gui.recipe.RecipeTypeGetter;
import cc.thonly.reverie_dreams.gui.recipe.RecipeTypeGuiInfo;
import cc.thonly.reverie_dreams.gui.recipe.display.*;
import cc.thonly.reverie_dreams.recipe.BaseRecipe;
import cc.thonly.reverie_dreams.recipe.BaseRecipeType;
import cc.thonly.reverie_dreams.recipe.entry.*;
import cc.thonly.reverie_dreams.recipe.type.*;
import cc.thonly.reverie_dreams.recipe.view.RecipeEntryWrapper;
import cc.thonly.reverie_dreams.registry.content.block.KitchenBlocks;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import cc.thonly.reverie_dreams.util.item.GuiElementBuilderSetter;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStackTemplate;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Slf4j
@SuppressWarnings("unchecked")
public class RecipeTypeCategoryManager {
    public static final Map<Identifier, RecipeTypeGuiInfo<? extends BasePageGui>> REGISTRIES = new Object2ObjectOpenHashMap<>();
    public static final List<RecipeTypeGuiInfo<? extends BasePageGui>> CATEGORY_ENTRIES = new LinkedList<>();

    public static final Identifier DANMAKU_TABLE_ICON = ReverieDreams.id("recipe/danmaku_table");
    public static final Identifier DANMAKU_SHAPE_ICON = ReverieDreams.id("recipe/danmaku_shape");
    public static final Identifier GENSOKYO_ALTAR_ICON = ReverieDreams.id("recipe/gensokyo_altar");
    public static final Identifier STRENGTH_TABLE_ICON = ReverieDreams.id("recipe/strength_table");
    public static final Identifier KITCHEN_ICON = ReverieDreams.id("recipe/kitchen");

    public static void addCategoryType(RecipeTypeGuiInfo<? extends BasePageGui> type) {
        CATEGORY_ENTRIES.add(type);
        REGISTRIES.put(type.getId(), type);
    }

    public static void open(Identifier categoryRecipeTypeId, ServerPlayer player, GuiOpeningPrevCallback prevGuiCallback) {
        player.closeContainer();
        RecipeTypeGuiInfo<BasePageGui> category = getCategory(categoryRecipeTypeId);
        if (category != null) {
            category.create(player, prevGuiCallback);
        }
    }

    public static void open(Identifier categoryRecipeTypeId, Identifier recipeId, ServerPlayer player, GuiOpeningPrevCallback prevGuiCallback) {
        player.closeContainer();
        RecipeTypeGuiInfo<BasePageGui> category = getCategory(categoryRecipeTypeId);
        if (category != null) {
            RecipeTypeGetter registryGetter = category.getRegistryGetter();
            BaseRecipeType<?> baseRecipeType = registryGetter.get();
            BaseRecipe recipe = baseRecipeType.getRecipeById(recipeId);
            if (recipe == null) {
                return;
            }
            RecipeEntryWrapper<?> key2ValueEntry = new RecipeEntryWrapper<>(recipe.getId(), recipe);
            Class<? extends DisplayView> viewClazz = category.getViewClazz();
            try {
                SimpleGui recipeView = DisplayView.create((Class<? extends SimpleGui>) viewClazz, player, key2ValueEntry, prevGuiCallback);
                if (recipeView != null) {
                    recipeView.open();
                }
            } catch (Exception e) {
                log.error("Can't create view type instance", e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends BasePageGui> RecipeTypeGuiInfo<T> getCategory(Identifier categoryRecipeTypeId) {
        return (RecipeTypeGuiInfo<T>) REGISTRIES.get(categoryRecipeTypeId);
    }

    @SuppressWarnings("unchecked")
    public static void registerCategories() {
        ReverieDreams.COMMON_LATE_INIT.add(() -> {
            addCategoryType(new RecipeTypeGuiInfo<>(new ItemStackTemplate(RDItems.POWER.asItem()), DANMAKU_TABLE_ICON, BasePageGui.class,
                    DanmakuTableDisplayView.class,
                    DanmakuRecipeType::getInstance,
                    ((gui, slotIndex) -> {
                        RecipeEntryWrapper<DanmakuRecipe> key2ValueEntry = (RecipeEntryWrapper<DanmakuRecipe>) gui.getEntries().get(slotIndex + gui.getPage() * BasePageGui.PER_PAGE_SIZE);
                        GuiElementBuilder icon = new GuiElementBuilder()
                                .setItem(key2ValueEntry.getValue().getOutput().getItem())
                                .setItemName(key2ValueEntry.getValue().getOutput().build().getHoverName())
                                .setCallback((slot, click, action, basedGui) -> {
                                    gui.close();
                                    gui.getPlayer().playSound(SoundEvents.UI_BUTTON_CLICK.value(), 1.0f, 1.0f);
                                    SimpleGui view = new DanmakuTableDisplayView(gui.getPlayer(), key2ValueEntry, () -> new BasePageGui(gui.getPlayer(), gui.getRecipeGuiInfo(), gui.getRecipeTypeInfo(), gui.getPrevGuiCallback()));
                                    view.open();
                                });
                        GuiElementBuilderSetter.setter(icon, key2ValueEntry.getValue().getOutput().build());
                        gui.setSlot(gui.getGridSlot(slotIndex), icon);
                    })
            ));
            addCategoryType(new RecipeTypeGuiInfo<>(new ItemStackTemplate(RDItems.DANMAKU_SHAPE_CREATOR.asItem()), DANMAKU_SHAPE_ICON, BasePageGui.class,
                    DanmakuShapeDisplayView.class,
                    DanmakuShapeDrawRecipeType::getInstance,
                    ((gui, slotIndex) -> {
                        RecipeEntryWrapper<DanmakuShapeDrawRecipe> key2ValueEntry = (RecipeEntryWrapper<DanmakuShapeDrawRecipe>) gui.getEntries().get(slotIndex + gui.getPage() * BasePageGui.PER_PAGE_SIZE);
                        GuiElementBuilder icon = new GuiElementBuilder()
                                .setItem(key2ValueEntry.getValue().getOutput().getItem())
                                .setItemName(key2ValueEntry.getValue().getOutput().build().getHoverName())
                                .setCallback((slot, click, action, basedGui) -> {
                                    gui.close();
                                    gui.getPlayer().playSound(SoundEvents.UI_BUTTON_CLICK.value(), 1.0f, 1.0f);
                                    SimpleGui view = new DanmakuShapeDisplayView(gui.getPlayer(), key2ValueEntry, () -> new BasePageGui(gui.getPlayer(), gui.getRecipeGuiInfo(), gui.getRecipeTypeInfo(), gui.getPrevGuiCallback()));
                                    view.open();
                                });
                        GuiElementBuilderSetter.setter(icon, key2ValueEntry.getValue().getOutput().build());
                        gui.setSlot(gui.getGridSlot(slotIndex), icon);
                    })
            ));
            addCategoryType(new RecipeTypeGuiInfo<>(new ItemStackTemplate(RDBlocks.GENSOKYO_ALTAR.asItem()), GENSOKYO_ALTAR_ICON, BasePageGui.class,
                    GensokyoAltarDisplayView.class,
                    GensokyoAltarRecipeType::getInstance,
                    ((gui, slotIndex) -> {
                        RecipeEntryWrapper<GensokyoAltarRecipe> key2ValueEntry = (RecipeEntryWrapper<GensokyoAltarRecipe>) gui.getEntries().get(slotIndex + gui.getPage() * BasePageGui.PER_PAGE_SIZE);
                        GuiElementBuilder icon = new GuiElementBuilder()
                                .setItem(key2ValueEntry.getValue().getOutput().getItem())
                                .setItemName(key2ValueEntry.getValue().getOutput().build().getHoverName())
                                .setCallback((slot, click, action, basedGui) -> {
                                    gui.close();
                                    gui.getPlayer().playSound(SoundEvents.UI_BUTTON_CLICK.value(), 1.0f, 1.0f);
                                    SimpleGui view = new GensokyoAltarDisplayView(gui.getPlayer(), key2ValueEntry, () -> new BasePageGui(gui.getPlayer(), gui.getRecipeGuiInfo(), gui.getRecipeTypeInfo(), gui.getPrevGuiCallback()));
                                    view.open();
                                });
                        GuiElementBuilderSetter.setter(icon, key2ValueEntry.getValue().getOutput().build());
                        gui.setSlot(gui.getGridSlot(slotIndex), icon);
                    })
            ));
            addCategoryType(new RecipeTypeGuiInfo<>(new ItemStackTemplate(RDBlocks.STRENGTH_TABLE.asItem()), STRENGTH_TABLE_ICON, BasePageGui.class,
                    StrengthTableDisplayView.class,
                    StrengthTableRecipeType::getInstance,
                    ((gui, slotIndex) -> {
                        RecipeEntryWrapper<StrengthTableRecipe> key2ValueEntry = (RecipeEntryWrapper<StrengthTableRecipe>) gui.getEntries().get(slotIndex + gui.getPage() * BasePageGui.PER_PAGE_SIZE);
                        GuiElementBuilder icon = new GuiElementBuilder()
                                .setItem(key2ValueEntry.getValue().getOutput().getItem())
                                .setItemName(key2ValueEntry.getValue().getOutput().build().getHoverName())
                                .setCallback((slot, click, action, basedGui) -> {
                                    gui.close();
                                    gui.getPlayer().playSound(SoundEvents.UI_BUTTON_CLICK.value(), 1.0f, 1.0f);
                                    SimpleGui view = new StrengthTableDisplayView(gui.getPlayer(), key2ValueEntry, () -> new BasePageGui(gui.getPlayer(), gui.getRecipeGuiInfo(), gui.getRecipeTypeInfo(), gui.getPrevGuiCallback()));
                                    view.open();
                                });
                        GuiElementBuilderSetter.setter(icon, key2ValueEntry.getValue().getOutput().build());
                        gui.setSlot(gui.getGridSlot(slotIndex), icon);
                    })
            ));
            addCategoryType(new RecipeTypeGuiInfo<>(new ItemStackTemplate(KitchenBlocks.COOKING_POT.asItem()), KITCHEN_ICON, BasePageGui.class,
                    KitchenBlockDisplayView.class,
                    KitchenRecipeType::getInstance,
                    ((gui, slotIndex) -> {
                        RecipeEntryWrapper<KitchenRecipe> key2ValueEntry = (RecipeEntryWrapper<KitchenRecipe>) gui.getEntries().get(slotIndex + gui.getPage() * BasePageGui.PER_PAGE_SIZE);
                        GuiElementBuilder icon = new GuiElementBuilder()
                                .setItem(key2ValueEntry.getValue().getOutput().getItem())
                                .setItemName(key2ValueEntry.getValue().getOutput().build().getHoverName())
                                .setCallback((slot, click, action, basedGui) -> {
                                    gui.close();
                                    gui.getPlayer().playSound(SoundEvents.UI_BUTTON_CLICK.value(), 1.0f, 1.0f);
                                    SimpleGui view = new KitchenBlockDisplayView(gui.getPlayer(), key2ValueEntry, () -> new BasePageGui(gui.getPlayer(), gui.getRecipeGuiInfo(), gui.getRecipeTypeInfo(), gui.getPrevGuiCallback()));
                                    view.open();
                                });
                        GuiElementBuilderSetter.setter(icon, key2ValueEntry.getValue().getOutput().build());
                        gui.setSlot(gui.getGridSlot(slotIndex), icon);
                    })
            ));
        });
    }
}
