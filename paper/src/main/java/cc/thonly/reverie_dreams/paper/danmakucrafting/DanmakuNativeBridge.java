package cc.thonly.reverie_dreams.paper.danmakucrafting;

import cc.thonly.reverie_dreams.paper.danmaku.DanmakuModule;
import io.papermc.paper.datacomponent.DataComponentTypes;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Keyed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.CrafterCraftEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

/** Native recipes own matching, per-slot consumption and remainders; this bridge only preserves source metadata/cost. */
@SuppressWarnings("UnstableApiUsage")
final class DanmakuNativeBridge implements Listener {
    private final DanmakuCraftingRules rules;
    DanmakuNativeBridge(DanmakuCraftingRules rules){this.rules=rules;}
    private static boolean dyeRecipe(Recipe recipe){return recipe instanceof Keyed keyed&&keyed.getKey().getNamespace().equals("reverie_dreams")&&keyed.getKey().getKey().startsWith("danmaku_dye/");}
    private static ItemStack tint(ItemStack nativeResult){
        if(nativeResult==null||nativeResult.isEmpty()||DanmakuModule.service().rules().type(nativeResult)==null)return nativeResult;
        var colour=nativeResult.getData(DataComponentTypes.DYED_COLOR);if(colour==null)return nativeResult;
        ItemStack result=nativeResult.clone();result.setData(DataComponentTypes.ITEM_NAME,nativeResult.effectiveName().color(TextColor.color(colour.color().asRGB())));return result;
    }
    @EventHandler(priority=EventPriority.HIGHEST)public void prepareDye(PrepareItemCraftEvent event){
        // No result is manufactured for an unmatched grid. CE's actual native DyeRecipe already assembled this one.
        if(dyeRecipe(event.getRecipe())&&event.getInventory().getResult()!=null)event.getInventory().setResult(tint(event.getInventory().getResult()));
    }
    @EventHandler(priority=EventPriority.HIGHEST,ignoreCancelled=true)public void takeDye(CraftItemEvent event){
        if(dyeRecipe(event.getRecipe())&&event.getCurrentItem()!=null)event.setCurrentItem(tint(event.getCurrentItem()));
    }
    @EventHandler(priority=EventPriority.HIGHEST,ignoreCancelled=true)public void crafterDye(CrafterCraftEvent event){if(dyeRecipe(event.getRecipe()))event.setResult(tint(event.getResult()));}
    @EventHandler(priority=EventPriority.HIGHEST)public void repair(PrepareAnvilEvent event){
        var first=event.getInventory().getItem(0);var second=event.getInventory().getItem(1);var preview=rules.previewRepair(first,second);if(preview==null)return;
        var view=event.getView();int oldCount=view.getRepairItemCountCost();if(oldCount<=preview.materialCount())return;
        int oldCost=view.getRepairCost(),correctedCost=Math.max(0,oldCost-(oldCount-preview.materialCount()));
        view.setRepairItemCountCost(preview.materialCount());view.setRepairCost(correctedCost);
        if(event.getResult()!=null||oldCost<view.getMaximumRepairCost()||correctedCost>=view.getMaximumRepairCost())return;
        // Only restore the result if CE rejected it solely because its exact-multiple overcharge reached the cost limit.
        var result=preview.result();int firstWork=first.getDataOrDefault(DataComponentTypes.REPAIR_COST,0),secondWork=second.getDataOrDefault(DataComponentTypes.REPAIR_COST,0);
        int renameCost=oldCost-oldCount-firstWork-secondWork;
        if(renameCost>0){String rename=view.getRenameText();result.editMeta(meta->meta.displayName(rename==null||rename.isBlank()?null:Component.text(rename)));}
        result.setData(DataComponentTypes.REPAIR_COST,Math.max(firstWork,secondWork)*2+1);event.setResult(result);
    }
}
