package cc.thonly.reverie_dreams.gui.item;

import cc.thonly.reverie_dreams.component.GapRecorder;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponentTypes;
import cc.thonly.reverie_dreams.registry.content.item.RDGuiPlaceholderItems;
import cc.thonly.reverie_dreams.util.sound.SoundEventPlayUtils;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("resource")
public class GapGui extends SimpleGui {
    public static final String[][] GRID = {
            {"X", "X", "I", "I", "I", "I", "I", "X", "X"},
            {"X", "X", "A", "A", "A", "A", "A", "X", "X"},
            {"X", "X", "D", "D", "D", "D", "D", "X", "X"},
    };
    public static final int MAX_INDEX = 4;
    public static final int MIN_INDEX = 0;
    private final ItemStack stack;
    private List<GapRecorder> gapRecorders;

    public GapGui(ServerPlayer player, ItemStack stack) {
        super(MenuType.GENERIC_9x3, player, false);
        this.stack = stack;
        this.gapRecorders = new ArrayList<>(stack.getOrDefault(
                RDDataComponentTypes.GAP_RECORDER.value(), new ArrayList<>()));
        this.fillMissingRecorders();
        this.init();
    }

    private void fillMissingRecorders() {
        while (this.gapRecorders.size() <= MAX_INDEX) {
            this.gapRecorders.add(new GapRecorder(
                    "未记录",
                    this.getPlayer().level().dimension().identifier().toString(),
                    BlockPos.ZERO,
                    false
            ));
        }
    }

    public void init() {
        Component text = this.stack.getCustomName() != null
                ? this.stack.getCustomName()
                : this.stack.getHoverName();
        this.setTitle(text);
        for (int row = 0; row < GRID.length; row++) {
            for (int col = 0; col < GRID[row].length; col++) {
                String pos = GRID[row][col];
                int slot = row * 9 + col;
                int index = col - 2;

                switch (pos) {
                    case "X" -> setSlot(slot, new GuiElementBuilder(RDGuiPlaceholderItems.EMPTY_SLOT.asItem()));
                    case "I" -> setSlot(slot, new GuiElementBuilder(RDGuiPlaceholderItems.EMPTY_SLOT.asItem()));
                    case "A" -> {
                        if (index < MIN_INDEX || index > MAX_INDEX)
                            break;
                        setSlot(slot, new GuiElementBuilder(Items.EMERALD_BLOCK)
                                .setName(Component.translatable("dialog.gap.record", index))
                                .setCallback(click -> add(index)));
                    }
                    case "D" -> {
                        if (index < MIN_INDEX || index > MAX_INDEX)
                            break;
                        setSlot(slot, new GuiElementBuilder(Items.REDSTONE_BLOCK)
                                .setName(Component.translatable("dialog.gap.delete", index))
                                .setCallback(click -> delete(index)));
                    }
                }
            }
        }
        this.updateTeleportSlots();
    }

    @Override
    public void onTick() {
        super.onTick();
        this.gapRecorders = new ArrayList<>(this.stack.getOrDefault(
                RDDataComponentTypes.GAP_RECORDER.value(), new ArrayList<>()));
        this.fillMissingRecorders();
        this.updateTeleportSlots();
    }

    private void updateTeleportSlots() {
        int selected = this.stack.getOrDefault(RDDataComponentTypes.GAP_TARGET_INDEX.value(), 0);
        for (int col = 2; col <= 6; col++) {
            int index = col - 2;
            int slot = col;
            GapRecorder recorder = this.gapRecorders.get(index);
            boolean hasTarget = recorder.enable();
            boolean isSelected = hasTarget && index == selected;

            Item item = !hasTarget
                    ? Items.BARRIER
                    : (isSelected ? Items.ENDER_EYE : Items.ENDER_PEARL);

            Component targetBase = Component.translatable(
                    isSelected ? "dialog.gap.target.selected" : "dialog.gap.target.normal", index);
            Component targetName = hasTarget
                    ? Component.translatable("dialog.gap.target.recorded", targetBase, recorder.name())
                    : Component.translatable("dialog.gap.target.empty", targetBase);
            this.setSlot(slot, new GuiElementBuilder(item)
                    .setName(targetName)
                    .setLore(List.of(
                            Component.translatable("dialog.gap.world", recorder.world()),
                            Component.translatable("dialog.gap.position",
                                    recorder.value().getX(),
                                    recorder.value().getY(),
                                    recorder.value().getZ()),
                            Component.translatable(isSelected
                                    ? "dialog.gap.current_hint"
                                    : "dialog.gap.select_hint")
                    ))
                    .setCallback(click -> selectTarget(index)));
        }
    }

    public void selectTarget(int index) {
        GapRecorder recorder = this.gapRecorders.get(index);
        SoundEventPlayUtils.playUISound(this.player, 1.0f, 1.0f);
        if (recorder == null || !recorder.enable()) {
            this.player.sendSystemMessage(Component.translatable("dialog.gap.target_missing"), false);
            return;
        }

        this.stack.set(RDDataComponentTypes.GAP_TARGET_INDEX.value(), index);
        this.player.getInventory().setChanged();
        this.player.sendSystemMessage(Component.translatable(
                "dialog.gap.selected", index, recorder.name()), false);
        this.updateTeleportSlots();
        this.close();
    }

    public void add(int index) {
        BlockPos pos = this.player.blockPosition();
        GapRecorder recorder = new GapRecorder(
                "位置 " + index,
                this.getPlayer().level().dimension().identifier().toString(),
                pos,
                true
        );
        SoundEventPlayUtils.playUISound(this.player, 1.0f, 1.0f);
        this.gapRecorders.set(index, recorder);
        this.stack.set(RDDataComponentTypes.GAP_RECORDER.value(), this.gapRecorders);

        int selected = this.stack.getOrDefault(RDDataComponentTypes.GAP_TARGET_INDEX.value(), 0);
        if (selected < MIN_INDEX || selected > MAX_INDEX
                || !this.gapRecorders.get(selected).enable()) {
            this.stack.set(RDDataComponentTypes.GAP_TARGET_INDEX.value(), index);
        }

        this.player.sendSystemMessage(Component.translatable("dialog.gap.recorded", index), false);
        this.player.getInventory().setChanged();
        this.init();
    }

    public void delete(int index) {
        GapRecorder recorder = this.gapRecorders.get(index);
        SoundEventPlayUtils.playUISound(this.player, 1.0f, 1.0f);
        if (recorder != null) {
            this.gapRecorders.set(index, recorder.toBuilder().enable(false).build());
            this.stack.set(RDDataComponentTypes.GAP_RECORDER.value(), this.gapRecorders);

            if (this.stack.getOrDefault(RDDataComponentTypes.GAP_TARGET_INDEX.value(), 0) == index) {
                int fallback = MIN_INDEX;
                for (int i = MIN_INDEX; i <= MAX_INDEX; i++) {
                    if (this.gapRecorders.get(i).enable()) {
                        fallback = i;
                        break;
                    }
                }
                this.stack.set(RDDataComponentTypes.GAP_TARGET_INDEX.value(), fallback);
            }

            this.player.sendSystemMessage(Component.translatable("dialog.gap.cleared", index), false);
        }
        this.player.getInventory().setChanged();
        this.init();
    }

    @Override
    public void onPlayerClose(boolean success) {
        super.onPlayerClose(success);
        this.stack.set(RDDataComponentTypes.GAP_RECORDER.value(), this.gapRecorders);
        this.player.getInventory().setChanged();
    }
}