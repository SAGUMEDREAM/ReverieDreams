package cc.thonly.reverie_dreams.paper.npc;

import java.util.UUID;
import org.bukkit.inventory.ItemStack;

/** Detached snapshot. UUID identifies the invisible AI carrier; avatarId identifies its mannequin. */
public record NPCSnapshot(UUID entityId, UUID avatarId, String roleId, String skinId, UUID ownerId,
                          NPCMode mode, double health, double maxHealth, ItemStack[] inventory) {
    public NPCSnapshot { inventory = copy(inventory); }
    @Override public ItemStack[] inventory() { return copy(inventory); }
    static ItemStack[] copy(ItemStack[] input) {
        var result = new ItemStack[input.length];
        for (int i = 0; i < input.length; i++) result[i] = input[i] == null ? ItemStack.empty() : input[i].clone();
        return result;
    }
}
