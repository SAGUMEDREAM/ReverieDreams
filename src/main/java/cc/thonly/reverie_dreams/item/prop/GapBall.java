package cc.thonly.reverie_dreams.item.prop;

import cc.thonly.reverie_dreams.component.GapRecorder;
import cc.thonly.reverie_dreams.component.ModDataComponentTypes;
import cc.thonly.reverie_dreams.item.ModGuiItems;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class GapBall extends Item {

    public GapBall(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (!world.isClient() && user instanceof ServerPlayerEntity serverPlayerEntity) {
            SimpleGui gapGui = new GapGUI(serverPlayerEntity, stack);
            gapGui.open();
            return ActionResult.SUCCESS_SERVER;
        }
        return ActionResult.SUCCESS;
    }

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

        public GapGUI(ServerPlayerEntity player, ItemStack stack) {
            super(ScreenHandlerType.GENERIC_9X3, player, false);
            this.stack = stack;
            this.gapRecorders = new ArrayList<>(stack.getOrDefault(ModDataComponentTypes.GAP_RECORDER, new ArrayList<>()));
            while (gapRecorders.size() <= MAX_INDEX) {
                this.gapRecorders.add(new GapRecorder("未记录", this.getPlayer().getWorld().getRegistryKey().getValue().toString(), BlockPos.ORIGIN, false));
            }
            this.init();
        }

        public void init() {
            Text text = this.stack.getCustomName() != null ? this.stack.getCustomName() : this.stack.getName();
            this.setTitle(text);
            for (int row = 0; row < GRID.length; row++) {
                for (int col = 0; col < GRID[row].length; col++) {
                    String pos = GRID[row][col];
                    int slot = row * 9 + col;
                    int index = col - 2;

                    switch (pos) {
                        case "X" -> setSlot(slot, new GuiElementBuilder(ModGuiItems.EMPTY_SLOT));
                        case "I" -> {
                            if (index < MIN_INDEX || index > MAX_INDEX) break;
                            GapRecorder recorder = this.gapRecorders.get(index);
                            setSlot(slot, new GuiElementBuilder(recorder.isEnable() ? Items.BARRIER : Items.ENDER_EYE)
                                    .setName(Text.literal("传送" + index))
                                    .setCallback(click -> teleport(index)));
                        }
                        case "A" -> {
                            if (index < MIN_INDEX || index > MAX_INDEX) break;
                            setSlot(slot, new GuiElementBuilder(Items.EMERALD_BLOCK)
                                    .setName(Text.literal("记录 " + index))
                                    .setCallback(click -> add(index)));
                        }
                        case "D" -> {
                            if (index < MIN_INDEX || index > MAX_INDEX) break;
                            setSlot(slot, new GuiElementBuilder(Items.REDSTONE_BLOCK)
                                    .setName(Text.literal("删除 " + index))
                                    .setCallback(click -> delete(index)));
                        }
                    }

                }
            }
        }

        @Override
        public void onTick() {
            super.onTick();
            this.gapRecorders = new ArrayList<>(this.stack.getOrDefault(ModDataComponentTypes.GAP_RECORDER, new ArrayList<>()));
            while (gapRecorders.size() <= MAX_INDEX) {
                this.gapRecorders.add(new GapRecorder("未记录", this.getPlayer().getWorld().getRegistryKey().getValue().toString(), BlockPos.ORIGIN, false));
            }
            this.updateTeleportSlots();
        }


        private void updateTeleportSlots() {
            for (int col = 2; col <= 6; col++) {
                int index = col - 2;
                int slot = 0 * 9 + col;

                GapRecorder recorder = this.gapRecorders.get(index);
                boolean hasTarget = recorder != null && recorder.isEnable();
                Item item = hasTarget ? Items.ENDER_EYE : Items.BARRIER;

                this.setSlot(slot, new GuiElementBuilder(item)
                        .setName(Text.literal("传送 " + index + (hasTarget ? (" - " + recorder.getName()) : "（未记录）")))
                        .setLore(List.of(
                                Text.literal("世界：" + recorder.getWorld()),
                                Text.literal("世界：" + recorder.getValue().getX() + " " + recorder.getValue().getY() + " " + recorder.getValue().getZ())
                        ))
                        .setCallback(click -> teleport(index)));
            }
        }

        public void teleport(int index) {
            GapRecorder recorder = this.gapRecorders.get(index);
            this.player.playSoundToPlayer(SoundEvents.UI_BUTTON_CLICK.value(), SoundCategory.PLAYERS, 1.0f, 1.0f);
            if (recorder != null && recorder.isEnable()) {
                BlockPos pos = recorder.getValue();
                MinecraftServer server = this.player.getServer();
                Identifier id = Identifier.of(recorder.getWorld());
                RegistryKey<World> worldKey = RegistryKey.of(RegistryKeys.WORLD, id);
                ServerWorld targetWorld = server.getWorld(worldKey);
                if (targetWorld == null) {
                    this.player.sendMessage(Text.literal("目标世界不存在: " + id), false);
                    return;
                }
                this.player.teleport(targetWorld,
                        pos.getX() + 0.5,
                        pos.getY(),
                        pos.getZ() + 0.5,
                        EnumSet.noneOf(PositionFlag.class),
                        this.player.getYaw(),
                        this.player.getPitch(),
                        true);
                this.player.sendMessage(Text.literal("已传送至：" + recorder.getName()), false);
                this.player.playSoundToPlayer(SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT, SoundCategory.PLAYERS, 1.0f, 1.0f);
                this.player.getItemCooldownManager().set(this.stack, 3 * 20);
                this.close();
            } else {
                this.player.sendMessage(Text.literal("目标位置不存在。"), false);
            }
        }

        public void add(int index) {
            BlockPos pos = this.player.getBlockPos();
            GapRecorder recorder = new GapRecorder("位置 " + index, this.getPlayer().getWorld().getRegistryKey().getValue().toString(), pos, true);
            player.playSoundToPlayer(SoundEvents.UI_BUTTON_CLICK.value(), SoundCategory.PLAYERS, 1.0f, 1.0f);
            this.gapRecorders.set(index, recorder);
            this.stack.set(ModDataComponentTypes.GAP_RECORDER, this.gapRecorders);
            this.player.sendMessage(Text.literal("已记录当前位置至槽位 " + index), false);
            this.init();
            this.stack.set(ModDataComponentTypes.GAP_RECORDER, this.gapRecorders);
            this.player.getInventory().markDirty();
        }

        public void delete(int index) {
            GapRecorder recorder = this.gapRecorders.get(index);
            player.playSoundToPlayer(SoundEvents.UI_BUTTON_CLICK.value(), SoundCategory.PLAYERS, 1.0f, 1.0f);
            if (recorder != null) {
                recorder.setEnable(false);
                this.stack.set(ModDataComponentTypes.GAP_RECORDER, this.gapRecorders);
                this.player.sendMessage(Text.literal("已清除槽位 " + index), false);
                this.init();
            }
            this.stack.set(ModDataComponentTypes.GAP_RECORDER, this.gapRecorders);
            this.player.getInventory().markDirty();
        }

        @Override
        public void onClose() {
            super.onClose();
            this.stack.set(ModDataComponentTypes.GAP_RECORDER, this.gapRecorders);
            this.player.getInventory().markDirty();
        }
    }

}
