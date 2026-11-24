package cc.thonly.reverie_dreams.board;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.block.decor.*;
import cc.thonly.reverie_dreams.block.entity.BoardGameBlockEntity;
import cc.thonly.reverie_dreams.server.dialog.DialogBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.*;
import net.minecraft.network.protocol.common.ServerboundCustomClickActionPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dialog.*;
import net.minecraft.server.dialog.action.StaticAction;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;

import java.util.*;

import net.minecraft.network.protocol.common.ClientboundClearDialogPacket;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;

/**
 * Server-owned sessions. Every action is checked again against the live block and match revision.
 */
public final class BoardGames {
    public static final Identifier ACTION = ReverieDreams.id("board/action");
    private static final Map<UUID, View> VIEWS = new HashMap<>();

    private static final class View extends BoardViewState {
        final String token = UUID.randomUUID().toString();
        final BoardGameBlockEntity host;
        long lastAction = -1;
        boolean playersAvailable;

        View(BoardGameBlockEntity host) {
            this.host = host;
        }
    }

    private BoardGames() {
    }

    public static boolean isBoard(ItemStack stack) {
        return stack.getItem() instanceof BlockItem item && item.getBlock() instanceof BoardGameBlock;
    }

    public static InteractionResult attach(Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(hand);
        if (!isBoard(stack) || hit.getDirection() != Direction.UP || player.isSpectator() || !player.mayUseItemAt(pos, Direction.UP, stack))
            return InteractionResult.FAIL;
        if (!(level.getBlockEntity(pos) instanceof BoardGameBlockEntity be) || level.getBlockState(pos).getValue(ChabudaiBlock.HAS_TEA_SET) || be.game() != null)
            return InteractionResult.FAIL;
        if (!level.isClientSide()) {
            BoardKind kind = ((BoardGameBlock) ((BlockItem) stack.getItem()).getBlock()).kind;
            if (!be.attach(kind, player.getDirection().getOpposite()))
                return InteractionResult.FAIL;
            if (!player.hasInfiniteMaterials())
                stack.shrink(1);
        }
        return InteractionResult.SUCCESS;
    }

    public static InteractionResult openAt(Level level, BlockPos pos, Player player) {
        if (!(level.getBlockEntity(pos) instanceof BoardGameBlockEntity be) || be.game() == null)
            return InteractionResult.PASS;
        if (player instanceof ServerPlayer server && valid(server, be)) {
            long viewers = VIEWS.values().stream().filter(v -> v.host == be).count();
            if (viewers >= 32 && !VIEWS.containsKey(player.getUUID()))
                return InteractionResult.FAIL;
            View view = new View(be);
            VIEWS.put(player.getUUID(), view);
            show(server, view);
        }
        return InteractionResult.SUCCESS;
    }

    private static boolean valid(ServerPlayer player, BoardGameBlockEntity be) {
        return player != null && player.isAlive() && !player.hasDisconnected() && !be.isRemoved() && be.game() != null && player.level() == be.getLevel()
                && player.distanceToSqr(be.getBlockPos().getCenter()) <= 64 && be.getLevel().hasChunkAt(be.getBlockPos())
                && be.getLevel().getBlockEntity(be.getBlockPos()) == be;
    }

    public static boolean available(BoardGameBlockEntity be) {
        var game = be.game();
        if (game == null || !(be.getLevel() instanceof ServerLevel level))
            return false;
        for (int seat = 0; seat < 2; seat++) {
            int side = seat == 0 ? 1 : -1;
            if (game.aiSide == side)
                continue;
            ServerPlayer p = game.players[seat] == null ? null : level.getServer().getPlayerList().getPlayer(game.players[seat]);
            if (p == null || !p.isAlive() || p.level() != level || p.distanceToSqr(be.getBlockPos().getCenter()) > 256)
                return false;
        }
        return true;
    }

