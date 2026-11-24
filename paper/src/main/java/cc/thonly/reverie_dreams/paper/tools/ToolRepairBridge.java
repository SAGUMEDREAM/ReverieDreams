package cc.thonly.reverie_dreams.paper.tools;

import io.papermc.paper.datacomponent.DataComponentTypes;
import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;

/** Corrects the target CE version's extra ingredient at exact quarter-durability multiples. */
final class ToolRepairBridge implements Listener {
    private final ToolsModule module;
    ToolRepairBridge(ToolsModule module) { this.module = module; }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAnvil(PrepareAnvilEvent event) {
        var first = event.getInventory().getItem(0); var second = event.getInventory().getItem(1);
        var preview = module.previewRepair(first, second).orElse(null);
        if (preview == null) return;
        var view = event.getView();
        int oldCount = view.getRepairItemCountCost();
        if (oldCount <= preview.materialCount()) return;
        int oldCost = view.getRepairCost();
        int correctedCost = Math.max(0, oldCost - (oldCount - preview.materialCount()));
        view.setRepairItemCountCost(preview.materialCount()); view.setRepairCost(correctedCost);
        if (event.getResult() != null || oldCost < view.getMaximumRepairCost() || correctedCost >= view.getMaximumRepairCost()) return;
        // CE had rejected only because it charged one extra material level. Preserve its rename/prior-work rules.
        var result = preview.result();
        int firstWork = first.getDataOrDefault(DataComponentTypes.REPAIR_COST, 0);
        int secondWork = second.getDataOrDefault(DataComponentTypes.REPAIR_COST, 0);
        int renameCost = oldCost - oldCount - firstWork - secondWork;
        if (renameCost > 0) {
            String rename = view.getRenameText();
            result.editMeta(meta -> meta.displayName(rename == null || rename.isBlank() ? null : Component.text(rename)));
        }
        result.setData(DataComponentTypes.REPAIR_COST, Math.max(firstWork, secondWork) * 2 + 1);
        event.setResult(result);
    }
}
