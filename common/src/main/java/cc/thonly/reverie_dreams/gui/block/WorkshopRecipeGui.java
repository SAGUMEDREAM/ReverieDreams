package cc.thonly.reverie_dreams.gui.block;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.block.decor.workshop.WorkshopKind;
import cc.thonly.reverie_dreams.block.entity.WorkshopStationBlockEntity;
import cc.thonly.reverie_dreams.recipe.RecipeManager;
import cc.thonly.reverie_dreams.recipe.entry.WorkshopProcessRecipe;
import cc.thonly.reverie_dreams.registry.content.ItemPropDescriptionConsts;
import cc.thonly.reverie_dreams.registry.content.item.RDGuiPlaceholderItems;
import cc.thonly.reverie_dreams.util.sound.SoundEventPlayUtils;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.AnimatedGuiElement;
import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Comparator;
import java.util.List;

/**
 * Recipe catalogue slots are ghost buttons, never an inventory backed by outputs.
 */
public final class WorkshopRecipeGui extends SimpleGui {
    private static final int MILL_RECIPES_PER_PAGE = 5;
    private static final int GRID_RECIPES_PER_PAGE = 45;
    private static final int DRYING_RECIPES_PER_PAGE = 2;
    private static final int DRYING_FOOTER = DRYING_RECIPES_PER_PAGE * 9;
    private static final int DRYING_STATUS_SLOT = DRYING_FOOTER + 4;
    private final WorkshopStationBlockEntity host;
    private final WorkshopKind kind;
    private int page;

    public WorkshopRecipeGui(ServerPlayer player, WorkshopStationBlockEntity host) {
        super(host.workshopKind() == WorkshopKind.HERB_DRYING_TRAY ? MenuType.GENERIC_9x3 : MenuType.GENERIC_9x6,
                player, false);
        this.host = host;
        this.kind = host.workshopKind();
        setLockPlayerInventory(true);
        if (kind == WorkshopKind.YAGEN)
            setTitle(panelTitle("\uE003", host.getBlockState().getBlock().getName(), 0x392A2B));
        refresh();
    }

    private Component panelTitle(String glyph, Component name, int color) {
        return Component.empty()
                .append(Component.translatable("space.-8"))
                .append(Component.literal(glyph).withStyle(Style.EMPTY.withColor(ChatFormatting.WHITE)
                        .withFont(new FontDescription.Resource(ReverieDreams.id("reverie_dreams")))))
                .append(Component.translatable("space.-168"))
                .append(name.copy().withStyle(style -> style.withColor(color)));
    }

    private boolean valid() {
        return player.isAlive() && player.level() == host.getLevel() && !host.isRemoved() && player.distanceToSqr(host.getBlockPos().getCenter()) <= 64 && player.level().hasChunkAt(host.getBlockPos()) && player.level().getBlockEntity(host.getBlockPos()) == host;
    }

    private Component text(String key, Object... args) {
        return Component.translatable("workshop.reverie_dreams." + key, args);
    }

    private void refresh() {
        if (kind == WorkshopKind.HERB_DRYING_TRAY) {
            refreshDryingCatalogue();
            return;
        }
        boolean stoneMill = kind == WorkshopKind.STONE_MILL;
        int footer = GRID_RECIPES_PER_PAGE;
        for (int slot = 0; slot < footer + 9; slot++)
            clearSlot(slot);

        var type = stoneMill ? RecipeManager.MILLING : RecipeManager.GRINDING;
        var recipes = type.values();
        int pageSize = stoneMill ? MILL_RECIPES_PER_PAGE : GRID_RECIPES_PER_PAGE;
        int totalPages = Math.max(1, Math.ceilDiv(recipes.size(), pageSize));
        page = Math.clamp(page, 0, totalPages - 1);
        int firstRecipe = page * pageSize;
        for (int index = 0; index < pageSize && firstRecipe + index < recipes.size(); index++)
            showRecipe(index, recipes.get(firstRecipe + index));

        if (page > 0)
            setSlot(footer, pageButton(new ItemStack(RDGuiPlaceholderItems.PREV.value()), "previous", -1));
        if (page + 1 < totalPages)
            setSlot(footer + 8, pageButton(new ItemStack(RDGuiPlaceholderItems.NEXT.value()), "next", 1));
        var guide = new GuiElementBuilder(Items.BOOK)
                .setName(host.getBlockState().getBlock().getName())
                .addLoreLine(Component.translatable("item.reverie_dreams.decoration.yagen_insert"))
                .addLoreLine(Component.translatable("item.reverie_dreams.decoration.yagen_work"));
        if (totalPages > 1)
            guide.addLoreLine(Component.translatable("book.pageIndicator", page + 1, totalPages));
        // The stone mill keeps the initial cursor position clear of tooltips.
        setSlot(footer + (stoneMill ? 3 : 4), guide);
        setSlot(footer + (stoneMill ? 5 : 6), new GuiElementBuilder(Items.BARRIER).setName(text("close")).setCallback((a, b, c, d) -> close()));
        if (stoneMill) {
            var heading = host.getBlockState().getBlock().getName().copy()
                    .append("  ·  ")
                    .append(Component.translatable("gui.recipebook.page", page + 1, totalPages).withStyle(style -> style.withColor(0x50686B)));
            var title = panelTitle("\uE002", heading, 0x304353);
            if (!title.equals(getTitle()))
                setTitle(title);
        }
    }