    public static void handle(ServerPlayer player, ServerboundCustomClickActionPacket packet) {
        if (player == null || !(packet.payload().orElse(null) instanceof CompoundTag tag))
            return;
        View view = VIEWS.get(player.getUUID());
        if (view == null || !view.token.equals(tag.getString("session").orElse("")))
            return;
        String action = tag.getString("action").orElse("");
        if (action.equals("close")) {
            VIEWS.remove(player.getUUID());
            player.connection.send(ClientboundClearDialogPacket.INSTANCE);
            return;
        }
        if (!valid(player, view.host)) {
            VIEWS.remove(player.getUUID());
            return;
        }
        BoardGame game = view.host.game();
        long now = player.level().getGameTime();
        if (view.lastAction == now)
            return;
        view.lastAction = now;
        if (!game.id.toString().equals(tag.getString("match").orElse("")) || game.revision != tag.getLongOr("revision", -1)) {
            view.reset();
            feedback(player, view, "updated");
            return;
        }
        int side = game.side(player.getUUID()), seat = game.seat(player.getUUID());
        if (!action.equals("resign"))
            view.cancelResign();
        if (action.equals("refresh")) {
            show(player, view);
            return;
        }
        if (action.equals("cancel")) {
            show(player, view);
            return;
        }
        if (player.isSpectator())
            return;
        boolean changed = false;
        String failure = "illegal";
        switch (action) {
            case "join" ->
                    changed = game.join(player.getUUID(), player.getGameProfile().name(), tag.getIntOr("value", -1));
            case "ready" -> {
                if (seat >= 0 && !game.started) {
                    game.ready(player.getUUID());
                    changed = true;
                }
            }
            case "leave" -> {
                if (seat >= 0 && !game.started) {
                    game.players[seat] = null;
                    game.names[seat] = "";
                    game.ready[seat] = false;
                    if (game.aiSide != 0)
                        game.names[game.aiSide > 0 ? 0 : 1] = "";
                    game.aiSide = 0;
                    game.revision++;
                    changed = true;
                }
            }
            case "ai" -> {
                int value = tag.getIntOr("value", -1);
                if (value >= 0 && value < 3)
                    changed = game.configureAI(player.getUUID(), BoardGame.Difficulty.values()[value]);
            }
            case "quick_ai" -> {
                int value = tag.getIntOr("value", -1);
                if (value >= 0 && value < 3)
                    changed = game.startAgainstAI(player.getUUID(), player.getGameProfile().name(), BoardGame.Difficulty.values()[value]);
            }
            case "human" -> {
                if (seat >= 0 && !game.started && game.aiSide != 0) {
                    game.names[game.aiSide > 0 ? 0 : 1] = "";
                    game.aiSide = 0;
                    game.revision++;
                    changed = true;
                }
            }
            case "cell" -> {
                int square = tag.getIntOr("value", -1);
                if (square < 0 || square >= game.position.size())
                    return;
                if (side == 0) {
                    feedback(player, view, "not_seated");
                    return;
                }
                if (!game.started) {
                    feedback(player, view, "not_started");
                    return;
                }
                if (game.position.phase() == BoardPosition.Phase.SCORING)
                    changed = game.toggleDead(square);
                else if (game.position.phase() == BoardPosition.Phase.PLAY) {
                    if (side != game.position.turn()) {
                        feedback(player, view, "not_turn");
                        return;
                    }
                    if (!available(view.host)) {
                        feedback(player, view, "paused");
                        return;
                    }
                    if (game.position.kind == BoardKind.GO) {
                        changed = game.move(side, new BoardPosition.Move(-1, square));
                        failure = "illegal_go";
                    } else if (game.position.at(square) * side > 0) {
                        view.select(square);
                        if (view.selected >= 0 && game.position.legalFrom(square).isEmpty())
                            feedback(player, view, "blocked");
                        else
                            show(player, view);
                        return;
                    } else if (view.selected >= 0) {
                        var choices = game.position.legalFrom(view.selected).stream().filter(m -> m.to() == square).toList();
                        if (choices.size() > 1) {
                            view.promotionFrom = view.selected;
                            view.promotionTo = square;
                            show(player, view);
                            return;
                        }
                        if (!choices.isEmpty())
                            changed = game.move(side, choices.getFirst());
                    } else
                        failure = "select_first";
                }
            }
            case "promote" -> {
                int piece = tag.getIntOr("value", 0);
                if (side == game.position.turn() && view.promotionFrom >= 0 && available(view.host))
                    changed = game.move(side, new BoardPosition.Move(view.promotionFrom, view.promotionTo, piece));
            }
            case "pass" -> {
                if (side != 0 && available(view.host))
                    changed = game.move(side, BoardPosition.Move.pass());
            }
            case "resign" -> {
                if (side != 0 && game.started && game.position.phase() != BoardPosition.Phase.FINISHED) {
                    if (tag.getIntOr("value", 0) != 1) {
                        view.requestResign(game.revision);
                        show(player, view);
                        return;
                    }
                    if (view.confirmResign(game.revision)) {
                        game.resign(side);
                        changed = true;
                    }
                }
            }
            case "draw" -> {
                if (side != 0 && game.started && game.position.phase() != BoardPosition.Phase.FINISHED) {
                    if (game.drawOffer == -side || game.aiSide != 0) {
                        game.position.finish("draw");
                    } else
                        game.drawOffer = side;
                    game.revision++;
                    changed = true;
                }
            }
            case "undo" -> {
                if (side != 0 && game.started && !game.moves.isEmpty()) {
                    if (game.undoOffer == -side || game.aiSide != 0)
                        changed = game.undo();
                    else {
                        game.undoOffer = side;
                        game.revision++;
                        changed = true;
                    }
                }
            }
            case "score" -> {
                if (side != 0 && game.position.phase() == BoardPosition.Phase.SCORING) {
                    game.acceptScore(side);
                    changed = true;
                }
            }
            case "resume" -> {
                if (side != 0 && game.position.phase() == BoardPosition.Phase.SCORING) {
                    game.position.resumeGo();
                    Arrays.fill(game.scoreAccepted, false);
                    game.revision++;
                    changed = true;
                }
            }
            case "restart" -> {
                if (side != 0 && game.position.phase() == BoardPosition.Phase.FINISHED) {
                    game.restart();
                    changed = true;
                }
            }
            case "rematch_ai" -> changed = game.rematchAgainstAI(player.getUUID());
        }
        if (changed) {
            view.reset();
            view.host.publish();
        } else
            feedback(player, view, failure);
    }

