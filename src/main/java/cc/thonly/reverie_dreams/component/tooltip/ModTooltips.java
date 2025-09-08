package cc.thonly.reverie_dreams.component.tooltip;

import cc.thonly.reverie_dreams.component.ModDataComponentTypes;
import net.fabricmc.fabric.api.item.v1.ComponentTooltipAppenderRegistry;

public class ModTooltips {
    public static void bootstrap() {
        ComponentTooltipAppenderRegistry.addFirst(ModDataComponentTypes.OVER_TOOLTIP_APPENDER);
    }
}
