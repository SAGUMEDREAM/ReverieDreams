package cc.thonly.reverie_dreams.paper.specialitems2;

import io.papermc.paper.datacomponent.DataComponentTypes;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.EquipmentSlot;
import java.util.concurrent.ThreadLocalRandom;

/** Same source hurtWithoutBreaking path as first batch, isolated to avoid editing the frozen module. */
final class ExpansionDurability {
    static void damage(Player player,EquipmentSlot hand){var item=player.getInventory().getItem(hand);if(player.getGameMode()==GameMode.CREATIVE||item.isEmpty()||item.getItemMeta().isUnbreakable())return;
        int max=item.getDataOrDefault(DataComponentTypes.MAX_DAMAGE,0),old=item.getDataOrDefault(DataComponentTypes.DAMAGE,0);if(max<=0||old>=max-1||ThreadLocalRandom.current().nextInt(item.getEnchantmentLevel(Enchantment.UNBREAKING)+1)>0)return;
        var result=item.clone();var event=new PlayerItemDamageEvent(player,result,1,1);Bukkit.getPluginManager().callEvent(event);if(event.isCancelled())return;result.setData(DataComponentTypes.DAMAGE,Math.max(0,Math.min(max-1,old+event.getDamage())));player.getInventory().setItem(hand,result);
    }
    private ExpansionDurability(){}
}
