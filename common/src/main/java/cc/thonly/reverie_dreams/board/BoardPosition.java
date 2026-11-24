package cc.thonly.reverie_dreams.board;

import java.util.*;

/**
 * Pure rules: coordinates never depend on a viewer's orientation. Positive pieces move first.
 */
public final class BoardPosition {
    public static final int PAWN = 1, KNIGHT = 2, BISHOP = 3, ROOK = 4, QUEEN = 5, KING = 6, ADVISOR = 7, CANNON = 8;

    public enum Phase {PLAY, SCORING, FINISHED}

    public record Move(int from, int to, int promotion) {
        public Move(int from, int to) {
            this(from, to, 0);
        }

        public static Move pass() {
            return new Move(-1, -1);
        }
    }

    public final BoardKind kind;
    private final int[] board;
    private int turn = 1, castling = 15, enPassant = -1, halfmove, ply, passes, lastFrom = -1, lastTo = -1;
    private Phase phase = Phase.PLAY;
    private String result = "";
    private final List<String> repetitions = new ArrayList<>();
    private final BitSet dead = new BitSet();

    public BoardPosition(BoardKind kind) {
        this.kind = Objects.requireNonNull(kind);
        board = new int[kind.width * kind.height];
        if (kind == BoardKind.CHESS) {
            int[] back = {ROOK, KNIGHT, BISHOP, QUEEN, KING, BISHOP, KNIGHT, ROOK};
            for (int x = 0; x < 8; x++) {
                put(x, 0, -back[x]);
                put(x, 1, -PAWN);
                put(x, 6, PAWN);
                put(x, 7, back[x]);
            }
        } else if (kind == BoardKind.XIANGQI) {
            int[] back = {ROOK, KNIGHT, BISHOP, ADVISOR, KING, ADVISOR, BISHOP, KNIGHT, ROOK};
            for (int x = 0; x < 9; x++) {
                put(x, 0, -back[x]);
                put(x, 9, back[x]);
            }
            for (int x : new int[]{1, 7}) {
                put(x, 2, -CANNON);
                put(x, 7, CANNON);
            }
            for (int x = 0; x < 9; x += 2) {
                put(x, 3, -PAWN);
                put(x, 6, PAWN);
            }
        }
        repetitions.add(key());
    }

    private BoardPosition(BoardPosition p) {
        kind = p.kind;
        board = p.board.clone();
        turn = p.turn;
        castling = p.castling;
        enPassant = p.enPassant;
        halfmove = p.halfmove;
        ply = p.ply;
        passes = p.passes;
        lastFrom = p.lastFrom;
        lastTo = p.lastTo;
        phase = p.phase;
        result = p.result;
        repetitions.addAll(p.repetitions);
        dead.or(p.dead);
    }

    public BoardPosition copy() {
        return new BoardPosition(this);
    }

    public int at(int square) {
        return square >= 0 && square < board.length ? board[square] : 0;
    }

    public int size() {
        return board.length;
    }

    public int turn() {
        return turn;
    }

    public int ply() {
        return ply;
    }

    public int lastFrom() {
        return lastFrom;
    }

    public int lastTo() {
        return lastTo;
    }

    public Phase phase() {
        return phase;
    }

    public String result() {
        return result;
    }

    public boolean dead(int at) {
        return dead.get(at);
    }

    public int index(int x, int y) {
        return y * kind.width + x;
    }

    private void put(int x, int y, int piece) {
        board[index(x, y)] = piece;
    }

    private boolean inside(int x, int y) {
        return x >= 0 && x < kind.width && y >= 0 && y < kind.height;
    }

    private int x(int at) {
        return at % kind.width;
    }

    private int y(int at) {
        return at / kind.width;
    }

    public String coordinate(int square) {
        return square < 0 ? "—" : "" + (char) ('A' + x(square)) + (kind.height - y(square));
    }

