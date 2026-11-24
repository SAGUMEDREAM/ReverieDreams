package cc.thonly.reverie_dreams.entity.npc.work;

import java.util.ArrayList;
import java.util.List;

/**
 * Shared skill catalogue used by the model and task executor.
 */
public final class NPCSkills {
    public record Skill(String id, String description, String action, String mode) {
    }

    public static final List<Skill> ALL = new ArrayList<>(List.of(
            new Skill("go_to", "Walk to the selected location and confirm arrival.", "move_to", ""),
            new Skill("follow_owner", "Continuously follow your owner until cancelled.", "follow", ""),
            new Skill("wait_here", "Wait here until cancelled.", "stay", ""),
            new Skill("deposit_items", "Walk beside an authorized chest/barrel and deposit item/count; exact transfer is verified.", "transfer_items", "deposit"),
            new Skill("withdraw_items", "Walk beside an authorized chest/barrel and withdraw item/count; exact transfer is verified.", "transfer_items", "withdraw"),
            new Skill("farm", "Walk to the work site, then tend crops using existing farming AI.", "work", "farm"),
            new Skill("fish", "Walk to the work site, then fish using existing fishing AI.", "work", "fishing"),
            new Skill("shear_sheep", "Walk to the work site, then shear sheep with actual supplies.", "work", "sheep_shearing"),
            new Skill("breed_animals", "Walk to the work site, then breed animals with actual supplies.", "work", "breed"),
            new Skill("smelt", "Walk to the work site, then use nearby furnaces and actual supplies.", "work", "smelt"),
            new Skill("sort_chests", "Walk to the work site, then use existing chest sorting AI.", "work", "chest_classification"),
            new Skill("harvest_honey", "Walk to the work site, then use existing honey harvesting AI.", "work", "honeycomb_harvesting"))
    );

    private NPCSkills() {
    }

    public static Skill find(String id) {
        return ALL.stream().filter(skill -> skill.id().equals(id)).findFirst().orElse(null);
    }
}
