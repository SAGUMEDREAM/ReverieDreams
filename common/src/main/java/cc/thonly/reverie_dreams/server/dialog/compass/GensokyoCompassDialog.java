package cc.thonly.reverie_dreams.server.dialog.compass;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.data.gensokyo.GensokyoLandmark;
import cc.thonly.reverie_dreams.item.prop.GensokyoCompassItem;
import cc.thonly.reverie_dreams.registry.content.GensokyoLandmarks;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponentTypes;
import cc.thonly.reverie_dreams.server.CustomClickActionRegistry;
import cc.thonly.reverie_dreams.server.dialog.ActionBuilder;
import cc.thonly.reverie_dreams.server.dialog.DialogBuilder;
import cc.thonly.reverie_dreams.util.sound.SoundEventPlayUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.common.ServerboundCustomClickActionPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.dialog.body.PlainMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Landmark picker for the Gensokyo compass.
 *
 * <p>The client only sends the hand, page and landmark identifier. The
 * landmark registry and the selected target component are always read on the
 * server, so a crafted payload cannot select a non-existent landmark.</p>
 */
public final class GensokyoCompassDialog {
    private static final int PAGE_SIZE = 8;
    private static final int BODY_WIDTH = 280;
    private static final int DESCRIPTION_WIDTH = 220;

    private static final String ACTION_OPEN = "open";
    private static final String ACTION_SELECT = "select";
    private static final String ACTION_PAGE = "page";
    private static final String ACTION_CLEAR = "clear";

    private GensokyoCompassDialog() {
    }

    public static void open(ServerPlayer player, InteractionHand hand, int page) {
        HandStack held = findCompass(player, hand);
        if (held == null) {
            player.sendSystemMessage(Component.translatable("dialog.reverie_dreams.gensokyo_compass.not_in_hand"), false);
            return;
        }

        List<Map.Entry<Identifier, GensokyoLandmark>> landmarks = GensokyoLandmarks.sortedEntries();
        int pageCount = Math.max(1, (landmarks.size() + PAGE_SIZE - 1) / PAGE_SIZE);
        int currentPage = Math.max(0, Math.min(page, pageCount - 1));
        int start = currentPage * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, landmarks.size());

        Identifier selectedId = held.stack().get(RDDataComponentTypes.GENSOKYO_COMPASS_TARGET.value());
        GensokyoLandmark selected = GensokyoLandmarks.get(selectedId).orElse(null);

        int finalPage = currentPage;
        DialogBuilder.builder(builder -> {
            builder.key(ReverieDreams.id("gensokyo_compass/main"));
            builder.columns(2);
            builder.common(common -> {
                common.title(Component.translatable("dialog.reverie_dreams.gensokyo_compass.title"));
                common.setCanCloseWithEscape(true);
                common.setPause(false);
                MutableComponent summary = Component.empty().append(Component.translatable(
                        "dialog.reverie_dreams.gensokyo_compass.page",
                        finalPage + 1, pageCount, landmarks.size()).withStyle(ChatFormatting.GRAY));

                summary.append("\n");
                if (selected == null) {
                    summary.append(Component.translatable(
                            "dialog.reverie_dreams.gensokyo_compass.current_none").withStyle(ChatFormatting.GRAY));
                } else {
                    summary.append(Component.translatable(
                            "dialog.reverie_dreams.gensokyo_compass.current",
                            selected.displayName().copy().withStyle(ChatFormatting.GOLD),
                            selected.x(),
                            selected.z(),
                            Math.round(selected.horizontalDistance(player.getX(), player.getZ()))
                    ));
                    selected.displayNote().ifPresent(note -> summary.append("\n")
                            .append(note.copy().withStyle(ChatFormatting.GRAY)));
                }
                if (landmarks.isEmpty()) {
                    summary.append("\n").append(Component.translatable(
                            "dialog.reverie_dreams.gensokyo_compass.empty"));
                }
                common.addTextBody(summary, BODY_WIDTH);

                for (int i = start; i < end; i++) {
                    Map.Entry<Identifier, GensokyoLandmark> entry = landmarks.get(i);
                    GensokyoLandmark landmark = entry.getValue();
                    boolean isSelected = entry.getKey().equals(selectedId);
                    Component label = Component.translatable(
                            isSelected
                                    ? "dialog.reverie_dreams.gensokyo_compass.entry.selected"
                                    : "dialog.reverie_dreams.gensokyo_compass.entry",
                            landmark.displayName(),
                            Component.literal(Long.toString(Math.round(
                                    landmark.horizontalDistance(player.getX(), player.getZ()))))
                                    .withStyle(ChatFormatting.GRAY)
                    );
                    common.addItemBody(
                            new ItemStackTemplate(landmark.kind().icon()),
                            Optional.of(new PlainMessage(
                                    label.copy().withStyle(clickableStyle(
                                            isSelected ? ChatFormatting.GOLD : ChatFormatting.AQUA,
                                            tag(held.hand(), ACTION_SELECT, finalPage, entry.getKey())
                                    )),
                                    DESCRIPTION_WIDTH
                            )),
                            false,
                            false,
                            16,
                            16
                    );
                }
            });
            builder.actions(actions -> {
                ActionBuilder actionBuilder = actions.actionBuilder();
                if (finalPage > 0) {
                    actions.addButton(
                            Component.translatable("dialog.reverie_dreams.gensokyo_compass.prev"),
                            Optional.of(actionBuilder.customAction(
                                    CustomClickActionRegistry.GENSOKYO_COMPASS_ACTION_KEY,
                                    tag(held.hand(), ACTION_PAGE, finalPage - 1, null)
                            ))
                    );
                }
                if (finalPage + 1 < pageCount) {
                    actions.addButton(
                            Component.translatable("dialog.reverie_dreams.gensokyo_compass.next"),
                            Optional.of(actionBuilder.customAction(
                                    CustomClickActionRegistry.GENSOKYO_COMPASS_ACTION_KEY,
                                    tag(held.hand(), ACTION_PAGE, finalPage + 1, null)
                            ))
                    );
                }
                if (selected != null) {
                    actions.addButton(
                            Component.translatable("dialog.reverie_dreams.gensokyo_compass.clear"),
                            Optional.of(actionBuilder.customAction(
                                    CustomClickActionRegistry.GENSOKYO_COMPASS_ACTION_KEY,
                                    tag(held.hand(), ACTION_CLEAR, finalPage, null)
                            ))
                    );
                }
                actions.addButton(Component.translatable("dialog.reverie_dreams.gensokyo_compass.close"), Optional.empty());
            });
        }).get().buildOrThrow().open(player);
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
        HandStack held = findCompass(player, hand);
        if (held == null) {
            player.sendSystemMessage(Component.translatable("dialog.reverie_dreams.gensokyo_compass.not_in_hand"), false);
            return;
        }