    private void refreshDryingCatalogue() {
        for (int slot = 0; slot < DRYING_FOOTER + 9; slot++)
            clearSlot(slot);
        var recipes = RecipeManager.HERB_DRYING.stream()
                .sorted(Comparator.comparing(recipe -> recipe.getId().toString())).toList();
        int totalPages = Math.max(1, Math.ceilDiv(recipes.size(), DRYING_RECIPES_PER_PAGE));
        page = Math.clamp(page, 0, totalPages - 1);
        for (int row = 0; row < DRYING_RECIPES_PER_PAGE && page * DRYING_RECIPES_PER_PAGE + row < recipes.size(); row++)
            showDryingRecipe(row * 9, recipes.get(page * DRYING_RECIPES_PER_PAGE + row));
        if (recipes.isEmpty())
            setSlot(5, new GuiElementBuilder(Items.PAPER).setName(dryingText("no_recipes", "No drying recipes")));
        if (page > 0)
            setSlot(DRYING_FOOTER, pageButton(RDGuiPlaceholderItems.PREV.createStack(), "previous", -1));
        if (page + 1 < totalPages)
            setSlot(DRYING_FOOTER + 8, pageButton(RDGuiPlaceholderItems.NEXT.createStack(), "next", 1));
        setSlot(DRYING_FOOTER + 3, new GuiElementBuilder(Items.BOOK)
                .setName(dryingText("guide", "Using the drying tray"))
                .addLoreLine(Component.translatable(ItemPropDescriptionConsts.DECOR_DRYING))
                .addLoreLine(text("dry_conditions"))
                .addLoreLine(Component.translatable(ItemPropDescriptionConsts.DECOR_DRYING_VIEW))
                .addLoreLine(Component.translatable(ItemPropDescriptionConsts.DECOR_DRYING_TAKE)));
        setSlot(DRYING_FOOTER + 6, new GuiElementBuilder(Items.BARRIER).setName(text("close"))
                .setCallback((a, b, c, d) -> close()));
        var name = host.getBlockState().getBlock().getName().copy();
        if (totalPages > 1)
            name.append("  ·  ").append(Component.translatable("gui.recipebook.page", page + 1, totalPages));
        var title = panelTitle("\uE00A", name, 0x49351F);
        if (!title.equals(getTitle()))
            setTitle(title);
        updateDryingStatus();
    }

    private void showDryingRecipe(int base, WorkshopProcessRecipe recipe) {
        var choices = BuiltInRegistries.ITEM.stream().map(item -> item.getDefaultInstance())
                .filter(recipe::matches).toList();
        var time = text("dry_work", Math.ceilDiv(recipe.work(), 20));
        var selection = dryingText("choose_one", "Use any one of these ingredients");
        for (int index = 0; index < Math.min(4, choices.size()); index++) {
            if (index == 3 && choices.size() > 4) {
                var frames = choices.subList(index, choices.size()).stream()
                        .map(stack -> new GuiElementBuilder(stack).addLoreLine(selection).asStack()).toArray(ItemStack[]::new);
                setSlot(base + index + 1, new AnimatedGuiElement(frames, 30, false, GuiElement.EMPTY_CALLBACK));
            } else {
                var ingredient = new GuiElementBuilder(choices.get(index));
                if (choices.size() > 1)
                    ingredient.addLoreLine(selection);
                setSlot(base + index + 1, ingredient);
            }
        }
        var drying = new GuiElementBuilder(Items.SUNFLOWER).setName(time).addLoreLine(text("dry_conditions"));
        if (choices.size() > 1)
            drying.addLoreLine(selection);
        if (choices.isEmpty())
            drying.addLoreLine(dryingText("no_ingredient", "No available ingredient for %s", recipe.ingredient()));
        setSlot(base + 5, drying);
        setSlot(base + 6, new GuiElementBuilder(RDGuiPlaceholderItems.NEXT.createStack()).setName(time));
        setSlot(base + 7, new GuiElementBuilder(recipe.getOutput().build()).addLoreLine(time));
    }

