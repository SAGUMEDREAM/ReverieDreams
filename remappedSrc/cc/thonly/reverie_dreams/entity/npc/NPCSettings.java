package cc.thonly.reverie_dreams.entity.npc;

import java.util.Set;

public interface NPCSettings {
    enum KeepInventoryTypes {
        ARCHIVED,
        DROP_ALL_ITEM,
        NOT_DROP_ANY,
        ONLY_HAND_AND_ARMOR
    }
    default KeepInventoryTypes getKeepInventoryType() {
        return KeepInventoryTypes.DROP_ALL_ITEM;
    }
    default Boolean canPickItem() {
        return true;
    }
    default Boolean canFeed(){
        return false;
    }
    default Boolean consumeHunger() {
        return false;
    }
    default Boolean canDamageEquipment() {
        return false;
    }
    default Set<Integer> getDonDropSlotIndex() {
        return Set.of();
    }
}
