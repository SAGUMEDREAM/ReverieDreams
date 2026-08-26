package cc.thonly.reverie_dreams.gui.item;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.block.cooking.AbstractKitchenwareBlock;
import cc.thonly.reverie_dreams.item.IngredientStack;
import cc.thonly.reverie_dreams.recipe.RecipeManager;
import cc.thonly.reverie_dreams.recipe.entry.KitchenRecipe;
import cc.thonly.reverie_dreams.registry.content.block.RDKitchenBlocks;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponentTypes;
import cc.thonly.reverie_dreams.registry.content.item.RDGuiPlaceholderItems;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import cc.thonly.reverie_dreams.registry.delegate.BlockDelegate;
import cc.thonly.reverie_dreams.util.sound.SoundEventPlayUtils;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

import java.util.*;

public class FastRecipeBookGui extends SimpleGui {
    public static final String[][] GRID = {
            {"X", "X", "X", "X", "X", "X", "X", "X", "X"},
            {"X", "X", "X", "X", "X", "X", "X", "X", "X"},
            {"X", "X", "X", "X", "X", "X", "X", "X", "X"},
            {"X", "X", "X", "X", "X", "X", "X", "X", "X"},
            {"X", "X", "X", "X", "X", "X", "X", "X", "X"},
            {"P", "W", "A", "B", "C", "D", "E", "W", "N"},
    };
    private final ItemStack itemStack;
    private final GuiElementBuilder next = new GuiElementBuilder(RDGuiPlaceholderItems.NEXT.value()).setItemName(Component.nullToEmpty("Next Page")).setCallback(this::next);
    private final GuiElementBuilder prev = new GuiElementBuilder(RDGuiPlaceholderItems.PREV.value()).setItemName(Component.nullToEmpty("Prev Page")).setCallback(this::prev);
    private final List<KitchenRecipe> data = new ArrayList<>();
    private int page = 0;
    private int maxPage = -1;
    private ClickType updateClickType = ClickType.MOUSE_LEFT;
    private boolean updateNext = false;
    private BlockDelegate selectWorkType = null;

    public FastRecipeBookGui(ServerPlayer player, ItemStack itemStack) {
        super(MenuType.GENERIC_9x6, player, false);
        this.itemStack = itemStack;
        this.init();
    }

    public MutableComponent updateTitle() {
        KitchenRecipe.IdEntry idEntry = this.itemStack.get(RDDataComponentTypes.RECIPE_MEMORY.value());
        MutableComponent titleComponent = Component.empty()
                .append(Component.translatable("space.-8"))
                .append(Component.literal("\ub006")
                        .withStyle(Style.EMPTY.withColor(ChatFormatting.WHITE)
                                .withFont(new FontDescription.Resource(ReverieDreams.id("reverie_dreams")))))
                .append(Component.translatable("space.-168"))
                .append(RDItems.FAST_RECIPE_BOOK.createStack().getItemName());
        if (idEntry != null && !idEntry.isEmpty()) {
            KitchenRecipe kitchenRecipe = idEntry.recipeOrThrow();
            titleComponent.append("：")
                    .append(kitchenRecipe.getOutput().buildTemplate().create().getHoverName());
        }
        return titleComponent;
    }