        String action = tag.getStringOr("action", "");
        int page = tag.getIntOr("page", 0);
        switch (action) {
            case ACTION_PAGE, ACTION_OPEN -> open(player, held.hand(), page);
            case ACTION_SELECT -> select(player, held, page, tag.getStringOr("landmark", ""));
            case ACTION_CLEAR -> clear(player, held, page);
            default -> {
                // Unknown or no-op action; do not trust it.
            }
        }
    }

    private static void select(ServerPlayer player, HandStack held, int page, String landmarkId) {
        Identifier id = Identifier.tryParse(landmarkId);
        Optional<GensokyoLandmark> landmark = GensokyoLandmarks.get(id);
        if (landmark.isEmpty()) {
            player.sendSystemMessage(Component.translatable(
                    "dialog.reverie_dreams.gensokyo_compass.unknown"), false);
            open(player, held.hand(), page);
            return;
        }

        held.stack().set(RDDataComponentTypes.GENSOKYO_COMPASS_TARGET.value(), id);
        player.getInventory().setChanged();
        SoundEventPlayUtils.playUISound(player, 1.0F, 1.0F);
        player.sendSystemMessage(Component.translatable(
                "dialog.reverie_dreams.gensokyo_compass.selected", landmark.get().displayName()), false);
        open(player, held.hand(), page);
    }

    private static void clear(ServerPlayer player, HandStack held, int page) {
        held.stack().remove(RDDataComponentTypes.GENSOKYO_COMPASS_TARGET.value());
        player.getInventory().setChanged();
        SoundEventPlayUtils.playUISound(player, 1.0F, 1.0F);
        player.sendSystemMessage(Component.translatable(
                "dialog.reverie_dreams.gensokyo_compass.cleared"), false);
        open(player, held.hand(), page);
    }

    private static Style clickableStyle(ChatFormatting color, CompoundTag tag) {
        return Style.EMPTY
                .withColor(color)
                .withUnderlined(true)
                .withClickEvent(new ClickEvent.Custom(
                        CustomClickActionRegistry.GENSOKYO_COMPASS_ACTION_KEY,
                        Optional.of(tag)
                ));
    }

    @Nullable
    private static HandStack findCompass(ServerPlayer player, InteractionHand preferred) {
        ItemStack stack = player.getItemInHand(preferred);
        if (stack.getItem() instanceof GensokyoCompassItem) {
            return new HandStack(preferred, stack);
        }

        InteractionHand other = preferred == InteractionHand.MAIN_HAND
                ? InteractionHand.OFF_HAND
                : InteractionHand.MAIN_HAND;
        stack = player.getItemInHand(other);
        if (stack.getItem() instanceof GensokyoCompassItem) {
            return new HandStack(other, stack);
        }
        return null;
    }

    private static CompoundTag tag(InteractionHand hand, String action, int page, @Nullable Identifier landmark) {
        CompoundTag tag = new CompoundTag();
        tag.putString("hand", hand == InteractionHand.OFF_HAND ? "off" : "main");
        tag.putString("action", action);
        tag.putInt("page", page);
        if (landmark != null) {
            tag.putString("landmark", landmark.toString());
        }
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
