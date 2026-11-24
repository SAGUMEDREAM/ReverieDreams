package cc.thonly.reverie_dreams.server.dialog.music;

import cc.thonly.reverie_dreams.block.entity.MusicBlockEntity;
import cc.thonly.reverie_dreams.item.prop.MusicalInstrumentItem;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponentTypes;
import cc.thonly.reverie_dreams.server.CustomClickActionRegistry;
import cc.thonly.reverie_dreams.server.dialog.ActionBuilder;
import cc.thonly.reverie_dreams.server.dialog.DialogBuilder;
import cc.thonly.reverie_dreams.util.advancements.SimpleTriggerFactory;
import cc.thonly.reverie_dreams.util.advancements.SimpleTriggerKeys;
import cc.thonly.reverie_dreams.util.nbs.NotaUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ServerboundCustomClickActionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * NBS 音乐选择 Dialog。
 *
 * <p>乐器和音乐盒共用同一套分页界面；页面按钮和歌曲按钮都通过
 * {@link CustomClickActionRegistry} 的自定义点击动作回到服务端处理。</p>
 */
public final class MusicSelectionDialog {
    public static final int PAGE_SIZE = 8;

    private MusicSelectionDialog() {
    }

    public static void openForInstrument(ServerPlayer player, InteractionHand hand, int page) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(stack.getItem() instanceof MusicalInstrumentItem)) {
            InteractionHand fallback = player.getMainHandItem().getItem() instanceof MusicalInstrumentItem
                    ? InteractionHand.MAIN_HAND
                    : InteractionHand.OFF_HAND;
            stack = player.getItemInHand(fallback);
            if (!(stack.getItem() instanceof MusicalInstrumentItem)) {
                player.sendSystemMessage(Component.translatable("dialog.music.no_instrument"), false);
                return;
            }
            hand = fallback;
        }
        open(player, Target.instrument(hand), page);
    }

    private static final int MAX_BLOCK_INTERACTION_DISTANCE = 8;

    private static boolean canUseBlock(ServerPlayer player, BlockPos pos) {
        return player.level().hasChunkAt(pos)
                && player.blockPosition().distSqr(pos) <= (long) MAX_BLOCK_INTERACTION_DISTANCE * MAX_BLOCK_INTERACTION_DISTANCE;
    }

    public static void openForBlock(ServerPlayer player, BlockPos pos, int page) {
        // 客户端自带的坐标不可信：区块未加载时 getBlockEntity 会加载区块。
        if (!canUseBlock(player, pos)
                || !(player.level().getBlockEntity(pos) instanceof MusicBlockEntity)) {
            player.sendSystemMessage(Component.translatable("dialog.music.block_missing"), false);
            return;
        }
        open(player, Target.block(pos), page);
    }

    private static void open(ServerPlayer player, Target target, int page) {
        List<String> files = NotaUtils.getFileNames();
        String current = currentSelection(player, target);

        int pageCount = Math.max(1, (files.size() + PAGE_SIZE - 1) / PAGE_SIZE);
        page = Math.max(0, Math.min(page, pageCount - 1));

        int start = page * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, files.size());
        int finalPage = page;

        DialogBuilder.builder(builder -> {
            builder.columns(2);
            builder.common(common -> {
                common.title(Component.translatable("dialog.title.music"));
                common.setCanCloseWithEscape(true);
                common.setPause(false);

                if (files.isEmpty()) {
                    common.addTextBody(Component.translatable("dialog.music.empty"), 280);
                } else {
                    common.addTextBody(Component.translatable("dialog.music.page", finalPage + 1, pageCount, files.size()), 280);
                    common.addTextBody(
                            Component.translatable(
                                    "dialog.music.current",
                                    current.isEmpty() ? "-" : displayName(current)
                            ),
                            280
                    );
                    common.addTextBody(Component.translatable("dialog.music.hint"), 280);
                }
            });

            builder.actions(actions -> {
                ActionBuilder actionBuilder = actions.actionBuilder();

                for (int i = start; i < end; i++) {
                    String file = files.get(i);
                    CompoundTag tag = targetTag(target, finalPage);
                    tag.putString("file", file);

                    Component label = Component.literal(
                            (file.equals(current) ? "▶ " : "") + displayName(file)
                    );
                    actions.addButton(
                            label,
                            Optional.of(actionBuilder.customAction(CustomClickActionRegistry.MUSIC_SELECT_KEY, tag))
                    );
                }

                if (finalPage > 0) {
                    CompoundTag tag = targetTag(target, finalPage - 1);
                    actions.addButton(
                            Component.translatable("dialog.music.prev"),
                            Optional.of(actionBuilder.customAction(CustomClickActionRegistry.MUSIC_PAGE_KEY, tag))
                    );
                }
                if (finalPage < pageCount - 1) {
                    CompoundTag tag = targetTag(target, finalPage + 1);
                    actions.addButton(
                            Component.translatable("dialog.music.next"),
                            Optional.of(actionBuilder.customAction(CustomClickActionRegistry.MUSIC_PAGE_KEY, tag))
                    );
                }

                actions.addButton(Component.translatable("dialog.text.exit"), Optional.empty());
            });
        }).get().buildOrThrow().open(player);
    }

    public static void handleSelect(ServerPlayer player, ServerboundCustomClickActionPacket packet) {
        if (player == null) {
            return;
        }
        CompoundTag tag = payload(packet);
        if (tag == null) {
            return;
        }

        String file = tag.getStringOr("file", "");
        Target target = Target.read(tag);
        if (file.isEmpty() || target == null) {
            return;
        }

        // 不信任客户端 payload，避免用 ../ 之类的路径读取任意文件
        if (!NotaUtils.getFileNames().contains(file)) {
            player.sendSystemMessage(Component.translatable("dialog.music.empty"), false);
            return;
        }

        if (target.block) {
            if (!canUseBlock(player, target.pos)
                    || !(player.level().getBlockEntity(target.pos) instanceof MusicBlockEntity blockEntity)) {
                player.sendSystemMessage(Component.translatable("dialog.music.block_missing"), false);
                return;
            }

            blockEntity.setSelect(file);
            blockEntity.setChanged();
            // 避免已通电的音乐盒在下一 tick 的 isFirst 逻辑里重复启动同一首歌
            blockEntity.isFirst = false;

            if (player.level().hasNeighborSignal(target.pos)) {
                blockEntity.startPlayback();
                player.sendSystemMessage(Component.translatable("item.reverie_dreams.music.switch_music", file), false);
            } else {
                player.sendSystemMessage(Component.translatable("dialog.music.waiting_redstone", file), false);
            }
            return;
        }

        InteractionHand hand = target.hand;
        ItemStack stack = player.getItemInHand(hand);
        if (!(stack.getItem() instanceof MusicalInstrumentItem)) {
            hand = InteractionHand.MAIN_HAND;
            stack = player.getItemInHand(hand);
            if (!(stack.getItem() instanceof MusicalInstrumentItem)) {
                hand = InteractionHand.OFF_HAND;
                stack = player.getItemInHand(hand);
            }
        }

        if (!(stack.getItem() instanceof MusicalInstrumentItem instrument)) {
            player.sendSystemMessage(Component.translatable("dialog.music.no_instrument"), false);
            return;
        }

        stack.set(RDDataComponentTypes.PLAYING_MUSIC.value(), file);
        NoteBlockInstrument noteType = stack.getOrDefault(
                RDDataComponentTypes.NOTE_TYPE.value(),
                NoteBlockInstrument.PLING
        );

        NotaUtils.play(player, file, noteType);
        instrument.playForMaidEntity(player.level(), player, file);

        player.sendSystemMessage(
                Component.translatable(
                        "item.reverie_dreams.music.playing_music",
                        file,
                        noteType.getSerializedName()
                ),
                false
        );
        SimpleTriggerFactory.create(SimpleTriggerKeys.USE_MUSICAL_INSTRUMENTS).trigger(player);
    }

    public static void handlePage(ServerPlayer player, ServerboundCustomClickActionPacket packet) {
        if (player == null) {
            return;
        }
        CompoundTag tag = payload(packet);
        if (tag == null) {
            return;
        }

        Target target = Target.read(tag);
        if (target == null) {
            return;
        }

        int page = tag.getIntOr("page", 0);
        if (target.block) {
            openForBlock(player, target.pos, page);
        } else {
            openForInstrument(player, target.hand, page);
        }
    }

    @Nullable
    private static CompoundTag payload(ServerboundCustomClickActionPacket packet) {
        Optional<Tag> payload = packet.payload();
        return payload.isPresent() && payload.get() instanceof CompoundTag tag ? tag : null;
    }

    private static CompoundTag targetTag(Target target, int page) {
        CompoundTag tag = new CompoundTag();
        target.write(tag);
        tag.putInt("page", page);
        return tag;
    }

    private static String currentSelection(ServerPlayer player, Target target) {
        if (target.block) {
            if (!player.level().hasChunkAt(target.pos)) {
                return "";
            }
            if (player.level().getBlockEntity(target.pos) instanceof MusicBlockEntity blockEntity) {
                String select = blockEntity.getSelect();
                return select == null ? "" : select;
            }
            return "";
        }

        String playing = player.getItemInHand(target.hand).get(RDDataComponentTypes.PLAYING_MUSIC.value());
        return playing == null ? "" : playing;
    }

    private static String displayName(String file) {
        if (file == null || file.isEmpty()) {
            return "-";
        }
        String name = file;
        if (name.toLowerCase(Locale.ROOT).endsWith(".nbs")) {
            name = name.substring(0, name.length() - 4);
        }
        if (name.length() > 42) {
            name = name.substring(0, 41) + "…";
        }
        return name;
    }

    private record Target(boolean block, InteractionHand hand, BlockPos pos) {
        private static Target instrument(InteractionHand hand) {
            return new Target(false, hand, BlockPos.ZERO);
        }

        private static Target block(BlockPos pos) {
            return new Target(true, InteractionHand.MAIN_HAND, pos);
        }

        private void write(CompoundTag tag) {
            if (this.block) {
                tag.putString("kind", "block");
                tag.putLong("pos", this.pos.asLong());
            } else {
                tag.putString("kind", "instrument");
                tag.putString("hand", this.hand == InteractionHand.OFF_HAND ? "off" : "main");
            }
        }

        @Nullable
        private static Target read(CompoundTag tag) {
            String kind = tag.getStringOr("kind", "");
            if ("block".equals(kind)) {
                return block(BlockPos.of(tag.getLongOr("pos", 0L)));
            }
            if ("instrument".equals(kind)) {
                InteractionHand hand = "off".equals(tag.getStringOr("hand", "main"))
                        ? InteractionHand.OFF_HAND
                        : InteractionHand.MAIN_HAND;
                return instrument(hand);
            }
            return null;
        }
    }
}
