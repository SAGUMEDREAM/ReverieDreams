package cc.thonly.reverie_dreams.test;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AsyncCountdown {

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public void startCountdown(int seconds, java.util.function.IntConsumer onTick, Runnable onComplete) {
        final int[] counter = {seconds};

        scheduler.scheduleAtFixedRate(() -> {
            if (counter[0] < 0) {   // 倒计时结束，停止调度
                scheduler.shutdown();
                onComplete.run();
            } else {
                onTick.accept(counter[0]);
                counter[0]--;
            }
        }, 0, 1, TimeUnit.SECONDS);
    }
}
