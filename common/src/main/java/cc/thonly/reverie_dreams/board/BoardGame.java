package cc.thonly.reverie_dreams.board;

import com.google.gson.Gson;

import java.util.*;

/**
 * Persistent match state. UI selections and callbacks deliberately live elsewhere.
 */
public final class BoardGame {
    private static final Gson JSON = new Gson();

    public enum Difficulty {EASY, NORMAL, HARD}

    public BoardPosition position;
    public final UUID[] players = new UUID[2];
    public final String[] names = {"", ""};
    public final boolean[] ready = new boolean[2], scoreAccepted = new boolean[2];
    public final List<BoardPosition.Move> moves = new ArrayList<>();
    public UUID id = UUID.randomUUID();
    public long revision;
    public boolean started;
    public int aiSide; // 0 means human match; otherwise +1 or -1.
    public Difficulty difficulty = Difficulty.NORMAL;
    public int drawOffer, undoOffer;

    public BoardGame(BoardKind kind) {
        position = new BoardPosition(kind);
    }

    public int seat(UUID player) {
        for (int i = 0; i < 2; i++)
            if (player.equals(players[i]))
                return i;
        return -1;
    }

    public int side(UUID player) {
        int seat = seat(player);
        return seat < 0 ? 0 : seat == 0 ? 1 : -1;
    }

    public boolean join(UUID player, String name, int seat) {
        if (started || seat < 0 || seat > 1 || players[seat] != null || aiSide == (seat == 0 ? 1 : -1) || seat(player) >= 0)
            return false;
        players[seat] = player;
        names[seat] = name;
        ready[seat] = false;
        revision++;
        return true;
    }

    public void ready(UUID player) {
        int seat = seat(player);
        if (started || seat < 0)
            return;
        ready[seat] = !ready[seat];
        revision++;
        if ((players[0] != null || aiSide == 1) && (players[1] != null || aiSide == -1)
                && (ready[0] || aiSide == 1) && (ready[1] || aiSide == -1))
            started = true;
    }

    public boolean configureAI(UUID player, Difficulty level) {
        int seat = seat(player);
        if (started || seat < 0 || players[1 - seat] != null)
            return false;
        aiSide = seat == 0 ? -1 : 1;
        difficulty = level;
        names[1 - seat] = "AI";
        Arrays.fill(ready, false);
        revision++;
        return true;
    }

    public boolean startAgainstAI(UUID player, String name, Difficulty level) {
        if (started || level == null)
            return false;
        int seat = seat(player);
        if (seat < 0) {
            // A shortcut must not occupy or replace somebody else's lobby.
            if (players[0] != null || players[1] != null || aiSide != 0)
                return false;
            if (!join(player, name, 0))
                return false;
        } else if (players[1 - seat] != null)
            return false;
        if (!configureAI(player, level))
            return false;
        ready(player);
        return started;
    }

    public boolean rematchAgainstAI(UUID player) {
        if (seat(player) < 0 || aiSide == 0 || position.phase() != BoardPosition.Phase.FINISHED)
            return false;
        restart();
        ready(player);
        return started;
    }

    public boolean move(int side, BoardPosition.Move move) {
        if (!started || position.phase() != BoardPosition.Phase.PLAY || side != position.turn())
            return false;
        BoardPosition next = position.play(move);
        if (next == null)
            return false;
        position = next;
        moves.add(move);
        drawOffer = undoOffer = 0;
        Arrays.fill(scoreAccepted, false);
        revision++;
        return true;
    }

    public boolean undo() {
        if (moves.isEmpty())
            return false;
        // In an AI game undo the reply and the player's previous move together.
        int count = aiSide != 0 && position.turn() != aiSide && moves.size() > 1 ? 2 : 1;
        List<BoardPosition.Move> remaining = new ArrayList<>(moves.subList(0, moves.size() - count));
        BoardPosition restored = new BoardPosition(position.kind);
        for (var move : remaining) {
            if (restored.phase() == BoardPosition.Phase.SCORING)
                restored.resumeGo();
            restored = restored.play(move);
            if (restored == null)
                throw new IllegalStateException("Invalid move history");
        }
        if (restored.phase() == BoardPosition.Phase.SCORING)
            restored.resumeGo();
        position = restored;
        moves.clear();
        moves.addAll(remaining);
        drawOffer = undoOffer = 0;
        Arrays.fill(scoreAccepted, false);
        revision++;
        return true;
    }