    private static void feedback(ServerPlayer player, View view, String key) {
        player.connection.send(new ClientboundSetActionBarTextPacket(Component.translatable("board.reverie_dreams.feedback." + key)));
        show(player, view);
    }

    private static CompoundTag payload(View view, String action, int value) {
        CompoundTag tag = new CompoundTag();
        tag.putString("session", view.token);
        tag.putString("action", action);
        tag.putInt("value", value);
        if (view.host.game() != null) {
            tag.putString("match", view.host.game().id.toString());
            tag.putLong("revision", view.host.game().revision);
        }
        return tag;
    }

    private static ClickEvent.Custom click(View view, String action, int value) {
        return new ClickEvent.Custom(ACTION, Optional.of(payload(view, action, value)));
    }

    private static Component link(View view, String key, String action, int value) {
        return Component.literal("[ ").append(Component.translatable("board.reverie_dreams." + key)).append(" ] ")
                .withStyle(s -> s.withColor(ChatFormatting.AQUA).withClickEvent(click(view, action, value)));
    }

    private static Component sideName(BoardGame game, int side) {
        return Component.translatable("board.reverie_dreams.side." + (game.position.kind == BoardKind.XIANGQI ? (side > 0 ? "red" : "black") :
                game.position.kind == BoardKind.CHESS ? (side > 0 ? "white" : "black") : (side > 0 ? "black" : "white")));
    }

    private static void show(ServerPlayer player, View view) {
        view.playersAvailable = available(view.host);
        player.openDialog(Holder.direct(dialog(player, view)));
    }

