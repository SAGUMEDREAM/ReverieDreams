package cc.thonly.reverie_dreams.paper.server;

import cc.thonly.reverie_dreams.paper.util.event.Event;
import cc.thonly.reverie_dreams.paper.util.event.EventFactory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class ItemInventoryTickManager {
    public static final ItemInventoryTickManager INSTANCE = new ItemInventoryTickManager();
    public static final Event<Callback> EVENT = EventFactory.createArrayBacked(Callback.class, (listeners) -> (player, slot, itemStack, inventory) -> {
        for (Callback listener : listeners) {
            listener.onTick(player, slot, itemStack, inventory);
        }
    });

    public void invokePlayer(Player player) {
        PlayerInventory inventory = player.getInventory();
        EVENT.invoker().onTick(player, EquipmentSlot.HAND, inventory.getItemInMainHand(), inventory);
        EVENT.invoker().onTick(player, EquipmentSlot.OFF_HAND, inventory.getItemInOffHand(), inventory);
        EVENT.invoker().onTick(player, EquipmentSlot.HEAD, inventory.getHelmet(), inventory);
        EVENT.invoker().onTick(player, EquipmentSlot.CHEST, inventory.getChestplate(), inventory);
        EVENT.invoker().onTick(player, EquipmentSlot.LEGS, inventory.getLeggings(), inventory);
        EVENT.invoker().onTick(player, EquipmentSlot.FEET, inventory.getBoots(), inventory);
    }

    public interface Callback {
        void onTick(Player player, EquipmentSlot slot, ItemStack itemStack, PlayerInventory playerInventory);
    }
}
