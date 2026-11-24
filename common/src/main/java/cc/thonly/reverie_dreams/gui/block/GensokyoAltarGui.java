package cc.thonly.reverie_dreams.gui.block;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.block.entity.GensokyoAltarBlockEntity;
import cc.thonly.reverie_dreams.block.props.GensokyoAltarBlock;
import cc.thonly.reverie_dreams.gui.GuiCommon;
import cc.thonly.reverie_dreams.recipe.type.GensokyoAltarRecipeType;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import cc.thonly.reverie_dreams.util.sound.SoundEventPlayUtils;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public class GensokyoAltarGui extends SimpleGui implements GuiCommon {
    private static final int STATUS_SLOT = 44;
    public static final String[][] GRID = {
            {"X", "X", "I", "X", "I", "X", "I", "X", "X"},
            {"X", "X", "X", "X", "X", "X", "X", "X", "X"},
            {"X", "X", "I", "X", "E", "X", "I", "X", "X"},
            {"X", "X", "X", "X", "X", "X", "X", "X", "X"},
            {"X", "X", "I", "X", "I", "X", "I", "X", "X"},
    };
    ServerPlayer player;
    GensokyoAltarBlockEntity blockEntity;
    BlockState state;
    BlockPos pos;
    private final List<Integer> ingredientSlots = new ArrayList<>(9);

    public GensokyoAltarGui(ServerPlayer player, BlockState state, Level world, BlockPos pos) {
        super(MenuType.GENERIC_9x5, player, false);
        this.player = player;
        this.state = state;
        this.blockEntity = (GensokyoAltarBlockEntity) world.getBlockEntity(pos);
        this.pos = pos;
        this.init();
    }

    public void init() {
        this.setTitle(
                Component.empty()
                        .append(Component.translatable("space.-8"))
                        .append(Component.literal("\ub002")
                                .withStyle(Style.EMPTY.withColor(ChatFormatting.WHITE)
                                        .withFont(new FontDescription.Resource(ReverieDreams.id("reverie_dreams")))))
                        .append(Component.translatable("space.-168"))
                        .append(Component.translatable(RDBlocks.GENSOKYO_ALTAR.asBlock().getDescriptionId()).withStyle(ChatFormatting.WHITE))
        );
        this.ingredientSlots.clear();
        int coreSlot = -1;
        for (int row = 0; row < GRID.length; row++) {
            for (int col = 0; col < GRID[row].length; col++) {
                String pos = GRID[row][col];
                int index = row * 9 + col;
                if (pos.equalsIgnoreCase("X")) {
                    continue;
                }
                if (pos.equalsIgnoreCase("I")) {
                    this.setSlot(index, new Slot(blockEntity.getInventory(), this.ingredientSlots.size(), 0, 0));
                    this.ingredientSlots.add(index);
                    continue;
                }
                if (pos.equalsIgnoreCase("E")) {
                    this.setSlot(index, new Slot(blockEntity.getInventory(), 8, 0, 0));
                    coreSlot = index;
                }
            }
        }
        this.ingredientSlots.add(coreSlot);
        this.refreshStatus();
    }

    @Override
    public void onTick() {
        super.onTick();
        if (blockEntity.getLevel() == null || blockEntity.getLevel().getBlockEntity(this.pos) != this.blockEntity
                || !blockEntity.getLevel().getBlockState(this.pos).is(RDBlocks.GENSOKYO_ALTAR.asBlock())) {
            this.close();
            return;
        }
        if (blockEntity.getLevel().getGameTime() % 10 == 0) this.refreshStatus();
    }

    private void refreshStatus() {
        if (this.blockEntity.getLevel() == null || !(this.state.getBlock() instanceof GensokyoAltarBlock altar)) return;
        var structure = altar.findStructureIssue(this.blockEntity.getLevel(), this.pos);
        GuiElementBuilder status;
        if (structure != null) {
            status = new GuiElementBuilder(Items.COMPASS)
                    .setName(Component.translatableWithFallback("gui.reverie_dreams.altar.structure", "Altar structure incomplete"))
                    .addLoreLine(Component.translatableWithFallback("gui.reverie_dreams.altar.structure_at",
                            "At offset (%s, %s, %s), place %s", structure.x(), structure.y(), structure.z(), structure.expected().getName()));
        } else {
            var inputs = GensokyoAltarBlock.readIngredients(this.blockEntity.getInventory());
            var recipes = GensokyoAltarRecipeType.getInstance();
            var matches = recipes.getMatches(inputs);
            if (matches.isEmpty()) {
                status = new GuiElementBuilder(Items.PAPER)
                        .setName(Component.translatableWithFallback("gui.reverie_dreams.altar.no_match", "Ritual materials do not match"));
                var issue = recipes.findSingleSlotFix(inputs);
                if (issue != null) this.addSlotHint(status, issue);
            } else {
                status = new GuiElementBuilder(Items.AMETHYST_SHARD)
                        .setName(Component.translatableWithFallback("gui.reverie_dreams.altar.ready", "Ritual ready"))
                        .addLoreLine(Component.translatableWithFallback("gui.reverie_dreams.altar.result", "Result: %s",
                                matches.getFirst().getOutput().getLazyStack().getHoverName()));
            }
        }
        status.addLoreLine(Component.translatableWithFallback("gui.reverie_dreams.altar.core", "The center holds the core, then the result"))
                .addLoreLine(Component.translatableWithFallback("gui.reverie_dreams.altar.activate", "Close this screen, then sneak and use the altar"));
        this.setSlot(STATUS_SLOT, status.setCallback(() -> SoundEventPlayUtils.playUISound(this.player, 1.0f, 1.0f)));
    }

    private void addSlotHint(GuiElementBuilder status, GensokyoAltarRecipeType.SlotIssue issue) {
        int slot = this.ingredientSlots.get(issue.index());
        int row = slot / 9 + 1;
        int column = slot % 9 + 1;
        var required = issue.required();
        status.addLoreLine(required.isEmpty()
                ? Component.translatableWithFallback("gui.reverie_dreams.altar.clear_slot", "Empty row %s, column %s", row, column)
                : Component.translatableWithFallback("gui.reverie_dreams.altar.fill_slot", "Row %s, column %s needs %s ×%s",
                        row, column, required.getLazyStack().getHoverName(), required.getCount()));
    }

    @Override
    public boolean onAnyClick(int index, ClickType type, ContainerInput action) {
        if (this.blockEntity.getLevel() != null) {
            this.blockEntity.getLevel().sendBlockUpdated(
                    this.pos,
                    this.state,
                    this.state,
                    Block.UPDATE_ALL
            );
            this.blockEntity.getLevel().sendBlockUpdated(
                    this.pos,
                    this.state,
                    this.state,
                    Block.UPDATE_CLIENTS
            );
            this.blockEntity.setChanged();
        }
        return super.onAnyClick(index, type, action);
    }

    @Override
    public void onOpen() {
        super.onOpen();
    }

    @Override
    public void onPlayerClose(boolean success) {
        super.onPlayerClose(success);
        if (this.blockEntity.getLevel() != null) {
            this.blockEntity.getLevel().sendBlockUpdated(
                    this.pos,
                    this.state,
                    this.state,
                    Block.UPDATE_ALL
            );
            this.blockEntity.getLevel().sendBlockUpdated(
                    this.pos,
                    this.state,
                    this.state,
                    Block.UPDATE_CLIENTS
            );
            this.blockEntity.setChanged();
        }
    }

}