    public String pieceLabel(int square) {
        int p = at(square);
        if (p == 0)
            return "·";
        if (kind == BoardKind.GO)
            return "●";
        if (kind == BoardKind.CHESS)
            return switch (Math.abs(p)) {
                case PAWN -> "P";
                case KNIGHT -> "N";
                case BISHOP -> "B";
                case ROOK -> "R";
                case QUEEN -> "Q";
                default -> "K";
            };
        return switch (Math.abs(p)) {
            case PAWN -> p > 0 ? "兵" : "卒";
            case KNIGHT -> "馬";
            case BISHOP -> p > 0 ? "相" : "象";
            case ROOK -> "車";
            case KING -> p > 0 ? "帥" : "將";
            case ADVISOR -> p > 0 ? "仕" : "士";
            default -> "炮";
        };
    }

    public List<Move> legalMoves() {
        if (phase != Phase.PLAY)
            return List.of();
        List<Move> moves = new ArrayList<>();
        if (kind == BoardKind.GO) {
            for (int to = 0; to < board.length; to++)
                if (goMove(to) != null)
                    moves.add(new Move(-1, to));
            moves.add(Move.pass());
            return moves;
        }
        for (int from = 0; from < board.length; from++)
            if (board[from] * turn > 0) {
                for (int to = 0; to < board.length; to++)
                    if (pseudo(from, to, false)) {
                        if (kind == BoardKind.CHESS && Math.abs(board[from]) == PAWN && (y(to) == 0 || y(to) == 7)) {
                            for (int promotion : new int[]{QUEEN, ROOK, BISHOP, KNIGHT})
                                addLegal(moves, new Move(from, to, promotion));
                        } else
                            addLegal(moves, new Move(from, to));
                    }
            }
        return moves;
    }

    public List<Move> legalFrom(int square) {
        return legalMoves().stream().filter(m -> m.from == square).toList();
    }

    private void addLegal(List<Move> moves, Move m) {
        BoardPosition next = copy();
        next.movePieces(m);
        if (!next.inCheck(turn))
            moves.add(m);
    }

    public BoardPosition play(Move move) {
        if (phase != Phase.PLAY || ply >= 2048)
            return null;
        if (kind == BoardKind.GO) {
            if (move.from != -1 || move.promotion != 0)
                return null;
            if (move.to == -1) {
                BoardPosition next = copy();
                next.passes++;
                next.ply++;
                next.turn = -turn;
                next.lastFrom = next.lastTo = -1;
                if (next.passes == 2)
                    next.phase = Phase.SCORING;
                return next;
            }
            return goMove(move.to);
        }
        if (!legalMoves().contains(move))
            return null;
        BoardPosition next = copy();
        next.movePieces(move);
        next.turn = -turn;
        next.ply++;
        next.repetitions.add(next.key());
        if (next.legalMoves().isEmpty())
            next.finish(next.inCheck(next.turn) || kind == BoardKind.XIANGQI ? winner(turn) : "draw");
        else if (Collections.frequency(next.repetitions, next.key()) >= 3 || next.halfmove >= 100 || next.insufficient())
            next.finish("draw");
        return next;
    }

    public void finish(String outcome) {
        phase = Phase.FINISHED;
        result = outcome;
    }

    public static String winner(int side) {
        return side > 0 ? "first" : "second";
    }

    private void movePieces(Move m) {
        int piece = board[m.from], captured = board[m.to], side = Integer.signum(piece);
        if (kind == BoardKind.CHESS) {
            if (Math.abs(piece) == PAWN && m.to == enPassant && captured == 0)
                board[m.to + 8 * side] = 0;
            if (Math.abs(piece) == KING && Math.abs(x(m.to) - x(m.from)) == 2) {
                int rookFrom = index(x(m.to) > x(m.from) ? 7 : 0, y(m.from)), rookTo = index(x(m.to) > x(m.from) ? 5 : 3, y(m.from));
                board[rookTo] = board[rookFrom];
                board[rookFrom] = 0;
            }
            if (Math.abs(piece) == KING)
                castling &= side > 0 ? 12 : 3;
            for (int square : new int[]{63, 56, 7, 0})
                if (m.from == square || m.to == square)
                    castling &= ~(square == 63 ? 1 : square == 56 ? 2 : square == 7 ? 4 : 8);
            enPassant = Math.abs(piece) == PAWN && Math.abs(y(m.from) - y(m.to)) == 2 ? (m.from + m.to) / 2 : -1;
        }
        board[m.from] = 0;
        board[m.to] = m.promotion != 0 ? side * m.promotion : piece;
        halfmove = Math.abs(piece) == PAWN || captured != 0 ? 0 : halfmove + 1;
        lastFrom = m.from;
        lastTo = m.to;
    }

