package cc.thonly.reverie_dreams.server.dialog.gap;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.component.GapRecorder;
import cc.thonly.reverie_dreams.item.prop.GapBall;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponentTypes;
import cc.thonly.reverie_dreams.server.CustomClickActionRegistry;
import cc.thonly.reverie_dreams.server.dialog.ActionBuilder;
import cc.thonly.reverie_dreams.server.dialog.DialogBuilder;
import cc.thonly.reverie_dreams.util.sound.SoundEventPlayUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.common.ServerboundCustomClickActionPacket;
import net.minecraft.server.dialog.body.PlainMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 隙间之球的 Dialog 界面。
 *
 * <p>布局参考 Guidebook 的目录页：每个目标使用“物品图标 + 可点击文本”的
 * {@link net.minecraft.server.dialog.body.ItemBody} 展示，而不是堆一排按钮。
 * 自定义点击动作只携带手部、槽位和动作类型，实际数据始终从服务端手中的
 * 隙间之球读取。</p>
 */
public final class GapDialog {
    private static final int MIN_INDEX = 0;
    private static final int MAX_INDEX = 4;
    private static final int BODY_WIDTH = 280;
    private static final int DESCRIPTION_WIDTH = 220;
    private static final int BUTTON_WIDTH = 180;

    private static final String ACTION_SELECT = "select";
    private static final String ACTION_RECORD = "record";
    private static final String ACTION_DELETE = "delete";
    private static final String ACTION_MANAGE = "manage";
    private static final String ACTION_MAIN = "main";

    private GapDialog() {
    }

    public static void open(ServerPlayer player, InteractionHand hand) {
        HandStack held = findGapBall(player, hand);
        if (held != null) {
            openMain(player, held);
        }
    }

    public static void handleAction(ServerPlayer player, ServerboundCustomClickActionPacket packet) {
        if (player == null) {
            return;
        }
        CompoundTag tag = payload(packet);
        if (tag == null) {
            return;
        }

        InteractionHand hand = "off".equals(tag.getStringOr("hand", "main"))
                ? InteractionHand.OFF_HAND
                : InteractionHand.MAIN_HAND;
        HandStack held = findGapBall(player, hand);
        if (held == null) {
            player.sendSystemMessage(Component.translatable("dialog.gap.not_in_hand"), false);
            return;
        }

        String action = tag.getStringOr("action", "");
        int index = tag.getIntOr("index", -1);
        switch (action) {
            case ACTION_SELECT -> select(player, held, index);
            case ACTION_RECORD -> record(player, held, index);
            case ACTION_DELETE -> delete(player, held, index);
            case ACTION_MANAGE -> openManage(player, held);
            case ACTION_MAIN -> openMain(player, held);
            default -> {
                // Unknown or no-op action; do not trust it.
            }
        }
    }

    private static void openMain(ServerPlayer player, HandStack held) {
        ItemStack stack = held.stack();
        List<GapRecorder> recorders = getRecorders(player, stack);
        int selected = stack.getOrDefault(RDDataComponentTypes.GAP_TARGET_INDEX.value(), MIN_INDEX);

        DialogBuilder.builder(builder -> {
            builder.key(ReverieDreams.id("gap/main"));
            builder.columns(2);
            builder.common(common -> {
                common.title(title(stack));
                common.setCanCloseWithEscape(true);
                common.setPause(false);
                common.addTextBody(currentTarget(recorders, selected), BODY_WIDTH);
                common.addTextBody(Component.translatable("dialog.gap.hint.main"), BODY_WIDTH);

                for (int i = MIN_INDEX; i <= MAX_INDEX; i++) {
                    GapRecorder recorder = recorders.get(i);
                    boolean isSelected = recorder.enable() && i == selected;
                    common.addItemBody(
                            new ItemStackTemplate(targetIcon(recorder, isSelected)),
                            Optional.of(new PlainMessage(targetDescription(
                                    held, recorder, i, isSelected), DESCRIPTION_WIDTH)),
                            false,
                            false,
                            16,
                            16
                    );
                }
            });
            builder.actions(actions -> {
                ActionBuilder actionBuilder = actions.actionBuilder();
                actions.addButton(
                        Component.translatable("dialog.gap.button.manage"),
                        Optional.of(actionBuilder.customAction(
                                CustomClickActionRegistry.GAP_ACTION_KEY,
                                tag(held.hand(), ACTION_MANAGE, 0)
                        ))
                );
                actions.addButton(Component.translatable("dialog.gap.button.close"), Optional.empty());
            });
        }).get().buildOrThrow().open(player);
    }

