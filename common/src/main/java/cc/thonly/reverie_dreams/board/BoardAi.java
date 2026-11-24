package cc.thonly.reverie_dreams.board;

import cc.thonly.reverie_dreams.block.entity.BoardGameBlockEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

import java.util.*;
import java.util.concurrent.*;

import com.mojang.logging.LogUtils;

/**
 * One bounded worker for the server. Results return to the main thread and never outlive their match revision.
 */
public final class BoardAi {
    private record Pending(UUID match, long revision, FutureTask<BoardAiSearch.Choice> task) {
    }

    private static final Map<BoardGameBlockEntity, Pending> PENDING = new IdentityHashMap<>();
    private static ThreadPoolExecutor worker;

    private BoardAi() {
    }

    private static ThreadPoolExecutor worker() {
        if (worker == null || worker.isShutdown())
            worker = new ThreadPoolExecutor(1, 1, 30, TimeUnit.SECONDS, new ArrayBlockingQueue<>(8), r -> {
                Thread t = new Thread(r, "Reverie-Board-AI");
                t.setDaemon(true);
                return t;
            }, new ThreadPoolExecutor.AbortPolicy());
        return worker;
    }

    public static void tick(BoardGameBlockEntity host) {
        BoardGame game = host.game();
        if (game == null || !game.started || game.aiSide == 0 || game.position.phase() != BoardPosition.Phase.PLAY || game.position.turn() != game.aiSide || !BoardGames.available(host)) {
            cancel(host);
            return;
        }
        Pending pending = PENDING.get(host);
        if (pending != null) {
            if (!pending.match.equals(game.id) || pending.revision != game.revision) {
                cancel(host);
                return;
            }
            if (!pending.task.isDone())
                return;
            PENDING.remove(host);
            try {
                var result = pending.task.get();
                if (result.move() != null && host.getLevel() instanceof ServerLevel level && !host.isRemoved() && level.hasChunkAt(host.getBlockPos())
                        && level.getBlockEntity(host.getBlockPos()) == host && BoardGames.available(host)
                        && game.id.equals(pending.match) && game.revision == pending.revision && game.move(game.aiSide, result.move()))
                    host.publish();
            } catch (CancellationException ignored) {
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
            } catch (ExecutionException failure) {
                LogUtils.getLogger().warn("Board AI search failed", failure.getCause());
            }
            return;
        }
        // Capture all inputs before handing work to the background thread.
        var snapshot = game.position.copy();
        var difficulty = game.difficulty;
        long seed = game.id.getLeastSignificantBits() ^ game.revision;
        var task = new FutureTask<>(() -> BoardAiSearch.choose(snapshot, difficulty, seed));
        try {
            worker().execute(task);
            PENDING.put(host, new Pending(game.id, game.revision, task));
        } catch (RejectedExecutionException busy) {
            task.cancel(false);
        }
    }

    public static void cancel(BoardGameBlockEntity host) {
        var pending = PENDING.remove(host);
        if (pending != null) {
            pending.task.cancel(true);
            if (worker != null)
                worker.remove(pending.task);
        }
    }

    public static void clear(MinecraftServer server) {
        for (var host : new ArrayList<>(PENDING.keySet()))
            if (host.getLevel() == null || host.getLevel().getServer() == server)
                cancel(host);
        if (PENDING.isEmpty() && worker != null) {
            worker.shutdownNow();
            worker = null;
        }
    }
}
