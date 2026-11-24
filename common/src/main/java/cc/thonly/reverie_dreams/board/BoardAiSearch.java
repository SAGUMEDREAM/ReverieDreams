package cc.thonly.reverie_dreams.board;

import java.util.*;

/**
 * Local, bounded search over pure position snapshots. No level, player, network or model API access.
 */
public final class BoardAiSearch {
    public record Choice(BoardPosition.Move move, int nodes, int depth, long millis) {
    }

    private static final int[] VALUES = {0, 100, 320, 330, 500, 900, 20000, 220, 450};
    private final long start = System.nanoTime(), deadline;
    private final int maxNodes;
    private int nodes;

    private static final class Stop extends RuntimeException {
        @Override
        public synchronized Throwable fillInStackTrace() {
            return this;
        }
    }

    private BoardAiSearch(BoardGame.Difficulty level) {
        deadline = start + (level == BoardGame.Difficulty.HARD ? 450_000_000L : 120_000_000L);
        maxNodes = level == BoardGame.Difficulty.HARD ? 6000 : 1200;
    }

    public static Choice choose(BoardPosition snapshot, BoardGame.Difficulty level, long seed) {
        return new BoardAiSearch(level).run(snapshot.copy(), level, new Random(seed));
    }

    private void budget() {
        if (++nodes > maxNodes || System.nanoTime() > deadline || Thread.currentThread().isInterrupted())
            throw new Stop();
    }

    private Choice run(BoardPosition board, BoardGame.Difficulty level, Random random) {
        List<BoardPosition.Move> moves = new ArrayList<>(board.legalMoves());
        if (moves.isEmpty())
            return new Choice(null, nodes, 0, (System.nanoTime() - start) / 1_000_000);
        Collections.shuffle(moves, random);
        if (level == BoardGame.Difficulty.EASY) {
            List<BoardPosition.Move> playable = moves.stream().filter(m -> m.to() >= 0).toList();
            BoardPosition.Move move = playable.isEmpty() ? moves.getFirst() : playable.get(random.nextInt(playable.size()));
            if (board.kind != BoardKind.GO && random.nextInt(3) == 0)
                move = moves.stream().max(Comparator.comparingInt(m -> Math.abs(board.at(m.to())) == 0 ? 0 : VALUES[Math.abs(board.at(m.to()))])).orElse(move);
            return new Choice(move, 0, 0, (System.nanoTime() - start) / 1_000_000);
        }
        BoardPosition.Move best = moves.getFirst();
        int completed = 0;
        if (board.kind == BoardKind.GO) {
            double bestScore = -Double.MAX_VALUE;
            // Evaluate captures, saving threatened groups, liberties, shape and area; higher difficulty also checks tactical replies.
            int stones = 0;
            for (int i = 0; i < board.size(); i++)
                if (board.at(i) != 0)
                    stones++;
            moves.sort(Comparator.comparingInt((BoardPosition.Move m) -> goOrder(board, m)).reversed());
            for (var move : moves) {
                try {
                    budget();
                    BoardPosition next = board.play(move);
                    if (next == null)
                        continue;
                    double score = goValue(next, board.turn());
                    if (move.to() < 0) {
                        double[] area = next.score();
                        double lead = (area[0] - area[1]) * board.turn();
                        score += stones < 55 ? -1000 : lead > 0 ? 3 : -30;
                        if (next.phase() == BoardPosition.Phase.SCORING)
                            score += lead > 0 ? 200 : -200;
                    } else {
                        int libs = next.liberties(move.to());
                        score += libs == 1 ? -70 : Math.min(4, libs) * 2;
                        score -= .2 * (Math.abs(move.to() % 9 - 4) + Math.abs(move.to() / 9 - 4));
                        if (level == BoardGame.Difficulty.HARD) {
                            double worst = score;
                            int replies = 0;
                            for (var reply : next.legalMoves()) {
                                if (reply.to() < 0)
                                    continue;
                                if (++replies > 20)
                                    break;
                                budget();
                                var response = next.play(reply);
                                if (response != null)
                                    worst = Math.min(worst, goValue(response, board.turn()));
                            }
                            score = .55 * score + .45 * worst;
                        }
                    }
                    if (score > bestScore) {
                        bestScore = score;
                        best = move;
                    }
                    completed = 1;
                } catch (Stop stop) {
                    break;
                }
            }
        } else {
            moves.sort(Comparator.comparingInt((BoardPosition.Move m) -> moveOrder(board, m)).reversed());
            int maxDepth = level == BoardGame.Difficulty.HARD ? 4 : 2;
            for (int depth = 1; depth <= maxDepth; depth++) {
                BoardPosition.Move candidate = best;
                double high = -Double.MAX_VALUE;
                try {
                    for (var move : moves) {
                        budget();
                        var next = board.play(move);
                        if (next == null)
                            continue;
                        double score = -negamax(next, depth - 1, -Double.MAX_VALUE, -high);
                        if (score > high) {
                            high = score;
                            candidate = move;
                        }
                    }
                    best = candidate;
                    completed = depth;
                    var previous = best;
                    moves.remove(previous);
                    moves.addFirst(previous);
                } catch (Stop stop) {
                    break;
                }
            }
        }
        return new Choice(best, nodes, completed, (System.nanoTime() - start) / 1_000_000);
    }

