package cc.thonly.reverie_dreams.ai.npc;

import cc.thonly.reverie_dreams.entity.npc.work.NPCSkills;

import cc.thonly.reverie_dreams.ai.ChatProvider;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * Mod gameplay adaptations, supported by the card's own evidence; never grants lore powers.
 */
public record NPCBehavior(String feedbackStyle, String selfPreservation, List<String> workPreferences,
                          List<String> evidence, boolean configured) {
    public static final List<String> WORK_SKILLS = List.of("farm", "fish", "shear_sheep", "breed_animals", "smelt", "sort_chests", "harvest_honey");
    private static final Set<String> STYLES = Set.of("neutral", "direct", "composed", "eager", "reserved", "gentle", "cautious");
    public static final NPCBehavior DEFAULT = new NPCBehavior("neutral", "balanced", List.of(), List.of(), false);

    public NPCBehavior {
        if (!STYLES.contains(feedbackStyle) || !Set.of("balanced", "cautious").contains(selfPreservation)
                || workPreferences.size() > WORK_SKILLS.size() || !WORK_SKILLS.containsAll(workPreferences)
                || workPreferences.stream().distinct().count() != workPreferences.size()
                || evidence.size() > 4 || configured && evidence.isEmpty()
                || evidence.stream().anyMatch(s -> s.isBlank() || s.length() > 1024))
            throw new IllegalArgumentException("Invalid behavior profile");
        workPreferences = List.copyOf(workPreferences);
        evidence = List.copyOf(evidence);
    }

    public static NPCBehavior fromCard(JsonObject card) {
        if (!card.has("behavior"))
            return DEFAULT;
        var data = card.getAsJsonObject("behavior");
        var preferences = new ArrayList<String>();
        data.getAsJsonArray("work_preferences").forEach(e -> preferences.add(e.getAsString()));
        var evidence = new ArrayList<String>();
        data.getAsJsonArray("evidence").forEach(e -> evidence.add(e.getAsString()));
        var verified = new ArrayList<String>();
        if (card.has("canon_facts"))
            card.getAsJsonArray("canon_facts").forEach(e -> verified.add(e.getAsString()));
        if (card.has("portrayal_guidance"))
            verified.add(card.get("portrayal_guidance").getAsString());
        if (!verified.containsAll(evidence))
            throw new IllegalArgumentException("Behavior evidence is not in this character card");
        return new NPCBehavior(data.get("feedback_style").getAsString(), data.get("self_preservation").getAsString(), preferences, evidence, true);
    }

    public int preference(String skill) {
        int index = workPreferences.indexOf(skill);
        return index < 0 ? WORK_SKILLS.size() : index;
    }

    public List<NPCSkills.Skill> skills() {
        // Stable sorting preserves the catalogue order for equally preferred and unspecified skills.
        return NPCSkills.ALL.stream().sorted(Comparator.comparingInt(s -> preference(s.id()))).toList();
    }

    public double restThreshold() {
        return selfPreservation.equals("cautious") ? 0.5 : 0.25;
    }

    public double recoveryThreshold() {
        return restThreshold() + 0.15;
    }

    public boolean needsRest(double healthRatio, boolean recovering) {
        return !Double.isFinite(healthRatio) || healthRatio < (recovering ? recoveryThreshold() : restThreshold());
    }

    public boolean notifyPhase(String phase) {
        return !feedbackStyle.equals("reserved") || Set.of("paused", "blocked", "failed", "completed", "cancelled").contains(phase);
    }

    public JsonObject context() {
        var data = new JsonObject();
        data.addProperty("configured", configured);
        data.addProperty("feedback_style", feedbackStyle);
        data.addProperty("self_preservation", selfPreservation);
        data.add("work_preferences", ChatProvider.GSON.toJsonTree(workPreferences));
        data.addProperty("rest_below_health_ratio", restThreshold());
        data.addProperty("resume_at_health_ratio", recoveryThreshold());
        data.addProperty("policy", "Mod gameplay interpretation, not canon numeric abilities. Preferences rank suggestions only; explicit owner orders remain authoritative. No extra production or permissions. Work may pause to recover; movement/follow orders remain available. Viewing does not start work.");
        return data;
    }
}
