package cc.thonly.reverie_dreams.entity.npc.work;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.Set;
import java.util.UUID;

/**
 * An immutable, bounded sequence. Each completed cursor is persisted before the next step starts.
 */
public final class NPCPlan {
    private final JsonArray steps;
    private final String goal;
    private int cursor;
    private UUID id = UUID.randomUUID();

    public NPCPlan(String goal, JsonArray steps) {
        if (goal == null || goal.isBlank() || goal.length() > 256 || steps == null || steps.isEmpty() || steps.size() > 8) {
            throw new IllegalArgumentException("A plan needs a goal and 1..8 steps");
        }
        this.goal = goal;
        this.steps = steps.deepCopy();
        double[] previous = null;
        for (int i = 0; i < steps.size(); i++) {
            var step = steps.get(i).getAsJsonObject();
            String skill = step.get("skill").getAsString();
            if (NPCSkills.find(skill) == null)
                throw new IllegalArgumentException("Unknown skill");
            double[] position = {step.get("x").getAsDouble(), step.get("y").getAsDouble(), step.get("z").getAsDouble()};
            for (double coordinate : position)
                if (!Double.isFinite(coordinate) || Math.abs(coordinate) > 30_000_000) {
                    throw new IllegalArgumentException("Invalid coordinates");
                }
            if (Math.abs(position[1]) > 4096)
                throw new IllegalArgumentException("Invalid height");
            if (previous != null && distanceSquared(previous, position) > 256)
                throw new IllegalArgumentException("Steps must stay within 16 blocks of each other");
            previous = position;
            if (NPCSkills.find(skill).action().equals("transfer_items")) {
                if (step.has("target_item") || step.has("target_count") || step.has("target_entity"))
                    throw new IllegalArgumentException("Transfer steps use item/count");
                requireItemCount(step, "item", "count");
            } else {
                if (step.has("item") || step.has("count"))
                    throw new IllegalArgumentException("Work steps use target_item/target_count");
                if (step.has("target_count") || step.has("target_item") || step.has("target_entity")) {
                    if (skill.equals("breed_animals")) {
                        if (step.has("target_item"))
                            throw new IllegalArgumentException("Breeding uses target_entity");
                        requireItemCount(step, "target_entity", "target_count");
                    } else {
                        if (step.has("target_entity") || !Set.of("farm", "fish", "shear_sheep", "harvest_honey", "smelt").contains(skill))
                            throw new IllegalArgumentException("Skill cannot verify a production objective");
                        requireItemCount(step, "target_item", "target_count");
                    }
                } else if (i < steps.size() - 1 && !skill.equals("go_to")) {
                    throw new IllegalArgumentException("Continuous skills must be last or have a quantity objective");
                }
            }
        }
    }

    private static void requireItemCount(JsonObject step, String item, String count) {
        String id = step.get(item).getAsString();
        int amount = step.get(count).getAsInt();
        if (!id.matches("[a-z0-9_.-]+:[a-z0-9_./-]+") || amount < 1 || amount > 1024 || step.get(count).getAsDouble() != amount) {
            throw new IllegalArgumentException("Invalid item/quantity");
        }
    }

    private static double distanceSquared(double[] a, double[] b) {
        return Math.pow(a[0] - b[0], 2) + Math.pow(a[1] - b[1], 2) + Math.pow(a[2] - b[2], 2);
    }

    public JsonObject current() {
        return steps.get(cursor).getAsJsonObject().deepCopy();
    }

    public boolean hasCurrent() {
        return cursor < steps.size();
    }

    public boolean advance() {
        if (hasCurrent())
            cursor++;
        return hasCurrent();
    }

    public JsonObject save() {
        JsonObject data = new JsonObject();
        data.addProperty("plan_id", id.toString());
        data.addProperty("goal", goal);
        data.addProperty("cursor", cursor);
        data.add("steps", steps.deepCopy());
        return data;
    }

    public static NPCPlan load(JsonObject data) {
        var result = new NPCPlan(data.get("goal").getAsString(), data.getAsJsonArray("steps"));
        result.cursor = data.get("cursor").getAsInt();
        if (data.has("plan_id"))
            result.id = UUID.fromString(data.get("plan_id").getAsString());
        if (result.cursor < 0 || result.cursor > result.steps.size())
            throw new IllegalArgumentException("Invalid plan cursor");
        return result;
    }
}