    private static Dialog dialog(ServerPlayer player, View view) {
        BoardGame game = view.host.game();
        BoardPosition board = game.position;
        int own = game.side(player.getUUID());
        boolean participant = own != 0 && !player.isSpectator(), confirming = view.resignPending(game.revision);
        boolean canMove = participant && game.started && board.phase() == BoardPosition.Phase.PLAY && own == board.turn() && view.playersAvailable && !confirming;
        List<BoardPosition.Move> legal = !canMove || view.selected < 0 ? List.of() : board.legalFrom(view.selected);
        return DialogBuilder.builder(b -> {
            b.key(ReverieDreams.id("board/match"));
            b.columns(board.kind.width);
            b.common(c -> {
                c.title(Component.translatable(board.kind.key())).setPause(false).setCanCloseWithEscape(true).setAfterAction(DialogAction.NONE);
                // Each separate dialog body adds a native 10px gap. Keep the summary in
                // one text widget so the full board stays visible after every update.
                MutableComponent summary = Component.empty();
                for (int seat = 0; seat < 2; seat++) {
                    if (seat != 0)
                        summary.append("  |  ");
                    Component name = game.aiSide == (seat == 0 ? 1 : -1) ? Component.translatable("board.reverie_dreams.ai_name", Component.translatable("board.reverie_dreams." + game.difficulty.name().toLowerCase(Locale.ROOT))) :
                            game.names[seat].isEmpty() ? Component.translatable("board.reverie_dreams.empty") : Component.literal(game.names[seat]);
                    if (own == (seat == 0 ? 1 : -1))
                        name = name.copy().withStyle(s -> s.withColor(ChatFormatting.GOLD)
                                .withHoverEvent(new HoverEvent.ShowText(Component.translatable("board.reverie_dreams.your_seat"))));
                    summary.append(sideName(game, seat == 0 ? 1 : -1).copy().append(": ").append(name)
                            .append(!game.started && game.ready[seat] ? " ✓" : ""));
                }
                Component rules = Component.translatable("board.reverie_dreams.rules." + board.kind.name().toLowerCase(Locale.ROOT));
                Component help = rules.copy().append("\n").append(Component.translatable("board.reverie_dreams.legend"));
                if (!game.started)
                    help = help.copy().append("\n").append(Component.translatable("board.reverie_dreams.lobby"));
                Component helpText = help;
                Component toolbar = Component.empty().append(link(view, "refresh", "refresh", 0)).append(Component.translatable("board.reverie_dreams.help")
                        .withStyle(s -> s.withColor(ChatFormatting.GRAY).withUnderlined(true).withHoverEvent(new HoverEvent.ShowText(helpText))));
                if (!game.started) {
                    MutableComponent join = Component.empty();
                    if (!player.isSpectator() && own == 0)
                        for (int seat = 0; seat < 2; seat++) {
                            if (game.players[seat] == null && game.aiSide != (seat == 0 ? 1 : -1))
                                join.append(link(view, seat == 0 ? "join_first" : "join_second", "join", seat));
                        }
                    if (participant)
                        join.append(link(view, game.ready[own > 0 ? 0 : 1] ? "unready" : "ready_short", "ready", 0)).append(link(view, "leave", "leave", 0));
                    summary.append("\n").append(join).append(toolbar);
                    boolean aiAvailable = !player.isSpectator() && (participant ? game.players[own > 0 ? 1 : 0] == null : game.players[0] == null && game.players[1] == null && game.aiSide == 0);
                    if (aiAvailable) {
                        summary.append("\n").append(Component.translatable("board.reverie_dreams.vs_ai"));
                        String[] levels = {"easy", "normal", "hard"};
                        for (int i = 0; i < levels.length; i++)
                            summary.append(link(view, levels[i], "quick_ai", i).copy()
                                    .withStyle(s -> s.withHoverEvent(new HoverEvent.ShowText(Component.translatable("board.reverie_dreams.quick_ai_help")))));
                        if (participant && game.aiSide != 0)
                            summary.append(link(view, "human", "human", 0));
                    } else if (participant)
                        summary.append("\n").append(Component.translatable(game.ready[own > 0 ? 0 : 1] ? "board.reverie_dreams.waiting_ready" : "board.reverie_dreams.ready_hint").withStyle(ChatFormatting.GRAY));
                } else if (board.phase() == BoardPosition.Phase.FINISHED) {
                    summary.append("\n").append(board.result().equals("draw") ? Component.translatable("board.reverie_dreams.draw") :
                                    Component.translatable("board.reverie_dreams.winner", sideName(game, board.result().equals("first") ? 1 : -1)))
                            .append("  ").append(toolbar);
                    if (participant) {
                        summary.append("\n").append(link(view, "restart", "restart", 0));
                        if (game.aiSide != 0)
                            summary.append(link(view, "rematch_ai", "rematch_ai", 0));
                    }
                } else {
                    summary.append("\n");
                    if (board.phase() == BoardPosition.Phase.SCORING) {
                        double[] score = board.score();
                        summary.append(Component.translatable("board.reverie_dreams.scoring_short", score[0], score[1])
                                .withStyle(s -> s.withHoverEvent(new HoverEvent.ShowText(Component.translatable("board.reverie_dreams.scoring", score[0], score[1])))));
                    } else if (view.playersAvailable) {
                        if (view.promotionFrom >= 0)
                            summary.append(Component.translatable("board.reverie_dreams.choose_promotion"));
                        else if (!participant)
                            summary.append(Component.translatable("board.reverie_dreams.watching_turn", sideName(game, board.turn())));
                        else if (board.turn() == game.aiSide)
                            summary.append(Component.translatable("board.reverie_dreams.waiting_ai"));
                        else if (own == board.turn()) {
                            summary.append(Component.translatable("board.reverie_dreams.your_turn").withStyle(ChatFormatting.GREEN));
                            if (view.selected >= 0)
                                summary.append(" · ").append(Component.translatable("board.reverie_dreams.selected", board.coordinate(view.selected)));
                            else
                                summary.append(" · ").append(Component.translatable(board.kind == BoardKind.GO ? "board.reverie_dreams.place_stone" : "board.reverie_dreams.select_piece"));
                        } else
                            summary.append(Component.translatable("board.reverie_dreams.waiting_turn", sideName(game, board.turn())));
                        if (board.kind != BoardKind.GO && board.inCheck(board.turn()))
                            summary.append(" · ").append(Component.translatable("board.reverie_dreams.in_check").withStyle(ChatFormatting.RED));
                    } else
                        summary.append(Component.translatable("board.reverie_dreams.paused_short", sideName(game, board.turn()))
                                .withStyle(s -> s.withHoverEvent(new HoverEvent.ShowText(Component.translatable("board.reverie_dreams.paused", sideName(game, board.turn()))))));
                    summary.append("  ").append(toolbar);
                    if (participant && board.phase() == BoardPosition.Phase.SCORING && !confirming)
                        summary.append("\n")
                                .append(game.scoreAccepted[own > 0 ? 0 : 1] ? Component.translatable("board.reverie_dreams.score_wait").withStyle(ChatFormatting.GREEN) : link(view, "confirm_score", "score", 0))
                                .append("  ").append(link(view, "resume", "resume", 0));
                    if (participant) {
                        MutableComponent controls = Component.empty();
                        if (confirming)
                            controls.append(link(view, "confirm_resign", "resign", 1).copy().withStyle(ChatFormatting.RED)).append(link(view, "cancel", "cancel", 0));
                        else {
                            controls.append(link(view, "resign", "resign", 0)).append(link(view, game.drawOffer == -own ? "accept_draw" : "offer_draw", "draw", 0));
                            if (!game.moves.isEmpty())
                                controls.append(link(view, game.undoOffer == -own ? "accept_undo" : "offer_undo", "undo", 0));
                            if (board.kind == BoardKind.GO && board.phase() == BoardPosition.Phase.PLAY && canMove)
                                controls.append(link(view, "pass", "pass", 0));
                        }
                        summary.append("\n").append(controls);
                    }
                }
                if (view.promotionFrom >= 0) {
                    summary.append("\n");
                    for (int p : new int[]{5, 4, 3, 2})
                        summary.append(link(view, "promote." + p, "promote", p));
                }
                c.addTextBody(summary, 360);
            });
            b.actions(a -> {
                for (int row = 0; row < board.kind.height; row++)
                    for (int col = 0; col < board.kind.width; col++) {
                        int square = own < 0 ? board.size() - 1 - (row * board.kind.width + col) : row * board.kind.width + col;
                        boolean possible = legal.stream().anyMatch(m -> m.to() == square);
                        boolean goStone = board.kind == BoardKind.GO && board.at(square) != 0;
                        ChatFormatting color = goStone ? (board.at(square) > 0 ? ChatFormatting.BLACK : ChatFormatting.WHITE) :
                                square == view.selected ? ChatFormatting.GOLD : board.dead(square) ? ChatFormatting.DARK_GRAY : possible ? ChatFormatting.GREEN :
                                        square == board.lastTo() ? ChatFormatting.YELLOW : board.at(square) > 0 ? (board.kind == BoardKind.XIANGQI ? ChatFormatting.RED : ChatFormatting.WHITE) : ChatFormatting.GRAY;
                        Component label = Component.literal(possible && board.at(square) == 0 ? "+" : board.pieceLabel(square)).withStyle(color);
                        if (square == view.selected)
                            label = label.copy().withStyle(ChatFormatting.BOLD);
                        Component tooltip = Component.literal(board.coordinate(square));
                        if (goStone) {
                            // Keep black/white identity even for the last move and marked dead groups.
                            if (square == board.lastTo())
                                label = label.copy().withStyle(ChatFormatting.BOLD, ChatFormatting.UNDERLINE);
                            if (board.dead(square))
                                label = label.copy().withStyle(ChatFormatting.STRIKETHROUGH);
                            tooltip = tooltip.copy().append(" · ").append(sideName(game, board.at(square) > 0 ? 1 : -1));
                        }
                        if (square == board.lastTo())
                            tooltip = tooltip.copy().append("\n").append(Component.translatable("board.reverie_dreams.last_to"));
                        if (square == board.lastFrom())
                            tooltip = tooltip.copy().append("\n").append(Component.translatable("board.reverie_dreams.last_from"));
                        if (board.dead(square))
                            tooltip = tooltip.copy().append("\n").append(Component.translatable("board.reverie_dreams.marked_dead"));
                        if (possible)
                            tooltip = tooltip.copy().append("\n").append(Component.translatable("board.reverie_dreams.move_to", board.coordinate(square)));
                        boolean actionable = participant && !confirming && (board.phase() == BoardPosition.Phase.SCORING ? board.at(square) != 0 :
                                canMove && (board.kind == BoardKind.GO ? board.at(square) == 0 : board.at(square) * own > 0 || possible));
                        a.addButton(label, Optional.of(tooltip), 24, actionable ? Optional.of(new StaticAction(click(view, "cell", square))) : Optional.empty());
                    }
            });
            b.exitAction(Component.translatable("board.reverie_dreams.close"), 180, Optional.of(new StaticAction(click(view, "close", 0))));
        }).get().buildOrThrow().get().value();
    }