    private double negamax(BoardPosition board, int depth, double alpha, double beta) {
        budget();
        if (board.phase() == BoardPosition.Phase.FINISHED)
            return board.result().equals("draw") ? 0 :
                    board.result().equals(BoardPosition.winner(board.turn())) ? 100000 + depth : -100000 - depth;
        if (depth == 0)
            return value(board) * board.turn();
        List<BoardPosition.Move> moves = new ArrayList<>(board.legalMoves());
        moves.sort(Comparator.comparingInt((BoardPosition.Move m) -> moveOrder(board, m)).reversed());
        double best = -Double.MAX_VALUE;
        for (var move : moves) {
            budget();
            var next = board.play(move);
            if (next == null)
                continue;
            double score = -negamax(next, depth - 1, -beta, -alpha);
            best = Math.max(best, score);
            alpha = Math.max(alpha, score);
            if (alpha >= beta)
                break;
        }
        return best == -Double.MAX_VALUE ? value(board) * board.turn() : best;
    }

    private static int moveOrder(BoardPosition board, BoardPosition.Move move) {
        return 10 * VALUES[Math.abs(board.at(move.to()))] - VALUES[Math.abs(board.at(move.from()))] + VALUES[move.promotion()];
    }

    private static int goOrder(BoardPosition board, BoardPosition.Move move) {
        if (move.to() < 0)
            return -10000;
        int at = move.to(), score = 8 - Math.abs(at % 9 - 4) - Math.abs(at / 9 - 4);
        Set<Integer> counted = new HashSet<>();
        for (int n : new int[]{at - 9, at + 9, at % 9 > 0 ? at - 1 : -1, at % 9 < 8 ? at + 1 : -1})
            if (n >= 0 && n < 81 && board.at(n) != 0 && counted.add(n)) {
                var group = board.group(n);
                counted.addAll(group);
                if (board.liberties(n) == 1)
                    score += (board.at(n) == board.turn() ? 100 : 200) * group.size();
            }
        return score;
    }

    private static double value(BoardPosition board) {
        double score = 0;
        for (int square = 0; square < board.size(); square++) {
            int piece = board.at(square), type = Math.abs(piece);
            if (piece == 0)
                continue;
            double bonus = type == BoardPosition.PAWN ? (piece > 0 ? board.kind.height - 1 - square / board.kind.width : square / board.kind.width) * 3 :
                    type == BoardPosition.KING ? 0 : 8 - Math.abs(square % board.kind.width - (board.kind.width - 1) * .5) - Math.abs(square / board.kind.width - (board.kind.height - 1) * .5);
            score += Integer.signum(piece) * (VALUES[type] + bonus);
        }
        return score;
    }

    private static double goValue(BoardPosition board, int side) {
        double score = 0;
        Set<Integer> counted = new HashSet<>();
        for (int square = 0; square < board.size(); square++)
            if (board.at(square) != 0) {
                score += board.at(square) * 10;
                if (counted.add(square)) {
                    var group = board.group(square);
                    counted.addAll(group);
                    int liberties = board.liberties(square);
                    score += board.at(square) * (liberties == 1 ? -8 * group.size() : Math.min(liberties, 5));
                }
            }
        return score * side;
    }
}
