package cc.thonly.reverie_dreams.gui;

import cc.thonly.mystias_izakaya.block.MIBlocks;
import cc.thonly.mystias_izakaya.recipe.entry.KitchenRecipe;
import cc.thonly.mystias_izakaya.recipe.type.KitchenRecipeType;
import cc.thonly.reverie_dreams.Touhou;
import cc.thonly.reverie_dreams.block.ModBlocks;
import cc.thonly.reverie_dreams.gui.recipe.GuiOpeningPrevCallback;
import cc.thonly.reverie_dreams.gui.recipe.RecipeTypeGetter;
import cc.thonly.reverie_dreams.gui.recipe.RecipeTypeGuiInfo;
import cc.thonly.reverie_dreams.gui.recipe.display.*;
import cc.thonly.reverie_dreams.interfaces.IGuiElementBuilderAccessor;
import cc.thonly.reverie_dreams.item.ModItems;
import cc.thonly.reverie_dreams.recipe.BaseRecipe;
import cc.thonly.reverie_dreams.recipe.BaseRecipeType;
import cc.thonly.reverie_dreams.recipe.entry.DanmakuRecipe;
import cc.thonly.reverie_dreams.recipe.entry.DanmakuShapeDrawRecipe;
import cc.thonly.reverie_dreams.recipe.entry.GensokyoAltarRecipe;
import cc.thonly.reverie_dreams.recipe.entry.StrengthTableRecipe;
import cc.thonly.reverie_dreams.recipe.type.DanmakuRecipeType;
import cc.thonly.reverie_dreams.recipe.type.DanmakuShapeDrawRecipeType;
import cc.thonly.reverie_dreams.recipe.type.GensokyoAltarRecipeType;
import cc.thonly.reverie_dreams.recipe.type.StrengthTableRecipeType;
import cc.thonly.reverie_dreams.recipe.view.RecipeEntryWrapper;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Slf4j
@SuppressWarnings("unchecked")
public class RecipeTypeCategoryManager {
    public static final Map<ResourceLocation, RecipeTypeGuiInfo<? extends BasePageGui>> REGISTRIES = new Object2ObjectOpenHashMap<>();
    public static final List<RecipeTypeGuiInfo<? extends BasePageGui>> CATEGORY_ENTRIES = new LinkedList<>();

    public static final ResourceLocation DANMAKU_TABLE_ICON = Touhou.id("recipe/danmaku_table");
    public static final ResourceLocation DANMAKU_SHAPE_ICON = Touhou.id("recipe/danmaku_shape");
    public static final ResourceLocation GENSOKYO_ALTAR_ICON = Touhou.id("recipe/gensokyo_altar");
    public static final ResourceLocation STRENGTH_TABLE_ICON = Touhou.id("recipe/strength_table");
    public static final ResourceLocation KITCHEN_ICON = Touhou.id("recipe/kitchen");

    public static void addCategoryType(RecipeTypeGuiInfo<? extends BasePageGui> type) {
        CATEGORY_ENTRIES.add(type);
        REGISTRIES.put(type.getId(), type);
    }

    public static void open(ResourceLocation categoryRecipeTypeId, ServerPlayer player, GuiOpeningPrevCallback prevGuiCallback) {
        player.closeContainer();
        RecipeTypeGuiInfo<BasePageGui> category = getCategory(categoryRecipeTypeId);
        if (category != null) {
            category.create(player, prevGuiCallback);
        }
    }

