package cc.thonly.reverie_dreams.gui.item;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.gui.GuiCommon;
import cc.thonly.reverie_dreams.item.IngredientStack;
import cc.thonly.reverie_dreams.mixin.accessor.GuiElementBuilderAccessor;
import cc.thonly.reverie_dreams.recipe.entry.DanmakuShapeDrawRecipe;
import cc.thonly.reverie_dreams.recipe.type.DanmakuShapeDrawRecipeType;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponents;
import cc.thonly.reverie_dreams.registry.content.item.RDGuiItems;
import cc.thonly.reverie_dreams.util.sound.SoundEventPlayUtils;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import eu.pb4.sgui.api.gui.SlotBasedGui;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import lombok.Getter;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

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
    private final InteractionHand hand;
    private final Map<Integer, GuiElementBuilder> INDEX_TO_BUILDER = new Object2ObjectLinkedOpenHashMap<>();

    public DanmakuShapeEditGui(ServerPlayer player, ItemStack source, InteractionHand hand) {
        super(MenuType.GENERIC_9x6, player, false);
        this.source = source;
        for (int i = 0; i < 6; i++) {
            ArrayList<Boolean> booleans = new ArrayList<>();
            for (int j = 0; j < 6; j++) {
                booleans.add(false);
            }
            this.shape.add(booleans);
        }
        this.hand = hand;
        this.setTitle(
                Component.empty()
                        .append(Component.translatable("space.-8"))
                        .append(Component.literal("\ub005")
                                .withStyle(Style.EMPTY.withColor(ChatFormatting.WHITE)
                                        .withFont(new FontDescription.Resource(ReverieDreams.id("reverie_dreams")))))
                        .append(Component.translatable("space.-168"))
                        .append(source.getHoverName())
        );
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
                    this.setSlot(counter, new ItemStack(Items.AIR));
                }
                if (c == 'X') {
                    final int shapeY = counter2 / 6;
                    final int shapeX = counter2 % 6;

                    int finalCounter = counter2;
                    GuiElementBuilder builder = new GuiElementBuilder(getItemForState(shape.get(shapeY).get(shapeX)))
                            .setCallback(new GuiElement.ClickCallback() {
                                @Override
                                public void click(int i, ClickType clickType, ContainerInput input, SlotBasedGui slotGuiInterface) {
                                    boolean current = shape.get(shapeY).get(shapeX);
                                    boolean next = !current;

                                    shape.get(shapeY).set(shapeX, next);

                                    GuiElementBuilder updated = new GuiElementBuilder(getItemForState(next))
                                            .setCallback(this);
                                    setSlot(i, updated);
                                    INDEX_TO_BUILDER.put(finalCounter, updated);
                                    SoundEventPlayUtils.playUISound(player, 1.0f, 1.0f);
                                }
                            });

                    INDEX_TO_BUILDER.put(counter2, builder);
                    this.setSlot(counter, builder);
                    counter2++;
                }
                if (c == 'S') {
                    this.setSlot(counter, new GuiElementBuilder(Items.AIR)
                            .setCallback((i, clickType, input, slotBasedGui) -> {
                                SoundEventPlayUtils.playUISound(this.player, 1.0f, 1.0f);
                                DanmakuShapeEditGui.this.apply();
                                DanmakuShapeEditGui.this.close();
                            })
                    );
                }
                if (c == 'E') {
                    this.setSlot(counter, new GuiElementBuilder(Items.AIR)
                            .setCallback((i, clickType, input, slotBasedGui) -> {
                                SoundEventPlayUtils.playUISound(this.player, 1.0f, 1.0f);
                                DanmakuShapeEditGui.this.close();
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
        IngredientStack ingredientStack = source.get(RDDataComponents.DANMAKU_SHAPE.value());
        if (ingredientStack == null) {
            return;
        }

        List<List<List<Boolean>>> shapesByOutput = recipeType.getShapesByOutput(ingredientStack);
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
                                .setCallback(((GuiElementBuilderAccessor) (Object) builder).getCallback());
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
            return RDGuiItems.ENABLE.createStack();
        } else {
            return RDGuiItems.DISABLE.createStack();
        }
    }

    public void apply() {
        var danmakuShapeDrawType = DanmakuShapeDrawRecipeType.getInstance();
        List<DanmakuShapeDrawRecipe> matches = danmakuShapeDrawType.getMatches(this.getShape(), Unit.INSTANCE);
        if (matches.isEmpty()) {
            this.player.sendSystemMessage(Component.translatable("item.action.click.shape_recipe.fail"), false);
            return;
        }
        DanmakuShapeDrawRecipe first = matches.getFirst();
        IngredientStack output = first.getOutput();
        ItemStack itemStack = output.build();
        this.player.sendSystemMessage(Component.translatable("item.action.click.shape_recipe.success"), false);
        SoundEventPlayUtils.playUISound(this.player, 1.0f, 1.0f);
        this.player.setItemInHand(this.hand, itemStack);
    }
}