    private boolean pseudo(int from, int to, boolean attack) {
        if (from == to || from < 0 || to < 0 || from >= board.length || to >= board.length)
            return false;
        int piece = board[from], side = Integer.signum(piece), target = board[to];
        if (piece == 0 || (!attack && (target * side > 0 || Math.abs(target) == KING)))
            return false;
        int dx = x(to) - x(from), dy = y(to) - y(from), ax = Math.abs(dx), ay = Math.abs(dy), type = Math.abs(piece);
        if (kind == BoardKind.CHESS)
            return switch (type) {
                case PAWN -> attack ? ax == 1 && dy == -side :
                        (dx == 0 && target == 0 && (dy == -side || (dy == -2 * side && y(from) == (side > 0 ? 6 : 1) && at(from - 8 * side) == 0)))
                                || (ax == 1 && dy == -side && (target * side < 0 || (to == enPassant && target == 0)));
                case KNIGHT -> ax * ay == 2;
                case BISHOP -> ax == ay && between(from, to) == 0;
                case ROOK -> (dx == 0 || dy == 0) && between(from, to) == 0;
                case QUEEN -> (dx == 0 || dy == 0 || ax == ay) && between(from, to) == 0;
                case KING -> Math.max(ax, ay) == 1 || (!attack && castle(from, to, side));
                default -> false;
            };
        return switch (type) {
            case PAWN -> (dx == 0 && dy == -side) || ((side > 0 ? y(from) <= 4 : y(from) >= 5) && ay == 0 && ax == 1);
            case KNIGHT ->
                    ax * ay == 2 && at(index(x(from) + (ax == 2 ? Integer.signum(dx) : 0), y(from) + (ay == 2 ? Integer.signum(dy) : 0))) == 0;
            case BISHOP ->
                    ax == 2 && ay == 2 && (side > 0 ? y(to) >= 5 : y(to) <= 4) && at(index(x(from) + dx / 2, y(from) + dy / 2)) == 0;
            case ROOK -> (dx == 0 || dy == 0) && between(from, to) == 0;
            case CANNON -> (dx == 0 || dy == 0) && between(from, to) == (attack || target != 0 ? 1 : 0);
            case ADVISOR -> ax == 1 && ay == 1 && palace(to, side);
            case KING ->
                    (ax + ay == 1 && palace(to, side)) || (attack && dx == 0 && Math.abs(target) == KING && between(from, to) == 0);
            default -> false;
        };
    }

    private boolean palace(int square, int side) {
        return x(square) >= 3 && x(square) <= 5 && (side > 0 ? y(square) >= 7 : y(square) <= 2);
    }

    private int between(int from, int to) {
        int dx = Integer.signum(x(to) - x(from)), dy = Integer.signum(y(to) - y(from)), xx = x(from) + dx, yy = y(from) + dy, count = 0;
        while (xx != x(to) || yy != y(to)) {
            if (!inside(xx, yy))
                return 99;
            if (at(index(xx, yy)) != 0)
                count++;
            xx += dx;
            yy += dy;
        }
        return count;
    }

    private boolean castle(int from, int to, int side) {
        int row = side > 0 ? 7 : 0;
        if (from != index(4, row) || y(to) != row || (x(to) != 2 && x(to) != 6) || inCheck(side))
            return false;
        boolean right = x(to) == 6;
        int flag = side > 0 ? (right ? 1 : 2) : (right ? 4 : 8);
        int rook = index(right ? 7 : 0, row);
        if ((castling & flag) == 0 || board[rook] != side * ROOK || between(from, rook) != 0)
            return false;
        return !attacked(index(right ? 5 : 3, row), -side) && !attacked(to, -side);
    }

