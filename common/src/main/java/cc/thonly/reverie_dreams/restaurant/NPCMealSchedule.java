package cc.thonly.reverie_dreams.restaurant;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.UUID;

/** Persistent daily allowance and cooldown, independent of nutrition. */
public final class NPCMealSchedule {
    private static final long DAY_TICKS = 24_000L;
    private static final long MEAL_INTERVAL = 3_600L;
    private static final long RETRY_INTERVAL = 600L;
    private long day = Long.MIN_VALUE;
    private int eaten;
    private long nextVisit;

    public boolean ready(ServerLevel level, UUID npc) {
        refresh(level, npc);
        return eaten < limit(npc) && level.getGameTime() >= nextVisit;
    }

    public void finishMeal(ServerLevel level, UUID npc) {
        refresh(level, npc);
        eaten = Math.min(limit(npc), eaten + 1);
        nextVisit = level.getGameTime() + MEAL_INTERVAL + stagger(npc);
    }

    public void retryLater(ServerLevel level, UUID npc) {
        nextVisit = Math.max(nextVisit, level.getGameTime() + RETRY_INTERVAL + stagger(npc) / 4);
    }

    public void allowVisitNow(ServerLevel level, UUID npc) {
        refresh(level, npc);
        nextVisit = level.getGameTime();
    }

    private void refresh(ServerLevel level, UUID npc) {
        long today = Math.floorDiv(level.getOverworldClockTime(), DAY_TICKS);
        if (today > day) {
            day = today;
            eaten = 0;
            nextVisit = Math.max(nextVisit, level.getGameTime() + stagger(npc));
        }
    }

    private int limit(UUID npc) {
        return 2 + RandomSource.create(npc.getMostSignificantBits() ^ npc.getLeastSignificantBits() ^ day).nextInt(3);
    }

    private static int stagger(UUID npc) {
        return Math.floorMod(npc.hashCode(), 1_200);
    }

    public void load(ValueInput in) {
        day = in.getLongOr("Restaurant.MealDay", Long.MIN_VALUE);
        eaten = Math.clamp(in.getIntOr("Restaurant.MealsEaten", 0), 0, 4);
        nextVisit = in.getLongOr("Restaurant.NextVisit", 0L);
    }

    public void save(ValueOutput out) {
        if (day == Long.MIN_VALUE) return;
        out.putLong("Restaurant.MealDay", day);
        out.putInt("Restaurant.MealsEaten", eaten);
        out.putLong("Restaurant.NextVisit", nextVisit);
    }
}