    private void updateDryingStatus() {
        if (!(host.getLevel() instanceof ServerLevel level))
            return;
        var conditions = WorkshopStationBlockEntity.dryingConditions(level, host.getBlockPos());
        int capacity = WorkshopStationBlockEntity.DRYING_CAPACITY;
        int occupied = 0;
        boolean processing = false;
        var status = new GuiElementBuilder(switch (conditions) {
            case SUNNY -> Items.SUNFLOWER;
            case RAIN -> Items.WATER_BUCKET;
            case COVERED -> Items.OAK_SLAB;
            case LOW_LIGHT -> Items.BLACK_DYE;
        });
        for (int slot = 0; slot < capacity; slot++) {
            var stack = host.getItem(slot);
            if (stack.isEmpty())
                continue;
            occupied++;
            var recipe = host.dryingRecipe(slot).orElse(null);
            if (recipe == null) {
                status.addLoreLine(dryingText("slot_pickup", "%s. %s ×%s · can be taken", slot + 1,
                        stack.getHoverName(), stack.getCount()));
            } else {
                processing = true;
                status.addLoreLine(dryingText("slot_progress", "%s. %s · %s/%s sec", slot + 1,
                        stack.getHoverName(), host.dryingProgress(slot, recipe) / 20, Math.ceilDiv(recipe.work(), 20)));
            }
        }
        var condition = switch (conditions) {
            case SUNNY -> processing ? dryingText("active", "Drying") : dryingText("sunny", "Enough sunlight to dry");
            case RAIN -> dryingText("rain", "Paused · Raining");
            case COVERED -> dryingText("covered", "Paused · Sky obstructed above the tray");
            case LOW_LIGHT -> dryingText("low_light", "Paused · Low daylight");
        };
        var element = status.setName(dryingText("status", "%s · %s/%s slots occupied", condition, occupied, capacity)).build();
        var previous = getGuiElement(DRYING_STATUS_SLOT);
        if (previous == null || !ItemStack.matches(previous.getItemStack(), element.getItemStack()))
            setSlot(DRYING_STATUS_SLOT, element);
    }

    private Component dryingText(String key, String fallback, Object... args) {
        return Component.translatableWithFallback("workshop.reverie_dreams.drying." + key, fallback, args);
    }

    private void showRecipe(int index, WorkshopProcessRecipe recipe) {
        var ingredients = BuiltInRegistries.ITEM.stream().map(item -> item.getDefaultInstance())
                .filter(recipe::matches).limit(5).toList();
        Component work = text("click_work", recipe.work());
        Component ingredient = text("ingredient", ingredientNames(recipe, ingredients));
        var result = new GuiElementBuilder(recipe.getOutput().build()).addLoreLine(ingredient).addLoreLine(work);
        if (kind == WorkshopKind.STONE_MILL) {
            if (!ingredients.isEmpty()) {
                var input = new GuiElementBuilder(ingredients.getFirst()).addLoreLine(ingredient).addLoreLine(work);
                setSlot(index * 9 + 2, input);
            }
            setSlot(index * 9 + 6, result);
        } else {
            setSlot(index, result);
        }
    }

    private Component ingredientNames(WorkshopProcessRecipe recipe, List<ItemStack> ingredients) {
        var names = Component.empty();
        for (int index = 0; index < Math.min(4, ingredients.size()); index++) {
            if (index > 0)
                names.append(" / ");
            names.append(ingredients.get(index).getHoverName());
        }
        if (ingredients.size() > 4)
            names.append(" / … (").append(recipe.ingredient()).append(")");
        else if (ingredients.isEmpty())
            names.append(recipe.ingredient());
        return names;
    }

    private GuiElementBuilder pageButton(ItemStack icon, String key, int offset) {
        return new GuiElementBuilder(icon).setName(text(key)).setCallback((a, b, c, d) -> {
            if (valid()) {
                page += offset;
                refresh();
            }
        });
    }

    @Override
    public boolean onClick(int index, ClickType type, ContainerInput action, GuiElement element) {
        if (action == ContainerInput.PICKUP && element != null && !element.getItemStack().isEmpty() && valid()) {
            SoundEventPlayUtils.playUISound(player, 1.0f, 1.0f);
        }
        return super.onClick(index, type, action, element);
    }

    @Override
    public void onTick() {
        if (!valid())
            close();
        else if (kind == WorkshopKind.HERB_DRYING_TRAY && player.tickCount % 10 == 0)
            updateDryingStatus();
    }
}