    private static void openManage(ServerPlayer player, HandStack held) {
        ItemStack stack = held.stack();
        List<GapRecorder> recorders = getRecorders(player, stack);
        int selected = stack.getOrDefault(RDDataComponentTypes.GAP_TARGET_INDEX.value(), MIN_INDEX);

        DialogBuilder.builder(builder -> {
            builder.key(ReverieDreams.id("gap/manage"));
            builder.columns(2);
            builder.common(common -> {
                common.title(Component.translatable("dialog.gap.title.manage"));
                common.setCanCloseWithEscape(true);
                common.setPause(false);
                common.addTextBody(Component.translatable("dialog.gap.hint.manage"), BODY_WIDTH);
                common.addTextBody(Component.translatable("dialog.gap.current_target", selectedName(recorders, selected)), BODY_WIDTH);

                for (int i = MIN_INDEX; i <= MAX_INDEX; i++) {
                    GapRecorder recorder = recorders.get(i);
                    common.addItemBody(
                            new ItemStackTemplate(manageIcon(recorder, i == selected)),
                            Optional.of(new PlainMessage(manageDescription(held, recorder, i), DESCRIPTION_WIDTH)),
                            false,
                            false,
                            16,
                            16
                    );
                }
            });
            builder.actions(actions -> {
                ActionBuilder actionBuilder = actions.actionBuilder();
                actions.addButton(
                        Component.translatable("dialog.gap.button.back"),
                        Optional.of(actionBuilder.customAction(
                                CustomClickActionRegistry.GAP_ACTION_KEY,
                                tag(held.hand(), ACTION_MAIN, 0)
                        ))
                );
                actions.addButton(Component.translatable("dialog.gap.button.close"), Optional.empty());
            });
        }).get().buildOrThrow().open(player);
    }

    private static void select(ServerPlayer player, HandStack held, int index) {
        if (!validIndex(index)) {
            return;
        }
        List<GapRecorder> recorders = getRecorders(player, held.stack());
        GapRecorder recorder = recorders.get(index);
        SoundEventPlayUtils.playUISound(player, 1.0F, 1.0F);
        if (!recorder.enable()) {
            player.sendSystemMessage(Component.translatable("dialog.gap.target_missing"), false);
            openMain(player, held);
            return;
        }

        held.stack().set(RDDataComponentTypes.GAP_TARGET_INDEX.value(), index);
        player.getInventory().setChanged();
        player.sendSystemMessage(Component.translatable(
                "dialog.gap.selected", index, recorder.name()), false);
        openMain(player, held);
    }

    private static void record(ServerPlayer player, HandStack held, int index) {
        if (!validIndex(index)) {
            return;
        }
        List<GapRecorder> recorders = getRecorders(player, held.stack());
        String world = player.level().dimension().identifier().toString();
        GapRecorder recorder = new GapRecorder("位置 " + index, world, player.blockPosition(), true);
        recorders.set(index, recorder);
        held.stack().set(RDDataComponentTypes.GAP_RECORDER.value(), recorders);

        int selected = held.stack().getOrDefault(RDDataComponentTypes.GAP_TARGET_INDEX.value(), MIN_INDEX);
        if (!validIndex(selected) || !recorders.get(selected).enable()) {
            held.stack().set(RDDataComponentTypes.GAP_TARGET_INDEX.value(), index);
        }

        SoundEventPlayUtils.playUISound(player, 1.0F, 1.0F);
        player.sendSystemMessage(Component.translatable("dialog.gap.recorded", index), false);
        player.getInventory().setChanged();
        openManage(player, held);
    }

    private static void delete(ServerPlayer player, HandStack held, int index) {
        if (!validIndex(index)) {
            return;
        }
        List<GapRecorder> recorders = getRecorders(player, held.stack());
        GapRecorder recorder = recorders.get(index);
        if (recorder.enable()) {
            recorders.set(index, recorder.toBuilder().enable(false).build());
            held.stack().set(RDDataComponentTypes.GAP_RECORDER.value(), recorders);
        }

        if (held.stack().getOrDefault(RDDataComponentTypes.GAP_TARGET_INDEX.value(), MIN_INDEX) == index) {
            int fallback = MIN_INDEX;
            for (int i = MIN_INDEX; i <= MAX_INDEX; i++) {
                if (recorders.get(i).enable()) {
                    fallback = i;
                    break;
                }
            }
            held.stack().set(RDDataComponentTypes.GAP_TARGET_INDEX.value(), fallback);
        }

        SoundEventPlayUtils.playUISound(player, 1.0F, 1.0F);
        player.sendSystemMessage(Component.translatable("dialog.gap.cleared", index), false);
        player.getInventory().setChanged();
        openManage(player, held);
    }

    private static Component targetDescription(HandStack held, GapRecorder recorder,
                                                  int index, boolean selected) {
        MutableComponent description = Component.empty();
        Component baseLabel = Component.translatable(
                selected ? "dialog.gap.target.selected" : "dialog.gap.target.normal", index);
        Component label = recorder.enable()
                ? Component.translatable("dialog.gap.target.recorded", baseLabel, recorder.name())
                : Component.translatable("dialog.gap.target.empty", baseLabel);

        if (recorder.enable()) {
            description.append(label.copy().withStyle(
                    clickableStyle(
                            selected ? ChatFormatting.GOLD : ChatFormatting.AQUA,
                            tag(held.hand(), ACTION_SELECT, index)
                    )
            ));
            description.append(Component.translatable(
                            "dialog.gap.target.details", recorder.world(), formatPos(recorder))
                    .withStyle(Style.EMPTY.withColor(ChatFormatting.WHITE)));
        } else {
            description.append(label.copy()
                    .withStyle(Style.EMPTY.withColor(ChatFormatting.WHITE)));
            description.append(Component.translatable("dialog.gap.not_recorded")
                    .withStyle(Style.EMPTY.withColor(ChatFormatting.WHITE)));
        }
        return description;
    }