    public static void changed(BoardGameBlockEntity be) {
        if (!(be.getLevel() instanceof ServerLevel level))
            return;
        for (var entry : new ArrayList<>(VIEWS.entrySet()))
            if (entry.getValue().host == be) {
                ServerPlayer player = level.getServer().getPlayerList().getPlayer(entry.getKey());
                if (valid(player, be)) {
                    entry.getValue().reset();
                    show(player, entry.getValue());
                } else
                    VIEWS.remove(entry.getKey());
            }
    }

    public static void removed(BoardGameBlockEntity be) {
        VIEWS.entrySet().removeIf(e -> e.getValue().host == be);
        BoardAi.cancel(be);
    }

    public static void tick(BoardGameBlockEntity be) {
        if (be.getLevel() instanceof ServerLevel level)
            for (var entry : new ArrayList<>(VIEWS.entrySet()))
                if (entry.getValue().host == be) {
                    ServerPlayer player = level.getServer().getPlayerList().getPlayer(entry.getKey());
                    if (!valid(player, be))
                        VIEWS.remove(entry.getKey());
                    else if (level.getGameTime() % 10 == 0 && entry.getValue().playersAvailable != available(be)) {
                        entry.getValue().reset();
                        show(player, entry.getValue());
                    }
                }
        BoardAi.tick(be);
    }

    public static void clear(MinecraftServer server) {
        VIEWS.entrySet().removeIf(e -> e.getValue().host.getLevel() != null && e.getValue().host.getLevel().getServer() == server);
        BoardAi.clear(server);
    }
}
