package cc.thonly.reverie_dreams.item.prop;

import cc.thonly.reverie_dreams.component.GapRecorder;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponents;
import cc.thonly.reverie_dreams.registry.content.item.RDGuiItems;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class GapBall extends Item {

    public GapBall(Properties settings) {
        super(settings);
    }

    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        ItemStack stack = user.getItemInHand(hand);
        if (!world.isClientSide() && user instanceof ServerPlayer serverPlayerEntity) {
            SimpleGui gapGui = new GapGUI(serverPlayerEntity, stack);
            gapGui.open();
            return InteractionResult.SUCCESS_SERVER;
        }
        return InteractionResult.SUCCESS;
    }

    @SuppressWarnings("resource")
    public static class GapGUI extends SimpleGui {
        public static final String[][] GRID = {
                {"X", "X", "I", "I", "I", "I", "I", "X", "X"},
                {"X", "X", "A", "A", "A", "A", "A", "X", "X"},
                {"X", "X", "D", "D", "D", "D", "D", "X", "X"},
        };
        public static final int MAX_INDEX = 4;
        public static final int MIN_INDEX = 0;
        private final ItemStack stack;
        private List<GapRecorder> gapRecorders;

        public GapGUI(ServerPlayer player, ItemStack stack) {
            super(MenuType.GENERIC_9x3, player, false);
            this.stack = stack;
            this.gapRecorders = new ArrayList<>(stack.getOrDefault(RDDataComponents.GAP_RECORDER.value(), new ArrayList<>()));
            while (gapRecorders.size() <= MAX_INDEX) {
                this.gapRecorders.add(new GapRecorder("未记录", this.getPlayer().level().dimension().identifier().toString(), BlockPos.ZERO, false));
            }
            this.init();
        }

        public void init() {
            Component text = this.stack.getCustomName() != null ? this.stack.getCustomName() : this.stack.getHoverName();
            this.setTitle(text);
            for (int row = 0; row < GRID.length; row++) {
                for (int col = 0; col < GRID[row].length; col++) {
                    String pos = GRID[row][col];
                    int slot = row * 9 + col;
                    int index = col - 2;

                    switch (pos) {
                        case "X" -> setSlot(slot, new GuiElementBuilder(RDGuiItems.EMPTY_SLOT.asItem()));
                        case "I" -> {
                            if (index < MIN_INDEX || index > MAX_INDEX) break;
                            GapRecorder recorder = this.gapRecorders.get(index);
                            setSlot(slot, new GuiElementBuilder(recorder.isEnable() ? Items.BARRIER : Items.ENDER_EYE)
                                    .setName(Component.literal("传送" + index))
                                    .setCallback(click -> teleport(index)));
                        }
                        case "A" -> {
                            if (index < MIN_INDEX || index > MAX_INDEX) break;
                            setSlot(slot, new GuiElementBuilder(Items.EMERALD_BLOCK)
                                    .setName(Component.literal("记录 " + index))
                                    .setCallback(click -> add(index)));
                        }
                        case "D" -> {
                            if (index < MIN_INDEX || index > MAX_INDEX) break;
                            setSlot(slot, new GuiElementBuilder(Items.REDSTONE_BLOCK)
                                    .setName(Component.literal("删除 " + index))
                                    .setCallback(click -> delete(index)));
                        }
                    }

                }
            }
        }

        @Override
        public void onTick() {
            super.onTick();
            this.gapRecorders = new ArrayList<>(this.stack.getOrDefault(RDDataComponents.GAP_RECORDER.value(), new ArrayList<>()));
            while (gapRecorders.size() <= MAX_INDEX) {
                this.gapRecorders.add(new GapRecorder("未记录", this.getPlayer().level().dimension().identifier().toString(), BlockPos.ZERO, false));
            }
            this.updateTeleportSlots();
        }


        private void updateTeleportSlots() {
            for (int col = 2; col <= 6; col++) {
                int index = col - 2;
                int slot = 0 * 9 + col;

                GapRecorder recorder = this.gapRecorders.get(index);
                if (recorder == null) {
                    return;
                }
                boolean hasTarget = recorder.isEnable();
                Item item = hasTarget ? Items.ENDER_EYE : Items.BARRIER;

                this.setSlot(slot, new GuiElementBuilder(item)
                        .setName(Component.literal("传送 " + index + (hasTarget ? (" - " + recorder.getName()) : "（未记录）")))
                        .setLore(List.of(
                                Component.literal("世界：" + recorder.getWorld()),
                                Component.literal("世界：" + recorder.getValue().getX() + " " + recorder.getValue().getY() + " " + recorder.getValue().getZ())
                        ))
                        .setCallback(click -> teleport(index)));
            }
        }

        public void teleport(int index) {
            GapRecorder recorder = this.gapRecorders.get(index);
            this.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 1.0f, 1.0f);
            if (recorder != null && recorder.isEnable()) {
                BlockPos pos = recorder.getValue();
                MinecraftServer server = this.player.level().getServer();
                Identifier id = Identifier.parse(recorder.getWorld());
                ResourceKey<Level> worldKey = ResourceKey.create(Registries.DIMENSION, id);
                ServerLevel targetWorld = server.getLevel(worldKey);
                if (targetWorld == null) {
                    this.player.sendSystemMessage(Component.literal("目标世界不存在: " + id), false);
                    return;
                }
                this.player.teleportTo(targetWorld,
                        pos.getX() + 0.5,
                        pos.getY(),
                        pos.getZ() + 0.5,
                        EnumSet.noneOf(Relative.class),
                        this.player.getYRot(),
                        this.player.getXRot(),
                        true);
                this.player.sendSystemMessage(Component.literal("已传送至：" + recorder.getName()), false);
                this.player.playSound(SoundEvents.CHORUS_FRUIT_TELEPORT, 1.0f, 1.0f);
                this.player.getCooldowns().addCooldown(this.stack, 3 * 20);
                this.close();
            } else {
                this.player.sendSystemMessage(Component.literal("目标位置不存在。"), false);
            }
        }

        public void add(int index) {
            BlockPos pos = this.player.blockPosition();
            GapRecorder recorder = new GapRecorder("位置 " + index, this.getPlayer().level().dimension().identifier().toString(), pos, true);
            this.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 1.0f, 1.0f);
            this.gapRecorders.set(index, recorder);
            this.stack.set(RDDataComponents.GAP_RECORDER.value(), this.gapRecorders);
            this.player.sendSystemMessage(Component.literal("已记录当前位置至槽位 " + index), false);
            this.init();
            this.stack.set(RDDataComponents.GAP_RECORDER.value(), this.gapRecorders);
            this.player.getInventory().setChanged();
        }

        public void delete(int index) {
            GapRecorder recorder = this.gapRecorders.get(index);
            this.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 1.0f, 1.0f);
            if (recorder != null) {
                recorder.setEnable(false);
                this.stack.set(RDDataComponents.GAP_RECORDER.value(), this.gapRecorders);
                this.player.sendSystemMessage(Component.literal("已清除槽位 " + index), false);
                this.init();
            }
            this.stack.set(RDDataComponents.GAP_RECORDER.value(), this.gapRecorders);
            this.player.getInventory().setChanged();
        }

        @Override
        public void close() {
            super.close();
            this.stack.set(RDDataComponents.GAP_RECORDER.value(), this.gapRecorders);
            this.player.getInventory().setChanged();
        }
    }

}
