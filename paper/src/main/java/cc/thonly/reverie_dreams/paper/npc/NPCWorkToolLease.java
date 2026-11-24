package cc.thonly.reverie_dreams.paper.npc;

import java.util.function.Predicate;
import org.bukkit.entity.Wolf;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.persistence.PersistentDataType;

/** Original WorkToolLease: real cargo/main-hand swap, persisted without hidden item copies. */
final class NPCWorkToolLease {
    private final NPCAiService ai; private final Wolf wolf;
    private int source=-1; private ItemStack original=ItemStack.empty(),sample=ItemStack.empty();
    NPCWorkToolLease(NPCAiService ai,Wolf wolf) {
        this.ai=ai;this.wolf=wolf;var data=wolf.getPersistentDataContainer();
        Integer slot=data.get(ai.toolSlotKey,PersistentDataType.INTEGER);byte[] bytes=data.get(ai.toolDataKey,PersistentDataType.BYTE_ARRAY);
        if(slot!=null&&bytes!=null){var stacks=ItemStack.deserializeItemsFromBytes(bytes);if(slot>=0&&slot<18&&stacks.length==2){source=slot;original=stacks[0];sample=stacks[1];}else throw new IllegalStateException("Invalid NPC tool lease metadata");}
    }
    int ensure(Predicate<ItemStack> wanted) {
        var items=ai.inventory(wolf);if(wanted.test(items[22]))return 22;if(wanted.test(items[23]))return 23;
        restore();items=ai.inventory(wolf);
        for(int i=0;i<18;i++)if(wanted.test(items[i])){
            source=i;original=items[22].clone();sample=items[i].asQuantity(1);
            var tool=items[i];items[i]=items[22];items[22]=tool;ai.inventory(wolf,items);save();return 22;
        }return -1;
    }
    void restore() {
        if(source<0)return;var items=ai.inventory(wolf);var hand=items[22];var cargo=items[source];
        boolean stillTool=hand.isEmpty()||NPCAiService.itemId(hand).equals(NPCAiService.itemId(sample))||sample.getType()==Material.GLASS_BOTTLE&&hand.getType()==Material.HONEY_BOTTLE;
        boolean originalThere=original.isEmpty()?cargo.isEmpty():!cargo.isEmpty()&&original.isSimilar(cargo);
        if(stillTool&&originalThere){items[22]=cargo;items[source]=hand;ai.inventory(wolf,items);}
        else if(original.isEmpty()&&stillTool){var remainder=hand.clone();items[22]=ItemStack.empty();NPCAiService.insertCargo(items,remainder);items[22]=remainder;ai.inventory(wolf,items);}
        source=-1;original=ItemStack.empty();sample=ItemStack.empty();save();
    }
    private void save(){var data=wolf.getPersistentDataContainer();if(source<0){data.remove(ai.toolSlotKey);data.remove(ai.toolDataKey);}else{data.set(ai.toolSlotKey,PersistentDataType.INTEGER,source);data.set(ai.toolDataKey,PersistentDataType.BYTE_ARRAY,ItemStack.serializeItemsAsBytes(new ItemStack[]{original,sample}));}}
}
