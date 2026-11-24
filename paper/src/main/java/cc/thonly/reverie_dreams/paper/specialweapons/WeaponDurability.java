package cc.thonly.reverie_dreams.paper.specialweapons;

import io.papermc.paper.datacomponent.DataComponentTypes;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.EquipmentSlot;
import java.util.concurrent.ThreadLocalRandom;

/** Trumpet's actual hurtWithoutBreaking path. Sunflower uses native breakable damage separately. */
final class WeaponDurability {
    static void withoutBreaking(Player p,EquipmentSlot hand){var item=p.getInventory().getItem(hand);if(p.getGameMode()==GameMode.CREATIVE||item.isEmpty()||item.getItemMeta().isUnbreakable())return;int maximum=item.getDataOrDefault(DataComponentTypes.MAX_DAMAGE,0),old=item.getDataOrDefault(DataComponentTypes.DAMAGE,0);if(maximum<=0||old>=maximum-1||ThreadLocalRandom.current().nextInt(item.getEnchantmentLevel(Enchantment.UNBREAKING)+1)>0)return;var result=item.clone();var event=new PlayerItemDamageEvent(p,result,1,1);Bukkit.getPluginManager().callEvent(event);if(!event.isCancelled()){result.setData(DataComponentTypes.DAMAGE,Math.clamp(old+event.getDamage(),0,maximum-1));p.getInventory().setItem(hand,result);}}
    private WeaponDurability(){}
}