    public boolean inCheck(int side) {
        for (int square = 0; square < board.length; square++)
            if (board[square] == side * KING)
                return attacked(square, -side);
        return true;
    }

    private boolean attacked(int square, int side) {
        for (int from = 0; from < board.length; from++)
            if (board[from] * side > 0 && pseudo(from, square, true))
                return true;
        return false;
    }

    private boolean insufficient() {
        if (kind != BoardKind.CHESS)
            return false;
        int minor = 0;
        boolean bishopsOnly = true;
        int color = -1;
        for (int square = 0; square < 64; square++) {
            int p = Math.abs(board[square]);
            if (p == 0 || p == KING)
                continue;
            if (p != BISHOP && p != KNIGHT)
                return false;
            minor++;
            if (p != BISHOP)
                bishopsOnly = false;
            int c = (x(square) + y(square)) % 2;
            if (color < 0)
                color = c;
            else if (color != c)
                bishopsOnly = false;
        }
        return minor <= 1 || bishopsOnly;
    }

    private List<Integer> neighbors(int square) {
        List<Integer> out = new ArrayList<>(4);
        for (int[] d : new int[][]{{0, 1}, {0, -1}, {1, 0}, {-1, 0}})
            if (inside(x(square) + d[0], y(square) + d[1]))
                out.add(index(x(square) + d[0], y(square) + d[1]));
        return out;
    }

    public Set<Integer> group(int square) {
        if (at(square) == 0)
            return Set.of();
        Set<Integer> group = new HashSet<>();
        ArrayDeque<Integer> todo = new ArrayDeque<>();
        todo.add(square);
        group.add(square);
        while (!todo.isEmpty())
            for (int next : neighbors(todo.removeFirst()))
                if (at(next) == at(square) && group.add(next))
                    todo.add(next);
        return group;
    }

    public int liberties(int square) {
        Set<Integer> empty = new HashSet<>();
        for (int p : group(square))
            for (int n : neighbors(p))
                if (board[n] == 0)
                    empty.add(n);
        return empty.size();
    }

    private BoardPosition goMove(int to) {
        if (to < 0 || to >= board.length || board[to] != 0 || phase != Phase.PLAY || ply >= 2048)
            return null;
        BoardPosition next = copy();
        next.board[to] = turn;
        for (int n : next.neighbors(to))
            if (next.board[n] == -turn && next.liberties(n) == 0)
                for (int captured : next.group(n))
                    next.board[captured] = 0;
        if (next.liberties(to) == 0)
            return null;
        next.turn = -turn;
        next.ply++;
        next.passes = 0;
        next.lastFrom = -1;
        next.lastTo = to;
        // Positional superko; passes are exempt so both players can end play.
        String position = Arrays.toString(next.board);
        if (repetitions.contains(position))
            return null;
        next.repetitions.add(position);
        return next;
    }

    public boolean toggleDead(int square) {
        if (kind != BoardKind.GO || phase != Phase.SCORING || at(square) == 0)
            return false;
        boolean remove = dead.get(square);
        for (int p : group(square))
            dead.set(p, !remove);
        return true;
    }

    public void resumeGo() {
        if (phase == Phase.SCORING) {
            phase = Phase.PLAY;
            passes = 0;
            dead.clear();
        }
    }