    public void init() {
        this.setTitle(this.updateTitle());
        Map<Identifier, KitchenRecipe> registryView = RecipeManager.KITCHEN_TYPE.getRegistryView();
        this.data.addAll(registryView.values());

        int pageSize = 5 * 9;
        this.maxPage = Math.max(0, (this.data.size() - 1) / pageSize);
        for (int row = 0; row < GRID.length; row++) {
            for (int col = 0; col < GRID[row].length; col++) {
                String c = GRID[row][col];
                int slot = row * 9 + col;
                if (c.equalsIgnoreCase("N")) {
                    this.setSlot(slot, this.next);
                }
                if (c.equalsIgnoreCase("P")) {
                    this.setSlot(slot, this.prev);
                }
                if (c.equalsIgnoreCase("W")) {
                    this.setSlot(slot, new GuiElementBuilder().setItem(Items.AIR));
                }
                if (c.equalsIgnoreCase("A")) {
                    this.setSlot(slot, new GuiElementBuilder().setItem(RDKitchenBlocks.COOKING_POT.asItem()).setCallback((clickType) -> {
                        this.selectWorkType = RDKitchenBlocks.COOKING_POT;
                        this.updateClickType = clickType;
                        this.updateNext = true;
                        SoundEventPlayUtils.playUISound(this.player, 1.0f, 1.0f);
                    }));
                }
                if (c.equalsIgnoreCase("B")) {
                    this.setSlot(slot, new GuiElementBuilder().setItem(RDKitchenBlocks.CUTTING_BOARD.asItem()).setCallback((clickType) -> {
                        this.selectWorkType = RDKitchenBlocks.CUTTING_BOARD;
                        this.updateClickType = clickType;
                        this.updateNext = true;
                        SoundEventPlayUtils.playUISound(this.player, 1.0f, 1.0f);
                    }));
                }
                if (c.equalsIgnoreCase("C")) {
                    this.setSlot(slot, new GuiElementBuilder().setItem(RDKitchenBlocks.FRYING_PAN.asItem()).setCallback((clickType) -> {
                        this.selectWorkType = RDKitchenBlocks.FRYING_PAN;
                        this.updateClickType = clickType;
                        this.updateNext = true;
                        SoundEventPlayUtils.playUISound(this.player, 1.0f, 1.0f);
                    }));
                }
                if (c.equalsIgnoreCase("D")) {
                    this.setSlot(slot, new GuiElementBuilder().setItem(RDKitchenBlocks.GRILL.asItem()).setCallback((clickType) -> {
                        this.selectWorkType = RDKitchenBlocks.GRILL;
                        this.updateClickType = clickType;
                        this.updateNext = true;
                        SoundEventPlayUtils.playUISound(this.player, 1.0f, 1.0f);
                    }));
                }
                if (c.equalsIgnoreCase("E")) {
                    this.setSlot(slot, new GuiElementBuilder().setItem(RDKitchenBlocks.STEAMER.asItem()).setCallback((clickType) -> {
                        this.selectWorkType = RDKitchenBlocks.STEAMER;
                        this.updateClickType = clickType;
                        this.updateNext = true;
                        SoundEventPlayUtils.playUISound(this.player, 1.0f, 1.0f);
                    }));
                }
            }
        }
        this.updateNext = true;
    }

    public List<GuiElementBuilder> getPageContents(int page, BlockDelegate blockType) {
        Block block = blockType.asBlock();
        if (!(block instanceof AbstractKitchenwareBlock kitchenwareBlock)) {
            return List.of();
        }
        List<GuiElementBuilder> list = new ArrayList<>();

        int pageSize = 5 * 9;
        int start = page * pageSize;
        List<KitchenRecipe> data = List.copyOf(this.data).stream()
                .filter(recipe -> Objects.equals(
                        recipe.getTypeInstance().defaultBlock(),
                        kitchenwareBlock
                ))
                .sorted(Comparator.comparing(recipe ->
                        BuiltInRegistries.ITEM.getKey(recipe.getOutput().getItem()).toString()
                ))
                .toList();
        if (this.updateClickType.isRight) {
            data = data.stream()
                    .sorted(Comparator.comparing(
                            (KitchenRecipe recipe) ->
                                    BuiltInRegistries.ITEM.getKey(recipe.getOutput().getItem()).toString()
                    ).reversed())
                    .toList();
        }
        int end = Math.min(start + pageSize, data.size());
        this.maxPage = this.computeMaxPage(data);
        this.page = Math.min(this.page, this.maxPage);

        for (int i = start; i < end; i++) {
            KitchenRecipe recipe = data.get(i);
            KitchenRecipe.IdEntry recipeIdEntry = new KitchenRecipe.IdEntry(recipe);
            GuiElementBuilder builder = new GuiElementBuilder(recipe.getOutput().build());
            MutableComponent component = Component.empty();
            component.append(" ");
            component.append(Component.translatable(recipe.getTypeInstance().toTranslateKey()));
            component.append("| ");
            for (IngredientStack ingredient : recipe.getIngredients()) {
                component.append(ingredient.getLazyStack().getHoverName()).append(" ");
            }
            builder.setLore(List.of(component));
            builder.setCallback(() -> {
                SoundEventPlayUtils.playUISound(this.player, 1.0f, 1.0f);
                this.itemStack.set(RDDataComponentTypes.RECIPE_MEMORY.value(), recipeIdEntry);
                this.updateNext = true;
            });
            if (Objects.equals(this.itemStack.get(RDDataComponentTypes.RECIPE_MEMORY.value()), recipeIdEntry)) {
                builder.glow(true);
            }

            list.add(builder);
        }

        return list;
    }