    public void resign(int side) {
        if (started && position.phase() != BoardPosition.Phase.FINISHED && side != 0) {
            position.finish(BoardPosition.winner(-side));
            revision++;
        }
    }

    public boolean toggleDead(int square) {
        if (!position.toggleDead(square))
            return false;
        Arrays.fill(scoreAccepted, false);
        revision++;
        return true;
    }

    public void acceptScore(int side) {
        if (position.phase() != BoardPosition.Phase.SCORING || side == 0)
            return;
        scoreAccepted[side > 0 ? 0 : 1] = true;
        if (aiSide != 0)
            scoreAccepted[aiSide > 0 ? 0 : 1] = true;
        if (scoreAccepted[0] && scoreAccepted[1])
            position.finishScoring();
        revision++;
    }

    public void restart() {
        if (position.phase() != BoardPosition.Phase.FINISHED)
            return;
        position = new BoardPosition(position.kind);
        moves.clear();
        started = false;
        Arrays.fill(ready, false);
        Arrays.fill(scoreAccepted, false);
        drawOffer = undoOffer = 0;
        id = UUID.randomUUID();
        revision++;
    }

    private record Saved(BoardPosition.Saved position, UUID[] players, String[] names, boolean[] ready,
                         boolean[] scoreAccepted,
                         List<BoardPosition.Move> moves, UUID id, long revision, boolean started, int aiSide,
                         Difficulty difficulty, int drawOffer, int undoOffer) {
    }

    public String encode() {
        return JSON.toJson(new Saved(position.save(), players, names, ready, scoreAccepted, moves, id, revision, started, aiSide, difficulty, drawOffer, undoOffer));
    }

    public String renderEncode() {
        var s = position.save();
        BoardGame render = new BoardGame(position.kind);
        render.position = BoardPosition.restore(new BoardPosition.Saved(s.kind(), s.pieces(), s.turn(), s.castling(), s.enPassant(), s.halfmove(), s.ply(), s.passes(),
                s.lastFrom(), s.lastTo(), s.phase(), s.result(), List.of(), s.dead()));
        render.id = id;
        render.revision = revision;
        return render.encode();
    }

    public static BoardGame decode(String text) {
        if (text == null || text.length() > 1500000)
            throw new IllegalArgumentException("Board save too large");
        Saved s = JSON.fromJson(text, Saved.class);
        if (s == null || s.players == null || s.players.length != 2 || s.names == null || s.names.length != 2 || s.ready == null || s.ready.length != 2
                || s.scoreAccepted == null || s.scoreAccepted.length != 2 || s.moves == null || s.moves.size() > 2048 || Math.abs(s.aiSide) > 1 || s.id == null)
            throw new IllegalArgumentException("Invalid match save");
        BoardGame game = new BoardGame(s.position.kind());
        game.position = BoardPosition.restore(s.position);
        for (int i = 0; i < 2; i++) {
            game.players[i] = s.players[i];
            game.names[i] = Objects.requireNonNullElse(s.names[i], "");
            if (game.names[i].length() > 80)
                throw new IllegalArgumentException("Invalid name");
            game.ready[i] = s.ready[i];
            game.scoreAccepted[i] = s.scoreAccepted[i];
        }
        if (s.players[0] != null && s.players[0].equals(s.players[1]))
            throw new IllegalArgumentException("Duplicate player");
        if (s.aiSide != 0 && s.players[s.aiSide > 0 ? 0 : 1] != null)
            throw new IllegalArgumentException("AI seat occupied");
        for (var m : s.moves)
            if (m == null || m.from() < -1 || m.from() >= game.position.size() || m.to() < -1 || m.to() >= game.position.size())
                throw new IllegalArgumentException("Invalid move");
        game.moves.addAll(s.moves);
        game.id = s.id;
        game.revision = Math.max(0, s.revision);
        game.started = s.started;
        game.aiSide = s.aiSide;
        game.difficulty = Objects.requireNonNullElse(s.difficulty, Difficulty.NORMAL);
        game.drawOffer = s.drawOffer;
        game.undoOffer = s.undoOffer;
        return game;
    }
}
