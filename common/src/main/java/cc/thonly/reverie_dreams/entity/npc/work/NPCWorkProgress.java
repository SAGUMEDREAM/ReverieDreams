package cc.thonly.reverie_dreams.entity.npc.work;

import com.google.gson.JsonObject;

/**
 * A quantity means newly held items, never inferred production or cumulative snapshots.
 */
public final class NPCWorkProgress {
    private final String item;
    private final int goal;
    private final int baseline;
    private int held;
    private boolean verified;
    private boolean offspring;
    private int produced;

    public NPCWorkProgress(String item, int goal, int baseline) {
        if (item == null || item.isBlank() || goal < 1 || goal > 1024 || baseline < 0) {
            throw new IllegalArgumentException("Invalid quantity objective");
        }
        this.item = item;
        this.goal = goal;
        this.baseline = baseline;
        this.held = baseline;
    }

    public void sample(int count) {
        held = Math.max(0, count);
    }

    public static NPCWorkProgress production(String item, int goal) {
        var result = new NPCWorkProgress(item, goal, 0);
        result.verified = true;
        return result;
    }

    public static NPCWorkProgress offspring(String entity, int goal) {
        var result = production(entity, goal);
        result.offspring = true;
        return result;
    }

    public boolean verified() {
        return verified;
    }

    public boolean countsOffspring() {
        return offspring;
    }

    public void record(String actual, int count) {
        if (verified && item.equals(actual) && count > 0)
            produced = (int) Math.min(1_000_000L, (long) produced + count);
    }

    public String item() {
        return item;
    }

    public int goal() {
        return goal;
    }

    public int acquired() {
        return verified ? produced : Math.max(0, held - baseline);
    }

    public boolean complete() {
        return acquired() >= goal && (!verified || offspring || held >= goal);
    }

    public JsonObject save() {
        JsonObject data = new JsonObject();
        data.addProperty("item", item);
        data.addProperty("goal", goal);
        data.addProperty("baseline", baseline);
        data.addProperty("held", held);
        data.addProperty("acquired", acquired());
        data.addProperty("basis", verified ? offspring ? "verified_offspring" : "verified_production_and_available_inventory" : "net_inventory_gain_since_task_start");
        if (verified)
            data.addProperty("produced", produced);
        return data;
    }

    public static NPCWorkProgress load(JsonObject data) {
        NPCWorkProgress result = new NPCWorkProgress(data.get("item").getAsString(),
                data.get("goal").getAsInt(), data.get("baseline").getAsInt());
        result.sample(data.get("held").getAsInt());
        String basis = data.has("basis") ? data.get("basis").getAsString() : "net_inventory_gain_since_task_start";
        result.verified = basis.equals("verified_production_and_available_inventory") || basis.equals("verified_offspring");
        result.offspring = basis.equals("verified_offspring");
        if (result.verified)
            result.produced = Math.clamp(data.get("produced").getAsInt(), 0, 1_000_000);
        return result;
    }
}