    public List<GuiElementBuilder> getPageContents(int page) {
        List<GuiElementBuilder> list = new ArrayList<>();
        List<KitchenRecipe> data = this.data.stream().sorted(Comparator.comparing(recipe ->
                BuiltInRegistries.ITEM.getKey(recipe.getOutput().getItem()).toString()
        )).toList();
        int pageSize = 5 * 9;
        int start = page * pageSize;
        int end = Math.min(start + pageSize, data.size());

        for (int i = start; i < end; i++) {
            KitchenRecipe recipe = data.get(i);
            KitchenRecipe.IdEntry recipeIdEntry = new KitchenRecipe.IdEntry(recipe);
            GuiElementBuilder builder = new GuiElementBuilder(recipe.getOutput().build());
            MutableComponent component = Component.empty();
            component.append(" ");
            component.append(Component.translatable(recipe.getTypeInstance().toTranslateKey()));
            component.append("| ");
            for (IngredientStack ingredient : recipe.getIngredients()) {
                component.append(ingredient.getLazyStack().getHoverName()).append(" ");
            }
            builder.setLore(List.of(component));
            builder.setCallback(() -> {
                SoundEventPlayUtils.playUISound(this.player, 1.0f, 1.0f);
                this.itemStack.set(RDDataComponentTypes.RECIPE_MEMORY.value(), recipeIdEntry);
                this.updateNext = true;
            });
            if (Objects.equals(this.itemStack.get(RDDataComponentTypes.RECIPE_MEMORY.value()), recipeIdEntry)) {
                builder.glow(true);
            }

            list.add(builder);
        }
        this.maxPage = this.computeMaxPage(data);
        this.page = Math.min(this.page, this.maxPage);

        return list;
    }

    @Override
    public void onTick() {
        super.onTick();
        if (!this.updateNext) {
            return;
        }
        List<GuiElementBuilder> contents = new ArrayList<>();
        boolean typeNull = this.selectWorkType == null;
        boolean shift = this.updateClickType.shift;
        if (typeNull) {
            contents = this.getPageContents(this.page);
        } else if (shift) {
            contents = this.getPageContents(this.page);
        } else {
            contents = this.getPageContents(this.page, this.selectWorkType);
        }

        int index = 0;

        for (int row = 0; row < GRID.length; row++) {
            for (int col = 0; col < GRID[row].length; col++) {
                String c = GRID[row][col];
                int slot = row * 9 + col;

                if (c.equalsIgnoreCase("X")) {
                    if (index < contents.size()) {
                        this.setSlot(slot, contents.get(index));
                        index++;
                    } else {
                        this.clearSlot(slot);
                    }
                }
            }
        }
        this.setTitle(this.updateTitle());
        this.updateNext = false;
    }

    private int computeMaxPage(List<KitchenRecipe> data) {
        int pageSize = 5 * 9;
        return Math.max(0, (data.size() - 1) / pageSize);
    }

    private void next() {
        SoundEventPlayUtils.playUISound(this.player, 1.0f, 1.0f);
        if (this.page < this.maxPage) {
            this.page++;
            this.updateNext = true;
        }
    }

    private void prev() {
        SoundEventPlayUtils.playUISound(this.player, 1.0f, 1.0f);
        if (this.page > 0) {
            this.page--;
            this.updateNext = true;
        }
    }
}
