package cc.thonly.reverie_dreams.gui;

import cc.thonly.reverie_dreams.component.ModDataComponentTypes;
import cc.thonly.reverie_dreams.item.ModGuiItems;
import cc.thonly.reverie_dreams.mixin.accessor.GuiElementBuilderAccessor;
import cc.thonly.reverie_dreams.recipe.BaseRecipeType;
import cc.thonly.reverie_dreams.recipe.ItemStackWrapper;
import cc.thonly.reverie_dreams.recipe.RecipeManager;
import cc.thonly.reverie_dreams.recipe.entry.DanmakuShapeDrawRecipe;
import cc.thonly.reverie_dreams.recipe.type.DanmakuShapeDrawRecipeType;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import eu.pb4.sgui.api.gui.SlotGuiInterface;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import lombok.Getter;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Unit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
public class DanmakuShapeEditGui extends SimpleGui implements GuiCommon {
    public static final char[][] grid = {
            {'A', 'X', 'X', 'X', 'X', 'X', 'X', 'A', 'A'},
            {'A', 'X', 'X', 'X', 'X', 'X', 'X', 'A', 'A'},
            {'A', 'X', 'X', 'X', 'X', 'X', 'X', 'A', 'A'},
            {'A', 'X', 'X', 'X', 'X', 'X', 'X', 'A', 'A'},
            {'A', 'X', 'X', 'X', 'X', 'X', 'X', 'A', 'S'},
            {'A', 'X', 'X', 'X', 'X', 'X', 'X', 'A', 'E'},
    };
    private final ItemStack source;
    private final List<List<Boolean>> shape = new ArrayList<>();
    private final Hand hand;
    private final Map<Integer, GuiElementBuilder> INDEX_TO_BUILDER = new Object2ObjectLinkedOpenHashMap<>();

    public DanmakuShapeEditGui(ServerPlayerEntity player, ItemStack source, Hand hand) {
        super(ScreenHandlerType.GENERIC_9X6, player, false);
        this.source = source;
        for (int i = 0; i < 6; i++) {
            ArrayList<Boolean> booleans = new ArrayList<>();
            for (int j = 0; j < 6; j++) {
                booleans.add(false);
            }
            this.shape.add(booleans);
        }
        this.hand = hand;
        this.setTitle(source.getName());
        this.init();
    }

    @Override
    public void init() {
        int counter = 0;
        int counter2 = 0;
        for (int y = 0; y < grid.length; y++) {
            for (int x = 0; x < grid[y].length; x++) {
                char c = grid[y][x];
                if (c == 'A') {
                    this.setSlot(counter, ModGuiItems.EMPTY_SLOT.getDefaultStack());
                }
                if (c == 'X') {
                    final int shapeY = counter2 / 6;
                    final int shapeX = counter2 % 6;

                    int finalCounter = counter2;
                    GuiElementBuilder builder = new GuiElementBuilder(getItemForState(shape.get(shapeY).get(shapeX)))
                            .setCallback(new GuiElementInterface.ClickCallback() {
                                @Override
                                public void click(int i, ClickType clickType, SlotActionType slotActionType, SlotGuiInterface slotGuiInterface) {
                                    boolean current = shape.get(shapeY).get(shapeX);
                                    boolean next = !current;

                                    shape.get(shapeY).set(shapeX, next);

                                    GuiElementBuilder updated = new GuiElementBuilder(getItemForState(next))
                                            .setCallback(this);
                                    setSlot(i, updated);
                                    INDEX_TO_BUILDER.put(finalCounter, updated);
                                    player.playSoundToPlayer(SoundEvents.UI_BUTTON_CLICK.value(), SoundCategory.PLAYERS, 1.0f, 1.0f);
                                }
                            });

                    INDEX_TO_BUILDER.put(counter2, builder);
                    this.setSlot(counter, builder);
                    counter2++;
                }
                if (c == 'S') {
                    this.setSlot(counter, new GuiElementBuilder(ModGuiItems.DONE)
                            .setCallback(new GuiElementInterface.ItemClickCallback() {
                                @Override
                                public void click(int i, ClickType clickType, SlotActionType slotActionType) {
                                    player.playSoundToPlayer(SoundEvents.UI_BUTTON_CLICK.value(), SoundCategory.PLAYERS, 1.0f, 1.0f);
                                    DanmakuShapeEditGui.this.apply();
                                    DanmakuShapeEditGui.this.close();
                                }
                            })
                    );
                }
                if (c == 'E') {
                    this.setSlot(counter, new GuiElementBuilder(ModGuiItems.CLOSE)
                            .setCallback(new GuiElementInterface.ItemClickCallback() {
                                @Override
                                public void click(int i, ClickType clickType, SlotActionType slotActionType) {
                                    player.playSoundToPlayer(SoundEvents.UI_BUTTON_CLICK.value(), SoundCategory.PLAYERS, 1.0f, 1.0f);
                                    DanmakuShapeEditGui.this.close();
                                }
                            }));
                }
                counter++;
            }
        }
        this.readData();
    }

