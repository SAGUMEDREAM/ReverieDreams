package cc.thonly.reverie_dreams.paper.specialweapons;

import cc.thonly.reverie_dreams.paper.util.Ids;

import io.papermc.paper.datacomponent.DataComponentTypes;
import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;

/** Isolated exact-multiple correction only for the new sunflower custom-ingredient rule; engine owns debit. */
public final class WeaponRepairBridge implements Listener {
    public record Preview(ItemStack result,int materials,int restored){}
    public Preview preview(ItemStack first,ItemStack second){if(first==null||second==null||!SpecialWeaponsModule.id(first).equals(Ids.MOD_ID + ":sunflower")||!SpecialWeaponsModule.id(second).equals(Ids.MOD_ID + ":power_block"))return null;int maximum=first.getDataOrDefault(DataComponentTypes.MAX_DAMAGE,0),damage=first.getDataOrDefault(DataComponentTypes.DAMAGE,0),each=maximum/4;if(damage<=0||each<=0)return null;int count=Math.min(second.getAmount(),(damage+each-1)/each),restored=Math.min(damage,count*each);var result=first.clone();result.setData(DataComponentTypes.DAMAGE,damage-restored);return new Preview(result,count,restored);}
    @EventHandler(priority=EventPriority.HIGHEST)public void prepare(PrepareAnvilEvent event){var a=event.getInventory().getItem(0);var b=event.getInventory().getItem(1);var plan=preview(a,b);if(plan==null)return;var view=event.getView();int oldCount=view.getRepairItemCountCost();if(oldCount<=plan.materials)return;int oldCost=view.getRepairCost(),cost=Math.max(0,oldCost-(oldCount-plan.materials));view.setRepairItemCountCost(plan.materials);view.setRepairCost(cost);if(event.getResult()!=null||oldCost<view.getMaximumRepairCost()||cost>=view.getMaximumRepairCost())return;
        var result=plan.result;int workA=a.getDataOrDefault(DataComponentTypes.REPAIR_COST,0),workB=b.getDataOrDefault(DataComponentTypes.REPAIR_COST,0);if(oldCost-oldCount-workA-workB>0){String name=view.getRenameText();result.editMeta(meta->meta.displayName(name==null||name.isBlank()?null:Component.text(name)));}result.setData(DataComponentTypes.REPAIR_COST,Math.max(workA,workB)*2+1);event.setResult(result);
    }
}
