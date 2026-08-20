package cc.thonly.reverie_dreams.util.network;

import cc.thonly.reverie_dreams.ReverieDreams;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("UnusedReturnValue")
@Slf4j
public class MCThreadHelper {

    private static final ScheduledExecutorService EXECUTOR =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread thread = new Thread(r, "mc-thread-sync-helper");
                thread.setDaemon(true);
                return thread;
            });

    /**
     * Condition check interval.
     */
    private static final long CHECK_INTERVAL = 250L;

    /**
     * Maximum waiting time.
     */
    private static final long TIMEOUT = 7500L;

    /**
     * Blocking wait until condition is satisfied or timeout.
     *
     * @return true if condition satisfied, false if timeout or interrupted
     */
    @SuppressWarnings("BusyWait")
    public static boolean await(WaitCondition condition) {
        final long startTime = System.currentTimeMillis();

        while (true) {
            try {
                if (condition.get()) {
                    log.debug(
                            "Condition satisfied after {} ms.",
                            elapsed(startTime)
                    );
                    return true;
                }
                if (isTimeout(startTime)) {
                    log.warn(
                            "Condition wait timed out after {} ms.",
                            elapsed(startTime)
                    );
                    return false;
                }
                Thread.sleep(CHECK_INTERVAL);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                log.warn(
                        "Thread interrupted while waiting for condition."
                );
                return false;

            } catch (Exception exception) {
                log.error(
                        "Exception while evaluating wait condition.",
                        exception
                );

                return false;
            }
        }
    }

    /**
     * Execute task on Minecraft main thread after condition is satisfied.
     */
    public static void run(
            Runnable task,
            WaitCondition condition
    ) {
        final long startTime = System.currentTimeMillis();
        final ScheduledFuture<?>[] futureHolder = new ScheduledFuture<?>[1];

        ScheduledFuture<?> future =
                EXECUTOR.scheduleAtFixedRate(() -> {
                    try {
                        boolean ready = condition.get();
                        boolean timeout = isTimeout(startTime);

                        if (!ready && !timeout) {
                            return;
                        }

                        if (timeout && !ready) {
                            log.warn(
                                    "Condition wait timed out after {} ms. Executing task anyway.",
                                    elapsed(startTime)
                            );
                        } else {
                            log.debug(
                                    "Condition satisfied after {} ms. Dispatching task.",
                                    elapsed(startTime)
                            );
                        }
                        dispatchToMainThread(task);

                        ScheduledFuture<?> current =
                                futureHolder[0];

                        if (current != null) {
                            current.cancel(false);
                        }
                    } catch (Exception exception) {
                        log.error(
                                "Exception while evaluating async condition.",
                                exception
                        );
                    }
                }, 0, CHECK_INTERVAL, TimeUnit.MILLISECONDS);
        futureHolder[0] = future;
    }

    private static void dispatchToMainThread(
            Runnable task
    ) {
        try {
            MinecraftServer server = ReverieDreams.getServer();
            if (server != null) {
                log.debug(
                        "Dispatching task to server thread."
                );
                server.execute(
                        wrapTask(task, "server")
                );
                return;
            }
            EnvExecutor.runInEnv(
                    Env.CLIENT,
                    () -> () -> {
                        log.debug(
                                "Dispatching task to client thread."
                        );
                        Minecraft.getInstance()
                                 .execute(
                                         wrapTask(task, "client")
                                 );
                    }
            );
        } catch (Exception exception) {
            log.error(
                    "Failed to dispatch task to Minecraft thread.",
                    exception
            );
        }
    }


    private static Runnable wrapTask(
            Runnable task,
            String side
    ) {
        return () -> {
            try {
                log.debug(
                        "Executing task on {} thread.",
                        side
                );
                task.run();
            } catch (Exception exception) {
                log.error(
                        "Task execution failed on {} thread.",
                        side,
                        exception
                );
            }
        };
    }

    private static boolean isTimeout(long startTime) {
        return elapsed(startTime) > TIMEOUT;
    }

    private static long elapsed(long startTime) {

        return System.currentTimeMillis() - startTime;
    }

    @FunctionalInterface
    public interface WaitCondition {

        boolean get();

    }

}