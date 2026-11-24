package cc.thonly.reverie_dreams.paper.specialitems;

import io.papermc.paper.datacomponent.DataComponentTypes;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.concurrent.ThreadLocalRandom;

/** Source hurtWithoutBreaking: never consumes the last durability; native breakable paths use Paper instead. */
final class SpecialItemDurability {
    private SpecialItemDurability(){}
    static void withoutBreaking(Player player,EquipmentSlot hand,int amount){
        var original=player.getInventory().getItem(hand);if(original==null||original.isEmpty()||player.getGameMode()==GameMode.CREATIVE||original.getItemMeta().isUnbreakable())return;
        int maximum=original.getDataOrDefault(DataComponentTypes.MAX_DAMAGE,0),damage=original.getDataOrDefault(DataComponentTypes.DAMAGE,0);if(maximum<=0)return;
        int processed=0,unbreaking=original.getEnchantmentLevel(Enchantment.UNBREAKING);for(int i=0;i<amount;i++)if(ThreadLocalRandom.current().nextInt(unbreaking+1)==0)processed++;
        int delta=Math.min(maximum-1,damage+processed)-damage;if(delta<=0)return;var result=original.clone();var event=new PlayerItemDamageEvent(player,result,delta,amount);Bukkit.getPluginManager().callEvent(event);
        if(event.isCancelled())return;result.setData(DataComponentTypes.DAMAGE,Math.max(0,Math.min(maximum-1,damage+event.getDamage())));player.getInventory().setItem(hand,result);
    }
}