    private void readData() {
        DanmakuShapeDrawRecipeType recipeType = DanmakuShapeDrawRecipeType.getInstance();
        ItemStack source = this.source;
        ItemStackWrapper itemStackWrapper = source.get(ModDataComponentTypes.Danmaku.SHAPE);
        if (itemStackWrapper == null) {
            return;
        }

        List<List<List<Boolean>>> shapesByOutput = recipeType.getShapesByOutput(itemStackWrapper);
        if (shapesByOutput.isEmpty()) return;

        List<List<Boolean>> shapeToShow = shapesByOutput.getFirst();

        int counter2 = 0;
        for (int gy = 0; gy < grid.length; gy++) {
            for (int gx = 0; gx < grid[gy].length; gx++) {
                if (grid[gy][gx] != 'X') {
                    continue;
                }

                int shapeY = counter2 / 6;
                int shapeX = counter2 % 6;

                if (shapeY < shapeToShow.size() && shapeX < shapeToShow.get(shapeY).size()) {
                    boolean state = shapeToShow.get(shapeY).get(shapeX);
                    shape.get(shapeY).set(shapeX, state);

                    // 更新 GUI
                    GuiElementBuilder builder = INDEX_TO_BUILDER.get(counter2);
                    if (builder != null) {
                        GuiElementBuilder updated = new GuiElementBuilder(getItemForState(state))
                                .setCallback(((GuiElementBuilderAccessor) builder).getCallback());
                        // 注意 setSlot 用 GUI 坐标 (gy*9 + gx)，而 INDEX_TO_BUILDER 用 counter2
                        setSlot(gy * 9 + gx, updated);
                        INDEX_TO_BUILDER.put(counter2, updated);
                    }
                }

                counter2++;
            }
        }
    }

    private ItemStack getItemForState(boolean state) {
        if (state) {
            return ModGuiItems.ENABLE.getDefaultStack();
        } else {
            return ModGuiItems.DISABLE.getDefaultStack();
        }
    }

    public void apply() {
        var danmakuShapeDrawType = DanmakuShapeDrawRecipeType.getInstance();
        List<DanmakuShapeDrawRecipe> matches = danmakuShapeDrawType.getMatches(this.getShape(), Unit.INSTANCE);
        if (matches.isEmpty()) {
            this.player.sendMessage(Text.translatable("item.action.click.shape_recipe.fail"), false);
            return;
        }
        DanmakuShapeDrawRecipe first = matches.getFirst();
        ItemStackWrapper output = first.getOutput();
        ItemStack itemStack = output.clone().getItemStack();
        this.player.sendMessage(Text.translatable("item.action.click.shape_recipe.success"), false);
        this.player.playSoundToPlayer(SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.PLAYERS, 1.0f, 1.0f);
        this.player.setStackInHand(this.hand, itemStack);
    }
}
