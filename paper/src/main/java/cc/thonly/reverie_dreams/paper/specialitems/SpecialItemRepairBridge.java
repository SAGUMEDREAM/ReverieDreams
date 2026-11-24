package cc.thonly.reverie_dreams.paper.specialitems;

import cc.thonly.reverie_dreams.paper.util.Ids;

import io.papermc.paper.datacomponent.DataComponentTypes;
import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.Set;

/** Only the four new custom-material consumers; vanilla materials keep the native anvil transaction unchanged. */
public final class SpecialItemRepairBridge implements Listener {
    public record Preview(ItemStack result,int materialCount,int restoredDurability){}
    private static final Map<String,Set<String>> TARGETS=Map.of(
            Ids.MOD_ID + ":silver_block",Set.of(Ids.MOD_ID + ":hakuroken",Ids.MOD_ID + ":rokanken"),
            Ids.MOD_ID + ":oni_horn",Set.of(Ids.MOD_ID + ":iron_bar",Ids.MOD_ID + ":oni_iron_bar"));
    static Preview preview(ItemStack first,ItemStack second){
        if(first==null||first.isEmpty()||second==null||second.isEmpty()||!TARGETS.getOrDefault(SpecialItemsModule.id(second),Set.of()).contains(SpecialItemsModule.id(first)))return null;
        int max=first.getDataOrDefault(DataComponentTypes.MAX_DAMAGE,0),damage=first.getDataOrDefault(DataComponentTypes.DAMAGE,0),each=max/4;if(damage<=0||each<=0)return null;
        int count=Math.min(second.getAmount(),(damage+each-1)/each),restored=Math.min(damage,count*each);var result=first.clone();result.setData(DataComponentTypes.DAMAGE,damage-restored);return new Preview(result,count,restored);
    }
    @EventHandler(priority=EventPriority.HIGHEST)public void repair(PrepareAnvilEvent event){
        var first=event.getInventory().getItem(0);var second=event.getInventory().getItem(1);var preview=preview(first,second);if(preview==null)return;
        var view=event.getView();int previous=view.getRepairItemCountCost();if(previous<=preview.materialCount())return;
        int oldCost=view.getRepairCost(),corrected=Math.max(0,oldCost-(previous-preview.materialCount()));view.setRepairItemCountCost(preview.materialCount());view.setRepairCost(corrected);
        if(event.getResult()!=null||oldCost<view.getMaximumRepairCost()||corrected>=view.getMaximumRepairCost())return;
        var result=preview.result();int workA=first.getDataOrDefault(DataComponentTypes.REPAIR_COST,0),workB=second.getDataOrDefault(DataComponentTypes.REPAIR_COST,0);
        if(oldCost-previous-workA-workB>0){String name=view.getRenameText();result.editMeta(meta->meta.displayName(name==null||name.isBlank()?null:Component.text(name)));}
        result.setData(DataComponentTypes.REPAIR_COST,Math.max(workA,workB)*2+1);event.setResult(result);
    }
}