    /**
     * Chinese area score: living stones + surrounded empties, white receives 7.5 points.
     */
    public double[] score() {
        BoardPosition clean = copy();
        for (int p = dead.nextSetBit(0); p >= 0; p = dead.nextSetBit(p + 1))
            clean.board[p] = 0;
        double[] points = {0, 7.5};
        Set<Integer> visited = new HashSet<>();
        for (int p = 0; p < board.length; p++) {
            if (clean.board[p] != 0) {
                points[clean.board[p] > 0 ? 0 : 1]++;
                continue;
            }
            if (!visited.add(p))
                continue;
            ArrayDeque<Integer> todo = new ArrayDeque<>();
            todo.add(p);
            int area = 0;
            Set<Integer> borders = new HashSet<>();
            while (!todo.isEmpty()) {
                int at = todo.removeFirst();
                area++;
                for (int n : neighbors(at)) {
                    if (clean.board[n] != 0)
                        borders.add(clean.board[n]);
                    else if (visited.add(n))
                        todo.add(n);
                }
            }
            if (borders.size() == 1)
                points[borders.contains(1) ? 0 : 1] += area;
        }
        return points;
    }

    public void finishScoring() {
        double[] score = score();
        finish(score[0] == score[1] ? "draw" : winner(score[0] > score[1] ? 1 : -1));
    }

    private String key() {
        return kind == BoardKind.GO ? Arrays.toString(board) : Arrays.toString(board) + ":" + turn + ":" + castling + ":" + effectiveEnPassant();
    }

    private int effectiveEnPassant() {
        if (enPassant < 0)
            return -1;
        for (int d : new int[]{-1, 1}) {
            int xx = x(enPassant) + d, yy = y(enPassant) + turn;
            if (inside(xx, yy) && at(index(xx, yy)) == turn * PAWN) {
                BoardPosition n = copyForKey();
                n.movePieces(new Move(index(xx, yy), enPassant));
                if (!n.inCheck(turn))
                    return enPassant;
            }
        }
        return -1;
    }

    private BoardPosition copyForKey() {
        return copy();
    }

    public record Saved(BoardKind kind, int[] pieces, int turn, int castling, int enPassant, int halfmove, int ply,
                        int passes, int lastFrom, int lastTo, Phase phase, String result, List<String> repetitions,
                        int[] dead) {
    }

    public Saved save() {
        return new Saved(kind, board.clone(), turn, castling, enPassant, halfmove, ply, passes, lastFrom, lastTo, phase, result, List.copyOf(repetitions), dead.stream().toArray());
    }

    public static BoardPosition restore(Saved data) {
        Objects.requireNonNull(data);
        BoardPosition p = new BoardPosition(data.kind);
        if (data.pieces == null || data.pieces.length != p.size() || Math.abs(data.turn) != 1 || data.ply < 0 || data.ply > 2048
                || data.repetitions == null || data.repetitions.size() > 2049 || data.phase == null || data.dead == null || data.dead.length > p.size()
                || data.castling < 0 || data.castling > 15 || data.enPassant < -1 || data.enPassant >= p.size() || data.halfmove < 0
                || data.passes < 0 || data.passes > 2)
            throw new IllegalArgumentException("Invalid board save");
        for (int piece : data.pieces)
            if (Math.abs(piece) > (data.kind == BoardKind.GO ? 1 : data.kind == BoardKind.CHESS ? 6 : 8))
                throw new IllegalArgumentException("Invalid piece");
        for (String key : data.repetitions)
            if (key == null || key.length() > 550)
                throw new IllegalArgumentException("Invalid repetition key");
        for (int square : data.dead)
            if (square < 0 || square >= p.size())
                throw new IllegalArgumentException("Invalid dead group");
        if (data.kind != BoardKind.GO)
            for (int side : new int[]{-1, 1})
                if (Arrays.stream(data.pieces).filter(v -> v == side * KING).count() != 1)
                    throw new IllegalArgumentException("Missing king");
        System.arraycopy(data.pieces, 0, p.board, 0, p.size());
        p.turn = data.turn;
        p.castling = data.castling;
        p.enPassant = data.enPassant;
        p.halfmove = data.halfmove;
        p.ply = data.ply;
        p.passes = data.passes;
        p.lastFrom = data.lastFrom;
        p.lastTo = data.lastTo;
        p.phase = data.phase;
        p.result = Objects.requireNonNullElse(data.result, "");
        p.repetitions.clear();
        p.repetitions.addAll(data.repetitions);
        for (int square : data.dead)
            p.dead.set(square);
        return p;
    }
}