    private static Component manageDescription(HandStack held, GapRecorder recorder, int index) {
        MutableComponent description = Component.empty();
        Component slot = recorder.enable()
                ? Component.translatable("dialog.gap.slot.recorded", index, recorder.name())
                : Component.translatable("dialog.gap.slot.empty", index);
        description.append(slot.copy().withStyle(Style.EMPTY.withColor(ChatFormatting.WHITE)));

        if (recorder.enable()) {
            description.append(Component.translatable(
                            "dialog.gap.slot.details", recorder.world(), formatPos(recorder))
                    .withStyle(Style.EMPTY.withColor(ChatFormatting.WHITE)));
        } else {
            description.append(Component.translatable("dialog.gap.not_recorded")
                    .withStyle(Style.EMPTY.withColor(ChatFormatting.WHITE)));
        }

        description.append(Component.literal("\n"));
        description.append(Component.translatable("dialog.gap.action.record").withStyle(
                clickableStyle(ChatFormatting.GREEN, tag(held.hand(), ACTION_RECORD, index))));
        if (recorder.enable()) {
            description.append(Component.literal("  "));
            description.append(Component.translatable("dialog.gap.action.clear").withStyle(
                    clickableStyle(ChatFormatting.RED, tag(held.hand(), ACTION_DELETE, index))));
        }
        return description;
    }

    private static Style clickableStyle(ChatFormatting color, CompoundTag tag) {
        return Style.EMPTY
                .withColor(color)
                .withUnderlined(true)
                .withClickEvent(new ClickEvent.Custom(
                        CustomClickActionRegistry.GAP_ACTION_KEY,
                        Optional.of(tag)
                ));
    }

    private static List<GapRecorder> getRecorders(ServerPlayer player, ItemStack stack) {
        List<GapRecorder> recorders = new ArrayList<>(stack.getOrDefault(
                RDDataComponentTypes.GAP_RECORDER.value(), List.of()));
        while (recorders.size() <= MAX_INDEX) {
            recorders.add(new GapRecorder(
                    "未记录",
                    player.level().dimension().identifier().toString(),
                    BlockPos.ZERO,
                    false
            ));
        }
        return recorders;
    }

    private static Item targetIcon(GapRecorder recorder, boolean selected) {
        if (!recorder.enable()) {
            return Items.BARRIER;
        }
        return selected ? Items.ENDER_EYE : Items.ENDER_PEARL;
    }

    private static Item manageIcon(GapRecorder recorder, boolean selected) {
        if (!recorder.enable()) {
            return Items.WRITABLE_BOOK;
        }
        return selected ? Items.ENDER_EYE : Items.ENDER_PEARL;
    }

    private static Component title(ItemStack stack) {
        return stack.getCustomName() != null ? stack.getCustomName() : stack.getHoverName();
    }

    private static Component currentTarget(List<GapRecorder> recorders, int selected) {
        return Component.translatable("dialog.gap.current_target",
                selectedName(recorders, selected));
    }

    private static Component selectedName(List<GapRecorder> recorders, int selected) {
        if (validIndex(selected) && recorders.get(selected).enable()) {
            return Component.translatable(
                    "dialog.gap.selected_name", selected, recorders.get(selected).name());
        }
        return Component.translatable("dialog.gap.unselected");
    }

    private static String formatPos(GapRecorder recorder) {
        BlockPos pos = recorder.value();
        return pos.getX() + " " + pos.getY() + " " + pos.getZ();
    }

    private static boolean validIndex(int index) {
        return index >= MIN_INDEX && index <= MAX_INDEX;
    }

    @Nullable
    private static HandStack findGapBall(ServerPlayer player, InteractionHand preferred) {
        ItemStack stack = player.getItemInHand(preferred);
        if (stack.getItem() instanceof GapBall) {
            return new HandStack(preferred, stack);
        }

        InteractionHand other = preferred == InteractionHand.MAIN_HAND
                ? InteractionHand.OFF_HAND
                : InteractionHand.MAIN_HAND;
        stack = player.getItemInHand(other);
        if (stack.getItem() instanceof GapBall) {
            return new HandStack(other, stack);
        }
        return null;
    }

    private static CompoundTag tag(InteractionHand hand, String action, int index) {
        CompoundTag tag = new CompoundTag();
        tag.putString("hand", hand == InteractionHand.OFF_HAND ? "off" : "main");
        tag.putString("action", action);
        tag.putInt("index", index);
        return tag;
    }

    @Nullable
    private static CompoundTag payload(ServerboundCustomClickActionPacket packet) {
        Optional<Tag> payload = packet.payload();
        return payload.isPresent() && payload.get() instanceof CompoundTag tag ? tag : null;
    }

    private record HandStack(InteractionHand hand, ItemStack stack) {
    }
}