    public static void open(ResourceLocation categoryRecipeTypeId, ResourceLocation recipeId, ServerPlayer player, GuiOpeningPrevCallback prevGuiCallback) {
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
    public static <T extends BasePageGui> RecipeTypeGuiInfo<T> getCategory(ResourceLocation categoryRecipeTypeId) {
        return (RecipeTypeGuiInfo<T>) REGISTRIES.get(categoryRecipeTypeId);
    }

    @SuppressWarnings("unchecked")
    public static void registerCategories() {
        addCategoryType(new RecipeTypeGuiInfo<>(new ItemStack(ModItems.POWER), DANMAKU_TABLE_ICON, BasePageGui.class,
                DanmakuTableDisplayView.class,
                DanmakuRecipeType::getInstance,
                ((gui, slotIndex) -> {
                    RecipeEntryWrapper<DanmakuRecipe> key2ValueEntry = (RecipeEntryWrapper<DanmakuRecipe>) gui.getEntries().get(slotIndex + gui.getPage() * BasePageGui.PER_PAGE_SIZE);
                    GuiElementBuilder icon = new GuiElementBuilder()
                            .setItem(key2ValueEntry.getValue().getOutput().getItem())
                            .setItemName(key2ValueEntry.getValue().getOutput().getItemStack().getName())
                            .setCallback((slot, click, action) -> {
                                gui.close();
                                gui.getPlayer().playSoundToPlayer(SoundEvents.UI_BUTTON_CLICK.value(), SoundCategory.PLAYERS, 1.0f, 1.0f);
                                SimpleGui view = new DanmakuTableDisplayView(gui.getPlayer(), key2ValueEntry, () -> new BasePageGui(gui.getPlayer(), gui.getRecipeGuiInfo(), gui.getRecipeTypeInfo(), gui.getPrevGuiCallback()));
                                view.open();
                            });
                    IGuiElementBuilderAccessor accessor = (IGuiElementBuilderAccessor) icon;
                    accessor.setItemStack(key2ValueEntry.getValue().getOutput().getItemStack());
                    gui.setSlot(gui.getGridSlot(slotIndex), icon);
                })
        ));
        addCategoryType(new RecipeTypeGuiInfo<>(new ItemStack(ModItems.DANMAKU_SHAPE_CREATOR), DANMAKU_SHAPE_ICON, BasePageGui.class,
                DanmakuShapeDisplayView.class,
                DanmakuShapeDrawRecipeType::getInstance,
                ((gui, slotIndex) -> {
                    RecipeEntryWrapper<DanmakuShapeDrawRecipe> key2ValueEntry = (RecipeEntryWrapper<DanmakuShapeDrawRecipe>) gui.getEntries().get(slotIndex + gui.getPage() * BasePageGui.PER_PAGE_SIZE);
                    GuiElementBuilder icon = new GuiElementBuilder()
                            .setItem(key2ValueEntry.getValue().getOutput().getItem())
                            .setItemName(key2ValueEntry.getValue().getOutput().getItemStack().getName())
                            .setCallback((slot, click, action) -> {
                                gui.close();
                                gui.getPlayer().playSoundToPlayer(SoundEvents.UI_BUTTON_CLICK.value(), SoundCategory.PLAYERS, 1.0f, 1.0f);
                                SimpleGui view = new DanmakuShapeDisplayView(gui.getPlayer(), key2ValueEntry, () -> new BasePageGui(gui.getPlayer(), gui.getRecipeGuiInfo(), gui.getRecipeTypeInfo(), gui.getPrevGuiCallback()));
                                view.open();
                            });
                    IGuiElementBuilderAccessor accessor = (IGuiElementBuilderAccessor) icon;
                    accessor.setItemStack(key2ValueEntry.getValue().getOutput().getItemStack());
                    gui.setSlot(gui.getGridSlot(slotIndex), icon);
                })
        ));
        addCategoryType(new RecipeTypeGuiInfo<>(new ItemStack(ModBlocks.GENSOKYO_ALTAR), GENSOKYO_ALTAR_ICON, BasePageGui.class,
                GensokyoAltarDisplayView.class,
                GensokyoAltarRecipeType::getInstance,
                ((gui, slotIndex) -> {
                    RecipeEntryWrapper<GensokyoAltarRecipe> key2ValueEntry = (RecipeEntryWrapper<GensokyoAltarRecipe>) gui.getEntries().get(slotIndex + gui.getPage() * BasePageGui.PER_PAGE_SIZE);
                    GuiElementBuilder icon = new GuiElementBuilder()
                            .setItem(key2ValueEntry.getValue().getOutput().getItem())
                            .setItemName(key2ValueEntry.getValue().getOutput().getItemStack().getName())
                            .setCallback((slot, click, action) -> {
                                gui.close();
                                gui.getPlayer().playSoundToPlayer(SoundEvents.UI_BUTTON_CLICK.value(), SoundCategory.PLAYERS, 1.0f, 1.0f);
                                SimpleGui view = new GensokyoAltarDisplayView(gui.getPlayer(), key2ValueEntry, () -> new BasePageGui(gui.getPlayer(), gui.getRecipeGuiInfo(), gui.getRecipeTypeInfo(), gui.getPrevGuiCallback()));
                                view.open();
                            });
                    IGuiElementBuilderAccessor accessor = (IGuiElementBuilderAccessor) icon;
                    accessor.setItemStack(key2ValueEntry.getValue().getOutput().getItemStack());
                    gui.setSlot(gui.getGridSlot(slotIndex), icon);
                })
        ));
        addCategoryType(new RecipeTypeGuiInfo<>(new ItemStack(ModBlocks.STRENGTH_TABLE), STRENGTH_TABLE_ICON, BasePageGui.class,
                StrengthTableDisplayView.class,
                StrengthTableRecipeType::getInstance,
                ((gui, slotIndex) -> {
                    RecipeEntryWrapper<StrengthTableRecipe> key2ValueEntry = (RecipeEntryWrapper<StrengthTableRecipe>) gui.getEntries().get(slotIndex + gui.getPage() * BasePageGui.PER_PAGE_SIZE);
                    GuiElementBuilder icon = new GuiElementBuilder()
                            .setItem(key2ValueEntry.getValue().getOutput().getItem())
                            .setItemName(key2ValueEntry.getValue().getOutput().getItemStack().getName())
                            .setCallback((slot, click, action) -> {
                                gui.close();
                                gui.getPlayer().playSoundToPlayer(SoundEvents.UI_BUTTON_CLICK.value(), SoundCategory.PLAYERS, 1.0f, 1.0f);
                                SimpleGui view = new StrengthTableDisplayView(gui.getPlayer(), key2ValueEntry, () -> new BasePageGui(gui.getPlayer(), gui.getRecipeGuiInfo(), gui.getRecipeTypeInfo(), gui.getPrevGuiCallback()));
                                view.open();
                            });
                    IGuiElementBuilderAccessor accessor = (IGuiElementBuilderAccessor) icon;
                    accessor.setItemStack(key2ValueEntry.getValue().getOutput().getItemStack());
                    gui.setSlot(gui.getGridSlot(slotIndex), icon);
                })
        ));
        addCategoryType(new RecipeTypeGuiInfo<>(new ItemStack(MIBlocks.COOKING_POT), KITCHEN_ICON, BasePageGui.class,
                KitchenBlockDisplayView.class,
                KitchenRecipeType::getInstance,
                ((gui, slotIndex) -> {
                    RecipeEntryWrapper<KitchenRecipe> key2ValueEntry = (RecipeEntryWrapper<KitchenRecipe>) gui.getEntries().get(slotIndex + gui.getPage() * BasePageGui.PER_PAGE_SIZE);
                    GuiElementBuilder icon = new GuiElementBuilder()
                            .setItem(key2ValueEntry.getValue().getOutput().getItem())
                            .setItemName(key2ValueEntry.getValue().getOutput().getItemStack().getName())
                            .setCallback((slot, click, action) -> {
                                gui.close();
                                gui.getPlayer().playSoundToPlayer(SoundEvents.UI_BUTTON_CLICK.value(), SoundCategory.PLAYERS, 1.0f, 1.0f);
                                SimpleGui view = new KitchenBlockDisplayView(gui.getPlayer(), key2ValueEntry, () -> new BasePageGui(gui.getPlayer(), gui.getRecipeGuiInfo(), gui.getRecipeTypeInfo(), gui.getPrevGuiCallback()));
                                view.open();
                            });
                    IGuiElementBuilderAccessor accessor = (IGuiElementBuilderAccessor) icon;
                    accessor.setItemStack(key2ValueEntry.getValue().getOutput().getItemStack());
                    gui.setSlot(gui.getGridSlot(slotIndex), icon);
                })
        ));
    }
}
